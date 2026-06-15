package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs;

/**
 * ECS system — moves every floating text upward each frame.
 *
 * <p>Stateless logic that sweeps the {@link FloatingTextWorld}'s component arrays by
 * index. It holds no per-entity state of its own; all data lives in the world. This
 * is the data-oriented counterpart to an OOP entity's {@code update()} method.</p>
 *
 * @author Tower Defence team
 */
public class MovementSystem {

    /** Creates a movement system. The system is stateless and holds no fields. */
    public MovementSystem() {
    }

    /**
     * Advances each live entity's Y position by its upward drift speed. Y decreases
     * because screen/world Y grows downward, so subtracting moves the text up.
     *
     * @param world     the component datastore to update
     * @param deltaTime the elapsed time since the previous frame, in seconds
     */
    public void update(FloatingTextWorld world, double deltaTime) {
        for (int i = 0; i < world.count; i++) {
            world.y[i] -= world.vy[i] * deltaTime;
        }
    }
}
