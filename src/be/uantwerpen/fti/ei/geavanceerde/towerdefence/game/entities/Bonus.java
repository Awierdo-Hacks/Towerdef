package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/*
 * Abstract base class for bonus pickups that appear on the map during play.
 *
 * Bonuses are temporary items the player can collect by clicking on them
 * (or automatically when a condition is met). Each bonus has a type that
 * determines its effect, and a lifetime after which it disappears uncollected.
 *
 * Examples of bonus types (defined per subclass or via BonusType enum in later phase):
 *   - EXTRA_GOLD       : grants the player extra gold immediately
 *   - DAMAGE_BOOST     : temporarily doubles all tower damage
 *   - SLOW_ALL_ENEMIES : briefly slows every enemy on the map
 *
 * The J2d subclass renders the bonus as an animated icon or glowing orb.
 */
public abstract class Bonus extends Entity {

    // How long this bonus remains on the map before disappearing (in seconds)
    protected double lifetime;

    // Time elapsed since this bonus was spawned — compared against lifetime
    protected double age;

    // Whether this bonus has been collected by the player
    protected boolean collected;

    /*
     * Creates a bonus at the given position with the specified on-map lifetime.
     */
    public Bonus(Position position, double width, double height, double lifetime) {
        super(position, width, height);
        this.lifetime  = lifetime;
        this.age       = 0.0;
        this.collected = false;
    }

    // -------------------------------------------------------------------------
    // Update — count down the on-map lifetime
    // -------------------------------------------------------------------------

    /*
     * Ages the bonus each frame. When age exceeds lifetime (and the bonus hasn't
     * been collected), it destroys itself so the game loop removes it.
     */
    @Override
    public void update(double deltaTime) {
        if (collected) return;

        age += deltaTime;
        if (age >= lifetime) {
            alive = false;  // expired — game loop will remove it
        }
    }

    // -------------------------------------------------------------------------
    // Collection — called by the game loop when the player picks it up
    // -------------------------------------------------------------------------

    /*
     * Applies this bonus's effect to the game and marks it as collected.
     *
     * Concrete subclasses implement the actual effect:
     *   - GoldBonus.collect()   → Game.getInstance().addGold(amount)
     *   - DamageBoost.collect() → temporarily increases tower damage
     */
    public void collect() {
        if (!collected) {
            collected = true;
            alive     = false;
            applyEffect();
        }
    }

    /*
     * Applies the specific effect of this bonus type.
     * Implemented by each concrete subclass.
     */
    protected abstract void applyEffect();

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public boolean isCollected() { return collected; }
    public double  getLifetime() { return lifetime; }
    public double  getAge()      { return age; }

    /* Remaining time before the bonus expires, in seconds. */
    public double getRemainingTime() { return Math.max(0, lifetime - age); }
}
