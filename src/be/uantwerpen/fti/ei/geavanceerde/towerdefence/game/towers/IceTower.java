package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;
import java.util.Optional;

/**
 * A support tower that slows all enemies within its range.
 *
 * <p>Default stats:</p>
 * <ul>
 *   <li>range: 3.0 game-world units</li>
 *   <li>damage: 0 (no direct damage)</li>
 *   <li>fire rate: 0 (does not fire projectiles — uses an area effect instead)</li>
 *   <li>cost: 75 gold</li>
 *   <li>slow factor: 0.4 (enemies in range move at 40% of normal speed)</li>
 *   <li>slow duration: 0.3 seconds (how long the slow lasts after leaving range)</li>
 *   <li>size: 0.8 × 0.8 units</li>
 * </ul>
 *
 * <p><strong>Mechanic:</strong> unlike the arrow and cannon towers, the ice tower
 * never fires a projectile. Instead the game loop calls
 * {@link #applyAreaEffect(List)} every frame, which calls
 * {@code enemy.applySlow(slowFactor, slowDuration)} for each enemy in range. The
 * {@code slowTimer} in {@code Enemy} ensures the effect expires after the enemy
 * leaves range.</p>
 *
 * <p>Abstract because {@code render()} from {@code Entity} is not implemented here;
 * {@code J2dIceTower} extends this class and implements it.</p>
 *
 * @author Tower Defence team
 */
public abstract class IceTower extends Tower {

    /** Default detection/effect radius in game-world units. */
    public static final double DEFAULT_RANGE         = 3.0;
    /** Default damage — the ice tower deals none. */
    public static final int    DEFAULT_DAMAGE        = 0;
    /** Default fire rate — the ice tower does not fire. */
    public static final double DEFAULT_FIRE_RATE     = 0.0;
    /** Default gold cost to place this tower. */
    public static final int    DEFAULT_COST          = 75;
    /** Width and height of the tower in game-world units. */
    public static final double SIZE                  = 0.8;

    /** Default slow multiplier (40% of base speed). */
    public static final double DEFAULT_SLOW_FACTOR   = 0.4;
    /** Default slow lingering duration in seconds after an enemy leaves range. */
    public static final double DEFAULT_SLOW_DURATION = 0.3;

    /** Speed multiplier applied to slowed enemies (e.g. {@code 0.4} = 40% of base speed). */
    protected double slowFactor;

    /** How long (seconds) the slow lingers after an enemy leaves the tower's range. */
    protected double slowDuration;

    /**
     * Creates an ice tower with the given stats. It deals no damage and never fires.
     *
     * @param position     the build position in game-world coordinates
     * @param range        the detection/effect radius in game-world units
     * @param slowFactor   the speed multiplier applied to enemies in range
     * @param slowDuration the seconds the slow lingers after leaving range
     * @param cost         the gold cost to place this tower
     */
    public IceTower(Position position, double range, double slowFactor, double slowDuration, int cost) {
        super(position, SIZE, SIZE, range, DEFAULT_DAMAGE, DEFAULT_FIRE_RATE, cost);
        this.slowFactor   = slowFactor;
        this.slowDuration = slowDuration;
    }

    // -------------------------------------------------------------------------
    // Area effect — applies slow to ALL enemies in range every frame
    // -------------------------------------------------------------------------

    /**
     * Slows every alive enemy within this tower's range.
     *
     * <p>Called by the game loop each frame instead of the normal fire/projectile
     * flow. {@code enemy.applySlow()} refreshes the slow timer, so as long as an
     * enemy stays in range it remains slowed; once it leaves, speed returns to normal
     * after {@code slowDuration} seconds.</p>
     *
     * <p>Streams usage: {@code filter} keeps alive enemies within range, then
     * {@code forEach} applies the slow to each one.</p>
     *
     * @param enemies the current list of enemies
     */
    @Override
    public void applyAreaEffect(List<Enemy> enemies) {
        Position towerPos = this.position;

        enemies.stream()
            .filter(Enemy::isAlive)
            .filter(e -> towerPos.distanceTo(e.getPosition()) <= this.range)
            .forEach(e -> e.applySlow(slowFactor, slowDuration));
    }

    // -------------------------------------------------------------------------
    // Targeting — IceTower has no single fire-target
    // -------------------------------------------------------------------------

    /**
     * Always returns {@link Optional#empty()} — the ice tower has no single
     * fire-target; its slow is applied to every enemy in range via
     * {@link #applyAreaEffect(List)}. This method only exists to satisfy the abstract
     * {@code findTarget()} contract in {@code Tower}.
     *
     * @param enemies the current list of enemies (ignored)
     * @return always {@link Optional#empty()}
     */
    @Override
    public Optional<Enemy> findTarget(List<Enemy> enemies) {
        return Optional.empty();
    }

    // -------------------------------------------------------------------------
    // Firing — IceTower never fires a projectile
    // -------------------------------------------------------------------------

    /**
     * Always returns {@code null} — the ice tower uses an area slow effect, not
     * projectiles. This method only exists to satisfy the abstract {@code fire()}
     * contract in {@code Tower} and is never called: the game loop guards firing with
     * {@link #isReadyToFire()}, which is always {@code false} here.
     *
     * @param factory the abstract factory (ignored)
     * @param target  the enemy (ignored)
     * @return always {@code null}
     */
    @Override
    public Projectile fire(EntityFactory factory, Enemy target) {
        return null;
    }

    // -------------------------------------------------------------------------
    // Override — IceTower never fires, so isReadyToFire is always false
    // -------------------------------------------------------------------------

    /**
     * Always returns {@code false} — the ice tower does not use the projectile system.
     *
     * @return always {@code false}
     */
    @Override
    public boolean isReadyToFire() {
        return false;  // IceTower does not use the projectile system
    }
}
