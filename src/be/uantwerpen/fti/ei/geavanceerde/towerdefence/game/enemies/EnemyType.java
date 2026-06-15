package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/**
 * The enemy types, as a polymorphic dispatcher over the Abstract Factory.
 *
 * <p>Each constant knows (a) which factory method creates its concrete enemy and
 * (b) whether it prefers the air path. This keeps the wave-string → creation mapping
 * <em>and</em> the "is this a flying enemy" question in one place, so the spawner in
 * {@code Game} needs no {@code switch} and no string comparison.</p>
 *
 * <p>The actual path is still chosen by the game loop (it depends on the
 * {@code GameMap}, which the factory must not know about), but it keys on
 * {@link #usesAirPath()} instead of a hard-coded string compare.</p>
 *
 * <p>Open/closed: a new enemy type means adding a constant here (and a factory
 * method) — {@code Game} is never touched.</p>
 *
 * @author Tower Defence team
 */
public enum EnemyType {

    /** Standard ground enemy — creates a {@code BasicEnemy}. */
    BASIC  ("basic",   false) { public Enemy create(EntityFactory f, List<Position> p) { return f.createBasicEnemy(p);   } },
    /** Tanky, resistant ground enemy — creates an {@code ArmoredEnemy}. */
    ARMORED("armored", false) { public Enemy create(EntityFactory f, List<Position> p) { return f.createArmoredEnemy(p); } },
    /** Fast aerial enemy — creates a {@code FlyingEnemy}. */
    FLYING ("flying",  true)  { public Enemy create(EntityFactory f, List<Position> p) { return f.createFlyingEnemy(p);  } };

    /** The config-string identifier used in level wave definitions (e.g. {@code "armored:2"}). */
    private final String id;

    /** Whether this enemy prefers the air path (when the map provides one). */
    private final boolean usesAirPath;

    /**
     * Associates this enemy type with its config identifier and path preference.
     *
     * @param id          the config-string identifier
     * @param usesAirPath whether this type prefers the air path
     */
    EnemyType(String id, boolean usesAirPath) {
        this.id          = id;
        this.usesAirPath = usesAirPath;
    }

    /**
     * Creates this enemy type on the given path via the abstract factory.
     *
     * @param f the abstract factory used to create the enemy
     * @param p the ordered list of waypoints the enemy will follow
     * @return the newly created enemy
     */
    public abstract Enemy create(EntityFactory f, List<Position> p);

    /**
     * Returns whether this enemy type prefers the air path when the map provides one.
     *
     * @return {@code true} if this type uses the air path
     */
    public boolean usesAirPath() { return usesAirPath; }

    /**
     * Resolves a wave-config string ({@code "basic"}, {@code "armored"},
     * {@code "flying"}) to a type. Unknown values fall back to {@link #BASIC}.
     *
     * @param id the config-string identifier (case-insensitive)
     * @return the matching enemy type, or {@link #BASIC} if unknown
     */
    public static EnemyType fromId(String id) {
        for (EnemyType t : values()) {
            if (t.id.equalsIgnoreCase(id)) {
                return t;
            }
        }
        return BASIC;
    }
}
