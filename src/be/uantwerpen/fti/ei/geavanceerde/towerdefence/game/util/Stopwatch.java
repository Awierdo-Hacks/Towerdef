package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util;

/**
 * Measures elapsed time between game loop ticks for frame-independent movement.
 *
 * <p>Because computers run at different speeds, entities must move based on
 * <em>elapsed time</em> rather than a fixed step. By multiplying speed by
 * {@code deltaTime} (seconds since last frame), entities always move at the
 * same real-world speed regardless of the machine's frame rate.</p>
 *
 * <p>Example usage in the game loop:</p>
 * <pre>
 *   Stopwatch stopwatch = new Stopwatch();
 *   while (running) {
 *       double deltaTime = stopwatch.tick();  // seconds since last tick
 *       enemy.move(speed * deltaTime);        // consistent speed on any PC
 *   }
 * </pre>
 *
 * <p>Internally uses {@link System#nanoTime()} for high-precision timing,
 * then converts to seconds for convenient use in movement calculations.</p>
 *
 * @author TowerDefence Team
 * @version 1.0
 */
public class Stopwatch {

    /** One nanosecond expressed as a fraction of one second. */
    private static final double NANOS_TO_SECONDS = 1.0 / 1_000_000_000.0;

    /**
     * The timestamp (in nanoseconds) of the last call to {@link #tick()} or construction.
     * Used to calculate elapsed time on the next tick.
     */
    private long lastTime;

    /**
     * Creates a new Stopwatch and records the current time as the starting point.
     * The first call to {@link #tick()} will return the time since construction.
     */
    public Stopwatch() {
        this.lastTime = System.nanoTime();
    }

    /**
     * Returns the elapsed time since the last tick (or construction) in seconds,
     * and resets the internal timer to now.
     *
     * <p>Call this once per game loop iteration at the start of each frame.
     * Pass the returned value to all {@code update(deltaTime)} methods so that
     * movement, cooldowns, and timers are frame-rate independent.</p>
     *
     * @return elapsed time in seconds (e.g. 0.016 for a 60 FPS frame)
     */
    public double tick() {
        long now = System.nanoTime();
        // Convert nanosecond difference to seconds
        double deltaTime = (now - lastTime) * NANOS_TO_SECONDS;
        lastTime = now;
        return deltaTime;
    }

    /**
     * Resets the internal timer to the current moment without returning a value.
     *
     * <p>Useful when the game is paused or a level is reloaded — prevents a large
     * spike in {@code deltaTime} on the first tick after the pause ends.</p>
     */
    public void reset() {
        this.lastTime = System.nanoTime();
    }
}
