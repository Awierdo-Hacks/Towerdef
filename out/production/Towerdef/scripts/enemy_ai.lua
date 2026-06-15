-- =============================================================================
-- enemy_ai.lua — Enemy-gedrag via Lua
-- =============================================================================
-- Elke levende vijand roept updateEnemy() aan per frame.
--
-- Java geeft een tabel mee:
--   enemy.type          → "basic" | "armored" | "flying"
--   enemy.currentHealth → huidig HP
--   enemy.maxHealth     → maximaal HP
--   enemy.healthPercent → currentHealth / maxHealth  (0.0 – 1.0)
--   enemy.speedMul      → huidige snelheidsmultiplier
--
-- Lua past de tabel aan; Java schrijft de gewijzigde waarden terug.
-- Alleen currentHealth, maxHealth en speedMul worden door Java gelezen.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Configuratie — pas deze waarden aan om het gedrag te veranderen
-- -----------------------------------------------------------------------------

-- Basissnelheid per type (1.0 = standaard)
local BASE_SPEED = {
    basic   = 20,
    armored = 0.7,   -- zwaar gepantserd, langzamer
    flying  = 1.4,   -- vliegt, sneller
}

-- Rage-modus: onder deze HP-drempel wordt de vijand sneller
local RAGE_HP_THRESHOLD = 0.3    -- 30% HP resterend
local RAGE_SPEED_BONUS  = 0.2    -- +50% snelheid op basissnelheid

-- =============================================================================
-- Hoofd-functie — elke frame aangeroepen voor elke levende vijand
-- =============================================================================

function updateEnemy(enemy, _deltaTime)
    local baseSpeed = BASE_SPEED[enemy.type] or 1.0
    local speed     = baseSpeed

    -- Rage-modus: vijand versnelt als hij bijna dood is
    if enemy.healthPercent < RAGE_HP_THRESHOLD then
        speed = baseSpeed + RAGE_SPEED_BONUS
    end

    enemy.speedMul = speed
end
