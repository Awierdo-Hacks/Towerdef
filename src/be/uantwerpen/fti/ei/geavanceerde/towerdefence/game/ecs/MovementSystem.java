package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs;

/*
 * ECS SYSTEM — moves every floating text upward each frame.
 *
 * Stateless logic that sweeps the FloatingTextWorld's component arrays by index.
 * It holds no per-entity state of its own; all data lives in the world. This is
 * the data-oriented counterpart to an OOP entity's update() method.
 */
public class MovementSystem {

    /*
     * Advances each live entity's Y position by its upward drift speed.
     * Y decreases because screen/world Y grows downward, so subtracting moves it up.
     */
    public void update(FloatingTextWorld world, double deltaTime) {
        for (int i = 0; i < world.count; i++) {
            world.y[i] -= world.vy[i] * deltaTime;
        }
    }
}
