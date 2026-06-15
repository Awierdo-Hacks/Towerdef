package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game;

/**
 * Abstraction for the visualization layer, used by {@link Game} to render and read
 * input without depending on any specific graphics library (Java2D, OpenGL, etc.).
 *
 * <p>{@code J2dGame} implements this interface. The {@code Game} class only ever sees
 * {@code GameView}, never the concrete J2d classes — this enforces the
 * game/visualization separation. The game loop in {@code Game.start()} calls
 * {@link #render()} once per frame and queries input state through the other
 * methods.</p>
 *
 * <p>The {@code was...Pressed()} / {@link #wasMouseClicked()} methods follow a
 * <em>consume-once</em> pattern: they return {@code true} exactly once after the
 * event occurs, then reset to {@code false}.</p>
 *
 * @author Tower Defence team
 */
public interface GameView {

    /** Draws the current frame: map, entities, and HUD. */
    void render();

    /**
     * Returns whether the player clicked the mouse since the last query
     * (consume-once).
     *
     * @return {@code true} once per click
     */
    boolean wasMouseClicked();

    /**
     * Returns whether the player pressed the pause key since the last query
     * (consume-once).
     *
     * @return {@code true} once per press
     */
    boolean wasPausePressed();

    /**
     * Returns whether the player pressed the start/confirm key (S) since the last
     * query (consume-once).
     *
     * @return {@code true} once per press
     */
    boolean wasStartPressed();

    /**
     * Returns whether the player pressed the quit key (Q) since the last query
     * (consume-once).
     *
     * @return {@code true} once per press
     */
    boolean wasQuitPressed();

    /**
     * Returns whether the player pressed the repair key (R) since the last query
     * (consume-once).
     *
     * @return {@code true} once per press
     */
    boolean wasRepairPressed();

    /**
     * Returns the mouse X position in game-world coordinates.
     *
     * @return the mouse X in game-world units
     */
    double getMouseGameX();

    /**
     * Returns the mouse Y position in game-world coordinates.
     *
     * @return the mouse Y in game-world units
     */
    double getMouseGameY();

    /**
     * Returns the currently selected tower type ({@code 0} = none, {@code 1} = arrow,
     * {@code 2} = cannon, {@code 3} = ice).
     *
     * @return the selected tower hotkey number
     */
    int getSelectedTower();
}
