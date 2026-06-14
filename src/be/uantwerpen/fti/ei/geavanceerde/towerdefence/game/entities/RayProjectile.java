package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/*
 * Abstract single-target projectile fired by the ArrowTower (raygun).
 *
 * On impact it deals flat damage to its primary target only — no area effect.
 * This is the "plain" projectile behaviour that used to live directly in
 * Projectile; it now sits in its own class so the hierarchy is symmetric with
 * CannonProjectile and the Projectile base stays free of concrete hit logic.
 *
 * The projectile holds no reference to the firing tower — only the damage it
 * needs. This preserves the decoupling described in Projectile: a projectile can
 * outlive the situation that created it without dangling references.
 *
 * J2dRayProjectile extends this class and implements render().
 */
public abstract class RayProjectile extends Projectile {

    public RayProjectile(Position startPosition, Position targetPosition,
                         double speed, int damage) {
        super(startPosition, targetPosition, speed, damage);
    }

    // -------------------------------------------------------------------------
    // Hit handling — single-target damage only
    // -------------------------------------------------------------------------

    /*
     * Deals flat damage to the primary target, then destroys this projectile.
     * The enemies list is unused here (no area effect) but is part of the shared
     * onHit contract so the game loop can stay projectile-type-agnostic.
     */
    @Override
    public void onHit(Enemy target, List<Enemy> enemies) {
        target.takeDamage(damage);
        alive = false;
    }
}
