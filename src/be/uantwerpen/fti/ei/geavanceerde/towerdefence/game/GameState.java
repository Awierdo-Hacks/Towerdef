package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game;

/**
 * Represents the possible states of the Tower Defence game.
 *
 * <p>The {@link Game} singleton holds a {@code GameState} at all times.
 * The game loop and UI rendering decisions are driven by the current state.
 * Transitions between states are managed by {@link Game#setState(GameState)}.</p>
 *
 * <p>Typical state flow:</p>
 * <pre>
 *   MENU → PLAYING → PAUSED → PLAYING → GAME_OVER
 *                                     → WON
 * </pre>
 *
 * @author TowerDefence Team
 * @version 1.0
 * @see Game
 */
public enum GameState {

    /**
     * The main menu screen is shown.
     * The game loop is not running. Waiting for the player to start.
     */
    MENU,

    /**
     * The game is actively running.
     * The main update/render loop ticks every frame, enemies move,
     * towers fire, and input is processed.
     */
    PLAYING,

    /**
     * The game is temporarily paused.
     * The update loop is frozen but the screen is still rendered.
     * The stopwatch should be reset when unpausing to avoid a deltaTime spike.
     */
    PAUSED,

    /**
     * The player has lost — the base reached 0 HP.
     * The game loop stops and the game-over screen is displayed.
     */
    GAME_OVER,

    /**
     * The player has won — all enemy waves have been defeated.
     * The game loop stops and the victory screen is displayed.
     */
    WON
}
