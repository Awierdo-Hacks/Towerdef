package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/*
 * The enemy types, as a polymorphic dispatcher over the Abstract Factory.
 *
 * Each constant knows (a) which factory method creates its concrete enemy and
 * (b) whether it prefers the air path. This keeps the wave-string -> creation
 * mapping AND the "is this a flying enemy" question in ONE place, so the spawner
 * in Game.java needs no switch and no string comparison.
 *
 * The actual path is still chosen by the game loop (it depends on the GameMap,
 * which the factory must not know about) — but it keys on usesAirPath() instead
 * of a hard-coded string compare.
 *
 * OPEN/CLOSED: a new enemy type means adding a constant here (and a factory
 * method) — Game.java is never touched.
 */
public enum EnemyType {

    BASIC  ("basic",   false) { public Enemy create(EntityFactory f, List<Position> p) { return f.createBasicEnemy(p);   } },
    ARMORED("armored", false) { public Enemy create(EntityFactory f, List<Position> p) { return f.createArmoredEnemy(p); } },
    FLYING ("flying",  true)  { public Enemy create(EntityFactory f, List<Position> p) { return f.createFlyingEnemy(p);  } };

    // The config-string identifier used in level wave definitions (e.g. "armored:2")
    private final String id;

    // Whether this enemy prefers the air path (when the map provides one)
    private final boolean usesAirPath;

    EnemyType(String id, boolean usesAirPath) {
        this.id          = id;
        this.usesAirPath = usesAirPath;
    }

    /* Creates this enemy type on the given path via the abstract factory. */
    public abstract Enemy create(EntityFactory f, List<Position> p);

    public boolean usesAirPath() { return usesAirPath; }

    /*
     * Resolves a wave-config string ("basic", "armored", "flying") to a type.
     * Unknown values fall back to BASIC — matching the previous spawner default.
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
