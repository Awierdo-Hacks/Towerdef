package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;
import java.util.Optional;

/**
 * Abstract base class for all tower types.
 *
 * <p>Towers are stationary defensive structures placed on designated build spots.
 * Each frame the game loop:</p>
 * <ol>
 *   <li>Calls {@link #update(double)} — decrements the fire cooldown.</li>
 *   <li>Calls {@link #findTarget(List)} — returns the best enemy in range.</li>
 *   <li>If a target is found and the tower is ready to fire:
 *       {@link #fire(EntityFactory, Enemy)} (the tower creates its projectile),
 *       then {@link #resetCooldown()} (starts the inter-shot timer).</li>
 *   <li>Calls {@link #applyAreaEffect(List)} — used by {@code IceTower} to apply its slow aura.</li>
 * </ol>
 *
 * <p>Each tower creates its <em>own</em> projectile via {@link #fire(EntityFactory, Enemy)}:
 * an {@code ArrowTower} asks the factory for a ray projectile, a {@code CannonTower}
 * for a cannon projectile. This keeps the game loop free of any type-checking (no
 * {@code instanceof}) — the right projectile is chosen polymorphically. The tower
 * depends only on the abstract {@link EntityFactory}, so the game/visualization
 * separation is preserved.</p>
 *
 * <p>Subclasses define the targeting strategy in {@link #findTarget(List)}:</p>
 * <ul>
 *   <li>{@code ArrowTower} — closest enemy in range (Streams: min by distance)</li>
 *   <li>{@code CannonTower} — highest HP enemy in range (Streams: max by currentHealth)</li>
 *   <li>{@code IceTower} — fastest enemy in range (Streams: max by speed)</li>
 * </ul>
 *
 * @author Tower Defence team
 */
public abstract class Tower extends Entity {

    /** Detection and attack radius in game-world units. */
    protected double range;

    /** Damage per projectile hit (unused by {@code IceTower}, which applies a slow instead). */
    protected int damage;

    /** Shots per second — higher means faster firing. */
    protected double fireRate;

    /** Counts down between shots; the tower may fire once this reaches {@code 0}. */
    protected double fireCooldown;

    /** Gold cost for the player to place this tower. */
    protected int cost;

    /**
     * Shared constructor — all tower subclasses pass their stats up through
     * {@code super(...)}. {@code fireCooldown} starts at {@code 0} so the tower can
     * fire immediately when placed.
     *
     * @param position the build position in game-world coordinates
     * @param width    the tower width in game-world units
     * @param height   the tower height in game-world units
     * @param range    the detection/attack radius in game-world units
     * @param damage   the damage dealt per projectile hit
     * @param fireRate the number of shots per second
     * @param cost     the gold cost to place this tower
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

    /**
     * Decrements the fire cooldown by the elapsed frame time.
     *
     * <p>Subclasses may override to add extra per-frame behaviour (e.g.
     * {@code IceTower} applies its slow in {@link #applyAreaEffect(List)} instead
     * of here), but should still call {@code super.update(deltaTime)} to keep the
     * cooldown ticking.</p>
     *
     * @param deltaTime the elapsed time since the previous frame, in seconds
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

    /**
     * Selects the best target from the given list using this tower's strategy.
     *
     * <p>Implementations <strong>must</strong>:</p>
     * <ul>
     *   <li>only consider enemies that are alive,</li>
     *   <li>only consider enemies within {@link #range},</li>
     *   <li>return {@link Optional#empty()} when no valid target exists (never {@code null}).</li>
     * </ul>
     *
     * <p>The Java Streams API is used here (project requirement): filter by range,
     * then apply a comparator to pick the best candidate.</p>
     *
     * @param enemies the current list of enemies to choose from
     * @return the selected target, or {@link Optional#empty()} if none is in range
     */
    public abstract Optional<Enemy> findTarget(List<Enemy> enemies);

    // -------------------------------------------------------------------------
    // Firing — each tower creates its own projectile via the factory
    // -------------------------------------------------------------------------

    /**
     * Creates and returns the projectile this tower fires at the given target,
     * using the abstract {@link EntityFactory}.
     *
     * <p>Called by the game loop only when the tower has a target and its cooldown
     * is ready. Each subclass picks the right factory method polymorphically:</p>
     * <ul>
     *   <li>{@code ArrowTower} → {@code factory.createRayProjectile(...)}</li>
     *   <li>{@code CannonTower} → {@code factory.createCannonProjectile(...)}</li>
     *   <li>{@code IceTower} → never fires (see {@code IceTower}).</li>
     * </ul>
     *
     * @param factory the abstract factory used to create the projectile
     * @param target  the enemy this tower is firing at
     * @return the newly created projectile
     */
    public abstract Projectile fire(EntityFactory factory, Enemy target);

    // -------------------------------------------------------------------------
    // Area effects — override in towers that affect all enemies in a radius
    // -------------------------------------------------------------------------

    /**
     * Applies a per-frame area effect to nearby enemies.
     *
     * <p>The default implementation is a no-op. {@code IceTower} overrides this to
     * apply a slow to all enemies within its range every frame. The game loop calls
     * this on every tower every frame, so keep it cheap.</p>
     *
     * @param enemies the current list of enemies that may be affected
     */
    public void applyAreaEffect(List<Enemy> enemies) {
        // no area effect by default — only IceTower overrides this
    }

    // -------------------------------------------------------------------------
    // Cooldown management — called by the game loop after firing
    // -------------------------------------------------------------------------

    /**
     * Resets the inter-shot cooldown.
     *
     * <p>Call this immediately after the game loop has created a projectile for this
     * tower, so it will not fire again until {@code 1/fireRate} seconds have passed.</p>
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

    /**
     * Returns the damage dealt per projectile hit.
     *
     * @return the damage per hit
     */
    public int     getDamage()       { return damage; }

    /**
     * Returns the detection/effect radius in game-world units — read by the view to
     * draw the range ring.
     *
     * @return the range radius
     */
    public double  getRange()        { return range; }

    /**
     * Returns the gold cost to place this tower — read by the game loop before purchase.
     *
     * @return the gold cost
     */
    public int     getCost()         { return cost; }

    /**
     * Returns whether the tower is allowed to fire (its cooldown has expired).
     *
     * @return {@code true} if the tower can fire this frame
     */
    public boolean isReadyToFire()   { return fireCooldown <= 0; }
}
