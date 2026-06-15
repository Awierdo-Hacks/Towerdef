package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.GameView;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Projectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dArrowTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dCannonTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dIceTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dBasicEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dArmoredEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dFlyingEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dRayProjectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dCannonProjectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities.J2dBase;

import java.util.List;

/**
 * Concrete factory — the Java2D implementation of {@link EntityFactory}.
 *
 * <p>This class is the other half of the Abstract Factory pattern:
 * {@code EntityFactory} is the interface (in {@code game.factory}) and
 * {@code J2dEntityFactory} is the concrete implementation (in {@code j2d}).</p>
 *
 * <p>Responsibilities: it creates the {@code J2dGame} window (the visualization layer)
 * from the config; every {@code create} method returns a {@code J2d*} entity (which
 * extends an abstract game-logic class and implements {@code render()} with
 * {@code Graphics2D}) typed as the abstract interface so game logic never sees J2d; and
 * {@link #getView()} provides the {@link GameView} interface to {@code Game}, keeping
 * the game/visualization separation intact.</p>
 *
 * <p>Wiring: {@code Main} creates a {@code J2dEntityFactory(config)} and passes it to
 * {@code Game.start()}. The factory builds the window internally and provides all
 * entity creation; the game logic only sees {@code EntityFactory} and {@code GameView}.</p>
 *
 * @author Tower Defence team
 */
public class J2dEntityFactory implements EntityFactory {

    /** The visualization layer — created in the constructor, passed to all J2d entities. */
    private final J2dGame j2dGame;

    /**
     * Creates the Java2D visualization (JFrame + Canvas) and prepares the factory for
     * entity creation. The config is used for window size, game-world dimensions,
     * title, etc.
     *
     * @param config the loaded game configuration
     */
    public J2dEntityFactory(ConfigManager config) {
        this.j2dGame = new J2dGame(config);
    }

    // -------------------------------------------------------------------------
    // GameView — visualization access for the Game singleton
    // -------------------------------------------------------------------------

    @Override
    public GameView getView() {
        return j2dGame;
    }

    // -------------------------------------------------------------------------
    // Towers
    // -------------------------------------------------------------------------

    @Override
    public Tower
    createArrowTower(Position position) {
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
    public Projectile createRayProjectile(Position start, Position targetPos, int damage) {
        return new J2dRayProjectile(start, targetPos, damage, j2dGame);
    }

    @Override
    public Projectile createCannonProjectile(Position start, Position targetPos, int damage,
                                             double splashRadius, int splashDamage) {
        return new J2dCannonProjectile(start, targetPos, damage, splashRadius, splashDamage, j2dGame);
    }

    // -------------------------------------------------------------------------
    // Other entities
    // -------------------------------------------------------------------------

    @Override
    public Base createBase(Position position, int maxHealth) {
        return new J2dBase(position, maxHealth, j2dGame);
    }
}
