-- =============================================================================
-- enemy_ai.lua  —  HP-test script
-- =============================================================================
-- Doel: verifiëren dat LuaScriptEngine correct werkt door de HP van vijanden
-- aan te passen vanuit Lua.
--
-- De Java-kant stuurt een LuaTable door met deze velden (dot-notatie):
--   enemy.type            → "basic", "armored" of "flying"
--   enemy.currentHealth   → huidig HP (number)
--   enemy.maxHealth       → maximaal HP (number)
--   enemy.healthPercent   → currentHealth / maxHealth  (0.0 – 1.0)
--   enemy.speedMul        → huidige snelheidsmultiplier (number)
--
-- Lua past de tabel aan, Java leest de gewijzigde waarden terug en
-- schrijft ze naar het Enemy-object.
-- =============================================================================

-- DEBUG: verschijnt in de console zodra het script geladen wordt.
-- Als je deze regel ziet, werkt LuaScriptEngine.loadScript() correct.
print("[Lua] enemy_ai.lua geladen!")

-- -----------------------------------------------------------------------------
-- Schakelaar: kies de testmodus
--   "boost"  → maxHP × HP_MULTIPLIER bij eerste aanroep (bijna onsterfelijk)
--   "drain"  → HP elke frame verlagen met HP_DRAIN_PER_SECOND (sterft vanzelf)
-- -----------------------------------------------------------------------------
local TEST_MODE = "boost"

-- Factor waarmee maxHP vergroot wordt (enkel bij TEST_MODE = "boost")
local HP_MULTIPLIER = 100

-- HP per seconde dat afgetrokken wordt (enkel bij TEST_MODE = "drain")
local HP_DRAIN_PER_SECOND = 50

-- maxHP boven deze waarde = boost al toegepast (voorkom herhaald vermenigvuldigen)
local BOOST_MARKER = 1000

-- Teller voor periodieke debug-output (elke 60 aanroepen ≈ elke seconde bij 60fps)
local callCount = 0

-- =============================================================================
-- Hoofd-functie — elke frame aangeroepen voor elke levende vijand
-- =============================================================================

function updateEnemy(enemy, deltaTime)
    callCount = callCount + 1

    -- Print elke 60 aanroepen zodat je in de console kunt zien dat Lua actief is
    if callCount % 60 == 1 then
        print(string.format(
            "[Lua] #%d | type=%-7s | hp=%6.1f / %6.1f (%.0f%%)",
            callCount,
            enemy.type,
            enemy.currentHealth,
            enemy.maxHealth,
            enemy.healthPercent * 100
        ))
    end

    if TEST_MODE == "boost" then
        applyHpBoost(enemy)
    elseif TEST_MODE == "drain" then
        applyHpDrain(enemy, deltaTime)
    end
end

-- =============================================================================
-- Testmodus 1: HP-boost
-- Vergroot maxHP eenmalig met HP_MULTIPLIER zodat vijanden bijna onsterfelijk
-- worden. De BOOST_MARKER voorkomt dat de boost elke frame opnieuw toegepast
-- wordt.
-- =============================================================================
function applyHpBoost(enemy)
    if enemy.maxHealth < BOOST_MARKER then
        local newMax = enemy.maxHealth * HP_MULTIPLIER
        -- Tabel aanpassen — Java leest deze waarden terug na de aanroep
        enemy.maxHealth     = newMax
        enemy.currentHealth = newMax

        print(string.format(
            "[Lua] HP BOOST op %s: %.0f → %.0f",
            enemy.type, enemy.maxHealth / HP_MULTIPLIER, newMax
        ))
    end
end

-- =============================================================================
-- Testmodus 2: HP-drain
-- Trekt elke frame HP_DRAIN_PER_SECOND × deltaTime af. Vijanden sterven
-- vanzelf zonder dat een toren ze hoeft te raken.
-- =============================================================================
function applyHpDrain(enemy, deltaTime)
    local newHp = enemy.currentHealth - HP_DRAIN_PER_SECOND * deltaTime
    enemy.currentHealth = newHp

    if newHp <= 0 then
        print(string.format("[Lua] %s gestorven door HP-drain!", enemy.type))
    end
end
