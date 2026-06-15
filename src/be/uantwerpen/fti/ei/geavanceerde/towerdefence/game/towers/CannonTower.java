package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * A slow, high-damage tower that fires cannonballs at the <em>highest-HP</em> enemy
 * in range.
 *
 * <p>Default stats:</p>
 * <ul>
 *   <li>range: 2.5 game-world units</li>
 *   <li>damage: 60 per direct hit</li>
 *   <li>fire rate: 0.6 shots per second</li>
 *   <li>cost: 100 gold</li>
 *   <li>size: 0.9 × 0.9 units</li>
 * </ul>
 *
 * <p><strong>Splash damage:</strong> the tower's splash stats
 * ({@code splashRadius} + {@code splashDamage}) are passed to the
 * {@code CannonProjectile} it fires. The area damage is applied inside
 * {@code CannonProjectile.onHit()} when the projectile lands — the tower itself does
 * not apply damage. The splash damage is a fraction of the base damage (default 40%).</p>
 *
 * <p><strong>Targeting strategy (Java Streams):</strong> targets the enemy with the
 * highest current HP in range, maximising splash value against clustered tanky
 * enemies and prioritising armoured enemies.</p>
 *
 * <p>Abstract because {@code render()} from {@code Entity} is not implemented here;
 * {@code J2dCannonTower} extends this class and implements it.</p>
 *
 * @author Tower Defence team
 */
public abstract class CannonTower extends Tower {

    /** Default detection/attack radius in game-world units. */
    public static final double DEFAULT_RANGE       = 2.5;
    /** Default direct-hit damage. */
    public static final int    DEFAULT_DAMAGE      = 100;
    /** Default fire rate in shots per second. */
    public static final double DEFAULT_FIRE_RATE   = 0.6;
    /** Default gold cost to place this tower. */
    public static final int    DEFAULT_COST        = 100;
    /** Width and height of the tower in game-world units. */
    public static final double SIZE                = 0.9;

    /** Default splash radius in game-world units. */
    public static final double DEFAULT_SPLASH_RADIUS = 2;
    /** Default splash damage as a fraction of the main damage (40%). */
    public static final double DEFAULT_SPLASH_DAMAGE_FRACTION = 0.4;

    /** Splash radius (game-world units) copied onto the fired {@code CannonProjectile}. */
    protected double splashRadius;

    /** Splash damage copied onto the fired {@code CannonProjectile}. */
    protected int    splashDamage;

    /**
     * Creates a cannon tower with the given stats. The splash damage is computed as
     * {@code damage * splashDamageFraction}.
     *
     * @param position             the build position in game-world coordinates
     * @param range                the detection/attack radius in game-world units
     * @param damage               the direct-hit damage
     * @param fireRate             the fire rate in shots per second
     * @param cost                 the gold cost to place this tower
     * @param splashRadius         the splash radius in game-world units
     * @param splashDamageFraction the splash damage as a fraction of {@code damage}
     */
    public CannonTower(Position position, double range, int damage, double fireRate, int cost,
                       double splashRadius, double splashDamageFraction) {
        super(position, SIZE, SIZE, range, damage, fireRate, cost);
        this.splashRadius = splashRadius;
        // Splash damage is a fraction of the main hit damage
        this.splashDamage = (int)(damage * splashDamageFraction);
    }

    // -------------------------------------------------------------------------
    // Targeting — HIGHEST HP alive enemy within range (Java Streams)
    // -------------------------------------------------------------------------

    /**
     * Finds the alive enemy with the most current HP within this tower's range.
     *
     * <p>Streams usage: {@code filter} keeps alive enemies within range, then
     * {@code max} picks the one with the highest current health. Prioritising high-HP
     * targets maximises splash effectiveness and ensures armoured enemies are dealt
     * with before they reach the base.</p>
     *
     * @param enemies the current list of enemies
     * @return the highest-HP enemy in range, or {@link Optional#empty()} if none is in range
     */
    @Override
    public Optional<Enemy> findTarget(List<Enemy> enemies) {
        Position towerPos = this.position;

        return enemies.stream()
            .filter(Enemy::isAlive)
            .filter(e -> towerPos.distanceTo(e.getPosition()) <= this.range)
            .max(Comparator.comparingDouble(Enemy::getCurrentHealth));
    }

    // -------------------------------------------------------------------------
    // Firing — a cannon projectile carrying this tower's splash stats
    // -------------------------------------------------------------------------

    /**
     * Creates a cannon projectile carrying this tower's splash stats, aimed at the
     * target's current position.
     *
     * @param factory the abstract factory used to create the projectile
     * @param target  the enemy being fired at
     * @return the newly created cannon projectile
     */
    @Override
    public Projectile fire(EntityFactory factory, Enemy target) {
        return factory.createCannonProjectile(
            position, target.getPosition(), damage, splashRadius, splashDamage);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the splash radius in game-world units.
     *
     * @return the splash radius
     */
    public double  getSplashRadius() { return splashRadius; }

    /**
     * Returns the splash damage applied to enemies caught in the splash radius.
     *
     * @return the splash damage
     */
    public int     getSplashDamage() { return splashDamage; }
}
