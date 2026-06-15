# 🧭 Navigatiedocument — Tower Defence

Dit document wijst per criterium aan **in welk bestand** (en waar nuttig **op welke regel**) het
is geïmplementeerd, zodat het project snel na te kijken is.

> Padbasis voor alle Java-bestanden:
> `src/be/uantwerpen/fti/ei/geavanceerde/towerdefence/`
> In dit document afgekort als **`…/`**.

---

## Mijlpalen

| # | Criterium | Bestand(en) | Regel(s) | Toelichting |
|---|-----------|-------------|----------|-------------|
| 1 | **Abstract Factory patroon** | `…/game/factory/EntityFactory.java`<br>`…/j2d/J2dEntityFactory.java` | interface: `EntityFactory.java:35`<br>concrete: `J2dEntityFactory.java:44` | Interface in de game-laag, concrete fabriek in de J2D-laag. `Game` maakt álle entities via deze interface. Zie ook de enum-dispatchers `…/game/towers/TowerType.java:25` en `…/game/enemies/EnemyType.java`. |
| 2 | **Scheiding game-logica / visualisatie (packages)** | `…/game/**` vs. `…/j2d/**` | n.v.t. (mappenstructuur) | `game/` bevat geen enkele `java.awt`/`javax.swing`-import; rendering + input bereiken de logica enkel via `…/game/GameView.java` en de factory. Uitleg per package in de `package-info.java`-bestanden. |
| 3 | **Documentatie (Javadoc)** | volledige codebase + map **`javadoc/`** | n.v.t. | Elke klasse/methode heeft Javadoc; gegenereerde HTML staat in **`javadoc/index.html`**. Bouwt foutloos met `-Xdoclint:all` (0 warnings). |
| 4 | **Functioneel spel** | `…/Main.java`<br>`…/game/Game.java` | entry: `Main.java:31`<br>game loop: `Game.java:198`<br>update-tick: `Game.java:426` | `Main` start het spel; de hoofdlus (`start`) doet input → update → render → sleep. `update(...)` bevat spawnen, bewegen, targeten, botsingen, opruimen, win/lose. |
| 5 | **Stopwatch / timer** | `…/game/util/Stopwatch.java`<br>gebruikt in `…/game/Game.java` | klasse: `Stopwatch.java:26`<br>`tick()`: `Stopwatch.java:55`<br>gebruik: `Game.java:196`–`199` | `deltaTime` (in seconden) maakt beweging frame-onafhankelijk; doorgegeven aan alle `update(deltaTime)`. |
| 6 | **Minstens 2 levels** | `resources/levels/level1.properties`<br>`resources/levels/level2.properties`<br>config: `resources/config/game.properties` | `game.properties:31` (`levels.count=2`)<br>laden: `Game.java:229` (`loadLevel`) | Twee level-bestanden; `levels.count` bepaalt de progressie en het "Ultimate Victory"-scherm. |
| 7 | **Minstens 1 Lua-script** | `resources/scripts/enemy_ai.lua`<br>`…/game/scripting/LuaScriptEngine.java` | engine: `LuaScriptEngine.java:43`<br>aanroep per frame: `Game.java:442` (`callUpdateEnemy`)<br>config-pad: `game.properties:37` | LuaJ-runtime draait `updateEnemy(enemy, dt)` per vijand; ondersteunt hot-reload. |
| 8 | **Configuratiebestand** | `resources/config/game.properties`<br>`…/game/util/ConfigManager.java` | klasse: `ConfigManager.java:38` | Properties-formaat (venster, wereldgrootte, goud, levens, level, Lua-pad). Veilige defaults via `getInt/getDouble/getString`. |

---

## Object-Georiënteerd Ontwerp

| # | Criterium | Bestand(en) | Regel(s) | Toelichting |
|---|-----------|-------------|----------|-------------|
| 9 | **Singleton patroon** | `…/game/Game.java` | private ctor: `Game.java:143`<br>`getInstance()`: `Game.java:159` | Eén centrale `Game`-instantie; private constructor + `getInstance()`. |
| 10 | **In-game decimaal coördinatensysteem** | `…/game/util/Position.java`<br>conversie in `…/j2d/J2dGame.java` | `double x,y`: `Position.java:19`<br>`distanceTo`: `Position.java:79`<br>pixel↔wereld: `J2dGame.java:199` (`toScreenX`), `:239` (`toGameX`) | Game-logica rekent in `double`-wereldunits, volledig los van schermpixels; J2D zet om naar pixels. |
| 11 | **HUD met score / levenspunten** | `…/j2d/J2dGame.java` | `renderHUD()`: `J2dGame.java:532`<br>HP: `:544`, Score: `:552` | Bovenbalk toont goud, score, base-HP, level, wave en resterende vijanden; plus eind-/pauze-overlays. |
| 12 | **Data-georiënteerd ontwerp (ECS)** | `…/game/ecs/` (hele package) | datastore: `FloatingTextWorld.java`<br>systemen: `MovementSystem.java`, `LifetimeSystem.java`<br>integratie: `Game.java:135`–`137`, update `:477`–`478` | Structure-of-Arrays datastore + stateless systemen voor zwevende combat-tekst (schade-/goud-popups). Bewust data-georiënteerd i.p.v. OO. |
| 13 | **Geen `null` in game-logica** | `…/game/entities/Tower.java`<br>`…/game/Game.java`<br>`…/game/wave/WaveManager.java` | `findTarget` → `Optional`: `Tower.java:120`<br>`getBase()` → `Optional`: `Game.java:682`<br>`tick()` → `Optional`: `WaveManager.java:117` | `Optional` wordt gebruikt als retourtype i.p.v. `null`; config gebruikt defaults. |
| 14 | **Minstens 1 Java Streams API-implementatie** | `…/game/towers/ArrowTower.java`<br>`…/game/towers/CannonTower.java`<br>`…/game/towers/IceTower.java` | `ArrowTower.java:79` (`stream().filter().min()`)<br>`CannonTower.java:104` (`…max()`)<br>`IceTower.java:98` (`…forEach()`) | Targeting-strategieën via Streams: dichtste (Arrow), hoogste HP (Cannon), area-slow (Ice). |

---

### Snelle start voor de nakijker
1. **Javadoc**: open `javadoc/index.html`.
2. **Spel draaien**: `Main.java` (entry point) uitvoeren met `lib/luaj-jse-3.0.1.jar` op het classpath; `resources/` moet op het classpath staan.

> Regelnummers verwijzen naar de declaratie van de betrokken klasse/methode/sectie en zijn
> exact op het moment van schrijven; bij latere aanpassingen kunnen ze licht verschuiven.
