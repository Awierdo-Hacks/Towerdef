package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/**
 * A fast flying enemy — ignores ground terrain and follows its own aerial path.
 *
 * <p>Default stats:</p>
 * <ul>
 *   <li>health: 60 (fragile but fast)</li>
 *   <li>speed: 3.5 game-world units per second</li>
 *   <li>reward: 20 gold on kill</li>
 *   <li>score: 15 points on kill</li>
 *   <li>size: 0.5 × 0.5 units (smaller hit-box — harder to hit)</li>
 * </ul>
 *
 * <p><strong>Flying behaviour:</strong> a flying enemy simply follows the waypoint
 * path given in its constructor and moves significantly faster than ground enemies.
 * When a level defines a separate aerial route ({@code path.flying.waypoints}),
 * {@code Game.updateSpawner()} passes that route in directly; otherwise the ground
 * path is used. No extra path field is needed — the standard {@code Enemy.path}
 * mechanism carries the route.</p>
 *
 * <p>Note: flying enemies <em>are</em> affected by the ice tower slow — they simply
 * fly slowly. The "flying" property is about terrain/path, not immunity to effects.</p>
 *
 * <p>Abstract because {@code render()} from {@code Entity} is not implemented here;
 * {@code J2dFlyingEnemy} extends this class and implements it with a visual offset to
 * simulate flying above ground level.</p>
 *
 * @author Tower Defence team
 */
public abstract class FlyingEnemy extends Enemy {

    /** Default starting/maximum HP. */
    public static final double DEFAULT_HEALTH = 60.0;
    /** Default movement speed in game-world units per second. */
    public static final double DEFAULT_SPEED  = 3.5;
    /** Default gold reward on kill. */
    public static final int    DEFAULT_REWARD = 20;
    /** Default score awarded on kill. */
    public static final int    DEFAULT_SCORE  = 15;
    /** Width and height in game-world units. */
    public static final double SIZE           = 0.5;

    /**
     * Creates a flying enemy with the given stats and path.
     *
     * @param startPosition the spawn position in game-world coordinates
     * @param health        the starting/maximum HP
     * @param speed         the movement speed in units per second
     * @param reward        the gold awarded on kill
     * @param scoreValue    the score awarded on kill
     * @param path          the ordered list of waypoints to follow (aerial or ground)
     */
    public FlyingEnemy(Position startPosition, double health, double speed,
                       int reward, int scoreValue, List<Position> path) {
        super(startPosition, SIZE, SIZE, health, speed, reward, scoreValue, path);
    }

    /**
     * Returns the type identifier {@code "flying"}, used by Lua scripts to check enemy
     * type.
     *
     * @return the string {@code "flying"}
     */
    @Override
    public String getType() {
        return "flying";
    }
}
