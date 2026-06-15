package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/**
 * Abstract single-target projectile fired by the {@code ArrowTower} (raygun).
 *
 * <p>On impact it deals flat damage to its primary target only — no area effect.
 * This is the "plain" projectile behaviour; it sits in its own class so the
 * hierarchy is symmetric with {@code CannonProjectile} and the {@link Projectile}
 * base stays free of concrete hit logic.</p>
 *
 * <p>The projectile holds no reference to the firing tower — only the damage it
 * needs. This preserves the decoupling described in {@link Projectile}: a projectile
 * can outlive the situation that created it without dangling references.</p>
 *
 * <p>{@code J2dRayProjectile} extends this class and implements {@code render()}.</p>
 *
 * @author Tower Defence team
 */
public abstract class RayProjectile extends Projectile {

    /**
     * Creates a single-target ray projectile.
     *
     * @param startPosition  the position the projectile is fired from
     * @param targetPosition the fixed world position the projectile flies toward
     * @param speed          the travel speed in game-world units per second
     * @param damage         the damage dealt on direct hit
     */
    public RayProjectile(Position startPosition, Position targetPosition,
                         double speed, int damage) {
        super(startPosition, targetPosition, speed, damage);
    }

    // -------------------------------------------------------------------------
    // Hit handling — single-target damage only
    // -------------------------------------------------------------------------

    /**
     * Deals flat damage to the primary target, then destroys this projectile.
     *
     * <p>The {@code enemies} list is unused here (no area effect) but is part of the
     * shared {@link Projectile#onHit(Enemy, List)} contract so the game loop can stay
     * projectile-type-agnostic.</p>
     *
     * @param target  the enemy this projectile hit
     * @param enemies the full list of enemies (unused for single-target damage)
     */
    @Override
    public void onHit(Enemy target, List<Enemy> enemies) {
        target.takeDamage(damage);
        alive = false;
    }
}
