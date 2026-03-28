package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Bonus;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Obstacle;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dArrowTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dCannonTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dIceTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dBasicEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dArmoredEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dFlyingEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dProjectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dBase;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dObstacle;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dBonus;

import java.util.List;

/*
 * CONCRETE FACTORY — the Java2D implementation of EntityFactory.
 *
 * This class is the other half of the Abstract Factory pattern:
 *   EntityFactory  (interface, lives in game.factory)
 *   J2dEntityFactory (concrete, lives in j2d)
 *
 * Every create method returns a J2d* entity (which extends the abstract
 * game-logic class AND implements render() with Graphics2D drawing).
 * The return types are the abstract interfaces (Tower, Enemy, etc.) so
 * the game logic package never sees the J2d classes directly.
 *
 * WIRING:
 *   Main.java creates a J2dEntityFactory with the J2dGame reference,
 *   then passes it to Game.getInstance().init(factory, config).
 *   From that point on, every entity the game creates goes through
 *   this factory — the game logic stays visualization-free.
 *
 * J2dGame reference:
 *   Passed to every J2d entity so it can access the Graphics2D context
 *   and coordinate conversion during render().
 */
public class J2dEntityFactory implements EntityFactory {

    // The visualization layer — passed to every J2d entity for rendering
    private final J2dGame j2dGame;

    public J2dEntityFactory(J2dGame j2dGame) {
        this.j2dGame = j2dGame;
    }

    // -------------------------------------------------------------------------
    // Towers
    // -------------------------------------------------------------------------

    @Override
    public Tower createArrowTower(Position position) {
        return new J2dArrowTower(position, j2dGame);
    }

    @Override
    public Tower createCannonTower(Position position) {
        return new J2dCannonTower(position, j2dGame);
    }

    @Override
    public Tower createIceTower(Position position) {
        return new J2dIceTower(position, j2dGame);
    }

    // -------------------------------------------------------------------------
    // Enemies
    // -------------------------------------------------------------------------

    /*
     * The path's first waypoint is used as the start position.
     * The enemy walks along the entire path toward the base.
     */
    @Override
    public Enemy createBasicEnemy(List<Position> path) {
        return new J2dBasicEnemy(path, j2dGame);
    }

    @Override
    public Enemy createArmoredEnemy(List<Position> path) {
        return new J2dArmoredEnemy(path, j2dGame);
    }

    @Override
    public Enemy createFlyingEnemy(List<Position> path) {
        return new J2dFlyingEnemy(path, j2dGame);
    }

    // -------------------------------------------------------------------------
    // Projectiles
    // -------------------------------------------------------------------------

    @Override
    public Projectile createProjectile(Position start, Position targetPos, int damage) {
        return new J2dProjectile(start, targetPos, damage, j2dGame);
    }

    // -------------------------------------------------------------------------
    // Other entities
    // -------------------------------------------------------------------------

    @Override
    public Base createBase(Position position, int maxHealth) {
        return new J2dBase(position, maxHealth, j2dGame);
    }

    @Override
    public Obstacle createObstacle(Position position) {
        return new J2dObstacle(position, j2dGame);
    }

    @Override
    public Bonus createBonus(Position position) {
        return new J2dBonus(position, j2dGame);
    }
}
