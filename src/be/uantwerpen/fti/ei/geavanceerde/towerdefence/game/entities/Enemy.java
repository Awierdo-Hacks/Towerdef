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
 *
 * SLOW MECHANIC:
 *   IceTower calls applySlow(factor, duration) to temporarily reduce an enemy's
 *   speed. The slowTimer ticks down each frame; when it expires, speedMultiplier
 *   resets to 1.0 automatically. Multiple IceTowers refreshing the timer is safe.
 */
public abstract class Enemy extends Entity {

    // Maximum HP — stored so health bars can show a percentage
    protected double maxHealth;

    // Current HP — when this reaches 0 the enemy is destroyed
    protected double currentHealth;

    // Base movement speed in game-world units per second
    protected double speed;

    // Multiplier on top of base speed — set by IceTower slow or Lua scripts
    protected double speedMultiplier;

    // Remaining seconds of slow effect; when it hits 0 the slow expires
    protected double slowTimer;

    // Gold awarded to the player when this enemy is destroyed
    protected int reward;

    // Score points awarded when this enemy is destroyed
    protected int scoreValue;

    // The ordered list of waypoints this enemy walks toward, one by one
    protected List<Position> path;

    // Index into path pointing at the next waypoint to walk toward
    protected int currentWaypointIndex;

    /*
     * Sets up an enemy with its stats and the path it will follow.
     * The path must have at least one waypoint (the base position at the end).
     */
    public Enemy(Position startPosition, double width, double height,
                 double maxHealth, double speed, int reward, int scoreValue,
                 List<Position> path) {
        super(startPosition, width, height);
        this.maxHealth            = maxHealth;
        this.currentHealth        = maxHealth;
        this.speed                = speed;
        this.speedMultiplier      = 1.0;
        this.slowTimer            = 0.0;
        this.reward               = reward;
        this.scoreValue           = scoreValue;
        this.path                 = path;
        this.currentWaypointIndex = 0;
    }

    // -------------------------------------------------------------------------
    // Update — move + tick slow timer
    // -------------------------------------------------------------------------

    /*
     * Each frame: tick the slow timer down, reset speedMultiplier when it expires,
     * then move along the path.
     *
     * Subclasses can override but should call super.update(deltaTime) first.
     */
    @Override
    public void update(double deltaTime) {
        // Tick the slow timer; reset speed when the effect expires
        if (slowTimer > 0) {
            slowTimer -= deltaTime;
            if (slowTimer <= 0) {
                slowTimer        = 0;
                speedMultiplier  = 1.0;  // slow expired, return to full speed
            }
        }
        moveAlongPath(deltaTime);
    }

    /*
     * Moves this enemy toward the next waypoint by (speed * speedMultiplier * deltaTime) units.
     *
     * When the enemy is close enough to the current waypoint it snaps to it and
     * advances to the next index. If all waypoints are consumed, hasReachedBase()
     * returns true.
     */
    public void moveAlongPath(double deltaTime) {
        if (currentWaypointIndex >= path.size()) {
            return;
        }

        Position target       = path.get(currentWaypointIndex);
        double effectiveSpeed = speed * speedMultiplier;
        double step           = effectiveSpeed * deltaTime;
        double distance       = position.distanceTo(target);

        if (step >= distance) {
            // Reached waypoint — snap and advance
            position.setX(target.getX());
            position.setY(target.getY());
            currentWaypointIndex++;
        } else {
            // Move a partial step in the direction of the target
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
     * Reduces HP by the given amount.
     * ArmoredEnemy overrides this to apply a damage resistance multiplier.
     * When HP reaches 0 the entity is marked dead and the game loop removes it.
     */
    public void takeDamage(double amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
        }
    }

    // -------------------------------------------------------------------------
    // Slow effect — applied by IceTower each frame
    // -------------------------------------------------------------------------

    /*
     * Applies a temporary speed reduction to this enemy.
     *
     * slowFactor: multiplier for speed (e.g. 0.5 = half speed)
     * duration:   seconds the slow lasts before expiring automatically
     *
     * If the enemy is already slowed, the timer is refreshed (not stacked).
     * This is called each frame by IceTower.applyAreaEffect() for enemies in range.
     */
    public void applySlow(double slowFactor, double duration) {
        this.speedMultiplier = slowFactor;
        this.slowTimer       = duration;  // refresh timer each frame the enemy is in range
    }

    // -------------------------------------------------------------------------
    // Type identifier — used by Lua scripts (e.g. enemy:getType() == "armored")
    // -------------------------------------------------------------------------

    /*
     * Returns a string identifier for this enemy type.
     * Subclasses return "basic", "armored", or "flying".
     * Used by the Lua script engine to apply type-specific behaviour.
     */
    public abstract String getType();

    // -------------------------------------------------------------------------
    // State queries
    // -------------------------------------------------------------------------

    /* True when the enemy has walked past all waypoints and reached the base. */
    public boolean hasReachedBase() {
        return currentWaypointIndex >= path.size();
    }

    /* Health fraction 0.0–1.0, used for health bar rendering. */
    public double getHealthPercent() {
        return currentHealth / maxHealth;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public double getCurrentHealth()  { return currentHealth; }
    public double getMaxHealth()      { return maxHealth; }
    public double getSpeed()          { return speed; }
    public double getSpeedMultiplier(){ return speedMultiplier; }
    public int    getReward()         { return reward; }
    public int    getScoreValue()     { return scoreValue; }

    /* Lua scripts can call this to directly override the speed multiplier. */
    public void setSpeedMultiplier(double multiplier) {
        this.speedMultiplier = multiplier;
    }
}
