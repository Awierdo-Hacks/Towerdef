package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;
import java.util.Optional;

/*
 * Abstract base class for all tower types.
 *
 * Towers are stationary defensive structures placed on designated build spots.
 * Each frame the game loop:
 *   1. Calls tower.update(deltaTime)         — decrements fire cooldown
 *   2. Calls tower.findTarget(enemies)        — returns best enemy in range
 *   3. If target found and ready to fire:
 *        factory.createProjectile(...)         — game loop creates the projectile
 *        tower.resetCooldown()                 — starts the inter-shot timer
 *   4. Calls tower.applyAreaEffect(enemies)   — used by IceTower to apply slow aura
 *
 * Projectile creation is intentionally NOT done inside the tower — it's the game
 * loop's job to call the factory. This keeps towers decoupled from the factory.
 *
 * Subclasses define the targeting strategy in findTarget():
 *   ArrowTower  — closest enemy in range     (Streams: min by distance)
 *   CannonTower — highest HP enemy in range  (Streams: max by currentHealth)
 *   IceTower    — fastest enemy in range     (Streams: max by speed)
 */
public abstract class Tower extends Entity {

    // Detection and attack radius in game-world units
    protected double range;

    // Damage per projectile hit (not used by IceTower which applies a slow instead)
    protected int damage;

    // Shots per second — higher = faster firing
    protected double fireRate;

    // Counts down between shots; the tower may fire when this reaches 0
    protected double fireCooldown;

    // Gold cost for the player to place this tower
    protected int cost;

    /*
     * Shared constructor — all tower subclasses pass their stats up through super().
     * fireCooldown starts at 0 so the tower can fire immediately when placed.
     */
    public Tower(Position position, double width, double height,
                 double range, int damage, double fireRate, int cost) {
        super(position, width, height);
        this.range        = range;
        this.damage       = damage;
        this.fireRate     = fireRate;
        this.fireCooldown = 0.0;
        this.cost         = cost;
    }

    // -------------------------------------------------------------------------
    // Update — manages the fire cooldown each frame
    // -------------------------------------------------------------------------

    /*
     * Decrements the fire cooldown by the elapsed frame time.
     * Subclasses may override to add extra per-frame behaviour (e.g. IceTower
     * applies its slow in applyAreaEffect instead of here), but should still
     * call super.update(deltaTime) to keep the cooldown ticking.
     */
    @Override
    public void update(double deltaTime) {
        if (fireCooldown > 0) {
            fireCooldown -= deltaTime;
        }
    }

    // -------------------------------------------------------------------------
    // Targeting — must be implemented by each tower subclass
    // -------------------------------------------------------------------------

    /*
     * Selects the best target from the given list using this tower's strategy.
     *
     * Implementations MUST:
     *   - Only consider enemies that are alive
     *   - Only consider enemies within this.range
     *   - Return Optional.empty() when no valid target exists (never return null)
     *
     * The Java Streams API is used here (requirement): filter by range, then
     * apply a comparator to pick the best candidate.
     */
    public abstract Optional<Enemy> findTarget(List<Enemy> enemies);

    // -------------------------------------------------------------------------
    // Area effects — override in towers that affect all enemies in a radius
    // -------------------------------------------------------------------------

    /*
     * Applies a per-frame area effect to nearby enemies.
     *
     * Default: no-op. IceTower overrides this to apply a slow to all enemies
     * within its range every frame.
     *
     * The game loop calls this on every tower every frame, so keep it cheap.
     */
    public void applyAreaEffect(List<Enemy> enemies) {
        // no area effect by default — only IceTower overrides this
    }

    // -------------------------------------------------------------------------
    // Cooldown management — called by the game loop after firing
    // -------------------------------------------------------------------------

    /*
     * Resets the inter-shot cooldown.
     * Call this immediately after the game loop has created a projectile for
     * this tower, so it won't fire again until 1/fireRate seconds have passed.
     */
    public void resetCooldown() {
        // Guard against zero fireRate (e.g. IceTower) — avoids Infinity from 1.0/0.0
        if (fireRate > 0) {
            this.fireCooldown = 1.0 / fireRate;
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public double  getRange()        { return range; }
    public int     getDamage()       { return damage; }
    public double  getFireRate()     { return fireRate; }
    public int     getCost()         { return cost; }

    /* Returns true when the tower is allowed to fire (cooldown expired). */
    public boolean isReadyToFire()   { return fireCooldown <= 0; }
}
