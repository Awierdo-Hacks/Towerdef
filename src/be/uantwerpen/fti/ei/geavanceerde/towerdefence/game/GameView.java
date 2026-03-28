package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game;

/*
 * Abstraction for the visualization layer, used by Game to render and read input
 * without depending on any specific graphics library (Java2D, OpenGL, etc.).
 *
 * J2dGame implements this interface. The Game class only sees GameView,
 * never the concrete J2d classes — this enforces the game/visualization separation.
 *
 * The game loop in Game.start() calls render() once per frame and queries
 * input state through the other methods.
 */
public interface GameView {

    /* Draws the current frame: map, entities, HUD. */
    void render();

    /* True once after the player clicks, then resets. Consume-once pattern. */
    boolean wasMouseClicked();

    /* True once after the player presses the pause key. Consume-once pattern. */
    boolean wasPausePressed();

    /* Mouse X position in game-world coordinates. */
    double getMouseGameX();

    /* Mouse Y position in game-world coordinates. */
    double getMouseGameY();

    /* Currently selected tower type: 0=none, 1=Arrow, 2=Cannon, 3=Ice. */
    int getSelectedTower();
}
