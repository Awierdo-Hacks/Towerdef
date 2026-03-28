package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Bonus;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;

import java.util.ArrayList;
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
 *   - Runs the main update() loop each frame (implemented in Fase 6)
 *   - Uses EntityFactory (Fase 3) to create entities without knowing about J2D
 *
 * FACTORY (Fase 3 placeholder):
 *   entityFactory is currently Object. In Fase 3 it becomes EntityFactory.
 *   Injected via init() so game logic never imports the J2D package.
 */
public final class Game {

    // The single shared instance — null until first call to getInstance()
    private static Game instance;

    // -------------------------------------------------------------------------
    // Game state
    // -------------------------------------------------------------------------

    // Drives the game loop and UI: MENU, PLAYING, PAUSED, GAME_OVER, WON
    private GameState state;

    // Player stats
    private int score;
    private int gold;

    // -------------------------------------------------------------------------
    // Entity lists — populated by the factory in Fase 3+
    // -------------------------------------------------------------------------

    // All towers currently placed on the map
    private final List<Tower> towers;

    // All enemies currently alive on the map
    private final List<Enemy> enemies;

    // All projectiles currently in flight
    private final List<Projectile> projectiles;

    // Active bonuses sitting on the map waiting to be collected
    private final List<Bonus> bonuses;

    // The player's base — wrapped in Optional to avoid null (there is always one, but
    // it's not created until init() loads the level, so Optional is the clean choice)
    private Optional<Base> base;

    // -------------------------------------------------------------------------
    // Factory (placeholder until Fase 3)
    // -------------------------------------------------------------------------

    // TODO (Fase 3): Replace Object with EntityFactory interface
    @SuppressWarnings("unused")
    private Object entityFactory;

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    // Private constructor — use getInstance()
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

    /*
     * Returns the single Game instance, creating it on the first call.
     * All subsequent calls return the same object — guaranteed one instance.
     */
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
     * Sets up the game for play: stores the factory, reads config values.
     * Call this once before starting the game loop.
     *
     * factory parameter is Object for now — replaced by EntityFactory in Fase 3.
     */
    public void init(Object factory, ConfigManager config) {
        this.entityFactory = factory;
        this.gold          = config.getInt("starting.gold", 100);
        this.score         = 0;
        this.state         = GameState.MENU;
    }

    // -------------------------------------------------------------------------
    // Game Loop (stub — fully implemented in Fase 6)
    // -------------------------------------------------------------------------

    /*
     * Updates all game logic for one frame.
     *
     * In Fase 6 this will contain:
     *   1. waveManager.update(deltaTime) → spawn enemies
     *   2. Move all enemies along their paths
     *   3. Each tower finds a target and fires a projectile
     *   4. Move all projectiles toward their targets
     *   5. Collision check: projectile hits enemy → takeDamage, award gold/score
     *   6. Enemy reaches base → base.takeDamage, check GAME_OVER
     *   7. Remove dead entities from lists
     *   8. Check if all waves cleared → WON
     */
    public void update(double deltaTime) {
        // TODO (Fase 6): implement full game loop logic
    }

    /*
     * Resets everything back to a clean state for a new game or level restart.
     * Clears all entity lists, zeroes score/gold, returns to MENU.
     */
    public void reset() {
        this.score = 0;
        this.gold  = 0;
        this.state = GameState.MENU;
        this.towers.clear();
        this.enemies.clear();
        this.projectiles.clear();
        this.bonuses.clear();
        this.base = Optional.empty();
    }

    // -------------------------------------------------------------------------
    // Entity list access — used by the game loop and the rendering layer
    // -------------------------------------------------------------------------

    public List<Tower>      getTowers()      { return towers; }
    public List<Enemy>      getEnemies()     { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<Bonus>      getBonuses()     { return bonuses; }

    /* The base may be absent between levels — callers must handle Optional. */
    public Optional<Base> getBase() { return base; }

    public void setBase(Base base)  { this.base = Optional.of(base); }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    public GameState getState()            { return state; }
    public void      setState(GameState s) { this.state = s; }

    // -------------------------------------------------------------------------
    // Score & Gold
    // -------------------------------------------------------------------------

    public int  getScore()            { return score; }
    public void addScore(int points)  { this.score += points; }

    public int  getGold()             { return gold; }
    public void addGold(int amount)   { this.gold += amount; }

    /*
     * Deducts gold when the player places a tower.
     * Throws IllegalStateException if the player can't afford it —
     * the caller (input handler) should check getGold() >= cost first.
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
