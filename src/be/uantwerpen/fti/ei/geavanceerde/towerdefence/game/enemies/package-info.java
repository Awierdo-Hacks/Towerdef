/**
 * Concrete enemy logic and the {@code EnemyType} dispatcher.
 *
 * <p>Holds the three abstract enemy types — {@code BasicEnemy} (standard ground unit),
 * {@code ArmoredEnemy} (high HP with damage resistance) and {@code FlyingEnemy} (fast,
 * aerial path) — plus the {@code EnemyType} enum that maps a wave-config string to its
 * factory method and records whether the type prefers the air path.</p>
 *
 * @author Tower Defence team
 */
package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies;
