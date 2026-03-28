package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/*
 * A fast, low-damage tower that fires arrows at the CLOSEST enemy in range.
 *
 * Stats (defaults — can be overridden via game.properties):
 *   range    : 3.5 game-world units
 *   damage   : 15 per hit
 *   fireRate : 2.5 shots per second
 *   cost     : 50 gold
 *   size     : 0.8 x 0.8 units
 *
 * TARGETING STRATEGY (Java Streams):
 *   Uses stream().filter().min() to find the alive enemy closest to this tower.
 *   "Closest first" is the default strategy: it kills individual enemies faster
 *   and is effective against spread-out waves.
 *
 * ABSTRACT because render() from Entity is not implemented here.
 * J2dArrowTower (Fase 5) extends this class and implements render().
 */
public abstract class ArrowTower extends Tower {

    // Default stats — J2dArrowTower can read from config and pass different values
    public static final double DEFAULT_RANGE     = 3.5;
    public static final int    DEFAULT_DAMAGE    = 15;
    public static final double DEFAULT_FIRE_RATE = 2.5;
    public static final int    DEFAULT_COST      = 50;
    public static final double SIZE              = 0.8;

    public ArrowTower(Position position, double range, int damage, double fireRate, int cost) {
        super(position, SIZE, SIZE, range, damage, fireRate, cost);
    }

    // -------------------------------------------------------------------------
    // Targeting — CLOSEST alive enemy within range (Java Streams)
    // -------------------------------------------------------------------------

    /*
     * Finds the closest alive enemy within this tower's range.
     *
     * STREAMS USAGE (required by project spec):
     *   1. filter: keep only alive enemies within range
     *   2. min:    pick the one with the smallest distance to this tower
     *
     * Returns Optional.empty() if no enemy is in range — never returns null.
     */
    @Override
    public Optional<Enemy> findTarget(List<Enemy> enemies) {
        Position towerPos = this.position;

        return enemies.stream()
            .filter(Enemy::isAlive)
            .filter(e -> towerPos.distanceTo(e.getPosition()) <= this.range)
            .min(Comparator.comparingDouble(e -> towerPos.distanceTo(e.getPosition())));
    }
}
