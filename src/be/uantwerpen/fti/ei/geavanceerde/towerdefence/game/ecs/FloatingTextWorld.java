package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.ecs;

/**
 * Data-oriented / ECS datastore for short-lived floating combat text.
 *
 * <p>This is the project's required Entity-Component-System subsystem, built
 * deliberately in the opposite style of the rest of the game (which is OOP: each
 * {@code Enemy}/{@code Tower}/{@code Projectile} is an object with virtual
 * {@code update()}/{@code render()}).</p>
 *
 * <ul>
 *   <li><strong>Entity</strong>: just an {@code int} index (a slot in the arrays
 *       below). There is no {@code FloatingText} object with behaviour.</li>
 *   <li><strong>Components</strong>: plain data laid out as parallel arrays
 *       (Structure-of-Arrays); each field of every entity lives contiguously in its
 *       own array.</li>
 *   <li><strong>Systems</strong>: stateless logic classes ({@code MovementSystem},
 *       {@code LifetimeSystem}, and the rendering done in {@code j2d}) that sweep
 *       these arrays by index — no inheritance or per-entity polymorphism.</li>
 * </ul>
 *
 * <p>The component arrays are package-private so the systems in this package can
 * operate on the data directly (typical data-oriented style), while the rest of the
 * program only sees read-only index accessors. No {@code java.awt}/{@code javax.swing}
 * here — this stays in the {@code game} package; the actual drawing is done by
 * {@code J2dGame}, which reads the accessors below.</p>
 *
 * <p>Usage: floating numbers above enemies on hit, and gold popups on kill. The game
 * stores only the raw amount plus a semantic kind; the renderer formats the label
 * (e.g. {@code "30"} or {@code "+10g"}) and picks the colour. Texts drift upward and
 * fade out (alpha derived from age/lifetime).</p>
 *
 * @author Tower Defence team
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

    /** Creates an empty floating-text world with a fixed component capacity. */
    public FloatingTextWorld() {
    }

    // -------------------------------------------------------------------------
    // Spawning / clearing
    // -------------------------------------------------------------------------

    /**
     * Spawns one floating text at the given game-world position.
     *
     * <p>The caller supplies a semantic kind ({@link FloatingTextKind#DAMAGE} /
     * {@link FloatingTextKind#REWARD}), never a colour — how each kind is rendered is
     * decided entirely by the visualization layer. The call is silently ignored when
     * the world is full (count equals capacity), keeping the fixed-size storage
     * trivial and allocation-free during play.</p>
     *
     * @param worldX   the game-world X position of the text
     * @param worldY   the game-world Y position of the text
     * @param amount   the raw amount (damage or gold) to display
     * @param textKind the semantic category of this text
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

    /**
     * Returns the number of currently alive floating texts. Valid indices are
     * {@code [0, count)}.
     *
     * @return the number of live texts
     */
    public int             getCount()      { return count; }

    /**
     * Returns the game-world X position of the text at the given index.
     *
     * @param i the entity index in {@code [0, count)}
     * @return the X position
     */
    public double          getX(int i)     { return x[i]; }

    /**
     * Returns the game-world Y position of the text at the given index.
     *
     * @param i the entity index in {@code [0, count)}
     * @return the Y position
     */
    public double          getY(int i)     { return y[i]; }

    /**
     * Returns the raw amount (damage or gold) of the text at the given index.
     *
     * @param i the entity index in {@code [0, count)}
     * @return the raw display amount
     */
    public double          getValue(int i) { return value[i]; }

    /**
     * Returns the semantic kind of the text at the given index.
     *
     * @param i the entity index in {@code [0, count)}
     * @return the semantic category
     */
    public FloatingTextKind getKind(int i)  { return kind[i]; }

    /**
     * Returns the fade factor of the text at the given index: {@code 1.0} when fresh,
     * decreasing to {@code 0.0} when expired, clamped to {@code [0, 1]}.
     *
     * @param i the entity index in {@code [0, count)}
     * @return the alpha/fade factor in {@code [0, 1]}
     */
    public double getAlpha(int i) {
        double a = 1.0 - (age[i] / lifetime[i]);
        if (a < 0.0) return 0.0;
        if (a > 1.0) return 1.0;
        return a;
    }
}
