package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/*
 * A standard ground enemy — the most common unit in most waves.
 *
 * Stats (defaults):
 *   health    : 80
 *   speed     : 2.0 game-world units per second
 *   reward    : 10 gold on kill
 *   scoreValue: 10 points on kill
 *   size      : 0.6 x 0.6 units
 *
 * No special abilities — moves along the path, takes full damage from all towers.
 * Appears in wave 1 and in every subsequent wave, in increasing numbers.
 *
 * ABSTRACT because render() from Entity is not implemented here.
 * J2dBasicEnemy (Fase 5) extends this and implements render().
 */
public abstract class BasicEnemy extends Enemy {

    public static final double DEFAULT_HEALTH     = 80.0;
    public static final double DEFAULT_SPEED      = 2.0;
    public static final int    DEFAULT_REWARD     = 10;
    public static final int    DEFAULT_SCORE      = 10;
    public static final double SIZE               = 0.6;

    public BasicEnemy(Position startPosition, double health, double speed,
                      int reward, int scoreValue, List<Position> path) {
        super(startPosition, SIZE, SIZE, health, speed, reward, scoreValue, path);
    }

    /*
     * Returns the type identifier used by Lua scripts to check enemy type.
     * Example in Lua: if enemy:getType() == "basic" then ... end
     */
    @Override
    public String getType() {
        return "basic";
    }
}
