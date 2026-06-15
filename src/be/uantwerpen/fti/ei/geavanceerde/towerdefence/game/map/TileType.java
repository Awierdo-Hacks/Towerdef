package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map;

/**
 * Defines the different terrain types a tile can have on the game map.
 *
 * <p>Each tile in the {@code GameMap} grid has exactly one {@code TileType} that
 * determines whether enemies can walk through it, whether the player can place a
 * tower on it, and how the tile is rendered in the visualization layer. The grid is
 * populated by {@code GameMap} when it loads a level file.</p>
 *
 * @author Tower Defence team
 */
public enum TileType {

    /** Default empty terrain — not walkable, not buildable. */
    GRASS,

    /** Part of the enemy walking route — walkable, not buildable. */
    PATH,

    /** Impassable terrain — blocks ground enemies and tower placement. */
    WATER,

    /** Valid tower placement location — not walkable, buildable. */
    BUILD_SPOT,

    /** Enemy spawn point — the first tile enemies appear on. */
    SPAWN,

    /** Player's base location — the tile enemies are trying to reach. */
    BASE
}
