package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/**
 * A heavily armoured enemy — high HP, slow movement, and physical resistance.
 *
 * <p>Default stats:</p>
 * <ul>
 *   <li>health: 220</li>
 *   <li>speed: 1.2 game-world units per second</li>
 *   <li>reward: 25 gold on kill</li>
 *   <li>score: 20 points on kill</li>
 *   <li>damage resistance: 0.5 (takes 50% reduced damage from all sources)</li>
 *   <li>size: 0.75 × 0.75 units</li>
 * </ul>
 *
 * <p><strong>Damage resistance:</strong> {@link #takeDamage(double)} multiplies
 * incoming damage by {@code (1 - damageResistance)}. This makes armoured enemies
 * noticeably tougher against arrow towers but still vulnerable to cannon splash (high
 * base damage cuts through). Because the cannon tower targets highest-HP enemies,
 * armoured enemies are naturally prioritised by it — the intended synergy.</p>
 *
 * <p>Abstract because {@code render()} from {@code Entity} is not implemented here;
 * {@code J2dArmoredEnemy} extends this class and implements it.</p>
 *
 * @author Tower Defence team
 */
public abstract class ArmoredEnemy extends Enemy {

    /** Default starting/maximum HP. */
    public static final double DEFAULT_HEALTH              = 220.0;
    /** Default movement speed in game-world units per second. */
    public static final double DEFAULT_SPEED               = 1.2;
    /** Default gold reward on kill. */
    public static final int    DEFAULT_REWARD              = 25;
    /** Default score awarded on kill. */
    public static final int    DEFAULT_SCORE               = 20;
    /** Default damage resistance fraction (50% reduction). */
    public static final double DEFAULT_DAMAGE_RESISTANCE   = 0.5;
    /** Width and height in game-world units. */
    public static final double SIZE                        = 0.75;

    /** Fraction of incoming damage absorbed ({@code 0.0} = none, {@code 1.0} = immune). */
    protected double damageResistance;

    /**
     * Creates an armoured enemy with the given stats and path.
     *
     * @param startPosition    the spawn position in game-world coordinates
     * @param health           the starting/maximum HP
     * @param speed            the movement speed in units per second
     * @param reward           the gold awarded on kill
     * @param scoreValue       the score awarded on kill
     * @param damageResistance the fraction of incoming damage absorbed
     * @param path             the ordered list of waypoints to follow
     */
    public ArmoredEnemy(Position startPosition, double health, double speed,
                        int reward, int scoreValue, double damageResistance,
                        List<Position> path) {
        super(startPosition, SIZE, SIZE, health, speed, reward, scoreValue, path);
        this.damageResistance = damageResistance;
    }

    // -------------------------------------------------------------------------
    // Damage resistance — reduces all incoming damage by the resistance fraction
    // -------------------------------------------------------------------------

    /**
     * Applies damage resistance before subtracting from health.
     *
     * <p>With {@code damageResistance = 0.5}, an arrow tower's 15 damage becomes 7
     * effective damage and a cannon tower's 60 becomes 30. This makes the armoured
     * enemy significantly tougher and encourages the player to invest in cannon
     * towers for later waves.</p>
     *
     * @param amount the raw incoming damage before resistance
     */
    @Override
    public void takeDamage(double amount) {
        // Reduce incoming damage by the resistance fraction
        double effectiveDamage = amount * (1.0 - damageResistance);
        super.takeDamage(effectiveDamage);
    }

    /**
     * Returns the type identifier {@code "armored"}, used by Lua scripts to check
     * enemy type.
     *
     * @return the string {@code "armored"}
     */
    @Override
    public String getType() {
        return "armored";
    }
}
