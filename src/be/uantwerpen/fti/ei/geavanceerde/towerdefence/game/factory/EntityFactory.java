package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.GameView;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/**
 * Abstract Factory interface for creating all game entities.
 *
 * <p>This interface is the heart of the game/visualization separation:</p>
 * <ul>
 *   <li>It lives in the <strong>game</strong> package (pure logic, no awt imports).</li>
 *   <li>It is implemented in the <strong>j2d</strong> package ({@code J2dEntityFactory}).</li>
 *   <li>The {@code Game} singleton only ever holds an {@code EntityFactory} reference —
 *       it never knows which concrete implementation is behind it.</li>
 * </ul>
 *
 * <p><strong>How it works:</strong> {@code Main} creates a {@code J2dEntityFactory}
 * and passes it to {@code Game.start()}. From that point on, every entity the game
 * creates (towers, enemies, projectiles, etc.) goes through this interface. The
 * returned objects are typed as abstract game entities ({@code Tower}, {@code Enemy},
 * …) so the game loop never touches J2d-specific code.</p>
 *
 * <p><strong>Swapping visualizations:</strong> to replace Java2D with any other
 * renderer, create a new class that implements {@code EntityFactory} and pass it to
 * {@code Game.start()} instead. Zero changes to the game logic package are required.</p>
 *
 * @author Tower Defence team
 */
public interface EntityFactory {

    // -------------------------------------------------------------------------
    // Towers
    // -------------------------------------------------------------------------

    /**
     * Creates a fast, low-damage arrow tower at the given position.
     *
     * @param position the build position in game-world coordinates
     * @return the newly created tower
     */
    Tower createArrowTower(Position position);

    /**
     * Creates a slow, high-damage cannon tower with splash at the given position.
     *
     * @param position the build position in game-world coordinates
     * @return the newly created tower
     */
    Tower createCannonTower(Position position);

    /**
     * Creates an ice tower that slows enemies in range at the given position.
     *
     * @param position the build position in game-world coordinates
     * @return the newly created tower
     */
    Tower createIceTower(Position position);

    // -------------------------------------------------------------------------
    // Enemies
    // -------------------------------------------------------------------------

    /**
     * Creates a standard ground enemy that follows the given waypoint path.
     *
     * @param path the ordered list of waypoints the enemy will follow
     * @return the newly created enemy
     */
    Enemy createBasicEnemy(List<Position> path);

    /**
     * Creates a heavily armoured enemy with high HP and damage resistance.
     *
     * @param path the ordered list of waypoints the enemy will follow
     * @return the newly created enemy
     */
    Enemy createArmoredEnemy(List<Position> path);

    /**
     * Creates a fast flying enemy that ignores certain terrain restrictions.
     *
     * @param path the ordered list of waypoints the enemy will follow
     * @return the newly created enemy
     */
    Enemy createFlyingEnemy(List<Position> path);

    // -------------------------------------------------------------------------
    // Projectiles
    // -------------------------------------------------------------------------

    /**
     * Creates a single-target ray projectile fired from {@code start} toward
     * {@code targetPos} dealing {@code damage}. Used by the {@code ArrowTower}.
     *
     * @param start     the position the projectile is fired from
     * @param targetPos the world position the projectile flies toward
     * @param damage    the damage dealt on direct hit
     * @return the newly created projectile
     */
    Projectile createRayProjectile(Position start, Position targetPos, int damage);

    /**
     * Creates a cannon projectile that, on impact, deals direct damage to its primary
     * target <em>and</em> splash damage to all other enemies within
     * {@code splashRadius}. The splash logic lives in the projectile
     * ({@code CannonProjectile.onHit}), not in the game loop.
     *
     * @param start        the position the projectile is fired from
     * @param targetPos    the world position the projectile flies toward
     * @param damage       the direct-hit damage
     * @param splashRadius the splash radius in game-world units
     * @param splashDamage the damage applied to other enemies within the splash radius
     * @return the newly created projectile
     */
    Projectile createCannonProjectile(Position start, Position targetPos, int damage,
                                      double splashRadius, int splashDamage);

    // -------------------------------------------------------------------------
    // Other entities
    // -------------------------------------------------------------------------

    /**
     * Creates the player's base at the given position with the specified maximum HP.
     *
     * @param position  the position in game-world coordinates
     * @param maxHealth the maximum (and starting) HP of the base
     * @return the newly created base
     */
    Base createBase(Position position, int maxHealth);

    // -------------------------------------------------------------------------
    // Visualization
    // -------------------------------------------------------------------------

    /**
     * Returns the visualization layer created by this factory. {@code Game} calls this
     * in {@code start()} to obtain its render + input interface, without ever knowing
     * which concrete view ({@code J2dGame}, etc.) is behind it.
     *
     * @return the render/input abstraction for this factory
     */
    GameView getView();
}
