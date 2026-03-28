package be.uantwerpen.fti.ei.geavanceerde.towerdefence;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.Game;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.GameState;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map.GameMap;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.ArrowTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.CannonTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.IceTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Stopwatch;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.InputHandler;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dEntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;

/*
 * Entry point for the Tower Defence game.
 *
 * WIRING (how everything connects):
 *   1. Load game config from resources/config/game.properties
 *   2. Create the J2dGame window (Java2D visualization layer)
 *   3. Create a J2dEntityFactory — the concrete Abstract Factory
 *   4. Pass the factory to Game.getInstance().init() — the game logic
 *      never sees J2d classes directly (game/viz separation)
 *   5. Load the selected level (.properties file → GameMap)
 *   6. Create the base entity at the map's base position
 *   7. Start the game loop: tick → input → update → render → sleep
 *
 * The game loop runs at roughly 60 FPS (16ms sleep per frame).
 * All movement and timing uses deltaTime from Stopwatch for
 * frame-rate independence.
 */
public class Main {

    // Target ~60 FPS — sleep 16ms between frames
    private static final int FRAME_DELAY_MS = 16;

    // Total enemies to spawn in this session (placeholder until WaveManager in Fase 7)
    private static final int TOTAL_ENEMIES = 15;

    public static void main(String[] args) {

        // --- 1. Load game configuration ---
        ConfigManager config = new ConfigManager("config/game.properties");

        // --- 2. Create the J2D visualization layer (window + rendering) ---
        J2dGame j2dGame = new J2dGame(config);

        // --- 3. Create the concrete entity factory (Abstract Factory pattern) ---
        // The factory lives in the j2d package but is passed to the game logic
        // as the EntityFactory interface — the game never imports j2d classes.
        J2dEntityFactory factory = new J2dEntityFactory(j2dGame);

        // --- 4. Initialise the game singleton ---
        Game game = Game.getInstance();
        game.init(factory, config);

        // --- 5. Load the selected level ---
        int level = config.getInt("selected.level", 1);
        ConfigManager levelConfig = new ConfigManager("levels/level" + level + ".properties");
        game.loadLevel(levelConfig);

        // --- 6. Create the base at the map's base position ---
        GameMap map = game.getGameMap();
        int startingLives = config.getInt("starting.lives", 20);
        Position basePos = map.getBasePosition();

        // Place the base at the centre of the base tile (+0.5 offset)
        Base base = factory.createBase(
            new Position(basePos.getX() + 0.5, basePos.getY() + 0.5),
            startingLives
        );
        game.setBase(base);

        // --- 7. Start playing — enemies will begin spawning ---
        game.startPlaying(TOTAL_ENEMIES);

        // --- 8. Game loop ---
        Stopwatch stopwatch = new Stopwatch();
        InputHandler input = j2dGame.getInputHandler();

        while (true) {
            double deltaTime = stopwatch.tick();

            // ---- Handle input ----
            handleInput(game, input, factory, map);

            // ---- Update game logic (only while PLAYING) ----
            game.update(deltaTime);

            // ---- Render everything ----
            j2dGame.render();

            // ---- Frame rate limiter ----
            try {
                Thread.sleep(FRAME_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Input handling — called once per frame
    // -------------------------------------------------------------------------

    /*
     * Processes player input:
     *   - P key: toggle pause / unpause
     *   - Mouse click + tower selected: place a tower if affordable and buildable
     *   - GAME_OVER or WON: any click restarts the game (placeholder)
     */
    private static void handleInput(Game game, InputHandler input,
                                    J2dEntityFactory factory, GameMap map) {
        // --- Pause toggle ---
        if (input.wasPausePressed()) {
            if (game.getState() == GameState.PLAYING) {
                game.setState(GameState.PAUSED);
            } else if (game.getState() == GameState.PAUSED) {
                game.setState(GameState.PLAYING);
            }
        }

        // --- Mouse click ---
        if (!input.wasMouseClicked()) return;

        // If the game is over or won, a click restarts
        if (game.getState() == GameState.GAME_OVER || game.getState() == GameState.WON) {
            restartGame(game, factory, map);
            return;
        }

        // Only allow tower placement while playing
        if (game.getState() != GameState.PLAYING) return;

        // --- Tower placement ---
        int towerType = input.getSelectedTower();
        if (towerType == 0) return;  // no tower selected

        // Convert mouse position to game-world tile coordinates
        double gameX = input.getMouseGameX();
        double gameY = input.getMouseGameY();
        Position clickPos = new Position(gameX, gameY);

        // Check if the tile is a valid build spot
        if (!map.canBuildAt(clickPos)) return;

        // Snap the tower to the centre of the tile
        int tileX = (int) gameX;
        int tileY = (int) gameY;
        Position towerPos = new Position(tileX + 0.5, tileY + 0.5);

        // Check no tower already exists on this tile
        for (Tower existing : game.getTowers()) {
            double dx = Math.abs(existing.getPosition().getX() - towerPos.getX());
            double dy = Math.abs(existing.getPosition().getY() - towerPos.getY());
            if (dx < 0.5 && dy < 0.5) return;  // spot already occupied
        }

        // Determine cost and check if the player can afford it
        int cost;
        switch (towerType) {
            case 1: cost = ArrowTower.DEFAULT_COST;  break;
            case 2: cost = CannonTower.DEFAULT_COST; break;
            case 3: cost = IceTower.DEFAULT_COST;    break;
            default: return;
        }

        if (game.getGold() < cost) return;  // not enough gold

        // Create the tower via the Abstract Factory
        Tower tower;
        switch (towerType) {
            case 1: tower = factory.createArrowTower(towerPos);  break;
            case 2: tower = factory.createCannonTower(towerPos); break;
            case 3: tower = factory.createIceTower(towerPos);    break;
            default: return;
        }

        // Deduct gold and add the tower to the game
        game.spendGold(cost);
        game.getTowers().add(tower);
    }

    // -------------------------------------------------------------------------
    // Restart — resets the game and reloads the level
    // -------------------------------------------------------------------------

    /*
     * Called when the player clicks after GAME_OVER or WON.
     * Resets all game state and reloads the current level.
     */
    private static void restartGame(Game game, J2dEntityFactory factory, GameMap map) {
        game.reset();

        // Reinitialise with the same factory and config
        ConfigManager config = new ConfigManager("config/game.properties");
        game.init(factory, config);

        int level = config.getInt("selected.level", 1);
        ConfigManager levelConfig = new ConfigManager("levels/level" + level + ".properties");
        game.loadLevel(levelConfig);

        // Recreate the base
        GameMap newMap = game.getGameMap();
        int startingLives = config.getInt("starting.lives", 20);
        Position basePos = newMap.getBasePosition();
        Base base = factory.createBase(
            new Position(basePos.getX() + 0.5, basePos.getY() + 0.5),
            startingLives
        );
        game.setBase(base);

        // Start spawning enemies again
        game.startPlaying(TOTAL_ENEMIES);
    }
}
