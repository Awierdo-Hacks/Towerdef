# Game Flow - Wat gebeurt er wanneer je het spel runt?

Stap-voor-stap overzicht van de volledige game flow, van opstarten tot afsluiten.

---

## Overzicht Flowchart

```
  ┌──────────────────────┐
  │     java Main        │
  └──────────┬───────────┘
             │
             ▼
  ┌──────────────────────┐
  │  OPSTARTEN           │
  │  1. Config laden     │
  │  2. Factory aanmaken │
  │  3. Game.init()      │
  │  4. Venster openen   │
  └──────────┬───────────┘
             │
             ▼
  ┌──────────────────────┐
  │    LEVEL LADEN       │
  │  1. Map parsen       │
  │  2. Pad berekenen    │
  │  3. Build spots      │
  │  4. Waves laden      │
  │  5. Base plaatsen    │
  └──────────┬───────────┘
             │
             ▼
  ┌──────────────────────┐       ┌──────────────┐
  │     MENU SCHERM      │◄──────│  LEVEL KEUZE │
  │  Wacht op speler...  │       │  (optioneel) │
  └──────────┬───────────┘       └──────────────┘
             │ [Start]
             ▼
  ╔══════════════════════╗
  ║     GAME LOOP        ║◄───────────────────────────────┐
  ║  (herhaalt elk frame)║                                │
  ╚══════════╤═══════════╝                                │
             │                                            │
             ▼                                            │
  ┌──────────────────────────────────────────────┐        │
  │  1. TIMING                                   │        │
  │     stopwatch.tick() → deltaTime berekenen   │        │
  └──────────────────────┬───────────────────────┘        │
                         │                                │
                         ▼                                │
  ┌──────────────────────────────────────────────┐        │
  │  2. INPUT                                    │        │
  │     Muis/toetsenbord verwerken               │        │
  │     ┌─────────────────────────┐              │        │
  │     │ Klik op build spot?     │              │        │
  │     │ Genoeg gold?            │              │        │
  │     │ → factory.createTower() │              │        │
  │     │ → gold aftrekken        │              │        │
  │     └─────────────────────────┘              │        │
  └──────────────────────┬───────────────────────┘        │
                         │                                │
                         ▼                                │
  ┌──────────────────────────────────────────────┐        │
  │  3. WAVE SPAWNING                            │        │
  │     waveManager.update(deltaTime)            │        │
  │     ┌─────────────────────────┐              │        │
  │     │ Spawn timer afgelopen?  │              │        │
  │     │ → factory.createEnemy() │              │        │
  │     │ → enemy toevoegen aan   │              │        │
  │     │   enemies list          │              │        │
  │     └─────────────────────────┘              │        │
  └──────────────────────┬───────────────────────┘        │
                         │                                │
                         ▼                                │
  ┌──────────────────────────────────────────────┐        │
  │  4. ENEMY MOVEMENT                           │        │
  │     voor elke enemy:                         │        │
  │       enemy.moveAlongPath(deltaTime)         │        │
  │       lua: updateEnemy(enemy, deltaTime)     │        │
  │                                              │        │
  │     ┌───────────────────────┐                │        │
  │     │ Enemy bereikt base?   │──── JA ──┐     │        │
  │     └───────────────────────┘          │     │        │
  │                                        ▼     │        │
  │                              base.takeDamage()│        │
  │                              enemy verwijderen│        │
  └──────────────────────┬───────────────────────┘        │
                         │                                │
                         ▼                                │
  ┌──────────────────────────────────────────────┐        │
  │  5. TOWER TARGETING & FIRING                 │        │
  │     voor elke tower:                         │        │
  │       tower.findTarget(enemies)  ← Streams   │        │
  │       ┌───────────────────────┐              │        │
  │       │ Target gevonden?      │              │        │
  │       │ Cooldown afgelopen?   │              │        │
  │       │ → factory.create      │              │        │
  │       │   Projectile()        │              │        │
  │       └───────────────────────┘              │        │
  └──────────────────────┬───────────────────────┘        │
                         │                                │
                         ▼                                │
  ┌──────────────────────────────────────────────┐        │
  │  6. PROJECTILE MOVEMENT                      │        │
  │     voor elk projectiel:                     │        │
  │       beweeg richting target positie         │        │
  │       ┌───────────────────────┐              │        │
  │       │ Raakt enemy?          │              │        │
  │       │ (collidesWith)        │──── JA ──┐   │        │
  │       └───────────────────────┘          │   │        │
  │                                          ▼   │        │
  │                            enemy.takeDamage() │        │
  │                            projectiel weg     │        │
  └──────────────────────┬───────────────────────┘        │
                         │                                │
                         ▼                                │
  ┌──────────────────────────────────────────────┐        │
  │  7. CLEANUP                                  │        │
  │     Dode enemies verwijderen                 │        │
  │     ┌───────────────────────┐                │        │
  │     │ Enemy dood?           │                │        │
  │     │ → gold += reward      │                │        │
  │     │ → score += punten     │                │        │
  │     └───────────────────────┘                │        │
  │     Aangekomen projectielen verwijderen       │        │
  └──────────────────────┬───────────────────────┘        │
                         │                                │
                         ▼                                │
  ┌──────────────────────────────────────────────┐        │
  │  8. WIN/LOSE CHECK                           │        │
  │                                              │        │
  │  ┌─────────────┐       ┌─────────────┐      │        │
  │  │ base HP ≤ 0 │       │ Alle waves  │      │        │
  │  │             │       │ klaar EN    │      │        │
  │  │             │       │ geen enemies│      │        │
  │  └──────┬──────┘       └──────┬──────┘      │        │
  │         │                     │              │        │
  │         ▼                     ▼              │        │
  │    GAME_OVER              WON               │        │
  └──────────────────────┬───────────────────────┘        │
                         │                                │
                         │ [Nog bezig?]                    │
                         │                                │
                         ▼                                │
  ┌──────────────────────────────────────────────┐        │
  │  9. RENDER                                   │        │
  │     J2dGame.render():                        │        │
  │       - Clear scherm                         │        │
  │       - Teken map/tegels                     │        │
  │       - Teken build spots                    │        │
  │       - Teken base                           │        │
  │       - Teken towers                         │        │
  │       - Teken enemies (+ health bars)        │        │
  │       - Teken projectielen                   │        │
  │       - Teken HUD (score, gold, lives, wave) │        │
  │       - Buffer swap                          │        │
  └──────────────────────┬───────────────────────┘        │
                         │                                │
                         └────────────────────────────────┘
                              Terug naar stap 1


  ┌────────────────────────────┐     ┌────────────────────────────┐
  │       GAME OVER            │     │        VICTORY             │
  │  "Game Over" tonen         │     │  "You Won!" tonen          │
  │  Score weergeven           │     │  Score weergeven           │
  │  [Retry] → level herladen  │     │  [Next Level / Menu]       │
  │  [Menu]  → terug naar menu │     │                            │
  └────────────────────────────┘     └────────────────────────────┘
```

---

## Samenvatting: Wie doet wat?

| Component | Verantwoordelijkheid |
|-----------|---------------------|
| `Main.java` | Alles opstarten en verbinden |
| `ConfigManager` | Config laden (game.properties, level files) |
| `J2dEntityFactory` | Entities aanmaken met visuele representatie |
| `Game` (Singleton) | Alle game state beheren, update logica |
| `WaveManager` | Bepalen wanneer welke vijanden spawnen |
| `Enemy.moveAlongPath()` | Vijanden langs het pad bewegen |
| `Tower.findTarget()` | Dichtsbijzijnde vijand in range vinden (Streams) |
| `Projectile` | Naar target bewegen, schade toebrengen |
| `LuaScriptEngine` | Enemy AI script uitvoeren |
| `J2dGame` | Alles tekenen op het scherm, coordinaat conversie |
| `InputHandler` | Muis/toetsenbord events doorgeven |
| `Stopwatch` | Delta time meten voor frame-onafhankelijke beweging |
