package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ordered sequence of waypoints that enemies follow from spawn to base.
 *
 * <p>Enemies walk in a straight line from one waypoint to the next. The first
 * waypoint is the spawn point; the last is the base position.</p>
 *
 * <p>{@code GameMap} creates up to two {@code Path} instances per level: a ground
 * route used by {@code BasicEnemy} and {@code ArmoredEnemy}, and an optional aerial
 * route for {@code FlyingEnemy}. The waypoint list is stored as an unmodifiable
 * defensive copy so callers cannot accidentally modify the level data at runtime.</p>
 *
 * @author Tower Defence team
 */
public class Path {

    /** Ordered waypoints from spawn to base — immutable after construction. */
    private final List<Position> waypoints;

    /**
     * Creates a path from the given waypoints, taking a defensive copy so the original
     * list can be modified freely afterwards.
     *
     * @param waypoints the ordered waypoints from spawn to base
     */
    public Path(List<Position> waypoints) {
        this.waypoints = new ArrayList<>(waypoints);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns an unmodifiable view of the waypoints. Enemy constructors receive this
     * list and walk through it index by index.
     *
     * @return an unmodifiable list of waypoints from spawn to base
     */
    public List<Position> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }
}
