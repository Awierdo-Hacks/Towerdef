package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/*
 * Abstract base class representing the player's base that must be defended.
 *
 * The base is a stationary entity. Every time an enemy reaches it, the game loop
 * calls takeDamage() — typically reducing HP by 1 per enemy. When HP reaches 0,
 * the game transitions to GAME_OVER.
 *
 * The J2d subclass renders the base (castle/fortress sprite or coloured shape)
 * and may also draw a health bar above it.
 */
public abstract class Base extends Entity {

    // Maximum HP — stored so a health bar can display as a percentage
    protected int maxHealth;

    // Current HP — when 0 the game is lost
    protected int currentHealth;

    /*
     * Creates a base at the given position with the specified max HP.
     */
    public Base(Position position, double width, double height, int maxHealth) {
        super(position, width, height);
        this.maxHealth     = maxHealth;
        this.currentHealth = maxHealth;  // starts at full health
    }

    // -------------------------------------------------------------------------
    // Update — base does not move, nothing to update by default
    // -------------------------------------------------------------------------

    /*
     * The base is stationary so update() is a no-op by default.
     * Subclasses may override this for visual effects (pulsing, damage flash).
     */
    @Override
    public void update(double deltaTime) {
        // stationary — no movement or cooldown logic needed
    }

    // -------------------------------------------------------------------------
    // Damage
    // -------------------------------------------------------------------------

    /*
     * Reduces the base's HP by the given amount.
     * When HP reaches 0 the base is marked dead and the game loop triggers GAME_OVER.
     */
    public void takeDamage(int amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;   // signals the game loop: trigger GAME_OVER
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int    getCurrentHealth() { return currentHealth; }
    public int    getMaxHealth()     { return maxHealth; }

    /* Health as a 0.0–1.0 fraction — used for health bar rendering. */
    public double getHealthPercent() { return (double) currentHealth / maxHealth; }

    public boolean isDestroyed()     { return !alive; }
}
