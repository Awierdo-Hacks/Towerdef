package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/**
 * Represents a single cell in the {@code GameMap} grid.
 *
 * <p>Each tile has a {@link TileType} that defines its terrain and a {@link Position}
 * (the centre of the tile in game-world coordinates). The walkable and buildable
 * properties are derived from the {@code TileType} so they stay consistent
 * automatically.</p>
 *
 * <p>The grid uses integer indices {@code [x][y]} where each tile covers a 1×1 area:
 * a tile at {@code grid[3][5]} has its centre at game-world position {@code (3.5, 5.5)}.</p>
 *
 * @author Tower Defence team
 */
public class Tile {

    /** What kind of terrain this tile represents. */
    private TileType type;

    /** Centre of this tile in game-world coordinates. */
    private final Position position;

    /**
     * Creates a tile of the given type at the given centre position.
     *
     * @param type     the terrain type of this tile
     * @param position the centre of the tile in game-world coordinates
     */
    public Tile(TileType type, Position position) {
        this.type     = type;
        this.position = position;
    }

    // -------------------------------------------------------------------------
    // Derived properties — determined by tile type
    // -------------------------------------------------------------------------

    /**
     * Returns whether a tower may be placed on this tile. Towers can only be placed on
     * {@link TileType#BUILD_SPOT} tiles.
     *
     * @return {@code true} if this tile is a build spot
     */
    public boolean isBuildable() {
        return type == TileType.BUILD_SPOT;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    /**
     * Returns the terrain type of this tile.
     *
     * @return the tile type
     */
    public TileType getType()     { return type; }

    /**
     * Changes this tile's type — used by {@code GameMap} after initial grid creation.
     *
     * @param type the new terrain type
     */
    public void setType(TileType type) { this.type = type; }
}
