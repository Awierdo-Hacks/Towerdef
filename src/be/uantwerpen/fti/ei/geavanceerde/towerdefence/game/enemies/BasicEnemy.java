package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/**
 * A standard ground enemy — the most common unit in most waves.
 *
 * <p>Default stats:</p>
 * <ul>
 *   <li>health: 80</li>
 *   <li>speed: 2.0 game-world units per second</li>
 *   <li>reward: 10 gold on kill</li>
 *   <li>score: 10 points on kill</li>
 *   <li>size: 0.6 × 0.6 units</li>
 * </ul>
 *
 * <p>No special abilities — moves along the path and takes full damage from all
 * towers. Appears in wave 1 and every subsequent wave, in increasing numbers.</p>
 *
 * <p>Abstract because {@code render()} from {@code Entity} is not implemented here;
 * {@code J2dBasicEnemy} extends this class and implements it.</p>
 *
 * @author Tower Defence team
 */
public abstract class BasicEnemy extends Enemy {

    /** Default starting/maximum HP. */
    public static final double DEFAULT_HEALTH     = 80.0;
    /** Default movement speed in game-world units per second. */
    public static final double DEFAULT_SPEED      = 2.0;
    /** Default gold reward on kill. */
    public static final int    DEFAULT_REWARD     = 10;
    /** Default score awarded on kill. */
    public static final int    DEFAULT_SCORE      = 10;
    /** Width and height in game-world units. */
    public static final double SIZE               = 0.6;

    /**
     * Creates a basic enemy with the given stats and path.
     *
     * @param startPosition the spawn position in game-world coordinates
     * @param health        the starting/maximum HP
     * @param speed         the movement speed in units per second
     * @param reward        the gold awarded on kill
     * @param scoreValue    the score awarded on kill
     * @param path          the ordered list of waypoints to follow
     */
    public BasicEnemy(Position startPosition, double health, double speed,
                      int reward, int scoreValue, List<Position> path) {
        super(startPosition, SIZE, SIZE, health, speed, reward, scoreValue, path);
    }

    /**
     * Returns the type identifier {@code "basic"}, used by Lua scripts to check enemy
     * type (e.g. {@code if enemy:getType() == "basic" then ... end}).
     *
     * @return the string {@code "basic"}
     */
    @Override
    public String getType() {
        return "basic";
    }
}
