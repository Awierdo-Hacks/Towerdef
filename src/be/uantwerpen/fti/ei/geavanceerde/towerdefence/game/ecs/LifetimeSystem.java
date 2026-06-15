package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs;

/**
 * ECS system — ages every floating text and removes the expired ones.
 *
 * <p>Stateless logic that sweeps the {@link FloatingTextWorld}'s component arrays by
 * index. Removal uses "swap-remove": the last live entity is copied into the freed
 * slot and the count shrinks. This keeps the live entities packed in
 * {@code [0, count)} with O(1) removal and no gaps — a classic data-oriented
 * compaction technique.</p>
 *
 * @author Tower Defence team
 */
public class LifetimeSystem {

    /** Creates a lifetime system. The system is stateless and holds no fields. */
    public LifetimeSystem() {
    }

    /**
     * Ages each live entity by {@code deltaTime} and removes any whose age has reached
     * its lifetime.
     *
     * @param world     the component datastore to update
     * @param deltaTime the elapsed time since the previous frame, in seconds
     */
    public void update(FloatingTextWorld world, double deltaTime) {
        for (int i = 0; i < world.count; i++) {
            world.age[i] += deltaTime;

            if (world.age[i] >= world.lifetime[i]) {
                removeBySwap(world, i);
                i--;   // re-check the slot we just swapped into
            }
        }
    }

    /* Moves the last live entity into slot i and shrinks the live range by one. */
    private void removeBySwap(FloatingTextWorld w, int i) {
        int last = w.count - 1;
        if (i != last) {
            w.x[i]        = w.x[last];
            w.y[i]        = w.y[last];
            w.vy[i]       = w.vy[last];
            w.age[i]      = w.age[last];
            w.lifetime[i] = w.lifetime[last];
            w.value[i]    = w.value[last];
            w.kind[i]     = w.kind[last];
        }
        w.kind[last] = null;   // release the reference in the freed slot
        w.count--;
    }
}
