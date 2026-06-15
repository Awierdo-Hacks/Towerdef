package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.wave;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Beheert de volgorde en timing van alle golven in een level.
 *
 * <p>Verantwoordelijkheden:</p>
 * <ul>
 *   <li>golven inlezen uit de level {@code .properties} file ({@code wave.count},
 *       {@code wave.N.enemies});</li>
 *   <li>bijhouden welke golf actief is;</li>
 *   <li>de wachttijd tussen golven hanteren ({@code INTER_WAVE_DELAY} seconden);</li>
 *   <li>per frame {@link #tick(double)} aanbieden, dat een {@code Optional<String>}
 *       met het te spawnen vijandtype teruggeeft, of {@link java.util.Optional#empty()}
 *       als er niets spawnt.</li>
 * </ul>
 *
 * <p>Level config formaat:</p>
 * <pre>
 *   wave.count=5
 *   wave.1.enemies=basic:5
 *   wave.2.enemies=basic:8,armored:2
 *   wave.3.enemies=basic:10,armored:4,flying:1
 * </pre>
 *
 * <p>Elk {@code "type:count"} item beschrijft een {@link Wave.EnemyEntry} die
 * doorgestuurd wordt naar de {@link Wave} constructor.</p>
 *
 * @author Tower Defence team
 */

//logboek parsing problemen solved
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

    /**
     * Leest alle golfdefinities uit de level {@code ConfigManager}.
     *
     * <p>Als {@code wave.count} ontbreekt of nul is, wordt een lege golvenlijst
     * gemaakt zodat het spel niet crasht; de win-conditie in {@code Game} detecteert
     * dan meteen dat alle golven klaar zijn.</p>
     *
     * @param levelConfig de config van het level met de golfdefinities
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

    /**
     * Verwerkt de spawn-logica voor dit frame.
     *
     * <p>Gedrag:</p>
     * <ol>
     *   <li>als alle golven klaar zijn, wordt {@link Optional#empty()} teruggegeven;</li>
     *   <li>als de wachttijd tussen golven nog loopt, wordt {@link Optional#empty()}
     *       teruggegeven;</li>
     *   <li>anders wordt de actieve golf geticked: spawnt die een vijand dan wordt het
     *       type teruggegeven; is de golf klaar dan start de teller voor de volgende
     *       golf (of worden alle golven als afgerond gemarkeerd als het de laatste was).</li>
     * </ol>
     *
     * @param deltaTime verstreken tijd in seconden sinds het vorige frame
     * @return het vijandtype dat gespawnd moet worden ({@code "basic"},
     *         {@code "armored"}, {@code "flying"}), of {@link Optional#empty()} als er
     *         niets spawnt dit frame
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

    /**
     * Geeft terug of alle golven volledig gespawnd zijn.
     *
     * @return {@code true} als alle golven klaar zijn
     */
    public boolean isFinished() {
        return allWavesFinished;
    }

    /**
     * Geeft het (1-gebaseerde) nummer van de huidige golf terug.
     *
     * @return het huidige golfnummer
     */
    public int getCurrentWaveNumber() {
        return Math.min(currentWaveIndex + 1, waves.size());
    }

    /**
     * Geeft het totale aantal golven in dit level terug.
     *
     * @return het aantal golven
     */
    public int getTotalWaves() {
        return waves.size();
    }

    /**
     * Geeft het aantal vijanden terug dat in de huidige golf nog gespawnd moet worden.
     *
     * @return het resterende aantal spawns in de huidige golf, of {@code 0} als alles klaar is
     */
    public int getRemainingSpawnsInCurrentWave() {
        if (allWavesFinished || currentWaveIndex >= waves.size()) return 0;
        return waves.get(currentWaveIndex).getRemainingSpawns();
    }

    /**
     * Geeft het aantal seconden tot de volgende golf start terug ({@code 0} als een
     * golf actief is).
     *
     * @return de resterende wachttijd in seconden
     */
    public double getTimeUntilNextWave() {
        return Math.max(0.0, interWaveTimer);
    }

    /**
     * Geeft een alleen-lezen view op alle golven terug (voor tests/debug).
     *
     * @return een onveranderlijke lijst met alle golven
     */
    public List<Wave> getWaves() {
        return Collections.unmodifiableList(waves);
    }
}
