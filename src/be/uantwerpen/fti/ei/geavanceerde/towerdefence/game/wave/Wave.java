package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.wave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stelt één golf van vijanden voor.
 *
 * <p>Een golf bevat een geordende lijst van {@link EnemyEntry}-objecten. Elke entry
 * geeft aan welk type vijand gespawnd moet worden en hoeveel keer. Voorbeeld:
 * {@code wave.2.enemies=basic:8,armored:2} levert {@code EnemyEntry("basic", 8)} en
 * {@code EnemyEntry("armored", 2)} op.</p>
 *
 * <p>De {@code WaveManager} roept {@link #tick(double)} aan elke frame. Die telt de
 * spawn-timer af en geeft het type terug van de volgende vijand zodra het interval
 * verstreken is; als de golf klaar is geeft {@code tick()} een lege string terug.</p>
 *
 * <p>Volgorde van spawnen: alle entries worden uitgebreid tot een vlakke lijst
 * (bijvoorbeeld 8× {@code "basic"}, dan 2× {@code "armored"}). Vijanden worden één
 * voor één gespawnd met {@code SPAWN_INTERVAL} seconden ertussen.</p>
 *
 * @author Tower Defence team
 */
public class Wave {

    /** Tijd tussen opeenvolgende spawns binnen een golf (seconden). */
    private static final double SPAWN_INTERVAL = 1.5;

    // -------------------------------------------------------------------------
    // Inner class: één enemytype + hoeveelheid
    // -------------------------------------------------------------------------

    /**
     * Beschrijft één groep vijanden binnen een golf: een type
     * ({@code "basic"}, {@code "armored"} of {@code "flying"}) en het aantal te
     * spawnen exemplaren.
     */
    public static class EnemyEntry {
        /** Het vijandtype-id ({@code "basic"}, {@code "armored"} of {@code "flying"}). */
        private final String type;
        /** Het aantal te spawnen exemplaren van dit type. */
        private final int    count;

        /**
         * Maakt een nieuwe entry aan.
         *
         * @param type  het vijandtype-id
         * @param count het aantal te spawnen exemplaren
         */
        public EnemyEntry(String type, int count) {
            this.type  = type;
            this.count = count;
        }

        /**
         * Geeft het vijandtype-id terug.
         *
         * @return het type-id
         */
        public String getType()  { return type; }

        /**
         * Geeft het aantal te spawnen exemplaren terug.
         *
         * @return het aantal
         */
        public int    getCount() { return count; }
    }

    // -------------------------------------------------------------------------
    // Toestand
    // -------------------------------------------------------------------------

    /** Vlakke spawn-wachtrij: één string per te spawnen vijand. */
    private final List<String> spawnQueue;

    /** Index van de volgende te spawnen vijand in {@link #spawnQueue}. */
    private int spawnIndex;

    /** Aftelklok tot de volgende spawn. */
    private double spawnTimer;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Bouwt een golf op uit een lijst van {@link EnemyEntry}-objecten.
     *
     * <p>De entries worden uitgebreid tot een vlakke spawn-wachtrij (bijvoorbeeld
     * {@code [basic, basic, basic, armored, ...]}). De eerste vijand spawnt direct
     * ({@code timer = 0}).</p>
     *
     * @param entries de groepen vijanden waaruit deze golf bestaat
     */
    public Wave(List<EnemyEntry> entries) {
        this.spawnQueue = new ArrayList<>();

        // Uitbreiden: elke entry n keer herhalen
        for (EnemyEntry entry : entries) {
            for (int i = 0; i < entry.getCount(); i++) {
                spawnQueue.add(entry.getType());
            }
        }

        this.spawnIndex = 0;
        this.spawnTimer = 0.0;   // eerste vijand spawnt meteen
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Verwerkt de spawn-timer voor dit frame en geeft eventueel het volgende
     * vijandtype terug.
     *
     * <p>Retourneert het type van de volgende te spawnen vijand als het
     * spawn-interval verstreken is, anders een lege string ({@code ""}). Geeft ook
     * {@code ""} terug als de golf al volledig gespawnd is (controleer dit met
     * {@link #isFinished()}).</p>
     *
     * @param deltaTime verstreken tijd in seconden sinds het vorige frame
     * @return het te spawnen vijandtype, of {@code ""} als er niets spawnt dit frame
     */
    public String tick(double deltaTime) {
        // Golf is al klaar
        if (isFinished()) return "";

        spawnTimer -= deltaTime;

        if (spawnTimer <= 0) {
            String type = spawnQueue.get(spawnIndex);
            spawnIndex++;
            spawnTimer = SPAWN_INTERVAL;   // reset voor volgende spawn
            return type;
        }

        return "";
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Geeft terug of alle vijanden in deze golf gespawnd zijn.
     *
     * @return {@code true} als de golf volledig gespawnd is
     */
    public boolean isFinished() {
        return spawnIndex >= spawnQueue.size();
    }

    /**
     * Geeft het totale aantal te spawnen vijanden in deze golf terug.
     *
     * @return het totale aantal vijanden
     */
    public int getTotalEnemies() {
        return spawnQueue.size();
    }

    /**
     * Geeft het aantal vijanden terug dat nog gespawnd moet worden.
     *
     * @return het resterende aantal spawns
     */
    public int getRemainingSpawns() {
        return spawnQueue.size() - spawnIndex;
    }

    /**
     * Geeft een alleen-lezen view op de vlakke spawn-wachtrij (voor tests/debug).
     *
     * @return een onveranderlijke lijst met één type-id per te spawnen vijand
     */
    public List<String> getSpawnQueue() {
        return Collections.unmodifiableList(spawnQueue);
    }
}
