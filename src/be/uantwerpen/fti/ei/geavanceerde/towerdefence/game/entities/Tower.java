package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;
import java.util.Optional;

/*
 * Abstract base class for all tower types.
 *
 * Towers are stationary defensive structures placed by the player on build spots.
 * Each tower has a range, damage value, and fire rate. Each frame the tower:
 *   1. Checks if the fire cooldown has expired.
 *   2. Finds the best target within range using findTarget().
 *   3. Creates a projectile via fire() and returns it to the game loop.
 *
 * The concrete subclasses (ArrowTower, CannonTower, IceTower) define their own
 * targeting strategy and projectile type. J2d subclasses further extend these
 * to add rendering.
 *
 * Uses Optional<Projectile> instead of null to signal "no shot this frame".
 */
public abstract class Tower extends Entity {

    // How far this tower can detect and hit enemies (in game-world units)
    protected double range;

    // Base damage dealt per projectile hit
    protected int damage;

    // How many shots this tower fires per second (e.g. 2.0 = two shots/sec)
    protected double fireRate;

    // Countdown until the next shot is allowed — decremented each frame by deltaTime
    protected double fireCooldown;

    // Gold cost to place this tower — read by the Game when the player buys it
    protected int cost;

    /*
     * Sets up a tower at the given position with core combat stats.
     * fireCooldown starts at 0 so the tower can fire immediately when placed.
     */
    public Tower(Position position, double width, double height,
                 double range, int damage, double fireRate, int cost) {
        super(position, width, height);
        this.range        = range;
        this.damage       = damage;
        this.fireRate     = fireRate;
        this.fireCooldown = 0.0;  // ready to fire immediately
        this.cost         = cost;
    }

    // -------------------------------------------------------------------------
    // Core tower logic — runs each frame via Entity.update()
    // -------------------------------------------------------------------------

    /*
     * Decrements the fire cooldown each frame and fires when ready.
     *
     * Concrete subclasses can override this if they need special update behaviour
     * (e.g. IceTower applying a slow aura every frame), but should call
     * super.update(deltaTime) to keep the cooldown working.
     */
    @Override
    public void update(double deltaTime) {
        // Count down until the next shot is allowed
        if (fireCooldown > 0) {
            fireCooldown -= deltaTime;
        }
    }

    // -------------------------------------------------------------------------
    // Abstract targeting & firing — implemented differently per tower type
    // -------------------------------------------------------------------------

    /*
     * Selects the best enemy target from the given list.
     *
     * Subclasses decide the targeting strategy:
     *   - ArrowTower: nearest enemy in range
     *   - CannonTower: highest HP enemy in range
     *   - IceTower: fastest enemy in range
     *
     * Returns Optional.empty() if no valid target exists (no enemies in range).
     * Uses Optional to avoid returning null — null-free design as required.
     */
    public abstract Optional<Enemy> findTarget(List<Enemy> enemies);

    /*
     * Creates and returns a projectile aimed at the current target.
     *
     * The game loop calls update() first (which may set an internal target),
     * then calls fire() to retrieve the projectile if one was created.
     * Returns Optional.empty() if the tower is still on cooldown or has no target.
     *
     * After firing, the subclass must reset fireCooldown to 1.0 / fireRate.
     */
    public abstract Optional<Projectile> fire();

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public double getRange()    { return range; }
    public int    getDamage()   { return damage; }
    public double getFireRate() { return fireRate; }
    public int    getCost()     { return cost; }

    /* True when the cooldown has expired and the tower is ready to fire. */
    public boolean isReadyToFire() { return fireCooldown <= 0; }
}
