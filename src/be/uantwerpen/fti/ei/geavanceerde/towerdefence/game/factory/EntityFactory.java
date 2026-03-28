package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.GameView;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Bonus;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Obstacle;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/*
 * ABSTRACT FACTORY — interface for creating all game entities.
 *
 * This interface is the heart of the game/visualization separation:
 *   - Lives in the GAME package (pure logic, no awt imports).
 *   - Implemented in the J2D package (J2dEntityFactory).
 *   - The Game singleton only ever holds an EntityFactory reference —
 *     it never knows which concrete implementation is behind it.
 *
 * HOW IT WORKS:
 *   Main.java creates a J2dEntityFactory and passes it to Game.init().
 *   From that point on, every entity the game creates (towers, enemies,
 *   projectiles, etc.) goes through this interface. The returned objects
 *   are typed as abstract game entities (Tower, Enemy, …) so the game
 *   loop never touches J2d-specific code.
 *
 * SWAPPING VISUALIZATIONS:
 *   To replace Java2D with any other renderer, create a new class that
 *   implements EntityFactory and pass it to Game.init() instead.
 *   Zero changes to the game logic package are required.
 *
 *   Example:
 *     EntityFactory factory = new J2dEntityFactory();   // Java2D
 *     EntityFactory factory = new TerminalFactory();    // hypothetical text renderer
 *     Game.getInstance().init(factory, config);
 */
public interface EntityFactory {

    // -------------------------------------------------------------------------
    // Towers
    // -------------------------------------------------------------------------

    /* Creates a fast, low-damage arrow tower at the given position. */
    Tower createArrowTower(Position position);

    /* Creates a slow, high-damage cannon tower with splash at the given position. */
    Tower createCannonTower(Position position);

    /* Creates an ice tower that slows enemies in range at the given position. */
    Tower createIceTower(Position position);

    // -------------------------------------------------------------------------
    // Enemies
    // -------------------------------------------------------------------------

    /* Creates a standard ground enemy that follows the given waypoint path. */
    Enemy createBasicEnemy(List<Position> path);

    /* Creates a heavily armoured enemy with high HP and damage resistance. */
    Enemy createArmoredEnemy(List<Position> path);

    /* Creates a fast flying enemy that ignores certain terrain restrictions. */
    Enemy createFlyingEnemy(List<Position> path);

    // -------------------------------------------------------------------------
    // Projectiles
    // -------------------------------------------------------------------------

    /*
     * Creates a projectile fired from 'start' toward 'targetPos' dealing 'damage'.
     *
     * The game loop calls this after a tower selects its target:
     *   tower.findTarget(enemies).ifPresent(target -> {
     *       Projectile p = factory.createProjectile(
     *           tower.getPosition(), target.getPosition(), tower.getDamage());
     *       projectiles.add(p);
     *       tower.resetCooldown();
     *   });
     */
    Projectile createProjectile(Position start, Position targetPos, int damage);

    // -------------------------------------------------------------------------
    // Other entities
    // -------------------------------------------------------------------------

    /* Creates the player's base at the given position with the specified max HP. */
    Base createBase(Position position, int maxHealth);

    /* Creates an obstacle (blocks tower placement) at the given position. */
    Obstacle createObstacle(Position position);

    /* Creates a collectible bonus pickup at the given position. */
    Bonus createBonus(Position position);

    // -------------------------------------------------------------------------
    // Visualization
    // -------------------------------------------------------------------------

    /*
     * Returns the visualization layer created by this factory.
     * Game calls this in start() to get its render + input interface,
     * without ever knowing which concrete view (J2dGame, etc.) is behind it.
     */
    GameView getView();
}
