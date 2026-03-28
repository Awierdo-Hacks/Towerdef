package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/*
 * Abstract base class for map obstacles.
 *
 * Obstacles are stationary objects placed on the map that block tower placement.
 * The GameMap checks a tile's obstacle flag before allowing a tower to be placed.
 *
 * Obstacles do not move and do not interact with enemies directly.
 * They are visual elements that add variety to the map layout.
 *
 * The J2d subclass renders obstacles as rocks, trees, ruins, etc.
 */
public abstract class Obstacle extends Entity {

    /*
     * Creates an obstacle at the given position with the given size.
     */
    public Obstacle(Position position, double width, double height) {
        super(position, width, height);
    }

    // -------------------------------------------------------------------------
    // Update — obstacles are static, nothing to update
    // -------------------------------------------------------------------------

    /*
     * Obstacles don't move or change state, so update() is a no-op.
     */
    @Override
    public void update(double deltaTime) {
        // static object — nothing to update
    }
}
