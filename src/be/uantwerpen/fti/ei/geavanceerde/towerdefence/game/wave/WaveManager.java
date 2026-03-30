package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.wave;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/*
 * Beheert de volgorde en timing van alle golven in een level.
 *
 * VERANTWOORDELIJKHEDEN:
 *   - Golven inlezen uit de level .properties file (wave.count, wave.N.enemies)
 *   - Bijhouden welke golf actief is
 *   - Wachttijd tussen golven hanteren (INTER_WAVE_DELAY seconden)
 *   - Per frame tick(deltaTime) aanbieden → geeft Optional<String> met het
 *     te spawnen vijandtype terug, of Optional.empty() als er niets spawnt
 *
 * LEVEL CONFIG FORMAAT:
 *   wave.count=5
 *   wave.1.enemies=basic:5
 *   wave.2.enemies=basic:8,armored:2
 *   wave.3.enemies=basic:10,armored:4,flying:1
 *
 *   Elk "type:count" item beschrijft een EnemyEntry die doorgestuurd wordt
 *   naar de Wave constructor.
 *
 * GEBRUIK (in Game.java):
 *   waveManager = new WaveManager(levelConfig);
 *   // elke frame:
 *   Optional<String> spawn = waveManager.tick(deltaTime);
 *   spawn.ifPresent(type -> spawnEnemy(type));
 */
public class WaveManager {

    /** Wachttijd tussen het einde van een golf en de start van de volgende (seconden). */
    private static final double INTER_WAVE_DELAY = 5.0;

    // -------------------------------------------------------------------------
    // Golvenlijst
    // -------------------------------------------------------------------------

    /** Alle golven voor dit level, in volgorde. */
    private final List<Wave> waves;

    // -------------------------------------------------------------------------
    // Voortgangsstatus
    // -------------------------------------------------------------------------

    /** Index van de huidige actieve golf (0-gebaseerd). */
    private int currentWaveIndex;

    /**
     * Aftelklok voor de pauze tussen golven.
     * Als > 0: we wachten nog op de volgende golf.
     * Als <= 0 en currentWaveIndex < waves.size(): een golf is actief.
     */
    private double interWaveTimer;

    /** True als alle golven volledig gespawnd zijn. */
    private boolean allWavesFinished;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /*
     * Leest alle golfdefinities uit de level ConfigManager.
     *
     * Als wave.count ontbreekt of nul is, wordt een lege golvenlijst gemaakt
     * zodat het spel niet crasht. De win-conditie in Game.java detecteert dan
     * meteen dat alle golven klaar zijn.
     */
    public WaveManager(ConfigManager levelConfig) {
        this.waves              = new ArrayList<>();
        this.currentWaveIndex   = 0;
        this.interWaveTimer     = 0.0;   // eerste golf start meteen
        this.allWavesFinished   = false;

        int waveCount = levelConfig.getInt("wave.count", 0);

        for (int i = 1; i <= waveCount; i++) {
            // Lees de vijandstring, bijv. "basic:10,armored:4,flying:1"
            String enemyLine = levelConfig.getString("wave." + i + ".enemies", "");
            List<Wave.EnemyEntry> entries = parseEnemyLine(enemyLine);
            waves.add(new Wave(entries));
        }
    }

    // -------------------------------------------------------------------------
    // Update — elke frame aanroepen
    // -------------------------------------------------------------------------

    /*
     * Verwerkt de spawn-logica voor dit frame.
     *
     * Gedrag:
     *   1. Als allWavesFinished → geeft Optional.empty() terug.
     *   2. Als interWaveTimer > 0 → wachttijd loopt, geeft Optional.empty() terug.
     *   3. Anders wordt de actieve golf geticked:
     *        a. Als de golf een vijand wil spawnen → geef die terug.
     *        b. Als de golf klaar is → start de teller voor de volgende golf
     *           (of markeer allWavesFinished als het de laatste was).
     *
     * @param deltaTime verstreken tijd in seconden sinds het vorige frame
     * @return het vijandtype dat gespawnd moet worden ("basic", "armored",
     *         "flying"), of Optional.empty() als er niets spawnt dit frame
     */
    public Optional<String> tick(double deltaTime) {
        if (allWavesFinished) return Optional.empty();

        // Wachttijd tussen golven
        if (interWaveTimer > 0) {
            interWaveTimer -= deltaTime;
            return Optional.empty();
        }

        // Geen golven beschikbaar
        if (currentWaveIndex >= waves.size()) {
            allWavesFinished = true;
            return Optional.empty();
        }

        Wave current = waves.get(currentWaveIndex);

        // Laat de actieve golf een vijand spawnen
        String spawnType = current.tick(deltaTime);

        if (!spawnType.isEmpty()) {
            // Dit frame spawnt er één vijand
            return Optional.of(spawnType);
        }

        // Golf is leeg (isFinished) maar we zijn hier toch beland zonder spawn
        if (current.isFinished()) {
            advanceToNextWave();
        }

        return Optional.empty();
    }

    // -------------------------------------------------------------------------
    // Privé hulpmethode: naar volgende golf gaan
    // -------------------------------------------------------------------------

    /*
     * Gaat naar de volgende golf over.
     * Als het de laatste golf was, wordt allWavesFinished op true gezet.
     * Anders wordt de inter-wave timer gestart.
     */
    private void advanceToNextWave() {
        currentWaveIndex++;

        if (currentWaveIndex >= waves.size()) {
            allWavesFinished = true;
        } else {
            interWaveTimer = INTER_WAVE_DELAY;
        }
    }

    // -------------------------------------------------------------------------
    // Privé hulpmethode: parse "basic:8,armored:2,flying:1"
    // -------------------------------------------------------------------------

    /*
     * Zet een kommagescheiden vijandstring om naar een lijst van EnemyEntry's.
     *
     * Elk token heeft het formaat "type:count".
     * Tokens met fouten worden overgeslagen zodat één slechte entry de
     * rest van de golf niet blokkeert.
     *
     * Voorbeelden:
     *   "basic:5"              → [EnemyEntry("basic", 5)]
     *   "basic:8,armored:2"    → [EnemyEntry("basic", 8), EnemyEntry("armored", 2)]
     *   ""                     → []
     */
    private List<Wave.EnemyEntry> parseEnemyLine(String line) {
        List<Wave.EnemyEntry> entries = new ArrayList<>();
        if (line == null || line.trim().isEmpty()) return entries;

        for (String token : line.split(",")) {
            token = token.trim();
            String[] parts = token.split(":");
            if (parts.length != 2) continue;

            String type = parts[0].trim().toLowerCase();
            int count;
            try {
                count = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                continue;   // sla ongeldige token over
            }

            if (count > 0) {
                entries.add(new Wave.EnemyEntry(type, count));
            }
        }

        return entries;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /** Geeft true als alle golven volledig gespawnd zijn. */
    public boolean isFinished() {
        return allWavesFinished;
    }

    /** Het (1-gebaseerde) nummer van de huidige golf. */
    public int getCurrentWaveNumber() {
        return Math.min(currentWaveIndex + 1, waves.size());
    }

    /** Totaal aantal golven in dit level. */
    public int getTotalWaves() {
        return waves.size();
    }

    /**
     * Aantal vijanden dat in de huidige golf nog gespawnd moet worden.
     * Handig voor de HUD ("Vijanden over: X").
     */
    public int getRemainingSpawnsInCurrentWave() {
        if (allWavesFinished || currentWaveIndex >= waves.size()) return 0;
        return waves.get(currentWaveIndex).getRemainingSpawns();
    }

    /**
     * Seconden tot de volgende golf start (0 als een golf actief is).
     * Handig voor een afteltimer in de HUD.
     */
    public double getTimeUntilNextWave() {
        return Math.max(0.0, interWaveTimer);
    }

    /** Alleen-lezen view op alle golven (voor tests/debug). */
    public List<Wave> getWaves() {
        return Collections.unmodifiableList(waves);
    }
}
