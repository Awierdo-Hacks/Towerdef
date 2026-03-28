package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.map;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * An ordered sequence of waypoints that enemies follow from spawn to base.
 *
 * Enemies walk in a straight line from one waypoint to the next.
 * The first waypoint is the spawn point; the last is the base position.
 *
 * GameMap creates two Path instances per level:
 *   1. enemyPath  — ground route used by BasicEnemy and ArmoredEnemy
 *   2. flyingPath — optional aerial route for FlyingEnemy (may be null)
 *
 * The waypoint list is stored as an unmodifiable defensive copy so callers
 * cannot accidentally modify the level data at runtime.
 */
public class Path {

    // Ordered waypoints from spawn to base — immutable after construction
    private final List<Position> waypoints;

    /*
     * Creates a path from the given waypoints.
     * Makes a defensive copy so the original list can be modified freely.
     */
    public Path(List<Position> waypoints) {
        this.waypoints = new ArrayList<>(waypoints);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /*
     * Returns an unmodifiable view of the waypoints.
     * Enemy constructors receive this list and walk through it index by index.
     */
    public List<Position> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    /* First waypoint — where enemies spawn. */
    public Position getStartPosition() {
        return waypoints.get(0);
    }

    /* Last waypoint — the base the enemies are walking toward. */
    public Position getEndPosition() {
        return waypoints.get(waypoints.size() - 1);
    }

    public int getWaypointCount() {
        return waypoints.size();
    }

    public boolean isEmpty() {
        return waypoints.isEmpty();
    }

    @Override
    public String toString() {
        return "Path[" + waypoints.size() + " waypoints]";
    }
}
