package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Handles all keyboard and mouse input for the Java2D visualization.
 *
 * <p><strong>Keyboard:</strong> {@code 1}/{@code 2}/{@code 3} select a tower type
 * (Arrow / Cannon / Ice), Escape deselects, {@code P} toggles pause, {@code S} is
 * start/confirm (menu, next level, play again), {@code Q} quits, and {@code R} repairs
 * the base for gold.</p>
 *
 * <p><strong>Mouse:</strong> a left click places the selected tower at the clicked map
 * position; movement is tracked for hover effects and HUD feedback.</p>
 *
 * <p>The game loop reads input state each frame through the getter methods. Single
 * events are consumed after reading (e.g. {@link #wasMouseClicked()} returns
 * {@code true} once). This handler is attached to the {@code J2dGame} canvas in the
 * {@code J2dGame} constructor.</p>
 *
 * @author Tower Defence team
 */
public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {

    // Reference to J2dGame for coordinate conversion (screen → game world)
    private final J2dGame j2dGame;

    // -------------------------------------------------------------------------
    // Key state
    // -------------------------------------------------------------------------

    // Single-press flags — set on keyPressed, consumed when read
    private boolean pausePressed;
    private boolean startPressed;   // S — start / confirm
    private boolean quitPressed;    // Q — quit
    private boolean repairPressed;  // R — repair base

    // -------------------------------------------------------------------------
    // Mouse state
    // -------------------------------------------------------------------------

    // Current mouse position in screen pixels
    private int mouseScreenX;
    private int mouseScreenY;

    // True for one frame after the player clicks — consumed by wasMouseClicked()
    private boolean mouseClicked;

    // -------------------------------------------------------------------------
    // Tower selection
    // -------------------------------------------------------------------------

    // 0 = none, 1 = ArrowTower, 2 = CannonTower, 3 = IceTower
    private int selectedTower = 0;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    /**
     * Creates an input handler bound to the given game view, used for converting
     * screen coordinates to game-world coordinates.
     *
     * @param j2dGame the view used for screen-to-game coordinate conversion
     */
    public InputHandler(J2dGame j2dGame) {
        this.j2dGame = j2dGame;
    }

    // -------------------------------------------------------------------------
    // KeyListener
    // -------------------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Tower selection (number keys)
        switch (code) {
            case KeyEvent.VK_1: selectedTower = 1; break;  // Arrow
            case KeyEvent.VK_2: selectedTower = 2; break;  // Cannon
            case KeyEvent.VK_3: selectedTower = 3; break;  // Ice
            case KeyEvent.VK_ESCAPE: selectedTower = 0; break;  // Deselect
            case KeyEvent.VK_P: pausePressed = true; break;
            case KeyEvent.VK_S: startPressed = true; break;
            case KeyEvent.VK_Q: quitPressed  = true; break;
            case KeyEvent.VK_R: repairPressed = true; break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // not used — keyPressed handles single-press flags and tower selection
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // not used — keyPressed handles everything
    }

    // -------------------------------------------------------------------------
    // MouseListener
    // -------------------------------------------------------------------------

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseScreenX = e.getX();
            mouseScreenY = e.getY();
            mouseClicked = true;
        }
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    // -------------------------------------------------------------------------
    // MouseMotionListener
    // -------------------------------------------------------------------------

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseScreenX = e.getX();
        mouseScreenY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseScreenX = e.getX();
        mouseScreenY = e.getY();
    }

    // -------------------------------------------------------------------------
    // Getters — called by the game loop each frame
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} once after the player clicks, then resets to {@code false}.
     * The game loop calls this to detect a single click event.
     *
     * @return {@code true} exactly once per click
     */
    public boolean wasMouseClicked() {
        if (mouseClicked) {
            mouseClicked = false;
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} once after the player presses P, then resets. Used by the
     * game loop to toggle the pause state.
     *
     * @return {@code true} exactly once per press
     */
    public boolean wasPausePressed() {
        if (pausePressed) {
            pausePressed = false;
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} once after the player presses S, then resets.
     *
     * @return {@code true} exactly once per press
     */
    public boolean wasStartPressed() {
        if (startPressed) {
            startPressed = false;
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} once after the player presses Q, then resets.
     *
     * @return {@code true} exactly once per press
     */
    public boolean wasQuitPressed() {
        if (quitPressed) {
            quitPressed = false;
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} once after the player presses R, then resets.
     *
     * @return {@code true} exactly once per press
     */
    public boolean wasRepairPressed() {
        if (repairPressed) {
            repairPressed = false;
            return true;
        }
        return false;
    }

    /**
     * Returns the current mouse X position converted to game-world coordinates.
     *
     * @return the mouse X in game-world units
     */
    public double getMouseGameX() { return j2dGame.toGameX(mouseScreenX); }

    /**
     * Returns the current mouse Y position converted to game-world coordinates.
     *
     * @return the mouse Y in game-world units
     */
    public double getMouseGameY() { return j2dGame.toGameY(mouseScreenY); }

    /**
     * Returns the currently selected tower type ({@code 0} = none, {@code 1} = arrow,
     * {@code 2} = cannon, {@code 3} = ice).
     *
     * @return the selected tower hotkey number
     */
    public int getSelectedTower()             { return selectedTower; }
}
