package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.scripting;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/*
 * Wikkelt de LuaJ runtime voor enemy AI-scripts.
 *
 * AANPAK (gebaseerd op werkende LuaJ-voorbeeldcode):
 *   Java-objecten worden NIET direct doorgegeven aan Lua.
 *   In plaats daarvan wordt per aanroep een LuaTable aangemaakt met
 *   de relevante velden van de vijand. Lua past de tabel aan, en Java
 *   leest de gewijzigde waarden terug en past ze toe op het enemy-object.
 *
 *   Dit vermijdt problemen met CoerceJavaToLua die in LuaJ 3.0.1
 *   niet betrouwbaar werkt voor methode-aanroepen op Java-objecten.
 *
 * DATA-UITWISSELING (Java ↔ Lua via LuaTable):
 *
 *   Java vult de tabel vóór de aanroep:
 *     enemy.type          (string)  → "basic", "armored", "flying"
 *     enemy.currentHealth (number)  → huidig HP
 *     enemy.maxHealth     (number)  → maximaal HP
 *     enemy.healthPercent (number)  → currentHealth / maxHealth  (0.0–1.0)
 *     enemy.speedMul      (number)  → huidige snelheidsmultiplier
 *
 *   Lua schrijft terug naar de tabel (alle andere velden worden genegeerd):
 *     enemy.currentHealth → wordt toegepast via enemy.setCurrentHealth()
 *     enemy.maxHealth     → wordt toegepast via enemy.setMaxHealth()
 *     enemy.speedMul      → wordt toegepast via enemy.setSpeedMultiplier()
 *
 * SCRIPT LADEN:
 *   Via getClass().getResourceAsStream("/" + pad), zelfde techniek als
 *   het voorbeeldproject. De leading "/" maakt het pad absoluut vanaf
 *   de classpath-root. Pad in game.properties: "scripts/enemy_ai.lua".
 *
 * FOUTAFHANDELING:
 *   Script niet gevonden / syntaxfout / runtime-fout → stderr + no-op.
 *   Het spel crasht nooit door een Lua-fout.
 */
public class LuaScriptEngine {

    /*
     * De globale Lua-omgeving. Wordt éénmalig aangemaakt en hergebruikt
     * zodat top-level state in het script (variabelen, tellers) bewaard blijft
     * tussen frames.
     */
    private final Globals globals;

    /* True zodra een script succesvol geladen én uitgevoerd is. */
    private boolean loaded;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public LuaScriptEngine() {
        this.globals = JsePlatform.standardGlobals();
        this.loaded  = false;
    }

    // -------------------------------------------------------------------------
    // Script laden
    // -------------------------------------------------------------------------

    /*
     * Leest het Lua-script als tekst via de classpath (leading "/" = absoluut
     * pad vanaf classpath-root), compileert het met globals.load(code, naam)
     * en voert het meteen uit zodat alle functies geregistreerd worden.
     *
     * @param resourcePath pad relatief aan classpath-root, bijv. "scripts/enemy_ai.lua"
     */
    public void loadScript(String resourcePath) {
        // Zelfde aanpak als ConfigManager: getClassLoader().getResourceAsStream()
        // zonder leading slash, pad relatief aan classpath-root.
        // Werkt zodra "resources" in java.project.sourcePaths staat (settings.json).
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            System.err.println("[LuaScriptEngine] Script niet gevonden op classpath: /" + resourcePath);
            return;
        }

        try {
            // Script inlezen als één string — zelfde aanpak als voorbeeldcode
            String luaCode = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Compileren en uitvoeren: registreert alle top-level functies in globals
            LuaValue chunk = globals.load(luaCode, resourcePath);
            chunk.call();

            loaded = true;
            System.out.println("[LuaScriptEngine] Script geladen: " + resourcePath);

        } catch (LuaError e) {
            System.err.println("[LuaScriptEngine] Lua-fout bij laden van "
                    + resourcePath + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[LuaScriptEngine] Fout bij lezen script "
                    + resourcePath + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // updateEnemy aanroepen
    // -------------------------------------------------------------------------

    /*
     * Roept de Lua-functie "updateEnemy(enemyTable, deltaTime)" aan.
     *
     * STAP 1 — LuaTable vullen met de huidige enemy-staat:
     *   Alle relevante velden worden als Lua-nummers/strings ingevuld.
     *   Lua leest velden met punt-notatie: enemy.type, enemy.currentHealth, ...
     *
     * STAP 2 — Lua-functie aanroepen:
     *   globals.get("updateEnemy").call(enemyTable, deltaTime)
     *
     * STAP 3 — Gewijzigde waarden terugschrijven naar Java:
     *   Na de aanroep worden currentHealth, maxHealth en speedMul uit de
     *   tabel gelezen. Alleen waarden die daadwerkelijk veranderd zijn
     *   worden toegepast op het enemy-object.
     *
     * @param enemy     het vijand-object dat geüpdated wordt
     * @param deltaTime verstreken tijd in seconden
     */
    public void callUpdateEnemy(Enemy enemy, double deltaTime) {
        if (!loaded) return;

        LuaValue func = globals.get("updateEnemy");
        if (func.isnil()) return;

        // --- Stap 1: LuaTable vullen met enemy-data ---
        LuaTable enemyTable = new LuaTable();
        enemyTable.set("type",          LuaValue.valueOf(enemy.getType()));
        enemyTable.set("currentHealth", LuaValue.valueOf(enemy.getCurrentHealth()));
        enemyTable.set("maxHealth",     LuaValue.valueOf(enemy.getMaxHealth()));
        enemyTable.set("healthPercent", LuaValue.valueOf(enemy.getHealthPercent()));
        enemyTable.set("speedMul",      LuaValue.valueOf(enemy.getSpeedMultiplier()));

        // --- Stap 2: Lua aanroepen ---
        try {
            func.call(enemyTable, LuaValue.valueOf(deltaTime));
        } catch (LuaError e) {
            System.err.println("[LuaScriptEngine] Runtime-fout in updateEnemy: " + e.getMessage());
            return;
        } catch (Exception e) {
            System.err.println("[LuaScriptEngine] Onverwachte fout in updateEnemy: " + e.getMessage());
            return;
        }

        // --- Stap 3: gewijzigde waarden terugschrijven naar Java ---

        double newMaxHealth = enemyTable.get("maxHealth").todouble();
        if (newMaxHealth != enemy.getMaxHealth()) {
            enemy.setMaxHealth(newMaxHealth);
        }

        double newCurrentHealth = enemyTable.get("currentHealth").todouble();
        if (newCurrentHealth != enemy.getCurrentHealth()) {
            enemy.setCurrentHealth(newCurrentHealth);
        }

        double newSpeedMul = enemyTable.get("speedMul").todouble();
        if (newSpeedMul != enemy.getSpeedMultiplier()) {
            enemy.setSpeedMultiplier(newSpeedMul);
        }
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /** True als een script succesvol geladen is. */
    public boolean isLoaded() {
        return loaded;
    }
}
