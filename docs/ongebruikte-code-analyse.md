# Analyse: ongebruikte variabelen & functies

> Scope: alles binnen `src/`. Dit is enkel een oplijsting met reden — er is niets
> gewijzigd of verwijderd. Getters/setters die wél via Lua, de factory of de HUD
> gebruikt worden zijn hier bewust **niet** opgenomen.

## 1. Hele features die nooit geïmplementeerd zijn

### Bonus-systeem (volledig ongebruikt)
De `Game`-loop maakt nooit bonussen aan (`createBonus` wordt nergens aangeroepen) en
`collect()` wordt nooit getriggerd. De hele keten staat klaar maar hangt nergens aan vast.

| Element | Locatie | Reden |
|---|---|---|
| `EntityFactory.createBonus()` | `factory/EntityFactory.java:96` | Idee niet geïmplementeerd — `Game` spawnt nooit bonussen |
| `J2dEntityFactory.createBonus()` | `j2d/J2dEntityFactory.java:137` | Implementatie van bovenstaande, dus ook nooit aangeroepen |
| `J2dBonus` (hele klasse) | `j2d/entities/J2dBonus.java` | Wordt enkel in de factory genoemd, nooit geïnstantieerd |
| `Bonus.collect()` | `entities/Bonus.java:69` | Nergens aangeroepen (geen klik-detectie op bonussen) |
| `Bonus.applyEffect()` | `entities/Bonus.java:81` | Enkel aangeroepen via `collect()`, die zelf dood is |
| `Bonus.isCollected()` | `entities/Bonus.java:87` | Getter nooit gebruikt |
| `Bonus.getLifetime()` | `entities/Bonus.java:88` | Getter nooit gebruikt |
| `Bonus.getAge()` | `entities/Bonus.java:89` | Getter nooit gebruikt |
| veld `collected` | `entities/Bonus.java:28` | Alleen geschreven/gelezen binnen de dode `collect()`-keten |

`Bonus.getRemainingTime()` wordt wél gebruikt (in `J2dBonus.render()`), maar omdat
`J2dBonus` zelf nooit gemaakt wordt is ook dat transitief dood.

### Obstacle-systeem (volledig ongebruikt)
Net als Bonus: nooit aangemaakt door de game.

| Element | Locatie | Reden |
|---|---|---|
| `EntityFactory.createObstacle()` | `factory/EntityFactory.java:93` | Idee niet geïmplementeerd — geen obstakels op de map |
| `J2dEntityFactory.createObstacle()` | `j2d/J2dEntityFactory.java:132` | Implementatie, nooit aangeroepen |
| `J2dObstacle` (hele klasse) | `j2d/entities/J2dObstacle.java` | Enkel in factory genoemd, nooit geïnstantieerd |

### Vliegpad voor FlyingEnemy (Fase 4, niet afgewerkt)
De code zegt het zelf in de comments: "TODO (Fase 4)". `GameMap` heeft wél een
`flyingPath`, maar geeft die nooit door aan de enemy via `setFlyingPath()`.

| Element | Locatie | Reden |
|---|---|---|
| `FlyingEnemy.setFlyingPath()` | `enemies/FlyingEnemy.java:68` | Fase 4 niet afgewerkt — map-loader roept dit nooit aan |
| `FlyingEnemy.hasFlyingPath()` | `enemies/FlyingEnemy.java:81` | Getter nooit gebruikt |
| veld `flyingPath` | `enemies/FlyingEnemy.java:51` | Wordt op `null` gezet en nooit nuttig ingevuld |

> Let op: `GameMap.hasFlyingPath()` (andere klasse) wordt wél gebruikt in
> `Game.updateSpawner()`. Verwar deze twee niet.

## 2. Dubbel/parallel geïmplementeerd — één variant wint

### `CannonTower.applySplashDamage()` vs. inline splash in Game
`CannonTower` heeft een nette `applySplashDamage()`-methode (met Streams), maar
`Game.checkProjectileCollisions()` (`Game.java:506-512`) doet de splash-berekening
zélf inline op het projectiel. De methode op de tower wordt dus nooit aangeroepen.

| Element | Locatie | Reden |
|---|---|---|
| `CannonTower.applySplashDamage()` | `towers/CannonTower.java:98` | Game implementeert splash inline op het projectiel i.p.v. via de tower |
| `CannonTower.hasSplashDamage()` (getter) | `towers/CannonTower.java:109` | Getter nooit gebruikt |
| veld `hasSplashDamage` | `towers/CannonTower.java:48` | Enkel in constructor op `true` gezet, daarna enkel gelezen door de ongebruikte getter |

> `getSplashRadius()`/`getSplashDamage()` van CannonTower wórden wel gebruikt
> (in `updateTowerFiring`, om de waarden naar het projectiel te kopiëren).

### `IceTower.findTarget()` — dood op runtime
`findTarget()` is `abstract` in `Tower`, dus IceTower móét het implementeren (compileert
anders niet). Maar `Game.updateTowerFiring()` roept `findTarget()` alleen aan ná
`isReadyToFire()`, en `IceTower.isReadyToFire()` geeft altijd `false`. Het echte effect
loopt via `applyAreaEffect()`. De methode wordt dus nooit uitgevoerd — de comment zegt
zelf "used purely for UI feedback", maar die UI bestaat niet.

| Element | Locatie | Reden |
|---|---|---|
| `IceTower.findTarget()` | `towers/IceTower.java:97` | Verplicht door abstracte parent, maar nooit aangeroepen (target-indicator UI niet gebouwd) |

## 3. Getters/setters die nooit aangeroepen worden (API klaargezet "voor later")

**Tower (`entities/Tower.java`)**
- `getRange()` (l.129) — nooit gebruikt (range wordt intern direct via veld `range` benaderd, o.a. in `J2dIceTower.render()`)
- `getFireRate()` (l.131) — nooit gebruikt
- `getCost()` (l.132) — nooit gebruikt; kosten worden via de statische `DEFAULT_COST`-constanten bepaald in `Game.handleInput()`

**Projectile (`entities/Projectile.java`)**
- `getDamage()` (l.112) — nooit gebruikt
- `getSpeed()` (l.113) — nooit gebruikt
- `getTargetPosition()` (l.114) — nooit gebruikt
- `hasReachedTarget()` (l.119) — nooit gebruikt (Game checkt `isAlive()` rechtstreeks)

**IceTower (`towers/IceTower.java`)**
- `getSlowFactor()` (l.119) — nooit gebruikt
- `getSlowDuration()` (l.120) — nooit gebruikt

**ArmoredEnemy (`enemies/ArmoredEnemy.java`)**
- `getDamageResistance()` (l.76) — nooit gebruikt (resistance wordt enkel intern in `takeDamage()` toegepast)

**GameMap (`map/GameMap.java`)**
- `getSpawnPoint()` (l.334) — nooit gebruikt
- `getBuildSpots()` (l.338) — nooit gebruikt (placement gaat via `canBuildAt()`)

**Tile (`map/Tile.java`)**
- `isWalkable()` (l.39) — nooit gebruikt (er is geen pathfinding op tiles; enemies volgen waypoints)
- `getPosition()` (l.58) — nooit gebruikt
- `toString()` (l.64) — nooit gebruikt (enkel debug)

**Path (`map/Path.java`)**
- `getStartPosition()` (l.48) — nooit gebruikt
- `getEndPosition()` (l.53) — nooit gebruikt
- `getWaypointCount()` (l.57) — nooit gebruikt
- `isEmpty()` (l.61) — nooit gebruikt
- `toString()` (l.66) — nooit gebruikt

**Wave (`wave/Wave.java`)**
- `getTotalEnemies()` (l.131) — nooit gebruikt
- `getSpawnQueue()` (l.141) — comment zegt "voor tests/debug", maar er zijn geen tests die dit aanroepen

**WaveManager (`wave/WaveManager.java`)**
- `getTimeUntilNextWave()` (l.235) — nooit gebruikt (HUD toont geen aftelteller)
- `getWaves()` (l.240) — "voor tests/debug", nergens aangeroepen

**Game (`game/Game.java`)**
- `getEntityFactory()` (l.94) — nooit gebruikt
- `setBase()` (l.592) — nooit gebruikt (base wordt intern in `setupLevel()` gezet)
- `addScore()` (l.600) — nooit gebruikt (score wordt direct via `score +=` opgehoogd in `cleanupDeadEnemies()`)
- `addGold()` (l.603) — alleen aangeroepen vanuit `J2dBonus.applyEffect()`, dat zelf dood is → transitief ongebruikt

**LuaScriptEngine (`scripting/LuaScriptEngine.java`)**
- `isLoaded()` (l.217) — nooit gebruikt (interne `loaded`-vlag wordt al binnen de klasse gecheckt)

**ConfigManager (`util/ConfigManager.java`)**
- `getBoolean()` (l.142) — nooit gebruikt (geen enkele boolean-config-key in gebruik)

**Stopwatch (`util/Stopwatch.java`)**
- `reset()` (l.69) — nooit gebruikt; de comment beschrijft het bedoelde gebruik (na pauze de deltaTime-spike vermijden), maar `Game` roept het bij unpause niet aan → idee niet afgewerkt

**Position (`util/Position.java`)**
- `translate()` (l.94) — nooit gebruikt (helper "voor later", verschoven posities worden nergens berekend)

## 4. Input-API die deels braak ligt

In `InputHandler` (`j2d/InputHandler.java`):

| Element | Locatie | Reden |
|---|---|---|
| `isKeyDown()` | l.140 | Nooit gebruikt |
| `keys[]` array | l.36 | Wordt in `keyPressed`/`keyReleased` gevuld, maar alléén gelezen door het ongebruikte `isKeyDown()` → effectief dode toestand |
| `getMouseScreenX()` | l.170 | Nooit gebruikt (game gebruikt de game-world variant) |
| `getMouseScreenY()` | l.171 | Nooit gebruikt |
| `setSelectedTower()` | l.179 | Nooit gebruikt (selectie verloopt via toetsen in `keyPressed`) |

## 5. J2dGame: getters die nergens aangeroepen worden

In `j2d/J2dGame.java` — een reeks "complete API" getters die niemand gebruikt:

| Element | Locatie | Reden |
|---|---|---|
| `getInputHandler()` | l.404 | Nooit gebruikt |
| `getCanvas()` | l.405 | Nooit gebruikt |
| `getFrame()` | l.426 | Nooit gebruikt |
| `getWindowWidth()` | l.427 | Nooit gebruikt |
| `getWindowHeight()` | l.428 | Nooit gebruikt |
| `getGameWidth()` | l.429 | Nooit gebruikt |
| `getGameHeight()` | l.430 | Nooit gebruikt |

## 6. Lua-script (`resources/scripts/enemy_ai.lua`)

| Element | Locatie | Reden |
|---|---|---|
| parameter `_deltaTime` | l.36 | Bewust ongebruikt (underscore-prefix); de functie-signatuur moet het meekrijgen van Java, maar de logica gebruikt het niet |

> Geen ongebruikte variabele, maar wel een inconsistentie: `RAGE_SPEED_BONUS` (l.30)
> heeft waarde `0.2` maar de comment zegt **"+50% snelheid"**. De variabele wórdt
> gebruikt, alleen klopt de comment niet (0.2 = +20%).

---

## Samenvatting per oorzaak

- **Idee niet geïmplementeerd:** Bonus-keten, Obstacle-keten, FlyingEnemy-vliegpad
  (Fase 4), `Stopwatch.reset()`, `Position.translate()`, `ConfigManager.getBoolean()`,
  IceTower target-indicator (`findTarget`).
- **Parallel/dubbel opgelost — andere variant wint:** `CannonTower.applySplashDamage()`
  (Game doet splash inline), `Game.addScore()` (direct `score +=`), `Game.setBase()`
  (interne toewijzing).
- **"Volledige API" reflex — getters/setters voor de zekerheid:** de hele lijst in
  secties 3, 4 en 5.

---

# 7. Aanbevelingen — weghalen, houden of herzien

Beoordelingscriteria (in deze volgorde):

1. **Raakt het een verplichte richtlijn?** (Singleton, Abstract Factory, game/viz-scheiding,
   entity-hiërarchie, double-coördinaten, Stopwatch/deltaTime, Streams, Lua, config, 2 levels,
   "vermijd null").
2. **OOP-first** — is de ongebruikte code net de *nettere* OOP-oplossing die een huidige,
   minder propere aanpak zou corrigeren?
3. **Lost het een reëel probleem op** of bewaakt het iets dat in de huidige architectuur
   niet kan voorkomen?
4. **Risico/moeite** van verwijderen.

> Belangrijk: geen enkele verplichte richtlijn hangt af van de hieronder als "weghalen"
> gemarkeerde code. De Abstract Factory blijft volledig aangetoond door towers, enemies,
> projectiles en base; de entity-hiërarchie eveneens. Bonussen/obstakels weghalen
> verzwakt het patroon dus **niet**.

## A. Veilig weghalen (oude ideeën — geen richtlijn raakt eraan)

| Wat | Waarom veilig | Sleep mee |
|---|---|---|
| **Bonus-keten volledig** (`Bonus`, `J2dBonus`, `EntityFactory.createBonus`, `J2dEntityFactory.createBonus`) | Idee dat je niet meer implementeert. Patroon blijft intact. | `Game.addGold()` wordt **enkel** door `J2dBonus.applyEffect()` aangeroepen → wordt dan ook dood. Verwijder mee, of laat staan als je addGold later voor iets anders wilt. |
| **Obstacle-keten volledig** (`Obstacle`, `J2dObstacle`, `EntityFactory.createObstacle`, `J2dEntityFactory.createObstacle`) | Idem — nooit geïmplementeerd. | — |
| **`FlyingEnemy.setFlyingPath()` + `hasFlyingPath()` + veld `flyingPath`** | ⚠️ **Verwijderen breekt de vliegfunctie NIET.** Vliegvijanden krijgen hun luchtpad al via de constructor: `Game.updateSpawner()` geeft `gameMap.getFlyingPath().getWaypoints()` door aan `createFlyingEnemy(...)`, en `level{1,2}.properties` definiëren `path.flying.waypoints`. De `setFlyingPath`-route is een tweede, overbodig mechanisme dat nooit wordt aangeroepen. | — |

Doe dit als **verticale verwijderingen** (interface → J2d-implementatie → abstracte base)
zodat de Abstract Factory consistent blijft.

## B. Kritische gevallen — "corrigeert dit een foute omgang?"

Dit zijn net de gevallen die je bedoelde. Conclusie: bij nader inzien corrigeren ze
**geen** reëel probleem in de huidige architectuur.

### B1 — Splash: `CannonTower.applySplashDamage()` vs. inline in `Game`
Op het eerste zicht lijkt de inline splash in `Game.checkProjectileCollisions()` de
"foute" (niet-OOP) aanpak en `applySplashDamage()` de propere. **Maar**: het projectiel
is *bewust* ontkoppeld van zijn tower (zie de comment in `Projectile.java`: het bewaart
geen enemy-/tower-referentie om dode referenties te vermijden). Op het botsmoment heeft
`Game` dus geen `CannonTower`-referentie meer — enkel de splash-data die op het projectiel
gekopieerd is. `applySplashDamage()` op de tower kán daardoor niet betrokken worden zonder
die ontkoppeling terug te draaien.

- **Verdict:** de inline/projectiel-data-aanpak is architectuur-consistent. `applySplashDamage()`,
  `hasSplashDamage()` en het veld `hasSplashDamage` zijn een **verlaten alternatief** → **weghalen**.
- **Alternatief als je OOP-punten wil scoren:** verplaats de splash niet naar de tower maar
  naar een `onHit()`-override op het projectiel (bv. een `J2dCannonProjectile`/`CannonProjectile`).
  Dat is een *refactor*, geen verwijdering, en past beter bij OOP-first dan de logica in de
  Game-loop. Optioneel — niet vereist.

### B2 — `Stopwatch.reset()`
De comment zegt dat dit de deltaTime-spike na een pauze moet vermijden. **Dat probleem
bestaat niet** in de huidige loop: `Game.start()` roept `stopwatch.tick()` élke frame aan,
óók tijdens `PAUSED` (alleen `update()` keert vroeg terug). DeltaTime accumuleert dus nooit.

- **Verdict:** niet nodig. Weghalen kan. Houden enkel als vangnet zinvol is *indien* je de
  pauze later zo herwerkt dat de loop écht stilvalt (bv. blokkeren op input) — dán zou `reset()`
  bij het hervatten wél nodig worden. Zet desnoods een comment die dat expliciet maakt.

### B3 — `IceTower.findTarget()`
Nooit aangeroepen op runtime (IceTower vuurt niet), **maar** `findTarget()` is `abstract`
in `Tower`. Verwijderen kan niet zonder de hiërarchie te breken; het toont bovendien de
verplichte Streams-API.

- **Verdict:** **houden**. Eventueel de misleidende comment ("UI feedback") bijstellen,
  of het lichaam vereenvoudigen tot `return Optional.empty();` als je de stream-demonstratie
  liever enkel bij Arrow/Cannon houdt.

## C. Lage prioriteit — opruimen mag, geen richtlijn dwingt iets

Puur "voor de zekerheid" toegevoegde API. Verwijderen = nettere codebase; laten staan =
geen kwaad. Doe dit in één aparte opruim-commit zodat de diff klein en reviewbaar blijft:

- **Getters die nergens geroepen worden** (sectie 3): `Tower.getRange/getFireRate/getCost`,
  `Projectile.getDamage/getSpeed/getTargetPosition/hasReachedTarget`,
  `IceTower.getSlowFactor/getSlowDuration`, `ArmoredEnemy.getDamageResistance`,
  `GameMap.getSpawnPoint/getBuildSpots`, `Tile.isWalkable/getPosition/toString`,
  `Path.getStartPosition/getEndPosition/getWaypointCount/isEmpty/toString`,
  `Game.getEntityFactory/setBase/addScore`, `LuaScriptEngine.isLoaded`, `Position.translate`.
- **Input-API** (sectie 4): `InputHandler.isKeyDown` + de `keys[]`-array + `getMouseScreenX/Y`
  + `setSelectedTower`. De `keys[]`-array is enkel "levend" voor het ongebruikte `isKeyDown()`.
- **J2dGame-getters** (sectie 5): `getInputHandler/getCanvas/getFrame/getWindowWidth/
  getWindowHeight/getGameWidth/getGameHeight`.

**Twee nuances waar "houden" verdedigbaar is:**
- `ConfigManager.getBoolean()` — config is een *verplichte* richtlijn. Een volledige,
  symmetrische `getInt/getDouble/getString/getBoolean`-set is een nette util-API en is
  goed te verantwoorden, ook al gebruik je `getBoolean` (nog) niet.
- `WaveManager.getTimeUntilNextWave/getWaves` en `Wave.getTotalEnemies/getSpawnQueue` —
  rechtstreeks bruikbaar voor (a) een HUD-aftelteller tot de volgende golf en (b) unit tests.
  Houden als je een van beide nog plant; anders weg.

## Voorgestelde volgorde

1. ✅ **Bonus + Obstacle** verticaal verwijderd (incl. `Game.addGold`, `Game.bonuses`,
   `getBonuses`, en de bonus-renderlus in `J2dGame`).
2. ✅ **FlyingEnemy**: `setFlyingPath` + `hasFlyingPath` + `flyingPath` weg — vliegen blijft werken.
3. ✅ **CannonTower**: splash **gerefactord** naar een projectiel-`onHit()`-override
   (zie hieronder). `applySplashDamage` + `hasSplashDamage()` + veld verwijderd.
4. ✅ **Stopwatch.reset()**: verwijderd (incl. comment).
5. ✅ **IceTower.findTarget()**: behouden, body vereenvoudigd tot `Optional.empty()` +
   comment bijgewerkt; ongebruikte `Comparator`-import verwijderd.
6. ✅ **Sectie C**: uitgevoerd (zie detail hieronder).

## Uitgevoerd op 2026-06-13 — detail

**Sectie A (verwijderd):** `Bonus`, `Obstacle`, `J2dBonus`, `J2dObstacle`,
`EntityFactory.createBonus/createObstacle`, `J2dEntityFactory.createBonus/createObstacle`,
`Game.bonuses`/`getBonuses`/`addGold` en de bonus-renderlus in `J2dGame`.
`FlyingEnemy.setFlyingPath`/`hasFlyingPath`/`flyingPath`.

**B1 — splash-refactor (uitgevoerd als de OOP-plus variant):**
- Nieuwe abstracte klasse `game/entities/CannonProjectile` met de splash-logica in
  `onHit(target, enemies)`; nieuwe `j2d/entities/J2dCannonProjectile` (cannon-sprite).
- `Projectile.onHit(Enemy)` → `Projectile.onHit(Enemy, List<Enemy>)`; de splash-velden,
  `setSplash`, `getSplashRadius`/`getSplashDamage` zijn van de base `Projectile` verwijderd
  (ze leven nu op `CannonProjectile`).
- `EntityFactory.createCannonProjectile(...)` toegevoegd; `Game.updateTowerFiring()` kiest
  nu `createCannonProjectile` voor een `CannonTower`, anders `createProjectile`.
- `Game.checkProjectileCollisions()` roept enkel nog `p.onHit(e, enemies)` aan — de inline
  splash-lus is weg. De Game-loop bevat dus geen projectiel-type-specifieke code meer.
- `J2dProjectile` rendert nu altijd de ray-sprite (geen `splashRadius`-check meer).
- `CannonTower` behoudt `splashRadius`/`splashDamage` + getters (de factory leest ze uit).

**Validatie:** volledige `javac`-build van `src/` met `luaj-jse-3.0.1.jar` op de classpath
slaagt zonder fouten.

## Uitgevoerd op 2026-06-14 — sectie C detail

**Verwijderd (ongebruikte methodes):**
- `Tower`: `getRange`, `getFireRate`, `getCost`
- `Projectile`: `getSpeed`, `getTargetPosition`, `hasReachedTarget` (`getDamage` blijft —
  sinds B1 gebruikt door `CannonProjectile`)
- `IceTower`: `getSlowFactor`, `getSlowDuration`
- `ArmoredEnemy`: `getDamageResistance`
- `Enemy`: `getSpeed` — **verweesd door B3** (was gebruikt door de oude
  `IceTower.findTarget`); meegenomen als logisch gevolg
- `GameMap`: `getSpawnPoint`, `getBuildSpots` (+ ongebruikte `Collections`-import)
- `Tile`: `isWalkable`, `getPosition`, `toString`
- `Path`: `getStartPosition`, `getEndPosition`, `getWaypointCount`, `isEmpty`, `toString`
- `Game`: `getEntityFactory`, `setBase`, `addScore`
- `LuaScriptEngine`: `isLoaded`
- `Position`: `translate`
- `InputHandler`: `isKeyDown` + de `keys[]`-array (+ schrijfacties in `keyPressed`/`keyReleased`),
  `getMouseScreenX`, `getMouseScreenY`, `setSelectedTower`
- `J2dGame`: `getInputHandler`, `getCanvas`, `getFrame`, `getWindowWidth`, `getWindowHeight`,
  `getGameWidth`, `getGameHeight`

**Bewust behouden (jouw keuze — verdedigbaar):**
- `ConfigManager.getBoolean()` — symmetrische config-API (config is een verplichte richtlijn)
- `WaveManager.getTimeUntilNextWave`, `WaveManager.getWaves`, `Wave.getTotalEnemies`,
  `Wave.getSpawnQueue` — bruikbaar voor een latere HUD-aftelteller of unit tests

**Bewust NIET aangeraakt (buiten scope sectie C — optionele vervolgstap):**
Door het schrappen van enkel de getters zijn twee private velden nu "alleen-schrijven":
- `Tile.position` (+ `Position`-parameter in de constructor) — verwijderen raakt ook de
  `new Tile(...)`-aanroep in `GameMap.initializeGrid()`.
- `Tower.cost` — verwijderen raakt de constructor + alle `super(...)`-aanroepen in elke
  tower-subklasse.

Beide compileren probleemloos maar zijn dode toestand; laat maar weten als je ze ook wil
opruimen.

**Validatie:** volledige `javac -Xlint:all`-build van `src/` met `luaj-jse-3.0.1.jar` slaagt.
De 4 resterende waarschuwingen zijn pre-existing `this-escape`-meldingen in `GameMap` en
`J2dGame` (constructors die methodes/`this` gebruiken) — niet door deze opschoning veroorzaakt.

## Stemt deze analyse overeen met de richtlijnen?

Ja. Alles wat ik als "weghalen" markeer raakt **geen** verplichte richtlijn, en de
verplichte patronen (Singleton, Abstract Factory, game/viz-scheiding, entity-hiërarchie,
Streams, Stopwatch, Lua, config, 2 levels) blijven volledig aangetoond. De enige
gevallen waar een richtlijn *wél* meespeelt — `IceTower.findTarget()` (abstract contract +
Streams) en eventueel `ConfigManager.getBoolean()` (config-util) — heb ik expliciet bij
"houden/verdedigbaar" gezet. De vermeende "correcties" (`applySplashDamage`, `Stopwatch.reset`)
blijken bij analyse geen reëel probleem op te lossen in de huidige architectuur, dus ze zijn
niet nodig om "foute omgang" recht te trekken.