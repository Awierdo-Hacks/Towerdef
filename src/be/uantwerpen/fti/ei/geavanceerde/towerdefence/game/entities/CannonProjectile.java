package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/*
 * Abstract projectile fired by the CannonTower.
 *
 * On impact it deals direct damage to its primary target AND splash damage to
 * every OTHER alive enemy within splashRadius of the impact point.
 *
 * The splash logic lives here in onHit() — NOT in the game loop. This keeps the
 * game loop free of projectile-type-specific code: it simply calls
 * projectile.onHit(target, enemies) and the right behaviour is selected
 * polymorphically. (Previously the splash was computed inline inside
 * Game.checkProjectileCollisions; that has been moved here.)
 *
 * The projectile intentionally holds no reference to the firing tower — only the
 * splash stats it needs. This preserves the decoupling described in Projectile:
 * a projectile can outlive the situation that created it without dangling refs.
 *
 * J2dCannonProjectile extends this class and implements render().
 */
public abstract class CannonProjectile extends Projectile {

    // Radius (game-world units) around the impact point in which splash is dealt
    private final double splashRadius;

    // Damage applied to every other enemy caught in the splash radius
    private final int splashDamage;

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

    /*
     * Deals direct damage to the primary target, then splash damage to all OTHER
     * alive enemies within splashRadius of the impact point (this projectile's
     * current position). Finally destroys this projectile.
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

    public double getSplashRadius() { return splashRadius; }
    public int    getSplashDamage() { return splashDamage; }
}
