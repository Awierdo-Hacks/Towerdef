package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs;

/*
 * DATA-ORIENTED / ECS datastore for short-lived floating combat text.
 *
 * This is the project's required Entity-Component-System subsystem, built
 * deliberately in the OPPOSITE style of the rest of the game (which is OOP:
 * each Enemy/Tower/Projectile is an object with virtual update()/render()).
 *
 *   - ENTITY     : just an int index (a slot in the arrays below). There is no
 *                  "FloatingText" object with behaviour.
 *   - COMPONENTS : plain data laid out as parallel arrays (Structure-of-Arrays).
 *                  Each field of every entity lives contiguously in its own array.
 *   - SYSTEMS    : stateless logic classes (MovementSystem, LifetimeSystem, and
 *                  the rendering done in j2d) that sweep these arrays by index.
 *                  No inheritance or per-entity polymorphism.
 *
 * The arrays are package-private so the systems in this package can operate on
 * the data directly (typical data-oriented style), while the rest of the program
 * only sees read-only index accessors.
 *
 * No java.awt / javax.swing here — this stays in the game/ package. The actual
 * drawing is done by J2dGame, which reads the accessors below.
 *
 * Usage: floating numbers above enemies on hit, and gold popups on kill. The
 * game stores only the raw amount + a semantic kind; the renderer formats the
 * label (e.g. "30" or "+10g") and picks the colour. They drift upward (vy) and
 * fade out (alpha derived from age/lifetime).
 */
public class FloatingTextWorld {

    /** Maximum number of simultaneous floating texts; extra spawns are dropped. */
    private static final int CAPACITY = 256;

    /** Seconds a floating text stays alive before it is removed. */
    private static final double DEFAULT_LIFETIME = 1.0;

    /** Upward drift speed in game-world units per second. */
    private static final double DEFAULT_DRIFT = 1.5;

    // -------------------------------------------------------------------------
    // Components — Structure-of-Arrays. Index i identifies one entity.
    // Package-private so the systems in this package can iterate them directly.
    // -------------------------------------------------------------------------

    final double[]          x        = new double[CAPACITY];   // game-world X
    final double[]          y        = new double[CAPACITY];   // game-world Y (decreases as it drifts up)
    final double[]          vy       = new double[CAPACITY];   // upward drift speed
    final double[]          age      = new double[CAPACITY];   // seconds since spawn
    final double[]          lifetime = new double[CAPACITY];   // seconds until removal
    final double[]          value    = new double[CAPACITY];   // raw amount (damage / gold); formatting is the renderer's job
    final FloatingTextKind[] kind     = new FloatingTextKind[CAPACITY]; // semantic category (NOT a colour)

    /** Number of currently alive entities; live slots are [0, count). */
    int count = 0;

    // -------------------------------------------------------------------------
    // Spawning / clearing
    // -------------------------------------------------------------------------

    /*
     * Spawns one floating text at the given game-world position.
     *
     * The caller supplies a semantic kind (DAMAGE / REWARD), never a colour —
     * how each kind is rendered is decided entirely by the visualization layer.
     *
     * Silently ignored when the world is full (count == CAPACITY) — keeps the
     * fixed-size storage trivial and allocation-free during play.
     */
    public void spawn(double worldX, double worldY, double amount, FloatingTextKind textKind) {
        if (count >= CAPACITY) {
            return;
        }
        int i = count;
        x[i]        = worldX;
        y[i]        = worldY;
        vy[i]       = DEFAULT_DRIFT;
        age[i]      = 0.0;
        lifetime[i] = DEFAULT_LIFETIME;
        value[i]    = amount;
        kind[i]     = textKind;
        count++;
    }

    /** Removes all floating texts (called on game reset). */
    public void clear() {
        count = 0;
    }

    // -------------------------------------------------------------------------
    // Read-only accessors — used by the j2d render system. Arrays stay private
    // to the package so callers cannot mutate the data store directly.
    // -------------------------------------------------------------------------

    public int             getCount()      { return count; }
    public double          getX(int i)     { return x[i]; }
    public double          getY(int i)     { return y[i]; }
    public double          getValue(int i) { return value[i]; }
    public FloatingTextKind getKind(int i)  { return kind[i]; }

    /* Fade factor 1.0 (fresh) → 0.0 (expired), clamped to [0, 1]. */
    public double getAlpha(int i) {
        double a = 1.0 - (age[i] / lifetime[i]);
        if (a < 0.0) return 0.0;
        if (a > 1.0) return 1.0;
        return a;
    }
}
