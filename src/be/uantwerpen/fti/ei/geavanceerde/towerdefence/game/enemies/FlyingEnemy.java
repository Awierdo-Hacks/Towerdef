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
 *   A FlyingEnemy simply follows the waypoint path it is given in its constructor
 *   and moves significantly faster than ground enemies. When a level defines a
 *   separate aerial route (path.flying.waypoints), Game.updateSpawner() passes
 *   that route in directly; otherwise the ground path is used. No extra path
 *   field is needed — the standard Enemy.path mechanism carries the route.
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

    public FlyingEnemy(Position startPosition, double health, double speed,
                       int reward, int scoreValue, List<Position> path) {
        super(startPosition, SIZE, SIZE, health, speed, reward, scoreValue, path);
    }

    @Override
    public String getType() {
        return "flying";
    }
}
