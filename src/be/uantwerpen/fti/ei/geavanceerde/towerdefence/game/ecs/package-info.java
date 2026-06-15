/**
 * Data-oriented Entity-Component-System subsystem for floating combat text.
 *
 * <p>This is the project's required ECS system, built in a Structure-of-Arrays style:
 * {@code FloatingTextWorld} stores component data in parallel arrays, while the
 * stateless {@code MovementSystem} and {@code LifetimeSystem} sweep that data each
 * frame. {@code FloatingTextKind} is the semantic category the game supplies; the
 * renderer decides the colour and label.</p>
 *
 * @author Tower Defence team
 */
package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs;
