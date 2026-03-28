package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/*
 * A fast flying enemy — ignores ground terrain and follows its own aerial path.
 *
 * Stats (defaults):
 *   health    : 60   (fragile but fast)
 *   speed     : 3.5  game-world units per second
 *   reward    : 20 gold on kill
 *   scoreValue: 15 points on kill
 *   size      : 0.5 x 0.5 units  (smaller hit-box — harder to hit)
 *
 * FLYING BEHAVIOUR:
 *   In Fase 4 (Map) FlyingEnemies can be given a separate aerial path that
 *   bypasses terrain obstacles the ground path avoids. For now they use the
 *   same waypoint list as ground enemies but move significantly faster.
 *
 *   The flyingPath field is set separately from the ground path — once the map
 *   loader (Fase 4) is built it will provide dedicated aerial waypoints.
 *   Until then, flyingPath falls back to the standard ground path.
 *
 * NOTE on IceTower:
 *   FlyingEnemies ARE affected by the IceTower slow — they simply fly slowly.
 *   The "flying" property is about terrain/path, not immunity to effects.
 *
 * ABSTRACT because render() from Entity is not implemented here.
 * J2dFlyingEnemy (Fase 5) extends this and implements render() with a
 * visual offset to simulate flying above ground level.
 */
public abstract class FlyingEnemy extends Enemy {

    public static final double DEFAULT_HEALTH = 60.0;
    public static final double DEFAULT_SPEED  = 3.5;
    public static final int    DEFAULT_REWARD = 20;
    public static final int    DEFAULT_SCORE  = 15;
    public static final double SIZE           = 0.5;

    /*
     * Optional separate aerial path.
     * When not null, the flying enemy follows this path instead of the ground path.
     * Assigned by the level loader in Fase 4 if a flying path is defined.
     * Null-safe: if flyingPath is null the enemy uses the regular path from super.
     *
     * TODO (Fase 4): GameMap will provide an alternate flyingPath from level data.
     */
    protected List<Position> flyingPath;

    public FlyingEnemy(Position startPosition, double health, double speed,
                       int reward, int scoreValue, List<Position> groundPath) {
        super(startPosition, SIZE, SIZE, health, speed, reward, scoreValue, groundPath);
        this.flyingPath = null;  // uses ground path until Fase 4 provides an aerial one
    }

    // -------------------------------------------------------------------------
    // Aerial path support
    // -------------------------------------------------------------------------

    /*
     * Assigns a dedicated aerial waypoint path for this flying enemy.
     * Called by the map loader in Fase 4 after parsing the level file.
     * If never called, the enemy follows the ground path from its constructor.
     */
    public void setFlyingPath(List<Position> flyingPath) {
        if (flyingPath != null && !flyingPath.isEmpty()) {
            this.flyingPath           = flyingPath;
            this.path                 = flyingPath;  // override the path used in moveAlongPath()
            this.currentWaypointIndex = 0;
        }
    }

    @Override
    public String getType() {
        return "flying";
    }

    public boolean hasFlyingPath() {
        return flyingPath != null && !flyingPath.isEmpty();
    }
}
