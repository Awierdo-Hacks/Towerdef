package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Represents a single level's map: the tile grid, enemy paths, build spots,
 * spawn point and base position.
 *
 * LOADING:
 *   GameMap is constructed with a ConfigManager that has already loaded a
 *   level .properties file (e.g. resources/levels/level1.properties).
 *   The constructor parses all map data from that config and builds the
 *   internal tile grid.
 *
 * LEVEL FILE FORMAT (example):
 *   map.width=20
 *   map.height=15
 *   spawn.x=0.0
 *   spawn.y=7.0
 *   base.x=19.0
 *   base.y=7.0
 *   path.waypoints=0.0,7.0;5.0,7.0;5.0,3.0;15.0,3.0;15.0,12.0;19.0,12.0;19.0,7.0
 *   path.flying.waypoints=0.0,7.0;10.0,7.0;19.0,7.0     (optional)
 *   build.spots=3.0,5.0;3.0,9.0;7.0,1.0;7.0,5.0
 *
 * GRID:
 *   The tile grid is a 2D array [width][height] where each tile covers a
 *   1x1 game-world area. Tile centres are at (x+0.5, y+0.5).
 *   After loading, the grid has:
 *     - GRASS  for empty tiles
 *     - PATH   for every tile along the enemy route segments
 *     - BUILD_SPOT for designated tower placement locations
 *     - SPAWN  at the enemy spawn point
 *     - BASE   at the player's base
 *
 * PATHS:
 *   enemyPath — ground route used by BasicEnemy and ArmoredEnemy
 *   flyingPath — optional aerial route for FlyingEnemy. If the level file
 *                does not define path.flying.waypoints, flyingPath is null
 *                and flying enemies use the ground path instead.
 */
public class GameMap {

    // Tile grid [x][y] — each tile is 1x1 game-world units
    private Tile[][] grid;

    // Grid dimensions in tiles (also the game-world size in units)
    private int width;
    private int height;

    // Ground path — from spawn to base, used by BasicEnemy and ArmoredEnemy
    private Path enemyPath;

    // Optional aerial path — used by FlyingEnemy if present, otherwise null
    private Path flyingPath;

    // Where enemies appear on the map
    private Position spawnPoint;

    // Where the player's base is located (last waypoint of the path)
    private Position basePosition;

    // Designated tower placement locations
    private List<Position> buildSpots;

    // -------------------------------------------------------------------------
    // Construction — loads everything from a level config file
    // -------------------------------------------------------------------------

    /*
     * Creates a GameMap by reading all map data from the given level config.
     *
     * Usage:
     *   ConfigManager levelCfg = new ConfigManager("levels/level1.properties");
     *   GameMap map = new GameMap(levelCfg);
     */
    public GameMap(ConfigManager levelConfig) {
        loadFromConfig(levelConfig);
    }

    // -------------------------------------------------------------------------
    // Level loading
    // -------------------------------------------------------------------------

    /*
     * Parses the level config and populates all fields:
     * dimensions, spawn, base, paths, build spots, and the tile grid.
     */
    private void loadFromConfig(ConfigManager config) {
        // --- Map dimensions ---
        width  = config.getInt("map.width",  20);
        height = config.getInt("map.height", 15);

        // --- Spawn and base positions ---
        spawnPoint = new Position(
            config.getDouble("spawn.x", 0.0),
            config.getDouble("spawn.y", 0.0)
        );
        basePosition = new Position(
            config.getDouble("base.x", 19.0),
            config.getDouble("base.y", 7.0)
        );

        // --- Ground path (required) ---
        String waypointStr = config.getString("path.waypoints", "");
        List<Position> waypoints = parsePositionList(waypointStr);
        if (waypoints.isEmpty()) {
            throw new RuntimeException("Level file has no path.waypoints defined");
        }
        enemyPath = new Path(waypoints);

        // --- Flying path (optional — null if not defined) ---
        String flyingStr = config.getString("path.flying.waypoints", "");
        if (!flyingStr.isEmpty()) {
            flyingPath = new Path(parsePositionList(flyingStr));
        } else {
            flyingPath = null;
        }

        // --- Build spots ---
        String buildStr = config.getString("build.spots", "");
        buildSpots = parsePositionList(buildStr);

        // --- Build the tile grid ---
        initializeGrid();
    }

    // -------------------------------------------------------------------------
    // Position list parsing — "x,y;x,y;x,y" format
    // -------------------------------------------------------------------------

    /*
     * Parses a semicolon-separated list of x,y coordinate pairs into Positions.
     *
     * Format: "0.0,7.0;5.0,7.0;5.0,3.0"
     * Returns an empty list if the input is null or empty.
     */
    private List<Position> parsePositionList(String str) {
        List<Position> positions = new ArrayList<>();
        if (str == null || str.trim().isEmpty()) {
            return positions;
        }

        String[] pairs = str.split(";");
        for (String pair : pairs) {
            String[] coords = pair.split(",");
            if (coords.length != 2) {
                throw new RuntimeException(
                    "Invalid coordinate pair in level file: '" + pair + "' (expected x,y)"
                );
            }
            double x = Double.parseDouble(coords[0].trim());
            double y = Double.parseDouble(coords[1].trim());
            positions.add(new Position(x, y));
        }
        return positions;
    }

    // -------------------------------------------------------------------------
    // Grid initialisation
    // -------------------------------------------------------------------------

    /*
     * Builds the tile grid from the parsed level data.
     *
     * Order of operations:
     *   1. Fill entire grid with GRASS
     *   2. Mark all tiles along path segments as PATH
     *   3. Mark build spots as BUILD_SPOT
     *   4. Mark spawn tile as SPAWN
     *   5. Mark base tile as BASE
     *
     * Spawn and base are set LAST so they always override PATH tiles
     * at those positions (the spawn/base tiles are both on the path).
     */
    private void initializeGrid() {
        grid = new Tile[width][height];

        // 1. Fill with GRASS
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Tile(TileType.GRASS, new Position(x + 0.5, y + 0.5));
            }
        }

        // 2. Mark path tiles — walk between consecutive waypoints
        markPathTiles(enemyPath);
        if (flyingPath != null) {
            markPathTiles(flyingPath);
        }

        // 3. Mark build spots
        for (Position bp : buildSpots) {
            int tx = (int) bp.getX();
            int ty = (int) bp.getY();
            if (isInBounds(tx, ty)) {
                grid[tx][ty].setType(TileType.BUILD_SPOT);
            }
        }

        // 4. Mark spawn (overrides PATH)
        int sx = (int) spawnPoint.getX();
        int sy = (int) spawnPoint.getY();
        if (isInBounds(sx, sy)) {
            grid[sx][sy].setType(TileType.SPAWN);
        }

        // 5. Mark base (overrides PATH)
        int bx = (int) basePosition.getX();
        int by = (int) basePosition.getY();
        if (isInBounds(bx, by)) {
            grid[bx][by].setType(TileType.BASE);
        }
    }

    /*
     * Marks all tiles along a path's segments as PATH.
     *
     * Walks between each pair of consecutive waypoints. Supports both
     * axis-aligned segments (horizontal/vertical) and diagonal segments.
     *
     * For axis-aligned paths (the typical case in tower defence):
     *   waypoints (0,7)→(5,7) marks tiles x=0..5 at y=7
     *   waypoints (5,7)→(5,3) marks tiles y=3..7 at x=5
     *
     * For diagonal segments: uses linear interpolation stepping one tile at a time.
     */
    private void markPathTiles(Path path) {
        List<Position> waypoints = path.getWaypoints();

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Position from = waypoints.get(i);
            Position to   = waypoints.get(i + 1);
            markSegment(from, to);
        }
    }

    /*
     * Marks every tile along a straight line from 'from' to 'to' as PATH.
     */
    private void markSegment(Position from, Position to) {
        int x1 = (int) from.getX();
        int y1 = (int) from.getY();
        int x2 = (int) to.getX();
        int y2 = (int) to.getY();

        // Horizontal segment (same row)
        if (y1 == y2) {
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            for (int x = minX; x <= maxX; x++) {
                setTileIfGrass(x, y1, TileType.PATH);
            }
        }
        // Vertical segment (same column)
        else if (x1 == x2) {
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            for (int y = minY; y <= maxY; y++) {
                setTileIfGrass(x1, y, TileType.PATH);
            }
        }
        // Diagonal or arbitrary — step along the longest axis
        else {
            int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
            for (int s = 0; s <= steps; s++) {
                double t = (double) s / steps;
                int x = (int) (from.getX() + t * (to.getX() - from.getX()));
                int y = (int) (from.getY() + t * (to.getY() - from.getY()));
                setTileIfGrass(x, y, TileType.PATH);
            }
        }
    }

    /*
     * Sets a tile's type only if it is currently GRASS.
     * Prevents path marking from overwriting BUILD_SPOT, SPAWN, etc.
     */
    private void setTileIfGrass(int x, int y, TileType type) {
        if (isInBounds(x, y) && grid[x][y].getType() == TileType.GRASS) {
            grid[x][y].setType(type);
        }
    }

    // -------------------------------------------------------------------------
    // Queries — used by the game loop and input handler
    // -------------------------------------------------------------------------

    /* Checks if grid coordinates are within the map bounds. */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /* Returns the tile at the given grid coordinates, or null if out of bounds. */
    public Tile getTile(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return grid[x][y];
    }

    /*
     * Returns the tile at a game-world position (converts doubles to grid indices).
     * Used by the input handler when the player clicks to place a tower.
     */
    public Tile getTileAt(Position worldPos) {
        int tx = (int) worldPos.getX();
        int ty = (int) worldPos.getY();
        return getTile(tx, ty);
    }

    /*
     * Checks if a tower can be placed at the given game-world position.
     * True only if the tile exists and its type is BUILD_SPOT.
     */
    public boolean canBuildAt(Position worldPos) {
        Tile tile = getTileAt(worldPos);
        return tile != null && tile.isBuildable();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int         getWidth()        { return width; }
    public int         getHeight()       { return height; }
    public Tile[][]    getGrid()         { return grid; }
    public Path        getEnemyPath()    { return enemyPath; }
    public Path        getFlyingPath()   { return flyingPath; }
    public boolean     hasFlyingPath()   { return flyingPath != null; }
    public Position    getSpawnPoint()   { return spawnPoint; }
    public Position    getBasePosition() { return basePosition; }

    /* Returns an unmodifiable view of the build spots list. */
    public List<Position> getBuildSpots() {
        return Collections.unmodifiableList(buildSpots);
    }
}
