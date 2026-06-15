/**
 * Root package of the Tower Defence game.
 *
 * <p>Contains the application entry point ({@link be.uantwerpen.fti.ei.geavanceerde.towerdefence.Main})
 * and the two top-level sub-trees that enforce the project's central design rule —
 * a strict separation between game logic and game presentation:</p>
 *
 * <ul>
 *   <li>{@link be.uantwerpen.fti.ei.geavanceerde.towerdefence.game} — all game logic,
 *       free of any visualization dependency;</li>
 *   <li>{@link be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d} — the Java2D
 *       visualization and input layer.</li>
 * </ul>
 *
 * @author Tower Defence team
 */
package be.uantwerpen.fti.ei.geavanceerde.towerdefence;
