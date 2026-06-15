package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single level's map: the tile grid, enemy paths, build spots,
 * spawn point and base position.
 *
 * <p><strong>Loading:</strong> a {@code GameMap} is constructed with a
 * {@code ConfigManager} that has already loaded a level {@code .properties} file
 * (e.g. {@code resources/levels/level1.properties}). The constructor parses all map
 * data from that config and builds the internal tile grid.</p>
 *
 * <p><strong>Level file format (example):</strong></p>
 * <pre>
 *   map.width=20
 *   map.height=15
 *   spawn.x=0.0
 *   spawn.y=7.0
 *   base.x=19.0
 *   base.y=7.0
 *   path.waypoints=0.0,7.0;5.0,7.0;5.0,3.0;15.0,3.0;15.0,12.0;19.0,12.0;19.0,7.0
 *   path.flying.waypoints=0.0,7.0;10.0,7.0;19.0,7.0     (optional)
 *   build.spots=3.0,5.0;3.0,9.0;7.0,1.0;7.0,5.0
 * </pre>
 *
 * <p><strong>Grid:</strong> the tile grid is a 2D array {@code [width][height]} where
 * each tile covers a 1×1 game-world area, with tile centres at {@code (x+0.5, y+0.5)}.
 * After loading, tiles are {@code GRASS} (empty), {@code PATH} (route segments),
 * {@code BUILD_SPOT} (tower placement), {@code SPAWN} and {@code BASE}.</p>
 *
 * <p><strong>Paths:</strong> {@code enemyPath} is the ground route used by
 * {@code BasicEnemy} and {@code ArmoredEnemy}; {@code flyingPath} is an optional
 * aerial route for {@code FlyingEnemy}. If the level file does not define
 * {@code path.flying.waypoints}, the flying path is absent and flying enemies use the
 * ground path instead.</p>
 *
 * @author Tower Defence team
 */
public class GameMap {

    /** Tile grid {@code [x][y]} — each tile is 1×1 game-world units. */
    private Tile[][] grid;

    /** Grid width in tiles (also the game-world width in units). */
    private int width;
    /** Grid height in tiles (also the game-world height in units). */
    private int height;

    /** Ground path — from spawn to base, used by {@code BasicEnemy} and {@code ArmoredEnemy}. */
    private Path enemyPath;

    /** Optional aerial path — used by {@code FlyingEnemy} if present, otherwise {@code null}. */
    private Path flyingPath;

    /** Where enemies appear on the map. */
    private Position spawnPoint;

    /** Where the player's base is located (last waypoint of the path). */
    private Position basePosition;

    /** Designated tower placement locations. */
    private List<Position> buildSpots;

    // -------------------------------------------------------------------------
    // Construction — loads everything from a level config file
    // -------------------------------------------------------------------------

    /**
     * Creates a {@code GameMap} by reading all map data from the given level config.
     *
     * @param levelConfig a config manager that has loaded the level's
     *                    {@code .properties} file
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
        // Config gebruikt gehele tegel-indices; centreer op het tegelmidden (+0.5),
        // net als bij het bouwen van het grid (zie initializeGrid), zodat vijanden
        // door het midden van de padtegels lopen i.p.v. tegen de rand.
        centerOnTiles(waypoints);
        enemyPath = new Path(waypoints);

        // --- Flying path (optional — null if not defined) ---
        String flyingStr = config.getString("path.flying.waypoints", "");
        if (!flyingStr.isEmpty()) {
            List<Position> flyingWaypoints = parsePositionList(flyingStr);
            centerOnTiles(flyingWaypoints);
            flyingPath = new Path(flyingWaypoints);
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

    /*
     * Verschuift elke positie naar het midden van zijn tegel (+0.5 op x en y).
     *
     * Waypoints worden in de levelbestanden opgegeven als gehele tegel-indices.
     * Een tegel (x,y) heeft echter zijn midden op (x+0.5, y+0.5) — zie
     * initializeGrid. Door waypoints op datzelfde midden te leggen, loopt het
     * midden van een vijand door het centrum van de padtegels.
     */
    private void centerOnTiles(List<Position> positions) {
        positions.replaceAll(p -> new Position(p.getX() + 0.5, p.getY() + 0.5));
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

    /**
     * Returns whether the given grid coordinates are within the map bounds.
     *
     * @param x the grid x index
     * @param y the grid y index
     * @return {@code true} if {@code (x, y)} is inside the grid
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Returns the tile at the given grid coordinates.
     *
     * @param x the grid x index
     * @param y the grid y index
     * @return the tile, or {@code null} if the coordinates are out of bounds
     */
    public Tile getTile(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return grid[x][y];
    }

    /**
     * Returns the tile at a game-world position, converting the doubles to grid
     * indices. Used by the input handler when the player clicks to place a tower.
     *
     * @param worldPos the game-world position
     * @return the tile at that position, or {@code null} if out of bounds
     */
    public Tile getTileAt(Position worldPos) {
        int tx = (int) worldPos.getX();
        int ty = (int) worldPos.getY();
        return getTile(tx, ty);
    }

    /**
     * Returns whether a tower can be placed at the given game-world position. True
     * only if the tile exists and its type is {@link TileType#BUILD_SPOT}.
     *
     * @param worldPos the game-world position to test
     * @return {@code true} if a tower may be built there
     */
    public boolean canBuildAt(Position worldPos) {
        Tile tile = getTileAt(worldPos);
        return tile != null && tile.isBuildable();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the map width in tiles.
     *
     * @return the width in tiles
     */
    public int         getWidth()        { return width; }

    /**
     * Returns the map height in tiles.
     *
     * @return the height in tiles
     */
    public int         getHeight()       { return height; }

    /**
     * Returns the raw tile grid {@code [x][y]}.
     *
     * @return the tile grid
     */
    public Tile[][]    getGrid()         { return grid; }

    /**
     * Returns the ground path enemies follow from spawn to base.
     *
     * @return the ground path
     */
    public Path        getEnemyPath()    { return enemyPath; }

    /**
     * Returns the optional aerial path for flying enemies.
     *
     * @return the flying path, or {@code null} if the level defines none
     */
    public Path        getFlyingPath()   { return flyingPath; }

    /**
     * Returns whether this level defines a separate aerial path.
     *
     * @return {@code true} if a flying path is present
     */
    public boolean     hasFlyingPath()   { return flyingPath != null; }

    /**
     * Returns the position of the player's base.
     *
     * @return the base position in game-world coordinates
     */
    public Position    getBasePosition() { return basePosition; }
}
