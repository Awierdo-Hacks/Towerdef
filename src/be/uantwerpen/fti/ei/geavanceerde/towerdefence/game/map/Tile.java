package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/*
 * Represents a single cell in the GameMap grid.
 *
 * Each tile has:
 *   - A TileType that defines what kind of terrain it is
 *   - A Position (centre of the tile in game-world coordinates)
 *
 * Walkable and buildable flags are derived from the TileType so they stay
 * consistent automatically — no risk of a BUILD_SPOT being marked walkable.
 *
 * The grid uses integer indices [x][y] where each tile covers a 1x1 area.
 * A tile at grid[3][5] has its centre at game-world position (3.5, 5.5).
 */
public class Tile {

    // What kind of terrain this tile represents
    private TileType type;

    // Centre of this tile in game-world coordinates
    private final Position position;

    public Tile(TileType type, Position position) {
        this.type     = type;
        this.position = position;
    }

    // -------------------------------------------------------------------------
    // Derived properties — determined by tile type
    // -------------------------------------------------------------------------

    /*
     * Towers can only be placed on BUILD_SPOT tiles.
     * Once a tower occupies the spot the game loop should prevent placing another.
     */
    public boolean isBuildable() {
        return type == TileType.BUILD_SPOT;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public TileType getType()     { return type; }

    /* Allows GameMap to change a tile's type after initial grid creation. */
    public void setType(TileType type) { this.type = type; }
}
