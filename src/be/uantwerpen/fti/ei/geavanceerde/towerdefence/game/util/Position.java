package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util;

/**
 * Represents a 2D position in the game world using double-precision coordinates.
 *
 * <p>The game world uses its own coordinate system (e.g. 0.0 to 20.0 units wide)
 * that is completely independent from screen pixel coordinates. All game logic
 * (movement, collision detection, tower range) operates in these game-world units.</p>
 *
 * <p>This class is mutable — positions can be updated each frame as entities move.</p>
 *
 * @author TowerDefence Team
 * @version 1.0
 * @see be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Entity
 */
public class Position {

    /** The horizontal coordinate in game-world units. */
    private double x;

    /** The vertical coordinate in game-world units. */
    private double y;

    /**
     * Creates a new Position at the given game-world coordinates.
     *
     * @param x the horizontal coordinate in game-world units
     * @param y the vertical coordinate in game-world units
     */
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the horizontal coordinate.
     *
     * @return x coordinate in game-world units
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the vertical coordinate.
     *
     * @return y coordinate in game-world units
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the horizontal coordinate.
     *
     * @param x new x coordinate in game-world units
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the vertical coordinate.
     *
     * @param y new y coordinate in game-world units
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Calculates the Euclidean distance to another position.
     *
     * <p>Used primarily for tower range checks and pathfinding proximity tests.</p>
     *
     * @param other the other position to measure to
     * @return the straight-line distance in game-world units
     */
    public double distanceTo(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Returns a new Position shifted by the given amounts.
     *
     * <p>Does not modify this position — returns a new instance.</p>
     *
     * @param dx amount to shift horizontally
     * @param dy amount to shift vertically
     * @return a new Position at (x + dx, y + dy)
     */
    public Position translate(double dx, double dy) {
        return new Position(this.x + dx, this.y + dy);
    }

    /**
     * Returns a string representation for debugging purposes.
     *
     * @return formatted string like "(3.50, 7.00)"
     */
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}
