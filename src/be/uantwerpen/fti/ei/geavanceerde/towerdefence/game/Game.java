package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Bonus;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map.GameMap;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.ArrowTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.CannonTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.IceTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Stopwatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/*
 * Central singleton representing the entire Tower Defence game.
 *
 * SINGLETON PATTERN:
 *   Only one Game instance exists. The constructor is private.
 *   All access goes through Game.getInstance().
 *
 * ABSTRACT FACTORY:
 *   EntityFactory is injected via start(). The game package only knows the
 *   interface; the J2D package provides the concrete J2dEntityFactory.
 *   The factory also provides the GameView (render + input abstraction),
 *   keeping the game package completely free of visualization imports.
 *
 * ROLE:
 *   - Owns all entity lists (towers, enemies, projectiles, bonuses, base)
 *   - Tracks score and gold
 *   - Holds the current GameState
 *   - Runs the main game loop via start()
 *   - Handles player input (pause, tower placement, restart)
 *
 * GAME LOOP (inside start → update):
 *   1. Spawn enemies (simple timer, replaced by WaveManager in Fase 7)
 *   2. Update enemies (move + slow timer)
 *   3. Check enemies reaching base
 *   4. Update towers (cooldown tick)
 *   5. Tower area effects (IceTower slow)
 *   6. Tower targeting + firing projectiles
 *   7. Update projectiles (move toward target)
 *   8. Projectile ↔ enemy collision + splash damage
 *   9. Cleanup dead enemies (award gold/score)
 *  10. Cleanup dead projectiles + bonuses
 *  11. Win/lose check
 */
public final class Game {

    // The single shared instance — null until first call to getInstance()
    private static Game instance;

    // -------------------------------------------------------------------------
    // Game state
    // -------------------------------------------------------------------------

    private GameState state;
    private int score;
    private int gold;

    // -------------------------------------------------------------------------
    // Entity lists
    // -------------------------------------------------------------------------

    private final List<Tower> towers;
    private final List<Enemy> enemies;
    private final List<Projectile> projectiles;
    private final List<Bonus> bonuses;
    private Optional<Base> base;

    // -------------------------------------------------------------------------
    // Map
    // -------------------------------------------------------------------------

    private GameMap gameMap;

    // -------------------------------------------------------------------------
    // Abstract Factory + Visualization
    // -------------------------------------------------------------------------

    private EntityFactory entityFactory;
    private GameView view;
    private ConfigManager config;

    public EntityFactory getEntityFactory() { return entityFactory; }

    // -------------------------------------------------------------------------
    // Simple enemy spawner (replaced by WaveManager in Fase 7)
    // -------------------------------------------------------------------------

    private static final double SPAWN_INTERVAL = 1.5;  // seconds between spawns
    private static final int TOTAL_ENEMIES     = 15;   // placeholder until WaveManager
    private static final int FRAME_DELAY_MS    = 16;   // ~60 FPS

    // Countdown until the next enemy spawns
    private double spawnTimer;

    // How many enemies still need to be spawned this game
    private int enemiesToSpawn;

    // How many have been spawned so far (used to alternate types)
    private int enemiesSpawned;

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    private Game() {
        this.state       = GameState.MENU;
        this.score       = 0;
        this.gold        = 0;
        this.towers      = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.bonuses     = new ArrayList<>();
        this.base        = Optional.empty();
    }

    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    // =========================================================================
    // start() — the single entry point called by Main
    // =========================================================================

    /*
     * Sets up and runs the game. This is the only method Main needs to call.
     *
     * 1. Stores the factory and config
     * 2. Gets the GameView from the factory (render + input)
     * 3. Loads the selected level and creates the base
     * 4. Runs the game loop: input → update → render → sleep
     *
     * The factory provides both entity creation AND the visualization layer
     * via getView(), so Game never imports any J2D classes.
     */
    public void start(EntityFactory factory, ConfigManager config) {
        this.entityFactory = factory;
        this.config        = config;
        this.view          = factory.getView();

        // Load the level and create the base
        setupLevel();

        // Game loop — runs until the window is closed
        Stopwatch stopwatch = new Stopwatch();

        while (true) {
            double deltaTime = stopwatch.tick();

            // Process player input (pause, tower placement, restart)
            handleInput();

            // Update game logic (only runs while PLAYING)
            update(deltaTime);

            // Render everything via the abstract GameView
            view.render();

            // Frame rate limiter (~60 FPS)
            try {
                Thread.sleep(FRAME_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Level setup — used by start() and restartGame()
    // -------------------------------------------------------------------------

    /*
     * Loads the selected level from config, creates the map and base,
     * sets starting resources, and begins enemy spawning.
     */
    private void setupLevel() {
        // Load the level file
        int level = config.getInt("selected.level", 1);
        ConfigManager levelConfig = new ConfigManager("levels/level" + level + ".properties");
        this.gameMap = new GameMap(levelConfig);

        // Set starting resources
        this.gold  = config.getInt("starting.gold", 200);
        this.score = 0;

        // Create the base at the centre of the base tile
        int startingLives = config.getInt("starting.lives", 20);
        Position basePos = gameMap.getBasePosition();
        Base baseEntity = entityFactory.createBase(
            new Position(basePos.getX() + 0.5, basePos.getY() + 0.5),
            startingLives
        );
        this.base = Optional.of(baseEntity);

        // Begin spawning enemies
        this.enemiesToSpawn = TOTAL_ENEMIES;
        this.enemiesSpawned = 0;
        this.spawnTimer     = 0;
        this.state          = GameState.PLAYING;
    }

    // -------------------------------------------------------------------------
    // Input handling — called once per frame
    // -------------------------------------------------------------------------

    /*
     * Processes player input from the GameView:
     *   - P key: toggle pause / unpause
     *   - Mouse click + tower selected: place a tower if affordable and buildable
     *   - Click after GAME_OVER or WON: restart the game
     */
    private void handleInput() {
        // Pause toggle
        if (view.wasPausePressed()) {
            if (state == GameState.PLAYING) {
                state = GameState.PAUSED;
            } else if (state == GameState.PAUSED) {
                state = GameState.PLAYING;
            }
        }

        // Mouse click
        if (!view.wasMouseClicked()) return;

        // If the game is over or won, a click restarts
        if (state == GameState.GAME_OVER || state == GameState.WON) {
            restartGame();
            return;
        }

        // Only allow tower placement while playing
        if (state != GameState.PLAYING) return;

        // Tower placement
        int towerType = view.getSelectedTower();
        if (towerType == 0) return;

        // Convert mouse position to game-world tile coordinates
        double gameX = view.getMouseGameX();
        double gameY = view.getMouseGameY();
        Position clickPos = new Position(gameX, gameY);

        // Check if the tile is a valid build spot
        if (!gameMap.canBuildAt(clickPos)) return;

        // Snap the tower to the centre of the tile
        int tileX = (int) gameX;
        int tileY = (int) gameY;
        Position towerPos = new Position(tileX + 0.5, tileY + 0.5);

        // Check no tower already exists on this tile
        for (Tower existing : towers) {
            double dx = Math.abs(existing.getPosition().getX() - towerPos.getX());
            double dy = Math.abs(existing.getPosition().getY() - towerPos.getY());
            if (dx < 0.5 && dy < 0.5) return;
        }

        // Determine cost and check if the player can afford it
        int cost;
        switch (towerType) {
            case 1: cost = ArrowTower.DEFAULT_COST;  break;
            case 2: cost = CannonTower.DEFAULT_COST; break;
            case 3: cost = IceTower.DEFAULT_COST;    break;
            default: return;
        }

        if (gold < cost) return;

        // Create the tower via the Abstract Factory
        Tower tower;
        switch (towerType) {
            case 1: tower = entityFactory.createArrowTower(towerPos);  break;
            case 2: tower = entityFactory.createCannonTower(towerPos); break;
            case 3: tower = entityFactory.createIceTower(towerPos);    break;
            default: return;
        }

        // Deduct gold and add the tower to the game
        spendGold(cost);
        towers.add(tower);
    }

    // -------------------------------------------------------------------------
    // Restart — resets and reloads the level
    // -------------------------------------------------------------------------

    /*
     * Called when the player clicks after GAME_OVER or WON.
     * Clears all state and reloads the current level.
     */
    private void restartGame() {
        reset();
        setupLevel();
    }

    // =========================================================================
    // GAME LOOP — called once per frame with delta time in seconds
    // =========================================================================

    public void update(double deltaTime) {
        if (state != GameState.PLAYING) return;

        // 1. Spawn enemies
        updateSpawner(deltaTime);

        // 2. Update all enemies (movement + slow timer)
        for (Enemy e : enemies) {
            if (e.isAlive()) e.update(deltaTime);
        }

        // 3. Check if any enemy reached the base
        checkEnemiesReachBase();

        // 4. Update all towers (tick fire cooldowns)
        for (Tower t : towers) {
            t.update(deltaTime);
        }

        // 5. Tower area effects (IceTower slow aura)
        for (Tower t : towers) {
            t.applyAreaEffect(enemies);
        }

        // 6. Tower targeting + fire projectiles
        updateTowerFiring();

        // 7. Update all projectiles (move toward target)
        for (Projectile p : projectiles) {
            if (p.isAlive()) p.update(deltaTime);
        }

        // 8. Projectile ↔ enemy collision check
        checkProjectileCollisions();

        // 9. Cleanup dead enemies (award gold + score)
        cleanupDeadEnemies();

        // 10. Cleanup dead projectiles and expired bonuses
        projectiles.removeIf(p -> !p.isAlive());
        bonuses.removeIf(b -> !b.isAlive());

        // 11. Win/lose check
        checkWinLose();
    }

    // -------------------------------------------------------------------------
    // 1. Simple enemy spawner (Fase 6 placeholder for WaveManager)
    // -------------------------------------------------------------------------

    /*
     * Spawns enemies at regular intervals. Alternates between types:
     *   - Most are BasicEnemy
     *   - Every 3rd is ArmoredEnemy
     *   - Every 5th is FlyingEnemy
     *
     * This will be replaced by WaveManager in Fase 7 which reads wave
     * definitions from the level .properties file.
     */
    private void updateSpawner(double deltaTime) {
        if (enemiesToSpawn <= 0) return;

        spawnTimer -= deltaTime;
        if (spawnTimer <= 0) {
            List<Position> groundPath = gameMap.getEnemyPath().getWaypoints();
            Enemy enemy;

            // Alternate enemy types for variety
            if (enemiesSpawned % 5 == 4) {
                // Every 5th enemy is a FlyingEnemy
                if (gameMap.hasFlyingPath()) {
                    List<Position> flyingPath = gameMap.getFlyingPath().getWaypoints();
                    enemy = entityFactory.createFlyingEnemy(flyingPath);
                } else {
                    enemy = entityFactory.createFlyingEnemy(groundPath);
                }
            } else if (enemiesSpawned % 3 == 2) {
                // Every 3rd enemy is ArmoredEnemy
                enemy = entityFactory.createArmoredEnemy(groundPath);
            } else {
                // Default: BasicEnemy
                enemy = entityFactory.createBasicEnemy(groundPath);
            }

            enemies.add(enemy);
            enemiesSpawned++;
            enemiesToSpawn--;
            spawnTimer = SPAWN_INTERVAL;
        }
    }

    // -------------------------------------------------------------------------
    // 3. Check enemies that reached the base
    // -------------------------------------------------------------------------

    /*
     * Enemies that have walked past all waypoints have reached the base.
     * Each one deals 1 damage to the base and is removed from the game.
     */
    private void checkEnemiesReachBase() {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (e.isAlive() && e.hasReachedBase()) {
                base.ifPresent(b -> b.takeDamage(1));
                e.destroy();
                it.remove();
            }
        }
    }

    // -------------------------------------------------------------------------
    // 6. Tower targeting + firing
    // -------------------------------------------------------------------------

    /*
     * Each tower looks for a target (using its Streams-based findTarget).
     * If it finds one and its cooldown is ready, the game loop creates a
     * projectile via the factory and resets the tower's cooldown.
     *
     * CannonTower projectiles get splash data copied onto them so the
     * collision handler can apply area damage on impact.
     */
    private void updateTowerFiring() {
        for (Tower tower : towers) {
            if (!tower.isReadyToFire()) continue;

            Optional<Enemy> target = tower.findTarget(enemies);
            if (target.isPresent()) {
                Enemy t = target.get();

                Projectile proj = entityFactory.createProjectile(
                    tower.getPosition(), t.getPosition(), tower.getDamage()
                );

                // Copy splash damage data for CannonTower projectiles
                if (tower instanceof CannonTower) {
                    CannonTower ct = (CannonTower) tower;
                    proj.setSplash(ct.getSplashRadius(), ct.getSplashDamage());
                }

                projectiles.add(proj);
                tower.resetCooldown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // 8. Projectile ↔ enemy collision
    // -------------------------------------------------------------------------

    /*
     * Checks each alive projectile against all alive enemies.
     * On collision:
     *   - Direct damage via projectile.onHit(enemy)
     *   - Splash damage if the projectile has a splash radius (CannonTower)
     */
    private void checkProjectileCollisions() {
        for (Projectile p : projectiles) {
            if (!p.isAlive()) continue;

            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;

                if (p.collidesWith(e)) {
                    Position impact = new Position(
                        p.getPosition().getX(), p.getPosition().getY()
                    );

                    p.onHit(e);

                    // Splash damage — hit all nearby enemies (except the direct target)
                    if (p.getSplashRadius() > 0) {
                        for (Enemy splash : enemies) {
                            if (!splash.isAlive() || splash == e) continue;
                            if (impact.distanceTo(splash.getPosition()) <= p.getSplashRadius()) {
                                splash.takeDamage(p.getSplashDamage());
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 9. Cleanup dead enemies
    // -------------------------------------------------------------------------

    /*
     * Removes dead enemies from the list and awards their gold and score
     * to the player. Uses iterator for safe removal during iteration.
     */
    private void cleanupDeadEnemies() {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (!e.isAlive()) {
                gold  += e.getReward();
                score += e.getScoreValue();
                it.remove();
            }
        }
    }

    // -------------------------------------------------------------------------
    // 11. Win/lose check
    // -------------------------------------------------------------------------

    /*
     * GAME_OVER: base is destroyed (HP <= 0)
     * WON:       all enemies have been spawned AND no enemies remain alive
     */
    private void checkWinLose() {
        if (base.isPresent() && base.get().isDestroyed()) {
            state = GameState.GAME_OVER;
            return;
        }

        if (enemiesToSpawn <= 0 && enemies.isEmpty()) {
            state = GameState.WON;
        }
    }

    // =========================================================================
    // Reset
    // =========================================================================

    /*
     * Resets everything for a new game or level restart.
     * Clears all entities, zeroes stats, returns to MENU.
     */
    public void reset() {
        this.score = 0;
        this.gold  = 0;
        this.state = GameState.MENU;
        this.towers.clear();
        this.enemies.clear();
        this.projectiles.clear();
        this.bonuses.clear();
        this.base           = Optional.empty();
        this.gameMap         = null;
        this.enemiesToSpawn  = 0;
        this.enemiesSpawned  = 0;
        this.spawnTimer      = 0;
    }

    // =========================================================================
    // Getters / entity access
    // =========================================================================

    public List<Tower>      getTowers()      { return towers; }
    public List<Enemy>      getEnemies()     { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<Bonus>      getBonuses()     { return bonuses; }

    public Optional<Base> getBase()          { return base; }
    public void setBase(Base base)           { this.base = Optional.of(base); }

    public GameMap getGameMap()              { return gameMap; }

    public GameState getState()              { return state; }
    public void      setState(GameState s)   { this.state = s; }

    public int  getScore()                   { return score; }
    public void addScore(int points)         { this.score += points; }

    public int  getGold()                    { return gold; }
    public void addGold(int amount)          { this.gold += amount; }

    public int getEnemiesRemaining()         { return enemiesToSpawn; }

    public void spendGold(int amount) {
        if (amount > this.gold) {
            throw new IllegalStateException(
                "Not enough gold: tried to spend " + amount + " but only have " + this.gold
            );
        }
        this.gold -= amount;
    }
}
