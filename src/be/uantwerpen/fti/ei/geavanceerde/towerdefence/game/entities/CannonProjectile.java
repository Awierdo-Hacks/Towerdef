package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/**
 * Abstract projectile fired by the {@code CannonTower}.
 *
 * <p>On impact it deals direct damage to its primary target <em>and</em> splash
 * damage to every <em>other</em> alive enemy within {@code splashRadius} of the
 * impact point.</p>
 *
 * <p>The splash logic lives here in {@link #onHit(Enemy, List)} — not in the game
 * loop. This keeps the game loop free of projectile-type-specific code: it simply
 * calls {@code projectile.onHit(target, enemies)} and the right behaviour is selected
 * polymorphically.</p>
 *
 * <p>The projectile intentionally holds no reference to the firing tower — only the
 * splash stats it needs. This preserves the decoupling described in
 * {@link Projectile}: a projectile can outlive the situation that created it without
 * dangling references.</p>
 *
 * <p>{@code J2dCannonProjectile} extends this class and implements {@code render()}.</p>
 *
 * @author Tower Defence team
 */
public abstract class CannonProjectile extends Projectile {

    /** Radius (game-world units) around the impact point in which splash is dealt. */
    private final double splashRadius;

    /** Damage applied to every other enemy caught in the splash radius. */
    private final int splashDamage;

    /**
     * Creates a cannon projectile with both direct and splash damage stats.
     *
     * @param startPosition  the position the projectile is fired from
     * @param targetPosition the fixed world position the projectile flies toward
     * @param speed          the travel speed in game-world units per second
     * @param damage         the direct damage dealt to the primary target
     * @param splashRadius   the radius around the impact point in which splash applies
     * @param splashDamage   the damage applied to other enemies within the splash radius
     */
    public CannonProjectile(Position startPosition, Position targetPosition,
                            double speed, int damage,
                            double splashRadius, int splashDamage) {
        super(startPosition, targetPosition, speed, damage);
        this.splashRadius = splashRadius;
        this.splashDamage = splashDamage;
    }

    // -------------------------------------------------------------------------
    // Hit handling — direct damage + area splash
    // -------------------------------------------------------------------------

    /**
     * Deals direct damage to the primary target, then splash damage to all other
     * alive enemies within {@link #splashRadius} of the impact point (this
     * projectile's current position), and finally destroys this projectile.
     *
     * @param target  the enemy that took the direct hit
     * @param enemies the full list of enemies, scanned for splash victims
     */
    @Override
    public void onHit(Enemy target, List<Enemy> enemies) {
        // Direct hit on the primary target
        target.takeDamage(getDamage());

        // Splash to every other alive enemy within range of the impact point
        Position impact = getPosition();
        for (Enemy other : enemies) {
            if (!other.isAlive() || other == target) {
                continue;
            }
            if (impact.distanceTo(other.getPosition()) <= splashRadius) {
                other.takeDamage(splashDamage);
            }
        }

        destroy();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the splash radius in game-world units.
     *
     * @return the splash radius
     */
    public double getSplashRadius() { return splashRadius; }

    /**
     * Returns the splash damage applied to enemies caught in the splash radius.
     *
     * @return the splash damage
     */
    public int    getSplashDamage() { return splashDamage; }
}
