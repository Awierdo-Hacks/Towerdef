package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/*
 * Abstract base class for all enemy types.
 *
 * Enemies spawn at the map's spawn point and follow a predefined list of
 * waypoints toward the player's base. Each frame, moveAlongPath() advances
 * the enemy toward the next waypoint. When it reaches the last waypoint,
 * hasReachedBase() returns true and the game loop deducts HP from the base.
 *
 * Subclasses (BasicEnemy, ArmoredEnemy, FlyingEnemy) define their own
 * movement behaviour and any special abilities. J2d subclasses add rendering.
 *
 * Health uses doubles for precision — damage values from projectiles can be
 * fractional (e.g. splash damage distributing over an area).
 */
public abstract class Enemy extends Entity {

    // Maximum HP — stored so health bars can show a percentage
    protected double maxHealth;

    // Current HP — when this reaches 0 the enemy is destroyed
    protected double currentHealth;

    // Movement speed in game-world units per second
    protected double speed;

    // Multiplier applied on top of base speed (e.g. 1.5 when below 50% HP via Lua)
    protected double speedMultiplier;

    // Gold awarded to the player when this enemy is destroyed
    protected int reward;

    // Score points awarded when this enemy is destroyed
    protected int scoreValue;

    // The ordered list of waypoints this enemy walks toward, one by one
    protected List<Position> path;

    // Index into path[] pointing at the waypoint the enemy is currently heading for
    protected int currentWaypointIndex;

    /*
     * Sets up an enemy with its stats and the path it will follow.
     * The path must have at least one waypoint (the base position).
     */
    public Enemy(Position startPosition, double width, double height,
                 double maxHealth, double speed, int reward, int scoreValue,
                 List<Position> path) {
        super(startPosition, width, height);
        this.maxHealth            = maxHealth;
        this.currentHealth        = maxHealth;   // starts at full health
        this.speed                = speed;
        this.speedMultiplier      = 1.0;          // no modifier by default
        this.reward               = reward;
        this.scoreValue           = scoreValue;
        this.path                 = path;
        this.currentWaypointIndex = 0;
    }

    // -------------------------------------------------------------------------
    // Movement — runs each frame via Entity.update()
    // -------------------------------------------------------------------------

    /*
     * Default update: move along the path every frame.
     * Subclasses can override to add extra behaviour (e.g. ArmoredEnemy
     * triggering an ability), but should call super.update(deltaTime).
     */
    @Override
    public void update(double deltaTime) {
        moveAlongPath(deltaTime);
    }

    /*
     * Moves this enemy toward the next waypoint by (speed * speedMultiplier * deltaTime) units.
     *
     * When the enemy is close enough to the current waypoint (within one frame's
     * travel distance) it snaps to it and advances to the next one.
     * If all waypoints are consumed, hasReachedBase() will return true.
     */
    public void moveAlongPath(double deltaTime) {
        // Nothing to do if all waypoints are visited
        if (currentWaypointIndex >= path.size()) {
            return;
        }

        Position target      = path.get(currentWaypointIndex);
        double   effectiveSpeed = speed * speedMultiplier;
        double   step        = effectiveSpeed * deltaTime;
        double   distance    = position.distanceTo(target);

        if (step >= distance) {
            // Close enough — snap to waypoint and advance to the next one
            position.setX(target.getX());
            position.setY(target.getY());
            currentWaypointIndex++;
        } else {
            // Move a fraction of the way toward the target (normalised direction vector)
            double dx = (target.getX() - position.getX()) / distance;
            double dy = (target.getY() - position.getY()) / distance;
            position.setX(position.getX() + dx * step);
            position.setY(position.getY() + dy * step);
        }
    }

    // -------------------------------------------------------------------------
    // Damage handling
    // -------------------------------------------------------------------------

    /*
     * Reduces this enemy's health by the given amount.
     * If health drops to or below 0 the enemy is marked as dead (alive = false).
     * The game loop will then remove it, award gold/score, and clean up.
     */
    public void takeDamage(double amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
        }
    }

    // -------------------------------------------------------------------------
    // State queries
    // -------------------------------------------------------------------------

    /* Returns true when the enemy has walked past all waypoints (reached the base). */
    public boolean hasReachedBase() {
        return currentWaypointIndex >= path.size();
    }

    /* Health as a value between 0.0 and 1.0 — used for health bar rendering. */
    public double getHealthPercent() {
        return currentHealth / maxHealth;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public double getCurrentHealth() { return currentHealth; }
    public double getMaxHealth()     { return maxHealth; }
    public double getSpeed()         { return speed; }
    public int    getReward()        { return reward; }
    public int    getScoreValue()    { return scoreValue; }

    /* Lua scripts or special abilities use this to temporarily alter speed. */
    public void setSpeedMultiplier(double multiplier) {
        this.speedMultiplier = multiplier;
    }

    public double getSpeedMultiplier() { return speedMultiplier; }
}
