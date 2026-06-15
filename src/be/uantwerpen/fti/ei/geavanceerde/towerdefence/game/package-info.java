/**
 * Core game logic — the central {@link be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.Game}
 * singleton, the {@link be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.GameState}
 * enum, and the {@link be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.GameView}
 * abstraction.
 *
 * <p>This package and its sub-packages contain <strong>no</strong> visualization code:
 * no {@code java.awt} or {@code javax.swing} imports. Rendering and input reach the
 * game logic only through the {@link be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.GameView}
 * interface and the Abstract Factory in
 * {@link be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory}, so the same
 * logic could drive a completely different renderer.</p>
 *
 * @author Tower Defence team
 */
package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game;
