package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/**
 * Abstract base class representing the player's base that must be defended.
 *
 * <p>The base is a stationary entity. Every time an enemy reaches it, the game loop
 * calls {@link #takeDamage(int)} — typically reducing HP by 1 per enemy. When HP
 * reaches {@code 0}, the game transitions to {@code GAME_OVER}.</p>
 *
 * <p>The {@code J2d} subclass renders the base (castle/fortress sprite or coloured
 * shape) and may also draw a health bar above it.</p>
 *
 * @author Tower Defence team
 */
public abstract class Base extends Entity {

    /** Maximum HP — stored so a health bar can display as a percentage. */
    protected int maxHealth;

    /** Current HP — when {@code 0} the game is lost. */
    protected int currentHealth;

    /**
     * Creates a base at the given position with the specified maximum HP. The base
     * starts at full health.
     *
     * @param position  the position in game-world coordinates
     * @param width     the base width in game-world units
     * @param height    the base height in game-world units
     * @param maxHealth the maximum (and starting) HP
     */
    public Base(Position position, double width, double height, int maxHealth) {
        super(position, width, height);
        this.maxHealth     = maxHealth;
        this.currentHealth = maxHealth;  // starts at full health
    }

    // -------------------------------------------------------------------------
    // Update — base does not move, nothing to update by default
    // -------------------------------------------------------------------------

    /**
     * No-op by default — the base is stationary.
     *
     * <p>Subclasses may override this for visual effects (pulsing, damage flash).</p>
     *
     * @param deltaTime the elapsed time since the previous frame, in seconds
     */
    @Override
    public void update(double deltaTime) {
        // stationary — no movement or cooldown logic needed
    }

    // -------------------------------------------------------------------------
    // Damage
    // -------------------------------------------------------------------------

    /**
     * Reduces the base's HP by the given amount. When HP reaches {@code 0} the base
     * is marked dead and the game loop triggers {@code GAME_OVER}.
     *
     * @param amount the amount of HP to remove
     */
    public void takeDamage(int amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;   // signals the game loop: trigger GAME_OVER
        }
    }

    /**
     * Repairs the base by the given amount of HP, capped at {@link #maxHealth}.
     *
     * <p>Counterpart of {@link #takeDamage(int)}: the player can buy life points back
     * for gold.</p>
     *
     * @param amount the amount of HP to restore
     */
    public void repair(int amount) {
        currentHealth = Math.min(currentHealth + amount, maxHealth);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the current HP of the base.
     *
     * @return the current health
     */
    public int    getCurrentHealth() { return currentHealth; }

    /**
     * Returns the maximum HP of the base.
     *
     * @return the maximum health
     */
    public int    getMaxHealth()     { return maxHealth; }

    /**
     * Returns the current health as a fraction of the maximum, used for health-bar
     * rendering.
     *
     * @return a value in the range {@code 0.0}–{@code 1.0}
     */
    public double getHealthPercent() { return (double) currentHealth / maxHealth; }

    /**
     * Returns whether the base has been destroyed (HP depleted).
     *
     * @return {@code true} once the base is destroyed
     */
    public boolean isDestroyed()     { return !alive; }
}
