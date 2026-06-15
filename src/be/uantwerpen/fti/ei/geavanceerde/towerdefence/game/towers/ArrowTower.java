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
 * A fast, low-damage tower that fires arrows at the <em>closest</em> enemy in range.
 *
 * <p>Default stats (can be overridden via {@code game.properties}):</p>
 * <ul>
 *   <li>range: 3.5 game-world units</li>
 *   <li>damage: 15 per hit</li>
 *   <li>fire rate: 2.5 shots per second</li>
 *   <li>cost: 50 gold</li>
 *   <li>size: 0.8 × 0.8 units</li>
 * </ul>
 *
 * <p><strong>Targeting strategy (Java Streams):</strong> uses
 * {@code stream().filter().min()} to find the alive enemy closest to this tower.
 * "Closest first" kills individual enemies faster and is effective against
 * spread-out waves.</p>
 *
 * <p>Abstract because {@code render()} from {@code Entity} is not implemented here;
 * {@code J2dArrowTower} extends this class and implements it.</p>
 *
 * @author Tower Defence team
 */
public abstract class ArrowTower extends Tower {

    /** Default detection/attack radius in game-world units. */
    public static final double DEFAULT_RANGE     = 3.5;
    /** Default damage dealt per hit. */
    public static final int    DEFAULT_DAMAGE    = 15;
    /** Default fire rate in shots per second. */
    public static final double DEFAULT_FIRE_RATE = 2.5;
    /** Default gold cost to place this tower. */
    public static final int    DEFAULT_COST      = 50;
    /** Width and height of the tower in game-world units. */
    public static final double SIZE              = 0.8;

    /**
     * Creates an arrow tower with the given stats.
     *
     * @param position the build position in game-world coordinates
     * @param range    the detection/attack radius in game-world units
     * @param damage   the damage dealt per hit
     * @param fireRate the fire rate in shots per second
     * @param cost     the gold cost to place this tower
     */
    public ArrowTower(Position position, double range, int damage, double fireRate, int cost) {
        super(position, SIZE, SIZE, range, damage, fireRate, cost);
    }

    // -------------------------------------------------------------------------
    // Targeting — CLOSEST alive enemy within range (Java Streams)
    // -------------------------------------------------------------------------

    /**
     * Finds the closest alive enemy within this tower's range.
     *
     * <p>Streams usage (project requirement): {@code filter} keeps only alive enemies
     * within range, then {@code min} picks the one with the smallest distance to this
     * tower.</p>
     *
     * @param enemies the current list of enemies
     * @return the closest enemy in range, or {@link Optional#empty()} if none is in range
     */
    @Override
    public Optional<Enemy> findTarget(List<Enemy> enemies) {
        Position towerPos = this.position;

        return enemies.stream()
            .filter(Enemy::isAlive)
            .filter(e -> towerPos.distanceTo(e.getPosition()) <= this.range)
            .min(Comparator.comparingDouble(e -> towerPos.distanceTo(e.getPosition())));
    }

    // -------------------------------------------------------------------------
    // Firing — a single-target ray projectile toward the chosen enemy
    // -------------------------------------------------------------------------

    /**
     * Creates a single-target ray projectile aimed at the target's current position.
     *
     * @param factory the abstract factory used to create the projectile
     * @param target  the enemy being fired at
     * @return the newly created ray projectile
     */
    @Override
    public Projectile fire(EntityFactory factory, Enemy target) {
        return factory.createRayProjectile(position, target.getPosition(), damage);
    }
}
