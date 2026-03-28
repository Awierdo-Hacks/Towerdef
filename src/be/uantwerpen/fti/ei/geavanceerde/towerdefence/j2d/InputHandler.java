package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/*
 * Handles all keyboard and mouse input for the Java2D visualization.
 *
 * KEYBOARD:
 *   1 / 2 / 3  — select tower type (Arrow / Cannon / Ice)
 *   Escape      — deselect tower
 *   P           — toggle pause
 *
 * MOUSE:
 *   Left click  — place the selected tower at the clicked map position
 *   Movement    — tracked for hover effects and HUD feedback
 *
 * The game loop reads input state each frame through the getter methods.
 * Mouse clicks are consumed after reading (wasMouseClicked() returns true once).
 *
 * Attached to the J2dGame canvas in J2dGame's constructor.
 */
public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {

    // Reference to J2dGame for coordinate conversion (screen → game world)
    private J2dGame j2dGame;

    // -------------------------------------------------------------------------
    // Key state
    // -------------------------------------------------------------------------

    // Tracks which keys are currently held down
    private boolean[] keys = new boolean[256];

    // Single-press flags — set on keyPressed, consumed when read
    private boolean pausePressed;

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

    public InputHandler(J2dGame j2dGame) {
        this.j2dGame = j2dGame;
    }

    // -------------------------------------------------------------------------
    // KeyListener
    // -------------------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = true;
        }

        // Tower selection (number keys)
        switch (code) {
            case KeyEvent.VK_1: selectedTower = 1; break;  // Arrow
            case KeyEvent.VK_2: selectedTower = 2; break;  // Cannon
            case KeyEvent.VK_3: selectedTower = 3; break;  // Ice
            case KeyEvent.VK_ESCAPE: selectedTower = 0; break;  // Deselect
            case KeyEvent.VK_P: pausePressed = true; break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = false;
        }
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

    /* True if a key is currently held down. */
    public boolean isKeyDown(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) return false;
        return keys[keyCode];
    }

    /*
     * Returns true once after the player clicks, then resets to false.
     * The game loop calls this to detect a single click event.
     */
    public boolean wasMouseClicked() {
        if (mouseClicked) {
            mouseClicked = false;
            return true;
        }
        return false;
    }

    /*
     * Returns true once after the player presses P, then resets.
     * Used by the game loop to toggle pause state.
     */
    public boolean wasPausePressed() {
        if (pausePressed) {
            pausePressed = false;
            return true;
        }
        return false;
    }

    /* Mouse position in screen pixels. */
    public int getMouseScreenX() { return mouseScreenX; }
    public int getMouseScreenY() { return mouseScreenY; }

    /* Mouse position converted to game-world coordinates. */
    public double getMouseGameX() { return j2dGame.toGameX(mouseScreenX); }
    public double getMouseGameY() { return j2dGame.toGameY(mouseScreenY); }

    /* Currently selected tower type: 0=none, 1=Arrow, 2=Cannon, 3=Ice. */
    public int getSelectedTower()             { return selectedTower; }
    public void setSelectedTower(int tower)   { this.selectedTower = tower; }
}
