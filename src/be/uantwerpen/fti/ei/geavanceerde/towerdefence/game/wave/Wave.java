package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.wave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Stelt één golf van vijanden voor.
 *
 * Een golf bevat een geordende lijst van EnemyEntry-objecten. Elke entry
 * geeft aan welk type vijand gespawnd moet worden en hoeveel keer.
 *
 * Voorbeeld: wave.2.enemies=basic:8,armored:2
 *   → EnemyEntry("basic",  8)
 *   → EnemyEntry("armored", 2)
 *
 * De WaveManager roept tick(deltaTime) aan elke frame. tick() telt de
 * spawn-timer af en geeft het type terug van de volgende vijand zodra het
 * interval verstreken is. Als de golf klaar is, geeft tick() een lege Optional terug.
 *
 * Volgorde van spawnen:
 *   Alle entries worden uitgebreid tot een vlakke lijst (bijv. 8x "basic",
 *   dan 2x "armored"). Vijanden worden één voor één gespawnd met
 *   SPAWN_INTERVAL seconden ertussen.
 */
public class Wave {

    /** Tijd tussen opeenvolgende spawns binnen een golf (seconden). */
    private static final double SPAWN_INTERVAL = 1.5;

    // -------------------------------------------------------------------------
    // Inner class: één enemytype + hoeveelheid
    // -------------------------------------------------------------------------

    /*
     * Beschrijft één groep vijanden binnen een golf.
     * type: "basic", "armored" of "flying"
     * count: het aantal te spawnen exemplaren
     */
    public static class EnemyEntry {
        private final String type;
        private final int    count;

        public EnemyEntry(String type, int count) {
            this.type  = type;
            this.count = count;
        }

        public String getType()  { return type; }
        public int    getCount() { return count; }
    }

    // -------------------------------------------------------------------------
    // Toestand
    // -------------------------------------------------------------------------

    /** Vlakke spawn-wachtrij: één string per te spawnen vijand. */
    private final List<String> spawnQueue;

    /** Index van de volgende te spawnen vijand in spawnQueue. */
    private int spawnIndex;

    /** Aftelklok tot de volgende spawn. */
    private double spawnTimer;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /*
     * Bouwt een golf op uit een lijst van EnemyEntry-objecten.
     *
     * De entries worden uitgebreid tot een vlakke spawnQueue:
     *   [basic, basic, basic, armored, armored, ...]
     *
     * De eerste vijand spawnt direct (timer = 0).
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

    /*
     * Roep aan elke frame met deltaTime in seconden.
     *
     * Retourneert het type van de volgende te spawnen vijand als het
     * spawn-interval verstreken is, anders een lege string ("").
     *
     * Retourneert "" als de golf al volledig gespawnd is (gebruik
     * isFinished() om dit te controleren).
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

    /** Geeft true als alle vijanden in deze golf gespawnd zijn. */
    public boolean isFinished() {
        return spawnIndex >= spawnQueue.size();
    }

    /** Totaal aantal te spawnen vijanden in deze golf. */
    public int getTotalEnemies() {
        return spawnQueue.size();
    }

    /** Aantal vijanden dat nog gespawnd moet worden. */
    public int getRemainingSpawns() {
        return spawnQueue.size() - spawnIndex;
    }

    /** Alleen-lezen view op de vlakke spawn-wachtrij (voor tests/debug). */
    public List<String> getSpawnQueue() {
        return Collections.unmodifiableList(spawnQueue);
    }
}
