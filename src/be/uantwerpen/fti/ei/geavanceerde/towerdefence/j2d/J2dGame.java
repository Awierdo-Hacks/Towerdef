package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.Game;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.GameState;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.GameView;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs.FloatingTextKind;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs.FloatingTextWorld;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map.GameMap;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map.Tile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.IceTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.TowerType;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

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
public class J2dGame implements GameView {

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

    // Range ring drawn around every placed tower (subtle, always on).
    private static final Color COLOR_RANGE_FILL = new Color(255, 255, 255, 22);
    private static final Color COLOR_RANGE_EDGE = new Color(255, 255, 255, 90);

    // IceTower keeps its signature cyan slow-aura, like before.
    private static final Color COLOR_ICE_RANGE_FILL = new Color(0, 206, 209, 40);
    private static final Color COLOR_ICE_RANGE_EDGE = new Color(0, 206, 209, 120);

    // Tower placement preview — the detection-radius circle + hovered tile, shown
    // while a tower is selected. Green-ish on a valid build spot, red-ish otherwise.
    private static final Color COLOR_PREVIEW_OK_FILL  = new Color(0, 200, 255, 40);
    private static final Color COLOR_PREVIEW_OK_EDGE  = new Color(0, 200, 255, 170);
    private static final Color COLOR_PREVIEW_BAD_FILL = new Color(220, 60, 60, 40);
    private static final Color COLOR_PREVIEW_BAD_EDGE = new Color(220, 60, 60, 170);

    // HUD
    private static final Color COLOR_HUD_BG   = new Color(0, 0, 0, 150);
    private static final Color COLOR_HUD_TEXT  = Color.WHITE;
    private static final Font  FONT_HUD       = new Font("Monospaced", Font.BOLD, 16);

    // Overlay for game-over / victory screens
    private static final Color  COLOR_OVERLAY   = new Color(0, 0, 0, 255);
    private static final Font   FONT_OVERLAY    = new Font("SansSerif", Font.BOLD, 48);
    private static final Font   FONT_SUB        = new Font("SansSerif", Font.PLAIN, 20);

    // Visuele vergroting van sprites t.o.v. hun logische hitbox. Puur cosmetisch:
    // het venster schaalt alles al evenredig mee (toScreenWidth), maar de logische
    // groottes (enemy 0.6, projectiel 0.2, ...) ogen klein in een tile. Deze factor
    // tekent de sprite groter en GECENTREERD op dezelfde positie, terwijl width/height
    // — en dus collidesWith / gameplay / de bewust kleine flying-hitbox — onaangeroerd
    // blijven. Wordt door de J2d-entities gebruikt in hun render().
    public static final double SPRITE_SCALE = 1.5;

    // Floating combat text (ECS) — small bold numbers above entities.
    // The game layer only supplies a FloatingTextKind; these colours are the
    // visualization layer's mapping of each kind.
    private static final Font   FONT_FLOAT          = new Font("SansSerif", Font.BOLD, 14);
    private static final Color  COLOR_FLOAT_DAMAGE  = Color.WHITE;
    private static final Color  COLOR_FLOAT_REWARD  = new Color(255, 215, 0);  // gold

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

            // Bilinear sampling so the large source PNGs (e.g. 1024x1024 enemies)
            // downscale to ~40px smoothly instead of looking harsh/aliased.
            g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            // 1. Clear screen with grass colour as fallback
            g2d.setColor(COLOR_GRASS);
            g2d.fillRect(0, 0, windowWidth, windowHeight);

            // Title screen: no map/entities/HUD yet — just the menu, then done.
            if (Game.getInstance().getState() == GameState.MENU) {
                renderMenu();
                return;
            }

            // 2. Draw the tile grid
            renderMap();

            // 3. Draw all entities — each J2d entity implements render()
            Game game = Game.getInstance();

            // Base first (background layer)
            game.getBase().ifPresent(Base::render);

            // Range rings beneath the towers so the sprites stay crisp on top
            renderTowerRanges();

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

            // ECS floating combat text (damage numbers, gold popups) — above entities
            renderFloatingText();

            // 4. Tower placement preview (detection radius) — only while playing
            if (game.getState() == GameState.PLAYING) {
                renderPlacementPreview();
            }

            // 5. HUD overlay
            renderHUD();

        } finally {
            g2d.dispose();
            bufferStrategy.show();
        }
    }

    // -------------------------------------------------------------------------
    // ECS render system — draws the floating combat text
    // -------------------------------------------------------------------------

    /*
     * Renders the data-oriented floating text by sweeping the FloatingTextWorld's
     * read-only accessors by index. This is the visualization "system" for the ECS:
     * it reads the component data and converts game-world coordinates to pixels.
     * All Java2D drawing stays here in the j2d package.
     */
    private void renderFloatingText() {
        FloatingTextWorld ft = Game.getInstance().getFloatingText();
        g2d.setFont(FONT_FLOAT);

        for (int i = 0; i < ft.getCount(); i++) {
            // The game layer only gives us a raw amount + semantic kind. Choosing
            // the colour AND formatting the label are the visualization's job.
            FloatingTextKind kind = ft.getKind(i);
            long amount = Math.round(ft.getValue(i));

            String label;
            Color  base;
            if (kind == FloatingTextKind.REWARD) {
                label = "+" + amount + "g";
                base  = COLOR_FLOAT_REWARD;
            } else {
                label = String.valueOf(amount);
                base  = COLOR_FLOAT_DAMAGE;
            }

            int alpha = (int) (ft.getAlpha(i) * 255);
            g2d.setColor(new Color(
                base.getRed(), base.getGreen(), base.getBlue(), alpha
            ));
            g2d.drawString(
                label,
                toScreenX(ft.getX(i)),
                toScreenY(ft.getY(i))
            );
        }
    }

    /*
     * Draws a subtle detection-radius ring around every placed tower, so the player
     * can always see each tower's reach. The radius is read from the game layer
     * (Tower.getRange()); this layer only turns it into pixels — no presentation
     * detail leaks into the game package.
     */
    private void renderTowerRanges() {
        for (Tower t : Game.getInstance().getTowers()) {
            if (!t.isAlive()) continue;

            double cx    = t.getPosition().getX();
            double cy    = t.getPosition().getY();
            double range = t.getRange();

            int rx = toScreenX(cx - range);
            int ry = toScreenY(cy - range);
            int rw = toScreenWidth(range * 2);
            int rh = toScreenHeight(range * 2);

            // IceTower keeps its cyan aura; the others use the neutral white ring.
            boolean ice = t instanceof IceTower;
            g2d.setColor(ice ? COLOR_ICE_RANGE_FILL : COLOR_RANGE_FILL);
            g2d.fillOval(rx, ry, rw, rh);
            g2d.setColor(ice ? COLOR_ICE_RANGE_EDGE : COLOR_RANGE_EDGE);
            g2d.drawOval(rx, ry, rw, rh);
        }
    }

    // -------------------------------------------------------------------------
    // Tower placement preview — detection-radius indicator
    // -------------------------------------------------------------------------

    /*
     * While a tower type is selected, draws its detection radius as a translucent
     * circle around the hovered tile (plus the tile outline), so the player can see
     * the reach before committing. The circle is centred on the tile the tower would
     * snap to — the SAME centre Game.handlePlayInput() uses for placement.
     *
     * The radius comes from TowerType.getRange() (pure game-data), and buildability
     * from GameMap.canBuildAt() — this layer only reads that data and turns it into
     * pixels/colours, so no presentation logic leaks into the game package.
     */
    private void renderPlacementPreview() {
        TowerType type = TowerType.fromHotkey(inputHandler.getSelectedTower());
        if (type == null) return;   // nothing selected — no preview

        GameMap map = Game.getInstance().getGameMap();
        if (map == null) return;

        // Tile the tower would snap to (matches the placement maths in Game)
        double mouseX = inputHandler.getMouseGameX();
        double mouseY = inputHandler.getMouseGameY();
        int tileX = (int) mouseX;
        int tileY = (int) mouseY;
        double centerX = tileX + 0.5;
        double centerY = tileY + 0.5;

        boolean buildable = map.canBuildAt(new Position(mouseX, mouseY));
        Color fill = buildable ? COLOR_PREVIEW_OK_FILL : COLOR_PREVIEW_BAD_FILL;
        Color edge = buildable ? COLOR_PREVIEW_OK_EDGE : COLOR_PREVIEW_BAD_EDGE;

        // Detection radius circle, centred on the tile centre
        double range = type.getRange();
        int rx = toScreenX(centerX - range);
        int ry = toScreenY(centerY - range);
        int rw = toScreenWidth(range * 2);
        int rh = toScreenHeight(range * 2);
        g2d.setColor(fill);
        g2d.fillOval(rx, ry, rw, rh);
        g2d.setColor(edge);
        g2d.drawOval(rx, ry, rw, rh);

        // Highlight the target tile itself
        g2d.drawRect(toScreenX(tileX), toScreenY(tileY), toScreenWidth(1.0), toScreenHeight(1.0));
    }

    // -------------------------------------------------------------------------
    // Map rendering
    // -------------------------------------------------------------------------

    /*
     * Draws the tile grid. GRASS and BUILD_SPOT tiles use sprite images,
     * other tile types fall back to coloured rectangles.
     */
    private void renderMap() {
        GameMap map = Game.getInstance().getGameMap();
        if (map == null) return;

        Tile[][] grid = map.getGrid();
        int tileW = toScreenWidth(1.0);
        int tileH = toScreenHeight(1.0);

        BufferedImage floorSprite     = SpriteManager.getSprite("floor.png");
        BufferedImage buildSpotSprite = SpriteManager.getSprite("buildspot.png");

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int sx = toScreenX(x);
                int sy = toScreenY(y);

                switch (grid[x][y].getType()) {
                    case GRASS:
                        if (floorSprite != null) {
                            g2d.drawImage(floorSprite, sx, sy, tileW, tileH, null);
                        } else {
                            g2d.setColor(COLOR_GRASS);
                            g2d.fillRect(sx, sy, tileW, tileH);
                        }
                        break;
                    case BUILD_SPOT:
                        // Draw floor underneath, then build spot on top
                        if (floorSprite != null) {
                            g2d.drawImage(floorSprite, sx, sy, tileW, tileH, null);
                        }
                        if (buildSpotSprite != null) {
                            g2d.drawImage(buildSpotSprite, sx, sy, tileW, tileH, null);
                        } else {
                            g2d.setColor(COLOR_BUILD_SPOT);
                            g2d.fillRect(sx, sy, tileW, tileH);
                        }
                        break;
                    case PATH:
                        g2d.setColor(COLOR_PATH);
                        g2d.fillRect(sx, sy, tileW, tileH);
                        break;
                    case WATER:
                        g2d.setColor(COLOR_WATER);
                        g2d.fillRect(sx, sy, tileW, tileH);
                        break;
                    case SPAWN:
                        g2d.setColor(COLOR_SPAWN);
                        g2d.fillRect(sx, sy, tileW, tileH);
                        break;
                    case BASE:
                        g2d.setColor(COLOR_BASE_TILE);
                        g2d.fillRect(sx, sy, tileW, tileH);
                        break;
                }

                // Subtle grid lines
                g2d.setColor(COLOR_GRID_LINE);
                g2d.drawRect(sx, sy, tileW, tileH);
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

        // Level- en golf-info voor de HUD (bijv. "Level 1/2", "Wave 2/5")
        String levelText = "Level: " + game.getCurrentLevel() + "/" + game.getMaxLevels();
        String waveText  = "Wave: "  + game.getCurrentWave()  + "/" + game.getTotalWaves();

        String hudText = "Gold: " + game.getGold()
            + "  Score: " + game.getScore()
            + "  HP: " + hpText
            + "  " + levelText
            + "  " + waveText
            + "  Left: " + (game.getEnemiesRemaining() + game.getEnemies().size());

        // Show selected tower from input handler
        int sel = inputHandler.getSelectedTower();
        if (sel > 0) {
            String[] names = {"", "Raygun [50g]", "Cannon [100g]", "Ice [75g]"};
            hudText += "  Tower: " + names[sel];
        }

        g2d.drawString(hudText, 10, 22);

        // --- Full-screen overlay for PAUSED / GAME_OVER / WON ---
        String scoreLine = "Score: " + game.getScore();
        if (game.getState() == GameState.PAUSED) {
            renderOverlay("PAUSED", "Press P to resume",
                          "Q to quit",
                          new Color(120, 180, 255));
        } else if (game.getState() == GameState.GAME_OVER) {
            renderOverlay("GAME OVER", scoreLine,
                          "Press S to restart   —   Q to quit",
                          new Color(200, 50, 50));
        } else if (game.getState() == GameState.WON) {
            if (game.isLastLevel()) {
                renderOverlay("ULTIMATE VICTORY", scoreLine,
                              "Press S to play again   —   Q to quit",
                              new Color(255, 215, 0));
            } else {
                renderOverlay("LEVEL " + game.getCurrentLevel() + " COMPLETE", scoreLine,
                              "Press S for next level   —   Q to quit",
                              new Color(50, 200, 50));
            }
        }
    }

    /*
     * Draws a centred full-screen overlay with a title and two text lines
     * (typically a score line and a controls line). Used for the end screens.
     */
    private void renderOverlay(String title, String line1, String line2, Color accentColor) {
        // Darken the background
        g2d.setColor(COLOR_OVERLAY);
        g2d.fillRect(0, 0, windowWidth, windowHeight);

        // Title text — centred horizontally
        g2d.setFont(FONT_OVERLAY);
        g2d.setColor(accentColor);
        drawCentered(title, windowHeight / 2 - 30);

        // Two info lines — smaller, white, below the title
        g2d.setFont(FONT_SUB);
        g2d.setColor(COLOR_HUD_TEXT);
        drawCentered(line1, windowHeight / 2 + 15);
        drawCentered(line2, windowHeight / 2 + 45);
    }

    /* Draws a string horizontally centred at the given baseline Y. */
    private void drawCentered(String text, int y) {
        int w = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, (windowWidth - w) / 2, y);
    }

    // -------------------------------------------------------------------------
    // Title screen (MENU state)
    // -------------------------------------------------------------------------

    /*
     * Draws the title screen shown before a game starts. The background has
     * already been cleared by render(); here we add a dim panel and the title +
     * controls. Input (S to start, Q to quit) is handled in Game.handleInput().
     */
    private void renderMenu() {
        g2d.setColor(COLOR_OVERLAY);
        g2d.fillRect(0, 0, windowWidth, windowHeight);

        g2d.setFont(FONT_OVERLAY);
        g2d.setColor(COLOR_BASE_TILE);   // gold-ish title
        drawCentered("TOWER DEFENCE", windowHeight / 2 - 30);

        g2d.setFont(FONT_SUB);
        g2d.setColor(COLOR_HUD_TEXT);
        drawCentered("Press S to Start", windowHeight / 2 + 20);
        drawCentered("Q to Quit", windowHeight / 2 + 50);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /*
     * Returns the current Graphics2D context.
     * Only valid during a render() call — J2d entities use this in their render().
     */
    public Graphics2D getGraphics2D()  { return g2d; }

    // -------------------------------------------------------------------------
    // GameView interface — delegates input queries to InputHandler
    // Game.start() calls these so it never imports j2d classes directly.
    // -------------------------------------------------------------------------

    @Override
    public boolean wasMouseClicked()  { return inputHandler.wasMouseClicked(); }

    @Override
    public boolean wasPausePressed()  { return inputHandler.wasPausePressed(); }

    @Override
    public boolean wasStartPressed()  { return inputHandler.wasStartPressed(); }

    @Override
    public boolean wasQuitPressed()   { return inputHandler.wasQuitPressed(); }

    @Override
    public boolean wasRepairPressed() { return inputHandler.wasRepairPressed(); }

    @Override
    public double getMouseGameX()     { return inputHandler.getMouseGameX(); }

    @Override
    public double getMouseGameY()     { return inputHandler.getMouseGameY(); }

    @Override
    public int getSelectedTower()     { return inputHandler.getSelectedTower(); }
}
