/**
 * Java2D visualization and input layer.
 *
 * <p>{@code J2dGame} is the concrete {@code GameView}: it owns the window, the render
 * pipeline, and game-world ↔ pixel coordinate conversion. {@code J2dEntityFactory} is
 * the concrete Abstract Factory producing the visual entities in {@code j2d.entities};
 * {@code InputHandler} handles keyboard and mouse input; {@code SpriteManager} loads
 * and caches sprite images. This package depends on the game logic, never the reverse.</p>
 *
 * @author Tower Defence team
 */
package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d;
