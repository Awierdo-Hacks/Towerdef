/**
 * Concrete tower logic and the {@code TowerType} dispatcher.
 *
 * <p>Holds the three abstract tower types — {@code ArrowTower} (fast, closest-target),
 * {@code CannonTower} (slow, highest-HP target, splash) and {@code IceTower} (area
 * slow) — each defining its own targeting strategy with the Java Streams API. The
 * {@code TowerType} enum maps a tower type to its factory method, keeping the game loop
 * free of {@code switch}/{@code instanceof}.</p>
 *
 * @author Tower Defence team
 */
package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers;
