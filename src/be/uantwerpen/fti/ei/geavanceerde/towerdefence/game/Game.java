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
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies.EnemyType;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map.GameMap;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.TowerType;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Stopwatch;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.scripting.LuaScriptEngine;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.wave.WaveManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Central singleton representing the entire Tower Defence game.
 *
 * <p><strong>Singleton pattern:</strong> only one {@code Game} instance exists. The
 * constructor is private and all access goes through {@link #getInstance()}.</p>
 *
 * <p><strong>Abstract Factory:</strong> an {@link EntityFactory} is injected via
 * {@link #start(EntityFactory, ConfigManager)}. The game package only knows the
 * interface; the J2D package provides the concrete {@code J2dEntityFactory}. The
 * factory also provides the {@link GameView} (render + input abstraction), keeping the
 * game package completely free of visualization imports.</p>
 *
 * <p><strong>Role:</strong> the game singleton owns all entity lists (towers,
 * enemies, projectiles, base), tracks score and gold, holds the current
 * {@link GameState}, runs the main game loop via
 * {@link #start(EntityFactory, ConfigManager)}, and handles player input (pause, tower
 * placement, restart).</p>
 *
 * <p><strong>Game loop</strong> (inside {@code start} → {@link #update(double)}):
 * spawn enemies via the {@code WaveManager}, update enemies (move + slow timer), check
 * enemies reaching the base, update towers (cooldown tick), apply tower area effects
 * (ice slow), do tower targeting + firing, update projectiles, check projectile↔enemy
 * collisions + splash, clean up dead enemies (awarding gold/score), clean up dead
 * projectiles, and finally run the win/lose check.</p>
 *
 * @author Tower Defence team
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

    // Goudkost om de base volledig te repareren (uit config, default 50).
    private int repairCost;

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

    // Nullable: null until a level is loaded. The getter wraps it in an Optional
    // for callers — Optional is a return type, not a field type (Goetz).
    private Base base;

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
        this.base        = null;
    }

    /**
     * Returns the single shared {@code Game} instance, creating it on first access
     * (lazy initialization).
     *
     * @return the singleton game instance
     */
    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    // =========================================================================
    // start() — the single entry point called by Main
    // =========================================================================

    /**
     * Sets up and runs the game. This is the only method {@code Main} needs to call.
     *
     * <p>It stores the factory and config, obtains the {@link GameView} from the
     * factory (render + input), shows the title screen ({@link GameState#MENU} — a
     * level is only loaded once the player presses S, see {@code handleInput()}), and
     * then runs the game loop: input → update → render → sleep.</p>
     *
     * <p>The factory provides both entity creation <em>and</em> the visualization layer
     * via {@link EntityFactory#getView()}, so {@code Game} never imports any J2D
     * classes. This method does not return — it loops until the window is closed.</p>
     *
     * @param factory the abstract factory used to create entities and the view
     * @param config  the loaded game configuration
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
        this.repairCost = config.getInt("repair.cost", 50);

        // Create the base at the centre of the base tile
        int startingLives = config.getInt("starting.lives", 20);
        Position basePos = gameMap.getBasePosition();
        Base baseEntity = entityFactory.createBase(
            new Position(basePos.getX() + 0.5, basePos.getY() + 0.5),
            startingLives
        );
        this.base = baseEntity;

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

        // Repair: R herstelt de base volledig tegen repairCost goud.
        // Enkel betalen als er genoeg goud is én de base niet al op volle HP staat.
        if (view.wasRepairPressed() && base != null) {
            if (gold >= repairCost && base.getCurrentHealth() < base.getMaxHealth()) {
                spendGold(repairCost);
                base.repair(base.getMaxHealth());   // volledig herstel
            }
        }

        // Drain start/quit so a stray S/Q during play doesn't leak into the next
        // WON / GAME_OVER screen and skip it.
        view.wasStartPressed();
        view.wasQuitPressed();

        // No tower placement on the frame we just paused
        if (state != GameState.PLAYING) return;

        // Mouse click
        if (!view.wasMouseClicked()) return;

        // Tower placement — translate the player's hotkey (1/2/3) to a tower type.
        // Returns null for 0 (nothing selected) or any unknown key.
        TowerType towerType = TowerType.fromHotkey(view.getSelectedTower());
        if (towerType == null) return;

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

        // Create the tower via the Abstract Factory (the enum picks the right
        // factory method polymorphically — no switch, no concrete imports here).
        Tower tower = towerType.create(entityFactory, towerPos);

        // The tower reports its own cost; only place it if the player can afford it.
        if (gold < tower.getCost()) return;

        spendGold(tower.getCost());
        towers.add(tower);
    }

    // =========================================================================
    // GAME LOOP — called once per frame with delta time in seconds
    // =========================================================================

    /**
     * Advances the whole game world by one frame. Does nothing unless the state is
     * {@link GameState#PLAYING}.
     *
     * <p>Runs, in order: enemy spawning, enemy movement and Lua AI, base-reached
     * checks, tower cooldowns and area effects, tower targeting/firing, projectile
     * movement and collisions (including splash), cleanup of dead enemies (awarding
     * gold/score) and projectiles, the ECS floating-text update, and the win/lose
     * check.</p>
     *
     * @param deltaTime the elapsed time since the previous frame, in seconds
     */
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
        waveManager.tick(deltaTime).ifPresent(typeStr -> {
            List<Position> groundPath = gameMap.getEnemyPath().getWaypoints();

            // Resolve the wave-config string to a type (unknown → BASIC).
            EnemyType type = EnemyType.fromId(typeStr);

            // Path choice stays here (it depends on the GameMap, which the factory
            // must not know about) but keys on the type property, not a string.
            List<Position> path = (type.usesAirPath() && gameMap.hasFlyingPath())
                ? gameMap.getFlyingPath().getWaypoints()
                : groundPath;

            // The enum picks the right factory method polymorphically.
            enemies.add(type.create(entityFactory, path));
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
                if (base != null) base.takeDamage(1);
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
     * If it finds one and its cooldown is ready, the tower creates its own
     * projectile via the factory (tower.fire) and the game loop resets the
     * cooldown.
     *
     * The game loop does no type-checking: tower.fire() polymorphically returns
     * the right projectile (a ray projectile for an ArrowTower, a cannon
     * projectile with splash stats for a CannonTower).
     */
    private void updateTowerFiring() {
        for (Tower tower : towers) {
            if (!tower.isReadyToFire()) continue;

            Optional<Enemy> target = tower.findTarget(enemies);
            if (target.isPresent()) {
                Projectile proj = tower.fire(entityFactory, target.get());
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
    //logboek projectile switch case weggehaald, want projectiles handelen zelf hun eigen gedrag af in onHit()
    // — geen type-checking of switch nodig in de game loop.
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
        if (base != null && base.isDestroyed()) {
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

    /**
     * Returns the live list of towers currently placed in the game.
     *
     * @return the towers list
     */
    public List<Tower>      getTowers()      { return towers; }

    /**
     * Returns the live list of enemies currently in the game.
     *
     * @return the enemies list
     */
    public List<Enemy>      getEnemies()     { return enemies; }

    /**
     * Returns the live list of projectiles currently in flight.
     *
     * @return the projectiles list
     */
    public List<Projectile> getProjectiles() { return projectiles; }

    /**
     * Returns read-only access to the ECS floating-text world.
     *
     * @return the floating-text component datastore
     */
    public FloatingTextWorld getFloatingText() { return floatingText; }

    /**
     * Returns the player's base, if a level is currently loaded.
     *
     * @return an {@link Optional} containing the base, or empty before a level loads
     */
    public Optional<Base> getBase()          { return Optional.ofNullable(base); }

    /**
     * Returns the current level's map.
     *
     * @return the active game map
     */
    public GameMap getGameMap()              { return gameMap; }

    /**
     * Returns the current game state.
     *
     * @return the current {@link GameState}
     */
    public GameState getState()              { return state; }

    /**
     * Sets the current game state.
     *
     * @param s the new {@link GameState}
     */
    public void      setState(GameState s)   { this.state = s; }

    /**
     * Returns the player's cumulative score (preserved across levels).
     *
     * @return the current score
     */
    public int  getScore()                   { return score; }

    /**
     * Returns the player's current gold (reset per level).
     *
     * @return the current gold
     */
    public int  getGold()                    { return gold; }

    /**
     * Returns the number of enemies still to be spawned in the current wave.
     *
     * @return the remaining spawns in the current wave, or {@code 0} if no wave is active
     */
    public int getEnemiesRemaining() {
        return waveManager != null ? waveManager.getRemainingSpawnsInCurrentWave() : 0;
    }

    /**
     * Returns the current level number (1-based).
     *
     * @return the current level number
     */
    public int getCurrentLevel() { return currentLevel; }

    /**
     * Returns the total number of levels in the game.
     *
     * @return the total number of levels
     */
    public int getMaxLevels() { return maxLevels; }

    /**
     * Returns whether the current level is the final level (used to decide when the
     * player has completed the whole game).
     *
     * @return {@code true} if the current level is the final level
     */
    public boolean isLastLevel() { return currentLevel >= maxLevels; }

    /**
     * Returns the current wave number (1-based).
     *
     * @return the current wave number, or {@code 0} if no wave manager is active
     */
    public int getCurrentWave() {
        return waveManager != null ? waveManager.getCurrentWaveNumber() : 0;
    }

    /**
     * Returns the total number of waves in this level.
     *
     * @return the total number of waves, or {@code 0} if no wave manager is active
     */
    public int getTotalWaves() {
        return waveManager != null ? waveManager.getTotalWaves() : 0;
    }

    /**
     * Deducts the given amount of gold from the player.
     *
     * @param amount the amount of gold to spend
     * @throws IllegalStateException if the player does not have enough gold
     */
    public void spendGold(int amount) {
        if (amount > this.gold) {
            throw new IllegalStateException(
                "Not enough gold: tried to spend " + amount + " but only have " + this.gold
            );
        }
        this.gold -= amount;
    }
}
