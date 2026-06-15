package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/**
 * Abstract base class for all enemy types.
 *
 * <p>Enemies spawn at the map's spawn point and follow a predefined list of
 * waypoints toward the player's base. Each frame, {@link #moveAlongPath(double)}
 * advances the enemy toward the next waypoint. When it reaches the last waypoint,
 * {@link #hasReachedBase()} returns {@code true} and the game loop deducts HP from
 * the base.</p>
 *
 * <p>Subclasses ({@code BasicEnemy}, {@code ArmoredEnemy}, {@code FlyingEnemy})
 * define their own movement behaviour and any special abilities; {@code J2d}
 * subclasses add rendering.</p>
 *
 * <p>Health uses doubles for precision — damage values from projectiles can be
 * fractional (e.g. splash damage distributing over an area).</p>
 *
 * <p><strong>Slow mechanic:</strong> {@code IceTower} calls
 * {@link #applySlow(double, double)} to temporarily reduce an enemy's speed. The
 * slow timer ticks down each frame; when it expires, the speed multiplier resets to
 * {@code 1.0} automatically. Multiple {@code IceTower}s refreshing the timer is safe.</p>
 *
 * @author Tower Defence team
 */
public abstract class Enemy extends Entity {

    /** Maximum HP — the starting health and the basis for the health fraction. */
    protected double maxHealth;

    /** Current HP — when this reaches {@code 0} the enemy is destroyed. */
    protected double currentHealth;

    /** Base movement speed in game-world units per second. */
    protected double speed;

    /** Multiplier on top of the base speed — set by {@code IceTower} slow or Lua scripts. */
    protected double speedMultiplier;

    /** Remaining seconds of the slow effect; when it hits {@code 0} the slow expires. */
    protected double slowTimer;

    /** Gold awarded to the player when this enemy is destroyed. */
    protected int reward;

    /** Score points awarded when this enemy is destroyed. */
    protected int scoreValue;

    /** The ordered list of waypoints this enemy walks toward, one by one. */
    protected List<Position> path;

    /** Index into {@link #path} pointing at the next waypoint to walk toward. */
    protected int currentWaypointIndex;

    /**
     * Sets up an enemy with its stats and the path it will follow. The path must
     * have at least one waypoint (the base position at the end).
     *
     * @param startPosition the spawn position in game-world coordinates
     * @param width         the enemy width in game-world units
     * @param height        the enemy height in game-world units
     * @param maxHealth     the maximum (and starting) HP
     * @param speed         the base movement speed in units per second
     * @param reward        the gold awarded when this enemy dies
     * @param scoreValue    the score awarded when this enemy dies
     * @param path          the ordered list of waypoints to follow
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

    /**
     * Advances the enemy by one frame: ticks the slow timer down, resets the speed
     * multiplier when it expires, then moves along the path.
     *
     * <p>Subclasses may override but should call {@code super.update(deltaTime)} first.</p>
     *
     * @param deltaTime the elapsed time since the previous frame, in seconds
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

    /**
     * Moves this enemy toward the next waypoint by
     * {@code speed * speedMultiplier * deltaTime} units.
     *
     * <p>When the enemy is close enough to the current waypoint it snaps to it and
     * advances to the next index. If all waypoints are consumed,
     * {@link #hasReachedBase()} returns {@code true}.</p>
     *
     * @param deltaTime the elapsed time since the previous frame, in seconds
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

    /**
     * Reduces HP by the given amount.
     *
     * <p>{@code ArmoredEnemy} overrides this to apply a damage-resistance multiplier.
     * When HP reaches {@code 0} the entity is marked dead and the game loop removes it.</p>
     *
     * @param amount the amount of damage to apply
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

    /**
     * Applies a temporary speed reduction to this enemy.
     *
     * <p>If the enemy is already slowed, the timer is refreshed (not stacked). This
     * is called each frame by {@code IceTower.applyAreaEffect()} for enemies in range.</p>
     *
     * @param slowFactor the speed multiplier (e.g. {@code 0.5} for half speed)
     * @param duration   the number of seconds the slow lasts before expiring automatically
     */
    public void applySlow(double slowFactor, double duration) {
        this.speedMultiplier = slowFactor;
        this.slowTimer       = duration;  // refresh timer each frame the enemy is in range
    }

    // -------------------------------------------------------------------------
    // Type identifier — used by Lua scripts (e.g. enemy:getType() == "armored")
    // -------------------------------------------------------------------------

    /**
     * Returns a string identifier for this enemy type.
     *
     * <p>Subclasses return {@code "basic"}, {@code "armored"}, or {@code "flying"}.
     * Used by the Lua script engine to apply type-specific behaviour.</p>
     *
     * @return the enemy type identifier
     */
    public abstract String getType();

    // -------------------------------------------------------------------------
    // State queries
    // -------------------------------------------------------------------------

    /**
     * Returns whether the enemy has walked past all waypoints and reached the base.
     *
     * @return {@code true} once the final waypoint has been consumed
     */
    public boolean hasReachedBase() {
        return currentWaypointIndex >= path.size();
    }

    /**
     * Returns the current health as a fraction of the maximum.
     *
     * @return a value in the range {@code 0.0}–{@code 1.0}
     */
    public double getHealthPercent() {
        return currentHealth / maxHealth;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    /**
     * Returns the current HP of this enemy.
     *
     * @return the current health
     */
    public double getCurrentHealth()  { return currentHealth; }

    /**
     * Returns the maximum HP of this enemy.
     *
     * @return the maximum health
     */
    public double getMaxHealth()      { return maxHealth; }

    /**
     * Returns the current speed multiplier (e.g. reduced while slowed).
     *
     * @return the speed multiplier
     */
    public double getSpeedMultiplier(){ return speedMultiplier; }

    /**
     * Returns the gold awarded to the player when this enemy is destroyed.
     *
     * @return the gold reward
     */
    public int    getReward()         { return reward; }

    /**
     * Returns the score awarded to the player when this enemy is destroyed.
     *
     * @return the score value
     */
    public int    getScoreValue()     { return scoreValue; }

    /**
     * Directly overrides the speed multiplier — callable from Lua scripts.
     *
     * @param multiplier the new speed multiplier
     */
    public void setSpeedMultiplier(double multiplier) {
        this.speedMultiplier = multiplier;
    }

    /**
     * Directly sets the current HP (clamped to {@code 0..maxHealth}) — callable from
     * Lua scripts. The enemy is marked dead when the result is {@code 0}.
     *
     * @param health the requested HP value
     */
    public void setCurrentHealth(double health) {
        this.currentHealth = Math.max(0, Math.min(health, maxHealth));
        if (this.currentHealth <= 0) alive = false;
    }

    /**
     * Changes the maximum HP of this enemy — callable from Lua scripts. The current
     * HP is clamped if it now exceeds the new maximum.
     *
     * @param health the new maximum HP (at least {@code 1})
     */
    public void setMaxHealth(double health) {
        this.maxHealth = Math.max(1, health);
        // Clamp current HP in case it now exceeds the new max
        if (currentHealth > this.maxHealth) currentHealth = this.maxHealth;
    }
}
