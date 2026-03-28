package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Bonus;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map.GameMap;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.CannonTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

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
 * ROLE:
 *   - Owns all entity lists (towers, enemies, projectiles, bonuses, base)
 *   - Tracks score and gold
 *   - Holds the current GameState
 *   - Runs the main update() loop each frame
 *   - Uses EntityFactory (Abstract Factory pattern) to create entities
 *     without knowing about J2D — game logic stays visualization-free.
 *
 * ABSTRACT FACTORY:
 *   EntityFactory is injected via init(). The game package only knows the
 *   interface; the J2D package provides the concrete J2dEntityFactory.
 *
 * GAME LOOP (update):
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
    // Abstract Factory
    // -------------------------------------------------------------------------

    private EntityFactory entityFactory;

    public EntityFactory getEntityFactory() { return entityFactory; }

    // -------------------------------------------------------------------------
    // Simple enemy spawner (replaced by WaveManager in Fase 7)
    // -------------------------------------------------------------------------

    private static final double SPAWN_INTERVAL = 1.5;  // seconds between spawns

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

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /*
     * Sets up the game: stores the factory, reads config values.
     * Call once before starting the game loop.
     */
    public void init(EntityFactory factory, ConfigManager config) {
        this.entityFactory = factory;
        this.gold          = config.getInt("starting.gold", 200);
        this.score         = 0;
        this.state         = GameState.MENU;
    }

    /*
     * Loads a level from a level config file.
     * Called after init() — creates the GameMap with tile grid, paths, etc.
     */
    public void loadLevel(ConfigManager levelConfig) {
        this.gameMap = new GameMap(levelConfig);
    }

    /*
     * Prepares the game to start playing.
     * Sets up the enemy spawn queue and transitions to PLAYING state.
     *
     * totalEnemies: how many enemies to spawn this session
     *               (will be read from wave data in Fase 7)
     */
    public void startPlaying(int totalEnemies) {
        this.enemiesToSpawn = totalEnemies;
        this.enemiesSpawned = 0;
        this.spawnTimer     = 0;  // spawn first enemy immediately
        this.state          = GameState.PLAYING;
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
                // Deal damage to the base
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

                // Create a projectile flying from tower to the enemy's current position
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
                    // Store impact position before onHit modifies state
                    Position impact = new Position(
                        p.getPosition().getX(), p.getPosition().getY()
                    );

                    // Direct hit
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
                    break;  // projectile dies after one hit
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
        // Lose condition — base destroyed
        if (base.isPresent() && base.get().isDestroyed()) {
            state = GameState.GAME_OVER;
            return;
        }

        // Win condition — all enemies spawned and cleared
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
        this.base         = Optional.empty();
        this.gameMap       = null;
        this.enemiesToSpawn = 0;
        this.enemiesSpawned = 0;
        this.spawnTimer     = 0;
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
