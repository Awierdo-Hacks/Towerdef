# Build Process - Ontwikkelingsplan

Overzicht van alle fases van begin tot eind.
Fases 1-6 zijn gedetailleerd, fases 7-10 worden abstracter naarmate het einde nadert.

---

## Flowchart Overzicht

```
 ┌─────────────────────────────────────────────────────────────────┐
 │                    FASE 1: FOUNDATION                           │
 │  Position, Stopwatch, ConfigManager, GameState, Game singleton  │
 └────────────────────────────┬────────────────────────────────────┘
                              │
                              ▼
 ┌─────────────────────────────────────────────────────────────────┐
 │                  FASE 2: ENTITY HIERARCHY                       │
 │  Entity, Tower, Enemy, Projectile, Base, Obstacle, Bonus        │
 └────────────────────────────┬────────────────────────────────────┘
                              │
                              ▼
 ┌─────────────────────────────────────────────────────────────────┐
 │                 FASE 3: ABSTRACT FACTORY                        │
 │  EntityFactory interface + concrete towers & enemies            │
 └────────────────────────────┬────────────────────────────────────┘
                              │
                              ▼
 ┌─────────────────────────────────────────────────────────────────┐
 │                 FASE 4: MAP & PATHFINDING                       │
 │  GameMap, Tile, Path, level loading                             │
 └────────────────────────────┬────────────────────────────────────┘
                              │
                              ▼
 ┌─────────────────────────────────────────────────────────────────┐
 │                FASE 5: J2D VISUALIZATION                        │
 │  J2dGame, J2dEntityFactory, J2d entities, InputHandler          │
 └────────────────────────────┬────────────────────────────────────┘
                              │
                              ▼
 ┌─────────────────────────────────────────────────────────────────┐
 │              FASE 6: GAME LOOP & MECHANICS                      │
 │  Main.java, update/render loop, targeting, collisions, waves    │
 └────────────────────────────┬────────────────────────────────────┘
                              │
                              ▼
 ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐
 │              FASE 7: WAVES & LEVELS                             │
 │  WaveManager, meerdere levels, moeilijkheidsgraad              │
 └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┬ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘
                              │
                              ▼
 ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐
 │              FASE 8: LUA & CONFIG                               │
 │  LuaScriptEngine, enemy_ai.lua, config files                   │
 └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┬ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘
                              │
                              ▼
 ┌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌┐
 ╎              FASE 9: POLISH                                     ╎
 ╎  HUD, game over schermen, balancing, bonussen                   ╎
 └ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ┬ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌┘
                              │
                              ▼
 ┌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌┐
 ╎              FASE 10: EXTRAS                                    ╎
 ╎  ECS, extra vijanden/torens, effecten, upgrades, ...            ╎
 └ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌ ╌┘
```

> Legenda: `━━━` = gedetailleerd, `─ ─` = semi-gedetailleerd, `╌ ╌` = abstract/flexibel

---

## Fase 1: Foundation (Gedetailleerd)

De basis waarop alles wordt gebouwd. Zonder deze klassen kan niets functioneren.

```
┌─────────────┐   ┌──────────────┐   ┌────────────────┐   ┌───────────────┐
│  Position    │   │  Stopwatch   │   │ ConfigManager  │   │  GameState    │
│  (double x,y)│   │  (deltaTime) │   │ (.properties)  │   │  (enum)       │
└──────┬──────┘   └──────┬───────┘   └───────┬────────┘   └──────┬────────┘
       │                 │                    │                    │
       └─────────────────┴────────────────────┴────────────────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   Game (Singleton)   │
                         │   - instance         │
                         │   - state            │
                         │   - entity lists     │
                         │   - score, gold      │
                         └─────────────────────┘
```

**Te maken bestanden:**
| Bestand | Package | Beschrijving |
|---------|---------|--------------|
| `Position.java` | game.util | Double x,y coordinaat, distanceTo() |
| `Stopwatch.java` | game.util | Tijd meten tussen frames (ms) |
| `ConfigManager.java` | game.util | Properties file laden en uitlezen |
| `GameState.java` | game | Enum: MENU, PLAYING, PAUSED, GAME_OVER, WON |
| `Game.java` | game | Singleton, private constructor, getInstance() |

---

## Fase 2: Entity Hierarchy (Gedetailleerd)

Alle abstracte entity klassen in het game package. Deze bevatten GEEN visualisatie code.

```
                         ┌──────────────────┐
                         │  Entity (abstract)│
                         │  - position       │
                         │  - width, height  │
                         │  - alive          │
                         │  + update(dt)     │
                         │  + collidesWith() │
                         └────────┬─────────┘
               ┌──────────┬──────┴──────┬──────────┬──────────┐
               ▼          ▼             ▼          ▼          ▼
        ┌───────────┐ ┌────────┐ ┌───────────┐ ┌──────┐ ┌────────┐
        │   Tower   │ │ Enemy  │ │Projectile │ │ Base │ │Obstacle│
        │ - range   │ │- health│ │ - damage  │ │- hp  │ │        │
        │ - damage  │ │- speed │ │ - speed   │ │      │ │        │
        │ - fireRate│ │- path  │ │ - target  │ │      │ │        │
        └───────────┘ └────────┘ └───────────┘ └──────┘ └────────┘
```

**Te maken bestanden:**
| Bestand | Package | Beschrijving |
|---------|---------|--------------|
| `Entity.java` | game.entities | Abstract basis: position, size, alive, update(), collidesWith() |
| `Tower.java` | game.entities | Abstract: range, damage, fireRate, findTarget(), fire() |
| `Enemy.java` | game.entities | Abstract: health, speed, path, moveAlongPath(), takeDamage() |
| `Projectile.java` | game.entities | Abstract: damage, speed, target positie |
| `Base.java` | game.entities | Abstract: health points, takeDamage() |
| `Obstacle.java` | game.entities | Abstract: blokkeert tower plaatsing |
| `Bonus.java` | game.entities | Abstract: effect type, duur |

---

## Fase 3: Abstract Factory (Gedetailleerd)

Het Abstract Factory pattern + concrete game-logic subtypes.

```
  ┌──────────────────────────┐
  │  <<interface>>           │
  │  EntityFactory           │
  │  + createArrowTower()    │
  │  + createCannonTower()   │
  │  + createIceTower()      │
  │  + createBasicEnemy()    │
  │  + createArmoredEnemy()  │
  │  + createFlyingEnemy()   │
  │  + createProjectile()    │
  │  + createBase()          │
  └──────────────────────────┘

  Concrete tower/enemy klassen (game logic only, geen rendering):

  Tower                          Enemy
    ├── ArrowTower                 ├── BasicEnemy
    ├── CannonTower                ├── ArmoredEnemy
    └── IceTower                   └── FlyingEnemy
```

**Te maken bestanden:**
| Bestand | Package | Beschrijving |
|---------|---------|--------------|
| `EntityFactory.java` | game.factory | Interface met alle create-methodes |
| `ArrowTower.java` | game.towers | Snel vuren, lage schade |
| `CannonTower.java` | game.towers | Langzaam, hoge schade, splash |
| `IceTower.java` | game.towers | Vertraagt vijanden |
| `BasicEnemy.java` | game.enemies | Standaard vijand |
| `ArmoredEnemy.java` | game.enemies | Veel HP, langzaam |
| `FlyingEnemy.java` | game.enemies | Negeert bepaald terrein |

---

## Fase 4: Map & Pathfinding (Gedetailleerd)

De kaart, tegels en paden die vijanden volgen.

```
  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
  │   GameMap     │─────▶│    Tile      │      │    Path      │
  │ - grid[][]   │      │ - type       │      │ - waypoints  │
  │ - buildSpots │      │ - position   │      │   (Position) │
  │ - spawnPoint │      │ - walkable   │      │              │
  │ - path       │      │ - buildable  │      │              │
  └──────────────┘      └──────────────┘      └──────────────┘
                              │
                        TileType enum:
                        GRASS, PATH, WATER,
                        BUILD_SPOT, SPAWN, BASE
```

**Te maken bestanden:**
| Bestand | Package | Beschrijving |
|---------|---------|--------------|
| `GameMap.java` | game.map | Laadt level uit .properties, bevat grid en pad |
| `Tile.java` | game.map | Enkele tegel met type en positie |
| `Path.java` | game.map | Lijst van waypoints die vijanden volgen |

---

## Fase 5: J2D Visualization (Gedetailleerd)

Het volledige visualisatie-package. Implementeert de EntityFactory en extends alle abstracte entities.

```
  ┌────────────────────────┐
  │   J2dEntityFactory     │──implements──▶ EntityFactory
  │   + createArrowTower() │
  │     → new J2dArrowTower│
  └────────────────────────┘

  ┌─────────────────┐     ┌─────────────────┐
  │   J2dGame       │     │  InputHandler   │
  │ - JFrame        │     │ - KeyListener   │
  │ - Canvas        │     │ - MouseListener │
  │ - BufferStrategy│     │ - key/mouse     │
  │ + toScreenX()   │     │   state         │
  │ + toScreenY()   │     └─────────────────┘
  │ + render()      │
  └─────────────────┘

  J2d entity klassen:
  ┌──────────────┐ ┌──────────────┐ ┌────────────────┐
  │ J2dTower     │ │ J2dEnemy     │ │ J2dProjectile  │
  │ extends Tower│ │ extends Enemy│ │ extends        │
  │ + draw(g2d)  │ │ + draw(g2d)  │ │   Projectile   │
  └──────────────┘ └──────────────┘ │ + draw(g2d)    │
                                    └────────────────┘
  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
  │ J2dBase      │ │ J2dObstacle  │ │ J2dBonus     │
  │ extends Base │ │ extends      │ │ extends Bonus│
  │ + draw(g2d)  │ │   Obstacle   │ │ + draw(g2d)  │
  └──────────────┘ │ + draw(g2d)  │ └──────────────┘
                   └──────────────┘
```

**Te maken bestanden:**
| Bestand | Package | Beschrijving |
|---------|---------|--------------|
| `J2dGame.java` | j2d | JFrame + Canvas, render loop, coordinate mapping |
| `J2dEntityFactory.java` | j2d | Concrete factory, maakt J2d entities |
| `InputHandler.java` | j2d | KeyListener + MouseListener |
| `J2dTower.java` | j2d.entities | Tekent torens met Graphics2D |
| `J2dEnemy.java` | j2d.entities | Tekent vijanden met Graphics2D |
| `J2dProjectile.java` | j2d.entities | Tekent projectielen |
| `J2dBase.java` | j2d.entities | Tekent de basis |
| `J2dObstacle.java` | j2d.entities | Tekent obstakels |
| `J2dBonus.java` | j2d.entities | Tekent bonussen |

---

## Fase 6: Game Loop & Mechanics (Gedetailleerd)

Alles samenvoegen tot een werkend spel.

```
  Main.java
    │
    ├── ConfigManager laden
    ├── J2dEntityFactory aanmaken
    ├── Game.getInstance().init(factory, config)
    ├── J2dGame venster openen
    │
    └── GAME LOOP ─────────────────────────────────────────┐
         │                                                  │
         ├── stopwatch.tick() → deltaTime                   │
         ├── input verwerken (tower plaatsen)                │
         ├── waveManager.update() → enemies spawnen          │
         ├── enemies bewegen langs pad                       │
         ├── towers zoeken targets → schieten projectielen   │
         ├── projectielen bewegen naar target                 │
         ├── collision check (projectiel ↔ enemy)            │
         ├── dode entities verwijderen, gold/score updaten    │
         ├── win/lose check                                  │
         ├── render alles via J2dGame                        │
         └── herhaal ──────────────────────────────────────┘
```

**Te maken bestanden:**
| Bestand | Package | Beschrijving |
|---------|---------|--------------|
| `Main.java` | (root) | Entry point, wiring, start game loop |

---

## Fase 7: Waves & Levels (Semi-gedetailleerd)

```
  ┌─────────────────┐      ┌──────────────────┐
  │  WaveManager    │─────▶│     Wave         │
  │ - waves list    │      │ - enemy entries  │
  │ - currentWave   │      │ - spawnInterval  │
  │ + update(dt)    │      │ - difficulty     │
  └─────────────────┘      └──────────────────┘

  - Level 1 en Level 2 laden uit properties files
  - Waves worden moeilijker (meer HP, sneller, speciale vijanden)
  - Meerdere spawn points mogelijk
```

**Te maken bestanden:**
| Bestand | Package |
|---------|---------|
| `Wave.java` | game.wave |
| `WaveManager.java` | game.wave |
| `level1.properties` | resources/levels |
| `level2.properties` | resources/levels |

---

## Fase 8: Lua & Config (Semi-gedetailleerd)

```
  ┌───────────────────┐      ┌──────────────────┐
  │ LuaScriptEngine   │─────▶│  enemy_ai.lua    │
  │ - LuaJ globals    │      │  (Lua script)    │
  │ + loadScript()    │      └──────────────────┘
  │ + callFunction()  │
  └───────────────────┘

  - game.properties voor window size, starting gold, lives
  - Lua script voor enemy AI (speed buffs, abilities)
  - Config file selecteert actief level
```

**Te maken bestanden:**
| Bestand | Locatie |
|---------|---------|
| `LuaScriptEngine.java` | game.scripting |
| `enemy_ai.lua` | resources/scripts |
| `game.properties` | resources/config |

---

## Fase 9: Polish (Abstract)

> Deze fase is flexibel en wordt ingevuld naarmate het project vordert.

- Score en gold HUD overlay
- Game over scherm (verloren)
- Victory scherm (gewonnen)
- Menu scherm met level selectie
- Balancing van tower stats, enemy stats, wave difficulty
- Bonus entities implementeren
- Visuele feedback (health bars, range indicators)

---

## Fase 10: Extras (Abstract)

> Optionele uitbreidingen, afhankelijk van beschikbare tijd.

- ECS systeem (verplicht minimum 1 systeem)
- Extra vijand types
- Extra tower types
- Tower upgrades
- Particle effects en animaties
- Geluid / muziek
- Collectables op de map
- Moving obstacles
- Meerdere paden per level
- Highscore systeem
