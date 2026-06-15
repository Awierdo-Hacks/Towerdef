package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/**
 * Abstract base class for every object in the game world.
 *
 * <p>All entities share a position (double coordinates), a size (width/height),
 * and an alive-flag. Two abstract methods must be implemented by every subclass:</p>
 * <ul>
 *   <li>{@link #update(double)} — game logic per frame (implemented in {@code game.entities} subclasses)</li>
 *   <li>{@link #render()} — draw to screen (implemented in {@code j2d.entities} subclasses)</li>
 * </ul>
 *
 * <p>Because {@link #render()} is abstract here, the concrete game-logic subclasses
 * ({@code ArrowTower}, {@code BasicEnemy}, ...) remain abstract until the {@code J2d*}
 * visualization subclasses implement it. This enforces the strict
 * game/visualization separation.</p>
 *
 * <p>Coordinate system: all positions and sizes are expressed in game-world units
 * (doubles), <strong>not</strong> in screen pixels. The {@code J2dGame} class converts
 * to pixels when rendering.</p>
 *
 * @author Tower Defence team
 */
public abstract class Entity {

    /** Position in game-world coordinates (double, not pixels). */
    protected Position position;

    /** Width in game-world units — used for collision detection. */
    protected double width;

    /** Height in game-world units — used for collision detection. */
    protected double height;

    /** When {@code false} the {@code Game} singleton removes this entity from its lists. */
    protected boolean alive;

    /**
     * Base constructor — sets position, size, and marks the entity as alive.
     * All subclasses must call {@code super(position, width, height)}.
     *
     * @param position the starting position in game-world coordinates
     * @param width    the entity width in game-world units
     * @param height   the entity height in game-world units
     */
    public Entity(Position position, double width, double height) {
        // Defensive copy — prevents shared mutable state if the caller reuses the same Position
        this.position = new Position(position.getX(), position.getY());
        this.width    = width;
        this.height   = height;
        this.alive    = true;
    }

    // -------------------------------------------------------------------------
    // Abstract methods — must be implemented by every concrete entity
    // -------------------------------------------------------------------------

    /**
     * Updates the game logic for this entity by one frame.
     *
     * <p>Called once per frame by the game loop. Use {@code deltaTime} (in seconds)
     * so that movement and timers are frame-rate independent across different
     * computers. Example:
     * {@code position.setX(position.getX() + speed * deltaTime)}.</p>
     *
     * @param deltaTime the elapsed time since the previous frame, in seconds
     */
    public abstract void update(double deltaTime);

    /**
     * Draws this entity on the screen.
     *
     * <p>This method is intentionally left abstract here so that the game-logic
     * package stays free of visualization code. {@code J2d} subclasses (e.g.
     * {@code J2dEnemy}) override it and use {@code Graphics2D} to draw the entity.
     * The game package never imports any visualization library.</p>
     */
    public abstract void render();

    // -------------------------------------------------------------------------
    // Collision detection
    // -------------------------------------------------------------------------

    /**
     * Tests whether this entity's bounding box overlaps another entity's.
     *
     * <p>Uses axis-aligned bounding box (AABB) collision: two boxes overlap when
     * the distance between their centres is less than the sum of their half-widths
     * (and half-heights). Works well for projectile-enemy hits and bonus pickups.</p>
     *
     * @param other the other entity to test against
     * @return {@code true} if the two bounding boxes overlap
     */
    public boolean collidesWith(Entity other) {
        double halfWidthSum  = (this.width  + other.width)  / 2.0;
        double halfHeightSum = (this.height + other.height) / 2.0;

        double dx = Math.abs(this.position.getX() - other.position.getX());
        double dy = Math.abs(this.position.getY() - other.position.getY());

        return dx < halfWidthSum && dy < halfHeightSum;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    /**
     * Returns the position of this entity in game-world coordinates.
     *
     * @return the current position
     */
    public Position getPosition() { return position; }

    /**
     * Returns the width of this entity in game-world units.
     *
     * @return the width
     */
    public double getWidth()  { return width; }

    /**
     * Returns the height of this entity in game-world units.
     *
     * @return the height
     */
    public double getHeight() { return height; }

    /**
     * Returns whether this entity is still alive (active in the game).
     *
     * @return {@code true} while the entity is alive, {@code false} once destroyed
     */
    public boolean isAlive() { return alive; }

    /** Marks this entity for removal — the game loop will clean it up. */
    public void destroy() { this.alive = false; }
}
