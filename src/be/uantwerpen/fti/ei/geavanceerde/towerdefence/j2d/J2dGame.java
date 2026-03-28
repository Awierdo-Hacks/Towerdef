package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.Game;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.GameState;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Bonus;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map.GameMap;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map.Tile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

/*
 * The Java2D visualization layer — manages the game window, coordinate
 * conversion, and the rendering pipeline.
 *
 * WINDOW:
 *   Uses a JFrame with an AWT Canvas and double-buffered BufferStrategy.
 *   All drawing goes through render() which is called once per frame.
 *
 * COORDINATE CONVERSION:
 *   The game logic works in game-world units (e.g. 20.0 x 15.0).
 *   J2dGame converts these to screen pixels for rendering, and converts
 *   screen pixels back to game-world coordinates for mouse input.
 *
 * RENDER PIPELINE:
 *   1. Acquire Graphics2D from BufferStrategy
 *   2. Store it in g2d so J2d entities can access it via getGraphics2D()
 *   3. Clear screen, draw map tiles
 *   4. Call render() on all alive entities (they use g2d internally)
 *   5. Draw HUD (score, gold)
 *   6. Dispose graphics and flip buffer
 *
 * J2d entities hold a reference to this J2dGame object so they can call
 * getGraphics2D() and toScreenX/Y() inside their render() method.
 */
public class J2dGame {

    // -------------------------------------------------------------------------
    // Window
    // -------------------------------------------------------------------------

    private JFrame frame;
    private Canvas canvas;
    private BufferStrategy bufferStrategy;

    // -------------------------------------------------------------------------
    // Dimensions
    // -------------------------------------------------------------------------

    // Screen size in pixels
    private int windowWidth;
    private int windowHeight;

    // Game world size in game units (e.g. 20.0 x 15.0)
    private double gameWidth;
    private double gameHeight;

    // -------------------------------------------------------------------------
    // Current graphics context — set at the start of each render() call
    // -------------------------------------------------------------------------

    // J2d entities call getGraphics2D() to access this during their render()
    private Graphics2D g2d;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private InputHandler inputHandler;

    // -------------------------------------------------------------------------
    // Tile colours
    // -------------------------------------------------------------------------

    private static final Color COLOR_GRASS      = new Color(76, 153, 0);
    private static final Color COLOR_PATH       = new Color(194, 178, 128);
    private static final Color COLOR_WATER      = new Color(64, 164, 223);
    private static final Color COLOR_BUILD_SPOT = new Color(140, 140, 140);
    private static final Color COLOR_SPAWN      = new Color(200, 80, 80);
    private static final Color COLOR_BASE_TILE  = new Color(255, 215, 0);
    private static final Color COLOR_GRID_LINE  = new Color(0, 0, 0, 30);

    // HUD
    private static final Color COLOR_HUD_BG   = new Color(0, 0, 0, 150);
    private static final Color COLOR_HUD_TEXT  = Color.WHITE;
    private static final Font  FONT_HUD       = new Font("Monospaced", Font.BOLD, 16);

    // Overlay for game-over / victory screens
    private static final Color  COLOR_OVERLAY   = new Color(0, 0, 0, 180);
    private static final Font   FONT_OVERLAY    = new Font("SansSerif", Font.BOLD, 48);
    private static final Font   FONT_SUB        = new Font("SansSerif", Font.PLAIN, 20);

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    /*
     * Creates the game window and sets up the rendering canvas.
     *
     * Reads window/game dimensions from the main game config:
     *   window.width, window.height  (pixels)
     *   game.width, game.height      (game-world units)
     */
    public J2dGame(ConfigManager config) {
        windowWidth  = config.getInt("window.width", 800);
        windowHeight = config.getInt("window.height", 600);
        gameWidth    = config.getDouble("game.width", 20.0);
        gameHeight   = config.getDouble("game.height", 15.0);

        // --- JFrame ---
        frame = new JFrame(config.getString("window.title", "Tower Defence"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // --- Canvas for high-performance rendering ---
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(windowWidth, windowHeight));
        canvas.setFocusable(true);
        frame.add(canvas);
        frame.pack();
        frame.setLocationRelativeTo(null);  // centre on screen
        frame.setVisible(true);

        // --- Double buffering via BufferStrategy ---
        canvas.createBufferStrategy(2);
        bufferStrategy = canvas.getBufferStrategy();

        // --- Input handling ---
        inputHandler = new InputHandler(this);
        canvas.addKeyListener(inputHandler);
        canvas.addMouseListener(inputHandler);
        canvas.addMouseMotionListener(inputHandler);
        canvas.requestFocus();
    }

    // -------------------------------------------------------------------------
    // Coordinate conversion — game world ↔ screen pixels
    // -------------------------------------------------------------------------

    /* Converts a game-world X coordinate to screen pixel X. */
    public int toScreenX(double gameX) {
        return (int) (gameX * windowWidth / gameWidth);
    }

    /* Converts a game-world Y coordinate to screen pixel Y. */
    public int toScreenY(double gameY) {
        return (int) (gameY * windowHeight / gameHeight);
    }

    /* Converts a game-world width to screen pixel width. */
    public int toScreenWidth(double w) {
        return (int) (w * windowWidth / gameWidth);
    }

    /* Converts a game-world height to screen pixel height. */
    public int toScreenHeight(double h) {
        return (int) (h * windowHeight / gameHeight);
    }

    /* Converts a screen pixel X to game-world X (for mouse input). */
    public double toGameX(int screenX) {
        return screenX * gameWidth / windowWidth;
    }

    /* Converts a screen pixel Y to game-world Y (for mouse input). */
    public double toGameY(int screenY) {
        return screenY * gameHeight / windowHeight;
    }

    // -------------------------------------------------------------------------
    // Render pipeline — called once per frame by the game loop
    // -------------------------------------------------------------------------

    /*
     * Draws everything: map tiles, entities, and HUD.
     *
     * Sets this.g2d before drawing so J2d entities can call getGraphics2D()
     * inside their render() methods.
     */
    public void render() {
        g2d = (Graphics2D) bufferStrategy.getDrawGraphics();

        try {
            // Antialiasing for smoother shapes
            g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            // 1. Clear screen with grass colour as fallback
            g2d.setColor(COLOR_GRASS);
            g2d.fillRect(0, 0, windowWidth, windowHeight);

            // 2. Draw the tile grid
            renderMap();

            // 3. Draw all entities — each J2d entity implements render()
            Game game = Game.getInstance();

            // Base first (background layer)
            game.getBase().ifPresent(Base::render);

            // Towers
            for (Tower t : game.getTowers()) {
                if (t.isAlive()) t.render();
            }

            // Enemies
            for (Enemy e : game.getEnemies()) {
                if (e.isAlive()) e.render();
            }

            // Projectiles
            for (Projectile p : game.getProjectiles()) {
                if (p.isAlive()) p.render();
            }

            // Bonuses
            for (Bonus b : game.getBonuses()) {
                if (b.isAlive()) b.render();
            }

            // 4. HUD overlay
            renderHUD();

        } finally {
            g2d.dispose();
            bufferStrategy.show();
        }
    }

    // -------------------------------------------------------------------------
    // Map rendering
    // -------------------------------------------------------------------------

    /*
     * Draws the tile grid. Each tile is a coloured rectangle based on its TileType.
     * BUILD_SPOT tiles get an extra border to show they're interactive.
     */
    private void renderMap() {
        GameMap map = Game.getInstance().getGameMap();
        if (map == null) return;

        Tile[][] grid = map.getGrid();
        int tileW = toScreenWidth(1.0);
        int tileH = toScreenHeight(1.0);

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int sx = toScreenX(x);
                int sy = toScreenY(y);

                // Choose colour by tile type
                switch (grid[x][y].getType()) {
                    case GRASS:      g2d.setColor(COLOR_GRASS);      break;
                    case PATH:       g2d.setColor(COLOR_PATH);       break;
                    case WATER:      g2d.setColor(COLOR_WATER);      break;
                    case BUILD_SPOT: g2d.setColor(COLOR_BUILD_SPOT); break;
                    case SPAWN:      g2d.setColor(COLOR_SPAWN);      break;
                    case BASE:       g2d.setColor(COLOR_BASE_TILE);  break;
                }
                g2d.fillRect(sx, sy, tileW, tileH);

                // Subtle grid lines
                g2d.setColor(COLOR_GRID_LINE);
                g2d.drawRect(sx, sy, tileW, tileH);

                // BUILD_SPOT gets an extra dashed border so the player can see it
                if (grid[x][y].getType() == be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map.TileType.BUILD_SPOT) {
                    g2d.setColor(new Color(255, 255, 255, 80));
                    g2d.drawRect(sx + 2, sy + 2, tileW - 4, tileH - 4);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // HUD rendering
    // -------------------------------------------------------------------------

    /*
     * Draws the heads-up display:
     *   - Top bar: gold, score, base HP, enemies left, selected tower
     *   - Full-screen overlay when GAME_OVER or WON
     */
    private void renderHUD() {
        Game game = Game.getInstance();

        // --- Top bar with game stats ---
        g2d.setColor(COLOR_HUD_BG);
        g2d.fillRect(0, 0, windowWidth, 30);

        g2d.setColor(COLOR_HUD_TEXT);
        g2d.setFont(FONT_HUD);

        // Base HP text — show current/max if the base exists
        String hpText = game.getBase()
            .map(b -> b.getCurrentHealth() + "/" + b.getMaxHealth())
            .orElse("?");

        String hudText = "Gold: " + game.getGold()
            + "  Score: " + game.getScore()
            + "  HP: " + hpText
            + "  Left: " + (game.getEnemiesRemaining() + game.getEnemies().size());

        // Show selected tower from input handler
        int sel = inputHandler.getSelectedTower();
        if (sel > 0) {
            String[] names = {"", "Arrow [50g]", "Cannon [100g]", "Ice [75g]"};
            hudText += "  Tower: " + names[sel];
        }

        // Show PAUSED indicator
        if (game.getState() == GameState.PAUSED) {
            hudText += "  [PAUSED]";
        }

        g2d.drawString(hudText, 10, 22);

        // --- Full-screen overlay for GAME_OVER or WON ---
        if (game.getState() == GameState.GAME_OVER) {
            renderOverlay("GAME OVER", "Score: " + game.getScore() + "  —  Click to restart",
                          new Color(200, 50, 50));
        } else if (game.getState() == GameState.WON) {
            renderOverlay("VICTORY!", "Score: " + game.getScore() + "  —  Click to restart",
                          new Color(50, 200, 50));
        }
    }

    /*
     * Draws a centred overlay with a title and subtitle.
     * Used for game-over and victory screens.
     */
    private void renderOverlay(String title, String subtitle, Color accentColor) {
        // Darken the background
        g2d.setColor(COLOR_OVERLAY);
        g2d.fillRect(0, 0, windowWidth, windowHeight);

        // Title text — centred horizontally
        g2d.setFont(FONT_OVERLAY);
        g2d.setColor(accentColor);
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (windowWidth - titleWidth) / 2, windowHeight / 2 - 20);

        // Subtitle — smaller, white, below the title
        g2d.setFont(FONT_SUB);
        g2d.setColor(COLOR_HUD_TEXT);
        int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
        g2d.drawString(subtitle, (windowWidth - subWidth) / 2, windowHeight / 2 + 20);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /*
     * Returns the current Graphics2D context.
     * Only valid during a render() call — J2d entities use this in their render().
     */
    public Graphics2D getGraphics2D()  { return g2d; }
    public InputHandler getInputHandler() { return inputHandler; }
    public Canvas getCanvas()          { return canvas; }
    public JFrame getFrame()           { return frame; }
    public int getWindowWidth()        { return windowWidth; }
    public int getWindowHeight()       { return windowHeight; }
    public double getGameWidth()       { return gameWidth; }
    public double getGameHeight()      { return gameHeight; }
}
