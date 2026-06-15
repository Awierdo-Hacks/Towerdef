package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/**
 * Abstract base class for all projectiles fired by towers.
 *
 * <p>A projectile is created by a tower when it fires (via {@code EntityFactory}),
 * then managed by the game loop each frame:</p>
 * <ol>
 *   <li>{@link #update(double)} moves it toward its target position.</li>
 *   <li>The game loop checks collision with enemies.</li>
 *   <li>On hit, {@link #onHit(Enemy, List)} is called and the projectile is destroyed.</li>
 *   <li>If the projectile travels past its target without hitting anything it is also
 *       destroyed ({@code alive} becomes {@code false} in {@link #update(double)}).</li>
 * </ol>
 *
 * <p>Projectiles target a fixed position in the world (not a moving entity
 * reference). This avoids holding a direct reference to an enemy that might die
 * mid-flight, which would require null checks. The projectile simply flies to where
 * the enemy was.</p>
 *
 * <p>This base class is purely abstract in terms of <em>hit</em> behaviour: it
 * carries the shared movement ({@link #update(double)}) and the shared fields, but
 * does not define what happens on impact. Each concrete projectile family decides
 * that for itself:</p>
 * <ul>
 *   <li>{@code RayProjectile} — single-target damage.</li>
 *   <li>{@code CannonProjectile} — direct damage + area splash.</li>
 * </ul>
 *
 * <p>The hit behaviour is therefore declared as the abstract {@link #onHit(Enemy, List)}
 * contract.</p>
 *
 * @author Tower Defence team
 */
public abstract class Projectile extends Entity {

    /** Damage dealt to an enemy on direct hit. */
    protected int damage;

    /** Travel speed in game-world units per second. */
    protected double speed;

    /** The fixed world position this projectile is flying toward. */
    protected Position targetPosition;

    /**
     * Creates a projectile starting at {@code startPosition}, flying toward
     * {@code targetPosition}. The size is intentionally small (visual only —
     * collision uses a point distance check).
     *
     * @param startPosition  the position the projectile is fired from
     * @param targetPosition the fixed world position the projectile flies toward
     * @param speed          the travel speed in game-world units per second
     * @param damage         the damage dealt on direct hit
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

    /**
     * Moves this projectile toward its target by {@code speed * deltaTime} units per
     * frame, destroying itself when it reaches (or overshoots) the target position.
     *
     * <p>Subclasses can override to add curved or homing flight paths.</p>
     *
     * @param deltaTime the elapsed time since the previous frame, in seconds
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

    /**
     * Called by the game loop when this projectile collides with an enemy.
     *
     * <p>This is the abstract hit contract — each concrete projectile family defines
     * its own impact behaviour ({@code RayProjectile} flat damage to the primary
     * target only; {@code CannonProjectile} direct damage + splash to other enemies
     * in range). The {@code enemies} list is passed so subclasses that affect an area
     * can reach every nearby enemy without the game loop needing any
     * projectile-type-specific code. Implementations must destroy the projectile
     * after hitting (set {@code alive = false} / call {@link #destroy()}).</p>
     *
     * @param target  the enemy this projectile collided with
     * @param enemies the full list of enemies, for area-effect projectiles
     */
    public abstract void onHit(Enemy target, List<Enemy> enemies);

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the direct-hit damage of this projectile.
     *
     * @return the damage value
     */
    public int      getDamage()        { return damage; }
}
