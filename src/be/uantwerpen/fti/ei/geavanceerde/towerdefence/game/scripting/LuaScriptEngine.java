package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.scripting;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Wikkelt de LuaJ runtime voor enemy AI-scripts.
 *
 * <p><strong>Hot-reload:</strong> het script wordt geladen via de classpath-URL,
 * maar het bestandspad op schijf wordt bijgehouden. Elke {@code CHECK_INTERVAL_MS}
 * milliseconden controleert {@link #callUpdateEnemy(Enemy, double)} of het bestand
 * gewijzigd is; bij een wijziging wordt het script opnieuw geladen zonder het spel te
 * herstarten. Dit werkt enkel als de applicatie direct vanuit het bestandssysteem
 * draait (IDE / loose classpath); vanuit een JAR is hot-reload niet beschikbaar.</p>
 *
 * <p><strong>Data-uitwisseling (Java ↔ Lua via {@code LuaTable}):</strong> Java vult
 * vóór de aanroep {@code type}, {@code currentHealth}, {@code maxHealth},
 * {@code healthPercent} en {@code speedMul} in. Lua schrijft eventueel
 * {@code currentHealth}, {@code maxHealth} en {@code speedMul} terug, die daarna naar
 * het {@code Enemy}-object worden gepropageerd.</p>
 *
 * @author Tower Defence team
 */
public class LuaScriptEngine {

    /** Minimale tijd (ms) tussen twee controles op bestandswijzigingen. */
    private static final long CHECK_INTERVAL_MS = 500;

    /** De LuaJ runtime-omgeving; wordt bij elke (her)load opnieuw aangemaakt. */
    private Globals globals;
    /** {@code true} zodra een script succesvol geladen is. */
    private boolean loaded;

    /** Bestandspad op schijf — {@code null} als hot-reload niet beschikbaar is. */
    private Path scriptFile;

    /** Tijdstip (ms) van de laatste bekende versie van het script. */
    private long lastModified;

    /** Tijdstip (ms) van de laatste controle op bestandswijzigingen. */
    private long lastCheckTime;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Maakt een nieuwe script-engine met een verse LuaJ runtime. Er is nog geen
     * script geladen tot {@link #loadScript(String)} aangeroepen wordt.
     */
    public LuaScriptEngine() {
        this.globals = JsePlatform.standardGlobals();
        this.loaded  = false;
    }

    // -------------------------------------------------------------------------
    // Script laden
    // -------------------------------------------------------------------------

    /**
     * Bepaalt het bestandspad en laadt het script.
     *
     * <p>Hot-reload strategie: eerst wordt het bronbestand via de werkdirectory
     * ({@code resources/<resourcePath>}) geprobeerd — dit werkt wanneer de applicatie
     * vanuit de projectroot draait en wijzigingen worden dan direct opgepikt door
     * {@code checkForChanges()} zonder rebuild. Wordt het bronbestand niet gevonden,
     * dan valt de engine terug op de classpath-URL; hot-reload werkt dan alleen als de
     * URL naar een los bestand wijst (geen JAR-entry).</p>
     *
     * @param resourcePath pad relatief aan de classpath-root, bijv.
     *                     {@code "scripts/enemy_ai.lua"}
     */
    public void loadScript(String resourcePath) {
        // 1. Bronbestand via werkdirectory — werkt in IDE zonder rebuild
        Path devPath = Paths.get("resources").resolve(resourcePath);
        if (Files.exists(devPath)) {
            scriptFile = devPath.toAbsolutePath();
            reloadScript();
            return;
        }

        // 2. Terugval op classpath-URL (gekopieerd uitvoerpad of JAR)
        URL url = getClass().getClassLoader().getResource(resourcePath);
        if (url == null) {
            System.err.println("[LuaScriptEngine] Script niet gevonden: " + resourcePath);
            return;
        }

        if ("file".equals(url.getProtocol())) {
            try {
                scriptFile = Paths.get(url.toURI());
            } catch (URISyntaxException e) {
                System.err.println("[LuaScriptEngine] Kan bestandspad niet bepalen: " + e.getMessage());
            }
        } else {
            System.out.println("[LuaScriptEngine] Hot-reload niet beschikbaar (JAR-modus).");
        }

        reloadScript();
    }

    // -------------------------------------------------------------------------
    // Intern: script (her)laden
    // -------------------------------------------------------------------------

    private void reloadScript() {
        if (scriptFile == null) return;

        try {
            // Nieuwe globals zodat oude state (tellers, functies) gewist wordt
            this.globals = JsePlatform.standardGlobals();

            LuaValue chunk = globals.load(new FileReader(scriptFile.toFile()), scriptFile.getFileName().toString());
            chunk.call();

            lastModified = Files.getLastModifiedTime(scriptFile).toMillis();
            loaded = true;
            System.out.println("[LuaScriptEngine] Script geladen: " + scriptFile.getFileName());

        } catch (LuaError e) {
            System.err.println("[LuaScriptEngine] Lua-fout: " + e.getMessage());
            loaded = false;
        } catch (IOException e) {
            System.err.println("[LuaScriptEngine] Leesfout: " + e.getMessage());
            loaded = false;
        }
    }

    /*
     * Controleert of het scriptbestand gewijzigd is. Wordt maximaal
     * één keer per CHECK_INTERVAL_MS uitgevoerd om I/O te beperken.
     */
    private void checkForChanges() {
        if (scriptFile == null) return;

        long now = System.currentTimeMillis();
        if (now - lastCheckTime < CHECK_INTERVAL_MS) return;
        lastCheckTime = now;

        try {
            long currentModified = Files.getLastModifiedTime(scriptFile).toMillis();
            if (currentModified != lastModified) {
                System.out.println("[LuaScriptEngine] Wijziging gedetecteerd — script herladen...");
                reloadScript();
            }
        } catch (IOException ignored) {
            // Bestand tijdelijk niet leesbaar (bijv. editor schrijft nog) — volgende check
        }
    }

    // -------------------------------------------------------------------------
    // updateEnemy aanroepen
    // -------------------------------------------------------------------------

    /**
     * Controleert eerst op bestandswijzigingen en roept dan de Lua-functie
     * {@code updateEnemy(enemyTable, deltaTime)} aan, waarna gewijzigde waarden
     * teruggeschreven worden naar het {@code Enemy}-object. Doet niets als er geen
     * script geladen is of als de functie niet bestaat.
     *
     * @param enemy     de vijand waarvan de toestand aan Lua wordt doorgegeven
     * @param deltaTime verstreken tijd in seconden sinds het vorige frame
     */
    public void callUpdateEnemy(Enemy enemy, double deltaTime) {
        checkForChanges();
        if (!loaded) return;

        LuaValue func = globals.get("updateEnemy");
        if (func.isnil()) return;

        // Tabel vullen
        LuaTable enemyTable = new LuaTable();
        enemyTable.set("type",          LuaValue.valueOf(enemy.getType()));
        enemyTable.set("currentHealth", LuaValue.valueOf(enemy.getCurrentHealth()));
        enemyTable.set("maxHealth",     LuaValue.valueOf(enemy.getMaxHealth()));
        enemyTable.set("healthPercent", LuaValue.valueOf(enemy.getHealthPercent()));
        enemyTable.set("speedMul",      LuaValue.valueOf(enemy.getSpeedMultiplier()));

        // Lua aanroepen
        try {
            func.call(enemyTable, LuaValue.valueOf(deltaTime));
        } catch (LuaError e) {
            System.err.println("[LuaScriptEngine] Runtime-fout in updateEnemy: " + e.getMessage());
            return;
        } catch (Exception e) {
            System.err.println("[LuaScriptEngine] Onverwachte fout in updateEnemy: " + e.getMessage());
            return;
        }

        // Gewijzigde waarden terugschrijven
        double newMaxHealth = enemyTable.get("maxHealth").todouble();
        if (newMaxHealth != enemy.getMaxHealth()) enemy.setMaxHealth(newMaxHealth);

        double newCurrentHealth = enemyTable.get("currentHealth").todouble();
        if (newCurrentHealth != enemy.getCurrentHealth()) enemy.setCurrentHealth(newCurrentHealth);

        double newSpeedMul = enemyTable.get("speedMul").todouble();
        if (newSpeedMul != enemy.getSpeedMultiplier()) enemy.setSpeedMultiplier(newSpeedMul);
    }
}
