package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs.FloatingTextKind;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs.FloatingTextWorld;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs.LifetimeSystem;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs.MovementSystem;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map.GameMap;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.ArrowTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.CannonTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.IceTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Stopwatch;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.scripting.LuaScriptEngine;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.wave.WaveManager;

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
 *   1. Spawn enemies via WaveManager (leest golven uit level .properties)
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

    // Current level (1-based) and the total number of levels (from config).
    // Used for level progression and the WON-screen branching (level complete
    // vs. ultimate victory).
    private int currentLevel;
    private int maxLevels;

    // -------------------------------------------------------------------------
    // Entity lists
    // -------------------------------------------------------------------------

    private final List<Tower> towers;
    private final List<Enemy> enemies;
    private final List<Projectile> projectiles;
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

    // -------------------------------------------------------------------------
    // Fase 7: WaveManager — beheert alle golven voor het huidige level
    // -------------------------------------------------------------------------

    private static final int FRAME_DELAY_MS = 16;  // ~60 FPS

    /*
     * WaveManager leest de golfdefinities uit de level .properties file en
     * spawnt vijanden op het juiste moment. Game vraagt elke frame aan de
     * manager of er een vijand gespawnd moet worden via waveManager.tick().
     */
    private WaveManager waveManager;

    // -------------------------------------------------------------------------
    // Fase 8: LuaScriptEngine — enemy AI via Lua script
    // -------------------------------------------------------------------------

    /*
     * Wikkelt de LuaJ runtime. Laadt enemy_ai.lua bij setup en roept
     * callUpdateEnemy(enemy, deltaTime) elke frame per vijand aan.
     * null als lua.script niet geconfigureerd is of het laden mislukt.
     */
    private LuaScriptEngine luaEngine;

    // -------------------------------------------------------------------------
    // ECS — data-oriented floating combat text (damage numbers, gold popups)
    // -------------------------------------------------------------------------

    /*
     * The project's required data-oriented Entity-Component-System subsystem.
     * The world holds the component data (Structure-of-Arrays); the two systems
     * are stateless logic that sweep that data each frame. Spawned on hit/kill
     * events below, rendered by J2dGame.
     */
    private final FloatingTextWorld floatingText = new FloatingTextWorld();
    private final MovementSystem    ftMovement   = new MovementSystem();
    private final LifetimeSystem    ftLifetime   = new LifetimeSystem();

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
     * 3. Shows the title screen (MENU); a level is only loaded once the player
     *    presses S — see handleInput()
     * 4. Runs the game loop: input → update → render → sleep
     *
     * The factory provides both entity creation AND the visualization layer
     * via getView(), so Game never imports any J2D classes.
     */
    public void start(EntityFactory factory, ConfigManager config) {
        this.entityFactory = factory;
        this.config        = config;
        this.view          = factory.getView();

        // Level count + starting level come from config; show the title screen first.
        this.maxLevels    = config.getInt("levels.count", 2);
        this.currentLevel = config.getInt("selected.level", 1);
        this.state        = GameState.MENU;

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
    // Level setup / progression
    // -------------------------------------------------------------------------

    /*
     * Loads the given level: builds the map and base, resets per-level resources
     * (gold back to starting.gold) and starts spawning. Does NOT touch score —
     * the score is cumulative across levels and is only reset in startNewGame().
     */
    private void loadLevel(int level) {
        this.currentLevel = level;

        ConfigManager levelConfig = new ConfigManager("levels/level" + level + ".properties");
        this.gameMap = new GameMap(levelConfig);

        // Fresh per-level state (entities + gold), score is preserved
        clearEntities();
        this.gold = config.getInt("starting.gold", 200);

        // Create the base at the centre of the base tile
        int startingLives = config.getInt("starting.lives", 20);
        Position basePos = gameMap.getBasePosition();
        Base baseEntity = entityFactory.createBase(
            new Position(basePos.getX() + 0.5, basePos.getY() + 0.5),
            startingLives
        );
        this.base = Optional.of(baseEntity);

        // WaveManager aanmaken op basis van de level config (leest wave.count, wave.N.enemies)
        this.waveManager = new WaveManager(levelConfig);

        // Lua script laden (pad staat in game.properties als "lua.script")
        // Lege string of ontbrekende key schakelt Lua uit zonder crash.
        String luaScript = config.getString("lua.script", "");
        if (!luaScript.isEmpty()) {
            this.luaEngine = new LuaScriptEngine();
            this.luaEngine.loadScript(luaScript);
        }

        this.state = GameState.PLAYING;
    }

    /*
     * Starts a brand-new game from the configured starting level.
     * Used from the MENU ("press S"), after GAME_OVER, and after the final
     * victory ("play again"). Resets the cumulative score to 0.
     */
    private void startNewGame() {
        this.score = 0;
        loadLevel(config.getInt("selected.level", 1));
    }

    /* Advances to the next level, keeping the cumulative score. */
    private void nextLevel() {
        loadLevel(currentLevel + 1);
    }

    /* Clears all per-game entity collections (towers, enemies, projectiles, ECS text). */
    private void clearEntities() {
        this.towers.clear();
        this.enemies.clear();
        this.projectiles.clear();
        this.floatingText.clear();
    }

    /* Ends the application (java.lang only — no visualization dependency). */
    private void quit() {
        System.exit(0);
    }

    // -------------------------------------------------------------------------
    // Input handling — called once per frame
    // -------------------------------------------------------------------------

    /*
     * Processes player input from the GameView, branching on the current state:
     *   - MENU       : S starts a new game, Q quits
     *   - PLAYING    : P pauses, mouse places towers
     *   - PAUSED     : P resumes, Q quits
     *   - WON        : S continues (next level or play-again), Q quits
     *   - GAME_OVER  : S restarts, Q quits
     */
    private void handleInput() {
        switch (state) {
            case MENU:
                if (view.wasStartPressed())      startNewGame();
                else if (view.wasQuitPressed())  quit();
                return;

            case WON:
                if (view.wasStartPressed())      { if (isLastLevel()) startNewGame(); else nextLevel(); }
                else if (view.wasQuitPressed())  quit();
                return;

            case GAME_OVER:
                if (view.wasStartPressed())      startNewGame();
                else if (view.wasQuitPressed())  quit();
                return;

            case PLAYING:
                handlePlayInput();
                return;

            case PAUSED:
                handlePauseInput();
                return;

            default:
                return;
        }
    }

    /*
     * Input while PAUSED: P resumes the game, Q quits. A stray S is drained so it
     * cannot leak into a later WON / GAME_OVER screen.
     */
    private void handlePauseInput() {
        if (view.wasPausePressed()) {
            state = GameState.PLAYING;   // resume
        } else if (view.wasQuitPressed()) {
            quit();
        }
        view.wasStartPressed();   // drain
    }

    /*
     * Input while PLAYING: P pauses, mouse places towers.
     */
    private void handlePlayInput() {
        // Pause
        if (view.wasPausePressed()) {
            state = GameState.PAUSED;
        }

        // Drain start/quit so a stray S/Q during play doesn't leak into the next
        // WON / GAME_OVER screen and skip it.
        view.wasStartPressed();
        view.wasQuitPressed();

        // No tower placement on the frame we just paused
        if (state != GameState.PLAYING) return;

        // Mouse click
        if (!view.wasMouseClicked()) return;

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

        // 2b. Lua AI — roept updateEnemy(enemy, deltaTime) aan voor elke levende vijand.
        //     Wordt VOOR IceTower.applyAreaEffect() uitgevoerd (stap 5), zodat
        //     IceTower altijd prioriteit heeft over een Lua-snelheidsboost.
        if (luaEngine != null) {
            for (Enemy e : enemies) {
                if (e.isAlive()) luaEngine.callUpdateEnemy(e, deltaTime);
            }
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

        // 10. Cleanup dead projectiles
        projectiles.removeIf(p -> !p.isAlive());

        // 10b. ECS — advance the floating combat text (move up + age/expire)
        ftMovement.update(floatingText, deltaTime);
        ftLifetime.update(floatingText, deltaTime);

        // 11. Win/lose check
        checkWinLose();
    }

    // -------------------------------------------------------------------------
    // 1. Fase 7: WaveManager-spawner
    // -------------------------------------------------------------------------

    /*
     * Vraagt de WaveManager elke frame of er een vijand gespawnd moet worden.
     * WaveManager.tick() geeft een Optional<String> terug met het vijandtype
     * ("basic", "armored" of "flying"), of Optional.empty() als er dit frame
     * niets spawnt (wachttijd binnen golf, pauze tussen golven, of klaar).
     *
     * Het juiste pad wordt meegegeven: vliegende vijanden krijgen het luchtpad
     * als dat beschikbaar is, anders het grondpad.
     */
    private void updateSpawner(double deltaTime) {
        waveManager.tick(deltaTime).ifPresent(type -> {
            List<Position> groundPath = gameMap.getEnemyPath().getWaypoints();
            Enemy enemy;

            switch (type) {
                case "flying":
                    // Vliegende vijanden nemen het luchtpad als dat bestaat
                    if (gameMap.hasFlyingPath()) {
                        enemy = entityFactory.createFlyingEnemy(
                            gameMap.getFlyingPath().getWaypoints());
                    } else {
                        enemy = entityFactory.createFlyingEnemy(groundPath);
                    }
                    break;
                case "armored":
                    enemy = entityFactory.createArmoredEnemy(groundPath);
                    break;
                default:
                    // "basic" en alle onbekende types → BasicEnemy
                    enemy = entityFactory.createBasicEnemy(groundPath);
                    break;
            }

            enemies.add(enemy);
        });
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
     * A CannonTower fires a CannonProjectile (carries its own splash stats and
     * applies area damage in onHit); every other tower fires a plain projectile.
     */
    private void updateTowerFiring() {
        for (Tower tower : towers) {
            if (!tower.isReadyToFire()) continue;

            Optional<Enemy> target = tower.findTarget(enemies);
            if (target.isPresent()) {
                Enemy t = target.get();

                Projectile proj;
                if (tower instanceof CannonTower) {
                    CannonTower ct = (CannonTower) tower;
                    proj = entityFactory.createCannonProjectile(
                        tower.getPosition(), t.getPosition(), tower.getDamage(),
                        ct.getSplashRadius(), ct.getSplashDamage()
                    );
                } else {
                    proj = entityFactory.createProjectile(
                        tower.getPosition(), t.getPosition(), tower.getDamage()
                    );
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
     * Checks each alive projectile against all alive enemies. On the first
     * collision the projectile's onHit(target, enemies) is called and the
     * projectile stops checking further enemies.
     *
     * onHit is polymorphic: a plain projectile damages only its target, while a
     * CannonProjectile also applies splash to nearby enemies. The game loop needs
     * no projectile-type-specific code here.
     */
    private void checkProjectileCollisions() {
        for (Projectile p : projectiles) {
            if (!p.isAlive()) continue;

            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;

                if (p.collidesWith(e)) {
                    // Capture HP before/after so the spawned number reflects the
                    // REAL damage dealt (e.g. armour resistance reduces it).
                    double before = e.getCurrentHealth();
                    p.onHit(e, enemies);
                    double dealt = before - e.getCurrentHealth();
                    if (dealt > 0) {
                        floatingText.spawn(
                            e.getPosition().getX(), e.getPosition().getY(),
                            dealt, FloatingTextKind.DAMAGE
                        );
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
                // ECS popup: gold reward at the kill site (label + colour chosen by the renderer)
                floatingText.spawn(
                    e.getPosition().getX(), e.getPosition().getY(),
                    e.getReward(), FloatingTextKind.REWARD
                );
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

        // Gewonnen als de WaveManager alle golven heeft gespawnd én er geen vijanden meer leven
        if (waveManager.isFinished() && enemies.isEmpty()) {
            state = GameState.WON;
        }
    }

    // =========================================================================
    // Getters / entity access
    // =========================================================================

    public List<Tower>      getTowers()      { return towers; }
    public List<Enemy>      getEnemies()     { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }

    /** Read-only access to the ECS floating-text world (used by the renderer). */
    public FloatingTextWorld getFloatingText() { return floatingText; }

    public Optional<Base> getBase()          { return base; }

    public GameMap getGameMap()              { return gameMap; }

    public GameState getState()              { return state; }
    public void      setState(GameState s)   { this.state = s; }

    public int  getScore()                   { return score; }

    public int  getGold()                    { return gold; }

    /** Aantal vijanden dat nog gespawnd wordt in de huidige golf (voor de HUD). */
    public int getEnemiesRemaining() {
        return waveManager != null ? waveManager.getRemainingSpawnsInCurrentWave() : 0;
    }

    /** Huidig levelnummer (1-gebaseerd, voor de HUD). */
    public int getCurrentLevel() { return currentLevel; }

    /** Totaal aantal levels (voor de HUD en de WON-schermkeuze). */
    public int getMaxLevels() { return maxLevels; }

    /** True als het huidige level het laatste is (→ Ultimate Victory i.p.v. level complete). */
    public boolean isLastLevel() { return currentLevel >= maxLevels; }

    /** Huidig golfnummer (1-gebaseerd, voor de HUD). */
    public int getCurrentWave() {
        return waveManager != null ? waveManager.getCurrentWaveNumber() : 0;
    }

    /** Totaal aantal golven in dit level (voor de HUD). */
    public int getTotalWaves() {
        return waveManager != null ? waveManager.getTotalWaves() : 0;
    }

    public void spendGold(int amount) {
        if (amount > this.gold) {
            throw new IllegalStateException(
                "Not enough gold: tried to spend " + amount + " but only have " + this.gold
            );
        }
        this.gold -= amount;
    }
}
