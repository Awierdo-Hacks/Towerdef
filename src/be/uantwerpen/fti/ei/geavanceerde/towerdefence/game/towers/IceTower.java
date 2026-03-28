package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/*
 * A support tower that slows all enemies within its range.
 *
 * Stats (defaults):
 *   range      : 2.0 game-world units
 *   damage     : 0 (no direct damage)
 *   fireRate   : 0 (doesn't fire projectiles — uses area effect instead)
 *   cost       : 75 gold
 *   slowFactor : 0.4 (enemies in range move at 40% of their normal speed)
 *   slowDuration: 0.3 seconds (how long the slow lasts after leaving range)
 *   size       : 0.8 x 0.8 units
 *
 * MECHANIC:
 *   Unlike Arrow/Cannon towers, the IceTower never fires a projectile.
 *   Instead, the game loop calls applyAreaEffect(enemies) every frame.
 *   This calls enemy.applySlow(slowFactor, slowDuration) for every enemy
 *   currently within range. The slowTimer in Enemy ensures the effect
 *   expires naturally after the enemy leaves range.
 *
 * TARGETING STRATEGY (Java Streams):
 *   findTarget() returns the FASTEST enemy in range (optional — used to
 *   show a visual indicator of the primary target). The real effect is
 *   always applied to ALL enemies in range via applyAreaEffect().
 *
 * ABSTRACT because render() from Entity is not implemented here.
 * J2dIceTower (Fase 5) extends this and implements render().
 */
public abstract class IceTower extends Tower {

    public static final double DEFAULT_RANGE         = 2.0;
    public static final int    DEFAULT_DAMAGE        = 0;     // no direct damage
    public static final double DEFAULT_FIRE_RATE     = 0.0;   // does not fire
    public static final int    DEFAULT_COST          = 75;
    public static final double SIZE                  = 0.8;

    public static final double DEFAULT_SLOW_FACTOR   = 0.4;   // 40% speed
    public static final double DEFAULT_SLOW_DURATION = 0.3;   // seconds after leaving range

    // How much to reduce enemy speed (multiplier, e.g. 0.4 = 40% of base speed)
    protected double slowFactor;

    // How long the slow lingers after an enemy leaves the ice tower's range
    protected double slowDuration;

    public IceTower(Position position, double range, double slowFactor, double slowDuration, int cost) {
        super(position, SIZE, SIZE, range, DEFAULT_DAMAGE, DEFAULT_FIRE_RATE, cost);
        this.slowFactor   = slowFactor;
        this.slowDuration = slowDuration;
    }

    // -------------------------------------------------------------------------
    // Area effect — applies slow to ALL enemies in range every frame
    // -------------------------------------------------------------------------

    /*
     * Slows every alive enemy within this tower's range.
     *
     * Called by the game loop each frame instead of the normal fire/projectile flow.
     * enemy.applySlow() refreshes the slow timer — so as long as an enemy stays
     * in range, it remains slowed. The moment it leaves, the timer ticks down
     * and speed returns to normal after slowDuration seconds.
     *
     * STREAMS USAGE:
     *   1. filter: alive enemies within range
     *   2. forEach: apply the slow effect to each one
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
    // Targeting — FASTEST alive enemy in range (for UI target indicator only)
    // -------------------------------------------------------------------------

    /*
     * Returns the fastest enemy in range — used purely for UI feedback
     * (e.g. drawing a highlight around the primary slow target).
     * The actual slow is applied to ALL enemies via applyAreaEffect().
     */
    @Override
    public Optional<Enemy> findTarget(List<Enemy> enemies) {
        Position towerPos = this.position;

        return enemies.stream()
            .filter(Enemy::isAlive)
            .filter(e -> towerPos.distanceTo(e.getPosition()) <= this.range)
            .max(Comparator.comparingDouble(e -> e.getSpeed() * e.getSpeedMultiplier()));
    }

    // -------------------------------------------------------------------------
    // Override — IceTower never fires, so isReadyToFire is always false
    // -------------------------------------------------------------------------

    @Override
    public boolean isReadyToFire() {
        return false;  // IceTower does not use the projectile system
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public double getSlowFactor()   { return slowFactor; }
    public double getSlowDuration() { return slowDuration; }
}
