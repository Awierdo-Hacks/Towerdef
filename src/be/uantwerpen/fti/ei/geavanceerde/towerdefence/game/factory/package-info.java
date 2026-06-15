/**
 * The Abstract Factory interface for creating game entities.
 *
 * <p>{@link be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory}
 * lives here in the game-logic package and is implemented by the visualization layer
 * ({@code J2dEntityFactory}). The {@code Game} singleton creates all entities through
 * this interface, so the game logic never references any concrete visual class.</p>
 *
 * @author Tower Defence team
 */
package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory;
