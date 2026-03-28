package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/*
 * Abstract base class for every object in the game world.
 *
 * All entities share a position (double coordinates), a size (width/height),
 * and an alive-flag. Two abstract methods must be implemented by every subclass:
 *   - update(deltaTime)  : game logic per frame  (implemented in game.entities subclasses)
 *   - render()           : draw to screen         (implemented in j2d.entities subclasses)
 *
 * Because render() is abstract here, the concrete game-logic subclasses
 * (ArrowTower, BasicEnemy, ...) remain abstract until the J2d* visualization
 * subclasses implement it. This enforces the strict game/visualization separation.
 *
 * Coordinate system: all positions and sizes are in game-world units (doubles),
 * NOT in screen pixels. The J2dGame class converts to pixels when rendering.
 */
public abstract class Entity {

    // Position in game-world coordinates (double, NOT pixels)
    protected Position position;

    // Size in game-world units — used for collision detection and rendering
    protected double width;
    protected double height;

    // When alive = false the Game singleton removes this entity from its lists
    protected boolean alive;

    /*
     * Base constructor — sets position, size, and marks the entity as alive.
     * All subclasses must call super(position, width, height).
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

    /*
     * Update game logic for this entity by one frame.
     *
     * Called once per frame by the game loop. Use deltaTime (in seconds) so that
     * movement and timers are frame-rate independent across different computers.
     *
     * Example: position.setX(position.getX() + speed * deltaTime)
     */
    public abstract void update(double deltaTime);

    /*
     * Draw this entity on the screen.
     *
     * This method is intentionally left abstract here so that the game-logic
     * package stays clean. J2d subclasses (e.g. J2dEnemy) override this method
     * and use Graphics2D to draw the entity. The game package never imports
     * any visualization library.
     */
    public abstract void render();

    // -------------------------------------------------------------------------
    // Collision detection
    // -------------------------------------------------------------------------

    /*
     * Returns true if this entity's bounding box overlaps with another entity's.
     *
     * Uses axis-aligned bounding box (AABB) collision: two boxes overlap when
     * the distance between their centres is less than the sum of their half-widths
     * (and half-heights). Works well for projectile-enemy hits and bonus pickups.
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

    public Position getPosition() { return position; }

    public double getWidth()  { return width; }
    public double getHeight() { return height; }

    public boolean isAlive() { return alive; }

    /* Mark this entity for removal — the game loop will clean it up. */
    public void destroy() { this.alive = false; }
}
