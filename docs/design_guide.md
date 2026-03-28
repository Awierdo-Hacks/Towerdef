# Tower Defence Game - Design Guide

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Package Structure](#2-package-structure)
3. [Design Patterns](#3-design-patterns)
4. [Entity Hierarchy](#4-entity-hierarchy)
5. [Abstract Factory Pattern](#5-abstract-factory-pattern)
6. [Coordinate System](#6-coordinate-system)
7. [Key Classes](#7-key-classes)
8. [Configuration](#8-configuration)
9. [Lua Scripting](#9-lua-scripting)
10. [Java Streams API](#10-java-streams-api)
11. [Null Avoidance Strategy](#11-null-avoidance-strategy)
12. [Future: ECS / Data-Oriented](#12-future-ecs--data-oriented)

---

## 1. Project Overview

A 2D Top-Down Tower Defence game built in Java using Java2D for rendering.

**Core Gameplay:**
- Enemies spawn in waves and follow predefined paths toward the player's base.
- The player places towers on designated build locations to defend the base.
- Towers automatically detect and attack enemies within their range.
- Destroying enemies grants score and gold to build/upgrade towers.
- The game ends when all waves are survived (win) or the base reaches 0 HP (lose).

**Tower Types:**
- **ArrowTower** — fast fire rate, low damage per shot.
- **CannonTower** — slow fire rate, high damage, splash damage in an area.
- **IceTower** — slows enemies within range.

**Enemy Types:**
- **BasicEnemy** — standard ground unit, moderate HP and speed.
- **ArmoredEnemy** — high HP, slow movement, resistant to damage.
- **FlyingEnemy** — ignores certain terrain, may take alternate paths.

**Game Features:**
- At least 2 levels (loaded from configuration files).
- Increasing wave difficulty (more HP, faster, special abilities).
- Score tracking visible during gameplay.
- Configuration file for game parameters.
- At least one Lua script controlling game behavior (e.g. enemy AI).

---

## 2. Package Structure

The project enforces a strict separation between **game logic** and **game presentation**.

```
src/
└── be/uantwerpen/fti/ei/geavanceerde/towerdefence/
    ├── game/              # GAME LOGIC — no visualization code allowed here
    │   ├── entities/      # Abstract entity classes (Entity, Tower, Enemy, etc.)
    │   ├── towers/        # Concrete tower logic (ArrowTower, CannonTower, IceTower)
    │   ├── enemies/       # Concrete enemy logic (BasicEnemy, ArmoredEnemy, FlyingEnemy)
    │   ├── factory/       # Abstract Factory interface (EntityFactory)
    │   ├── map/           # Map, tiles, paths
    │   ├── wave/          # Wave definitions and wave manager
    │   ├── util/          # Stopwatch, Position, ConfigManager
    │   └── scripting/     # Lua script integration
    │
    ├── j2d/               # VISUALIZATION — Java2D rendering + input handling
    │   └── entities/      # Visual entity implementations (J2dTower, J2dEnemy, etc.)
    │
    └── Main.java          # Application entry point
```

### Rules
- **`game/`** contains ALL game logic. Classes here must NEVER import `java.awt.*`, `javax.swing.*`, or any visualization library.
- **`j2d/`** contains ALL visualization and input handling. It depends on `game/` (extends abstract classes, implements factory interface).
- The game logic package can be reused with a completely different visualization (e.g. OpenGL, terminal) by writing a new visualization package that implements the same `EntityFactory` interface.

---

## 3. Design Patterns

### 3.1 Singleton Pattern — `Game`

The `Game` class is the single central object that represents the entire game state.

```java
public final class Game {
    private static Game instance;

    // Game state
    private GameState state;
    private final List<Tower> towers;
    private final List<Enemy> enemies;
    private final List<Projectile> projectiles;
    private final List<Bonus> bonuses;
    private Base base;
    private GameMap gameMap;
    private WaveManager waveManager;
    private int score;
    private int gold;

    // Factory — injected to decouple from visualization
    private EntityFactory entityFactory;

    private Game() {
        this.towers = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.bonuses = new ArrayList<>();
        this.state = GameState.MENU;
        this.score = 0;
    }

    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    /** Initialize with a concrete factory and configuration. */
    public void init(EntityFactory factory, ConfigManager config) {
        this.entityFactory = factory;
        this.gold = config.getStartingGold();
        // load level, create base, etc.
    }

    /** Main game loop tick — called each frame with delta time. */
    public void update(double deltaTime) {
        // 1. Spawn enemies from wave manager
        // 2. Move enemies along paths
        // 3. Towers find targets and fire
        // 4. Move projectiles
        // 5. Check collisions (projectile-enemy)
        // 6. Remove dead entities
        // 7. Check win/lose conditions
    }
}
```

**Key points:**
- Private constructor prevents external instantiation.
- `getInstance()` provides global access to the single instance.
- The factory is injected via `init()` — the Game never knows about Java2D.
- All entity lists, score, gold, and game state live here.

### 3.2 Abstract Factory Pattern — `EntityFactory`

The factory interface lives in the game logic package. Concrete implementations live in the visualization package.

```
game/factory/EntityFactory.java      ← interface (game logic)
j2d/J2dEntityFactory.java            ← concrete implementation (visualization)
```

This pattern allows the `Game` singleton to create entities without knowing their visual representation. See [Section 5](#5-abstract-factory-pattern) for full details.

---

## 4. Entity Hierarchy

### Game Logic Package (Abstract)

```
Entity (abstract)
├── Tower (abstract)
│   ├── ArrowTower
│   ├── CannonTower
│   └── IceTower
├── Enemy (abstract)
│   ├── BasicEnemy
│   ├── ArmoredEnemy
│   └── FlyingEnemy
├── Projectile (abstract)
├── Base (abstract)
├── Obstacle (abstract)
└── Bonus (abstract)
```

### Visualization Package (Concrete Visual Extensions)

```
J2dTower extends Tower (or specific tower subclasses)
J2dEnemy extends Enemy (or specific enemy subclasses)
J2dProjectile extends Projectile
J2dBase extends Base
J2dObstacle extends Obstacle
J2dBonus extends Bonus
```

### Entity Base Class

Every game entity shares a common abstract base:

```java
public abstract class Entity {
    protected Position position;   // Double x, y in game-world coordinates
    protected double width;
    protected double height;
    protected boolean alive;

    public Entity(Position position, double width, double height) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.alive = true;
    }

    /** Called every frame to update logic. */
    public abstract void update(double deltaTime);

    /** Visualization hook — overridden in J2d subclasses. */
    public abstract void render();

    public boolean isAlive() { return alive; }
    public Position getPosition() { return position; }

    /** Check collision with another entity (bounding box). */
    public boolean collidesWith(Entity other) {
        return Math.abs(this.position.getX() - other.position.getX()) < (this.width + other.width) / 2
            && Math.abs(this.position.getY() - other.position.getY()) < (this.height + other.height) / 2;
    }
}
```

### Tower Base Class

```java
public abstract class Tower extends Entity {
    protected double range;
    protected int damage;
    protected double fireRate;       // shots per second
    protected double fireCooldown;   // time until next shot

    public abstract void findTarget(List<Enemy> enemies);
    public abstract Optional<Projectile> fire();
}
```

### Enemy Base Class

```java
public abstract class Enemy extends Entity {
    protected double maxHealth;
    protected double currentHealth;
    protected double speed;
    protected int reward;            // gold granted on kill
    protected List<Position> path;   // waypoints to follow
    protected int currentWaypoint;

    public abstract void moveAlongPath(double deltaTime);
    public boolean hasReachedBase() {
        return currentWaypoint >= path.size();
    }
    public void takeDamage(double amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            alive = false;
        }
    }
}
```

---

## 5. Abstract Factory Pattern

### The Interface (game package)

```java
// Located in: game/factory/EntityFactory.java
public interface EntityFactory {

    // Towers
    Tower createArrowTower(Position position);
    Tower createCannonTower(Position position);
    Tower createIceTower(Position position);

    // Enemies
    Enemy createBasicEnemy(List<Position> path);
    Enemy createArmoredEnemy(List<Position> path);
    Enemy createFlyingEnemy(List<Position> path);

    // Other entities
    Projectile createProjectile(Position start, Position target, int damage);
    Base createBase(Position position, int health);
    Obstacle createObstacle(Position position);
    Bonus createBonus(Position position);
}
```

### The Concrete Factory (J2d package)

```java
// Located in: j2d/J2dEntityFactory.java
public class J2dEntityFactory implements EntityFactory {

    @Override
    public Tower createArrowTower(Position position) {
        return new J2dArrowTower(position);  // extends ArrowTower, adds rendering
    }

    @Override
    public Enemy createBasicEnemy(List<Position> path) {
        return new J2dBasicEnemy(path);      // extends BasicEnemy, adds rendering
    }

    // ... etc for all entity types
}
```

### How It Flows

```
Main.java
  │
  ├── Creates J2dEntityFactory (concrete)
  ├── Passes it to Game.getInstance().init(factory, config)
  │
  └── Game (singleton) uses EntityFactory interface
        │
        ├── game.createArrowTower(pos)  →  factory.createArrowTower(pos)
        │                                    → returns J2dArrowTower (but Game sees it as Tower)
        │
        └── Game never imports j2d.* — it only knows about abstract types
```

### Adding a New Visualization

To create a terminal-based version:
1. Create a `terminal/` package.
2. Implement `TerminalEntityFactory implements EntityFactory`.
3. Create `TerminalTower extends Tower`, etc.
4. In `Main.java`, pass `new TerminalEntityFactory()` instead of `new J2dEntityFactory()`.
5. **Zero changes to the `game/` package.**

---

## 6. Coordinate System

### Game World (Logic)
- Uses `double` coordinates via the `Position` class.
- The game world has its own dimensions (e.g. 20.0 x 15.0 units).
- All game logic (movement, collision, range) operates in these coordinates.
- Independent of screen resolution.

### Screen (Visualization)
- Uses pixel coordinates (e.g. 800 x 600 pixels).
- The `J2dGame` class handles the conversion:

```java
public class J2dGame {
    private int windowWidth;    // pixels
    private int windowHeight;   // pixels
    private double gameWidth;   // game units
    private double gameHeight;  // game units

    /** Convert game-world X to screen pixel X. */
    public int toScreenX(double gameX) {
        return (int)(gameX * (windowWidth / gameWidth));
    }

    /** Convert game-world Y to screen pixel Y. */
    public int toScreenY(double gameY) {
        return (int)(gameY * (windowHeight / gameHeight));
    }

    /** Convert screen pixel X to game-world X. */
    public double toGameX(int screenX) {
        return screenX * (gameWidth / windowWidth);
    }

    /** Convert screen pixel Y to game-world Y. */
    public double toGameY(int screenY) {
        return screenY * (gameHeight / windowHeight);
    }
}
```

### Position Class

```java
public class Position {
    private double x;
    private double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // getters, setters
}
```

---

## 7. Key Classes

| Class | Package | Responsibility |
|-------|---------|----------------|
| `Game` | game | Singleton. Holds all game state, entity lists, main update loop. |
| `GameState` | game | Enum: `MENU`, `PLAYING`, `PAUSED`, `GAME_OVER`, `WON`. |
| `Entity` | game.entities | Abstract base class for all game objects. |
| `Tower` | game.entities | Abstract tower with range, damage, fire rate. |
| `Enemy` | game.entities | Abstract enemy with health, speed, path following. |
| `Projectile` | game.entities | Abstract projectile with damage, speed, target. |
| `Base` | game.entities | The structure the player defends. Has HP. |
| `Obstacle` | game.entities | Blocks tower placement. |
| `Bonus` | game.entities | Temporary power-ups or resource drops. |
| `EntityFactory` | game.factory | Abstract Factory interface for creating entities. |
| `GameMap` | game.map | Grid of tiles, spawn points, build locations, paths. |
| `Tile` | game.map | Represents a single map cell (type + position). |
| `Path` | game.map | Ordered list of waypoints enemies follow. |
| `Wave` | game.wave | Defines which enemies spawn and when. |
| `WaveManager` | game.wave | Manages wave progression, spawn timing. |
| `Stopwatch` | game.util | Measures delta time between frames in milliseconds. |
| `Position` | game.util | Immutable double x,y coordinate pair. |
| `ConfigManager` | game.util | Loads `game.properties` for game parameters. |
| `LuaScriptEngine` | game.scripting | Loads and runs `.lua` scripts using LuaJ. |
| `J2dEntityFactory` | j2d | Concrete factory creating J2d visual entities. |
| `J2dGame` | j2d | JFrame/Canvas setup, render loop, coordinate mapping. |
| `InputHandler` | j2d | KeyListener + MouseListener for player input. |
| `J2dTower` | j2d.entities | Renders towers using Graphics2D. |
| `J2dEnemy` | j2d.entities | Renders enemies using Graphics2D. |
| `J2dProjectile` | j2d.entities | Renders projectiles using Graphics2D. |
| `J2dBase` | j2d.entities | Renders the base using Graphics2D. |
| `J2dObstacle` | j2d.entities | Renders obstacles using Graphics2D. |
| `J2dBonus` | j2d.entities | Renders bonuses using Graphics2D. |
| `Main` | (root) | Entry point. Creates factory, initializes Game, starts loop. |

---

## 8. Configuration

### game.properties

Located at `resources/config/game.properties`. Loaded by `ConfigManager`.

```properties
# Window
window.width=800
window.height=600
window.title=Tower Defence

# Game world
game.width=20.0
game.height=15.0

# Gameplay
starting.gold=100
starting.lives=20
selected.level=1

# Towers
tower.arrow.cost=50
tower.arrow.damage=10
tower.arrow.range=3.0
tower.arrow.firerate=2.0

tower.cannon.cost=100
tower.cannon.damage=40
tower.cannon.range=2.5
tower.cannon.firerate=0.5

tower.ice.cost=75
tower.ice.range=2.0
tower.ice.slowfactor=0.5
```

### Level Files

Located at `resources/levels/level1.properties`, `resources/levels/level2.properties`.

```properties
# Level 1
map.width=20
map.height=15
spawn.x=0.0
spawn.y=7.0
base.x=19.0
base.y=7.0

# Path waypoints (comma-separated x,y pairs)
path.waypoints=0.0,7.0;5.0,7.0;5.0,3.0;15.0,3.0;15.0,12.0;19.0,12.0;19.0,7.0

# Wave definitions
wave.count=5
wave.1.enemies=basic:5
wave.2.enemies=basic:8,armored:2
wave.3.enemies=basic:10,armored:4,flying:1
wave.4.enemies=basic:12,armored:6,flying:3
wave.5.enemies=basic:15,armored:8,flying:5

# Build spots (comma-separated x,y pairs)
build.spots=3.0,5.0;3.0,9.0;7.0,1.0;7.0,5.0;10.0,5.0;10.0,1.0;13.0,5.0;13.0,10.0;17.0,10.0;17.0,14.0
```

### ConfigManager

```java
public class ConfigManager {
    private final Properties properties;

    public ConfigManager(String path) {
        properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            properties.load(is);
        }
    }

    public int getInt(String key, int defaultValue) { ... }
    public double getDouble(String key, double defaultValue) { ... }
    public String getString(String key, String defaultValue) { ... }
    public int getStartingGold() { return getInt("starting.gold", 100); }
    // etc.
}
```

---

## 9. Lua Scripting

The project uses **LuaJ** (`luaj-jse-3.0.1.jar`) to run Lua scripts.

### Use Case: Enemy AI / Wave Behavior

A Lua script can control enemy behavior such as speed modifiers, special abilities, or wave composition.

### Example: `resources/scripts/enemy_ai.lua`

```lua
-- enemy_ai.lua
-- Called each frame for each enemy to determine behavior

function updateEnemy(enemy, deltaTime)
    -- Example: enemies speed up when below 50% health
    if enemy:getHealthPercent() < 0.5 then
        enemy:setSpeedMultiplier(1.5)
    end

    -- Example: armored enemies slow nearby towers
    if enemy:getType() == "armored" then
        enemy:applyAura("slow_towers", 2.0)
    end
end
```

### LuaScriptEngine

```java
public class LuaScriptEngine {
    private final Globals globals;

    public LuaScriptEngine() {
        globals = JsePlatform.standardGlobals();
    }

    public void loadScript(String path) {
        globals.get("dofile").call(LuaValue.valueOf(path));
    }

    public void callFunction(String functionName, LuaValue... args) {
        LuaValue func = globals.get(functionName);
        if (!func.isnil()) {
            func.invoke(args);
        }
    }
}
```

---

## 10. Java Streams API

At least one meaningful use of the Streams API is required. Examples:

### Finding enemies in range of a tower

```java
public Optional<Enemy> findClosestEnemy(Tower tower, List<Enemy> enemies) {
    return enemies.stream()
        .filter(Enemy::isAlive)
        .filter(e -> tower.getPosition().distanceTo(e.getPosition()) <= tower.getRange())
        .min(Comparator.comparingDouble(e -> tower.getPosition().distanceTo(e.getPosition())));
}
```

### Removing dead entities

```java
enemies.removeIf(e -> !e.isAlive());
// Or with streams for calculating total reward:
int totalReward = enemies.stream()
    .filter(e -> !e.isAlive())
    .mapToInt(Enemy::getReward)
    .sum();
```

### Calculating total score from a wave

```java
int waveScore = destroyedEnemies.stream()
    .mapToInt(Enemy::getReward)
    .sum();
```

---

## 11. Null Avoidance Strategy

The game logic must avoid using `null` values. Use these alternatives:

| Instead of `null` | Use |
|---|---|
| No target found | `Optional<Enemy>` / `Optional.empty()` |
| Empty entity list | `Collections.emptyList()` or `new ArrayList<>()` |
| No active bonus | `Optional<Bonus>` |
| Missing config value | Default values via `getInt(key, defaultValue)` |
| No current wave | `Optional<Wave>` |

### Examples

```java
// Tower targeting — returns Optional instead of null
public Optional<Enemy> findTarget(List<Enemy> enemies) {
    return enemies.stream()
        .filter(Enemy::isAlive)
        .filter(e -> position.distanceTo(e.getPosition()) <= range)
        .min(Comparator.comparingDouble(e -> position.distanceTo(e.getPosition())));
}

// Using the result
tower.findTarget(enemies).ifPresent(target -> {
    tower.fire().ifPresent(projectile -> projectiles.add(projectile));
});
```

---

## 12. Future: ECS / Data-Oriented

> **Note:** This section is a placeholder. The ECS system will be implemented later. The current architecture is object-oriented.

The project requires at least one system designed in a Data-Oriented fashion using Entity-Component-System (ECS) architecture. A good candidate:

- **MovementSystem** — processes all entities with a `PositionComponent` and `VelocityComponent`.
- **DamageSystem** — processes all entities with a `HealthComponent` when hit.

This will be implemented as a separate system alongside the existing OOP hierarchy, not as a replacement.

---

## Quick Reference: File Locations

| What | Where |
|---|---|
| Game logic source | `src/be/uantwerpen/fti/ei/geavanceerde/towerdefence/game/` |
| Visualization source | `src/be/uantwerpen/fti/ei/geavanceerde/towerdefence/j2d/` |
| Entry point | `src/be/uantwerpen/fti/ei/geavanceerde/towerdefence/Main.java` |
| Game config | `resources/config/game.properties` |
| Level definitions | `resources/levels/level1.properties`, `level2.properties` |
| Lua scripts | `resources/scripts/enemy_ai.lua` |
| Image assets | `resources/images/` |
| Lua library | `lib/luaj-jse-3.0.1.jar` |
