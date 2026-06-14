package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/*
 * Abstract base class for all projectiles fired by towers.
 *
 * A projectile is created by a tower when it fires (via EntityFactory),
 * then managed by the game loop each frame:
 *   1. update(deltaTime) moves it toward its target position.
 *   2. The game loop checks collision with enemies.
 *   3. On hit: enemy.takeDamage(damage) is called and the projectile is destroyed.
 *   4. If the projectile travels past its target without hitting anything it is
 *      also destroyed (alive becomes false in update()).
 *
 * Projectiles target a fixed position in the world (not a moving entity reference).
 * This avoids holding a direct reference to an enemy that might die mid-flight,
 * which would require null checks. The projectile just flies to where the enemy was.
 *
 * Subclasses can override update() to add special behaviour:
 *   - CannonProjectile: explodes on arrival dealing splash damage in an area.
 *   - IceProjectile: applies a slow effect on hit instead of dealing damage.
 *
 * This base class is purely abstract in terms of HIT behaviour: it carries the
 * shared movement (update) and the shared fields, but does NOT define what
 * happens on impact. Each concrete projectile family decides that for itself:
 *   - RayProjectile:    single-target damage.
 *   - CannonProjectile: direct damage + area splash.
 * The hit behaviour is therefore declared as the abstract onHit() contract.
 */
public abstract class Projectile extends Entity {

    // Damage dealt to an enemy on direct hit
    protected int damage;

    // Travel speed in game-world units per second
    protected double speed;

    // The fixed world position this projectile is flying toward
    protected Position targetPosition;

    /*
     * Creates a projectile starting at 'startPosition', flying toward 'targetPosition'.
     * Size is intentionally small (visual only — collision uses a point distance check).
     */
    public Projectile(Position startPosition, Position targetPosition,
                      double speed, int damage) {
        super(startPosition, 0.2, 0.2);   // small bounding box for a projectile
        this.targetPosition = targetPosition;
        this.speed          = speed;
        this.damage         = damage;
    }

    // -------------------------------------------------------------------------
    // Movement — default: fly straight toward targetPosition
    // -------------------------------------------------------------------------

    /*
     * Moves this projectile toward its target by (speed * deltaTime) units per frame.
     * Destroys itself when it reaches (or overshoots) the target position.
     *
     * Subclasses can override to add curved or homing flight paths.
     */
    @Override
    public void update(double deltaTime) {
        double distance = position.distanceTo(targetPosition);
        double step     = speed * deltaTime;

        if (step >= distance) {
            // Reached target — snap to it and mark for removal
            position.setX(targetPosition.getX());
            position.setY(targetPosition.getY());
            alive = false;   // game loop will remove this projectile next frame
        } else {
            // Move a fraction of the way (normalised direction)
            double dx = (targetPosition.getX() - position.getX()) / distance;
            double dy = (targetPosition.getY() - position.getY()) / distance;
            position.setX(position.getX() + dx * step);
            position.setY(position.getY() + dy * step);
        }
    }

    // -------------------------------------------------------------------------
    // Hit handling — called by game loop on collision with an enemy
    // -------------------------------------------------------------------------

    /*
     * Called by the game loop when this projectile collides with an enemy.
     *
     * This is the abstract hit contract — each concrete projectile family defines
     * its own impact behaviour:
     *   - RayProjectile:    flat damage to the primary target only.
     *   - CannonProjectile: direct damage + splash to other enemies in range.
     * The {@code enemies} list is passed so subclasses that affect an area can
     * reach every nearby enemy without the game loop needing any
     * projectile-type-specific code. Implementations must destroy the projectile
     * after hitting (set alive = false / call destroy()).
     */
    public abstract void onHit(Enemy target, List<Enemy> enemies);

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int      getDamage()        { return damage; }
}
