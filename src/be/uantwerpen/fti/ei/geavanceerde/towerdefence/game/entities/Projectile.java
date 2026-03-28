package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/*
 * Abstract base class for all projectiles fired by towers.
 *
 * A projectile is created by a tower when it fires (via EntityFactory),
 * then managed by the game loop each frame:
 *   1. update(deltaTime) moves it toward its target position.
 *   2. The game loop checks collision with enemies.
 *   3. On hit: enemy.takeDamage(damage) is called and the projectile is destroyed.
 *   4. If the projectile travels past its target without hitting anything it is
 *      also destroyed (hasReachedTarget() returns true).
 *
 * Projectiles target a fixed position in the world (not a moving entity reference).
 * This avoids holding a direct reference to an enemy that might die mid-flight,
 * which would require null checks. The projectile just flies to where the enemy was.
 *
 * Subclasses can override update() to add special behaviour:
 *   - CannonProjectile: explodes on arrival dealing splash damage in an area.
 *   - IceProjectile: applies a slow effect on hit instead of dealing damage.
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
     * Called when this projectile hits an enemy.
     *
     * The default implementation deals flat damage to the target enemy.
     * CannonProjectile overrides this to apply splash damage to all nearby enemies.
     * IceProjectile overrides this to apply a slow effect instead.
     *
     * After hitting, the projectile destroys itself.
     */
    public void onHit(Enemy target) {
        target.takeDamage(damage);
        alive = false;
    }

    // -------------------------------------------------------------------------
    // Splash damage — set by the game loop for CannonTower projectiles
    // -------------------------------------------------------------------------

    // Defaults to 0 (no splash). The game loop calls setSplash() after creating
    // a projectile for a CannonTower, copying the tower's splash stats onto it.
    protected double splashRadius = 0;
    protected int    splashDamage = 0;

    public void setSplash(double radius, int damage) {
        this.splashRadius = radius;
        this.splashDamage = damage;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int      getDamage()        { return damage; }
    public double   getSpeed()         { return speed; }
    public Position getTargetPosition(){ return targetPosition; }
    public double   getSplashRadius()  { return splashRadius; }
    public int      getSplashDamage()  { return splashDamage; }

    /* True when the projectile has reached its destination (set in update). */
    public boolean hasReachedTarget()  { return !alive; }
}
