# Defense Guide — Tower Defence

> A single reference for defending this project before the jury. It does three things:
> **(1)** proves every mandatory requirement is met and points to the exact code,
> **(2)** rates how hard each file is to reason about (so you know where the tough
> questions live), and **(3)** explains the "advanced" Java/Lua APIs used, with a
> rehearsal Q&A you can practice out loud.

## How to use this guide

- **Before the defense:** read §1 (requirement matrix) so you can answer "where is X?"
  instantly, then §3 (feature catalog) for the APIs you must be able to name, and
  rehearse §4 (Q&A) out loud.
- **This guide does not repeat the other docs.** Read them for depth:
  - [`design_guide.md`](design_guide.md) — full architecture, patterns, coordinate system.
  - [`game_flow.md`](game_flow.md) — the 11-step game loop, step by step.
  - [`build_process.md`](build_process.md) — the 10-phase development roadmap.
  - [`ongebruikte-code-analyse.md`](ongebruikte-code-analyse.md) — dead/unused code analysis (NL).
- All line numbers below were verified against the source on 2026-06-15. If you edit
  files, the numbers may drift — the method names are the stable anchor.

The project: **47 Java files + 1 Lua script**, package root
`be.uantwerpen.fti.ei.geavanceerde.towerdefence`, split into a pure-logic `game/`
package and a Java2D `j2d/` package.

---

## 1. Requirement coverage matrix

| # | Requirement | Status | Where (file · method/line) | How it is satisfied |
|---|-------------|--------|----------------------------|---------------------|
| 1 | **Singleton** (Game) | ✅ Met | `game/Game.java:62` field, `:143` private ctor, `:153` `getInstance()` | `private static Game instance;` + private constructor + lazy `getInstance()`. Class is `final`. |
| 2 | **Abstract Factory** | ✅ Met | `game/factory/EntityFactory.java:38` (interface) · `j2d/J2dEntityFactory.java:44` (impl) | 9 `create*` methods + `getView()`; return types are abstract (`Tower`, `Enemy`…) so game never sees `J2d*`. Injected in `Game.start():176`. |
| 3 | **Strict game/ ↔ j2d/ separation** | ✅ Met | verified: `grep "import java.awt\|javax.swing"` under `game/` → **0 hits** | Only `j2d/` imports AWT/Swing. `game/` talks to the view through the `GameView` interface (`game/GameView.java`). |
| 4 | **Entity hierarchy** (abstract in game, visual in j2d) | ✅ Met | `game/entities/{Entity,Enemy,Tower,Projectile,Base}` + `j2d/entities/J2d*` | `Entity.render()` is abstract (`Entity.java:66`); each `J2d*` subclass implements it. Logic subclasses stay `abstract` until the J2d layer adds rendering. |
| 5 | **Double world coords ≠ pixels** | ✅ Met | `game/util/Position.java` (double x,y) · conversion `j2d/J2dGame.java:189` `toScreenX/Y`, `:209` `toGameX/Y` | Game logic is in world units (20×15); J2dGame converts world↔pixel only at the edges. |
| 6 | **Stopwatch delta-time** | ✅ Met | `game/util/Stopwatch.java:55` `tick()` (uses `System.nanoTime`) · used `game/Game.java:190` | Per-frame `deltaTime` in seconds multiplies all movement/timers → frame-rate independent. |
| 7 | **Score display** | ✅ Met | `game/Game.java:584` accrual · HUD `j2d/J2dGame.java:516` · ECS popups `Game.java:586` | Cumulative score across levels; drawn in the HUD and as floating `+Ng` popups. |
| 8 | **Java Streams API** | ✅ Met | `ArrowTower.java:61`, `CannonTower.java:80`, `IceTower.java:82`; `Game.java:444` `removeIf` | `filter` + `min`/`max`/`forEach`, method refs (`Enemy::isAlive`), comparators. |
| 9 | **Lua scripting** (luaj-jse-3.0.1) | ✅ Met | `game/scripting/LuaScriptEngine.java` + `resources/scripts/enemy_ai.lua` | Per-frame `updateEnemy(enemyTable, dt)` controls enemy speed (rage mode). Hot-reload on file change. |
| 10 | **Properties config** | ✅ Met | `game/util/ConfigManager.java:67` `Properties.load` · `resources/config/game.properties` | Typed getters with defaults; loaded from classpath. |
| 11 | **At least two levels** | ✅ Met | `resources/levels/level1.properties`, `level2.properties` · loaded `Game.java:223` `loadLevel(int)` | `levels.count=2`; `WON` screen offers "next level" via `nextLevel():264`. |
| 12 | **KeyEvent input** | ✅ Met | `j2d/InputHandler.java:73` `keyPressed` with `KeyEvent.VK_*` (`:78–84`) | Keys 1/2/3 select towers, ESC deselect, P pause, S start, Q quit; mouse places towers. |
| 13 | **ECS / data-oriented system** | ✅ Met | `game/ecs/{FloatingTextWorld,MovementSystem,LifetimeSystem,FloatingTextKind}` | Structure-of-Arrays datastore + two stateless systems; swap-remove compaction. Deliberately non-OOP. |
| 14 | **Avoid null values** | ✅ Met | `Optional<Base>` `Game.java:85`; `findTarget` returns `Optional<Enemy>` (`Tower.java:95`); `WaveManager.tick → Optional<String>` | Sprite cache and config getters return safe defaults instead of throwing on missing data. |

**Caveats to be ready for (honesty points with a jury):**

- **Req 13 (ECS) is intentionally a small subsystem** (floating combat text only), per the
  OOP-first design. Be ready to say *why*: the rest of the game is classic OOP and the ECS
  exists to demonstrate the data-oriented style on transient effects. See `FloatingTextWorld.java:5–28`.
- **Stale comment in `Tower.java:30`** claims *"IceTower — fastest enemy in range (max by speed)"*.
  The real `IceTower.findTarget()` (`IceTower.java:98`) returns `Optional.empty()` — ice deals no
  single-target damage; it applies an area slow in `applyAreaEffect()`. If asked, point to the real
  method, not the comment.
- **`SpriteManager.getSprite` caches `null`** (`SpriteManager.java:33`): a missing PNG is cached as
  `null` so it is not retried every frame; entities fall back to drawn shapes. That is deliberate, not a bug.

---

## 2. Complexity map

Each file rated **Straightforward → Moderate → Complex → Very Complex** by how much a
reader must hold in their head at once (control flow, state, external API, timing).

### 🔴 Very Complex — expect the hardest questions here (4 files)

| File | Lines | Why it's hard |
|------|------:|---------------|
| `game/Game.java` | 669 | The singleton hub. `update()` (`:396`) is an **11-step ordered loop** where step order *matters* (e.g. Lua runs at step 2b *before* IceTower at step 5 so ice always wins). Owns all entity lists, score/gold, state machine, wave spawning, collision, win/lose. Uses `Iterator.remove()` for safe in-loop deletion (`:494`, `:579`). |
| `j2d/J2dGame.java` | 639 | The whole render pipeline: `BufferStrategy` double-buffering, `RenderingHints`, world↔pixel math, a 6-layer draw order, HUD, menu/pause/win/lose overlays, ECS text rendering. The `render()` `try/finally` (`:228`) must always `dispose()`+`show()`. |
| `game/map/GameMap.java` | 352 | Level parsing **and** path rasterisation. `markSegment()` (`:263`) walks tiles for horizontal, vertical *and* diagonal segments (linear interpolation). Grid build order (`:198`) is load-bearing: grass → path → build → spawn → base so later types override earlier ones. |
| `game/scripting/LuaScriptEngine.java` | 211 | A hot-reload state machine over the luaj API. Tracks file mtime, rebuilds `Globals` on change, marshals an `Enemy` into a `LuaTable`, calls the Lua function, reads three fields back. Two failure modes (compile-time `LuaError`, runtime error) handled separately. |

### 🟠 Complex (5 files)

| File | Lines | Why |
|------|------:|-----|
| `game/wave/WaveManager.java` | 243 | 3-state timing machine (waiting / spawning / finished) + parses `"basic:8,armored:2"` lines. `tick()` returns `Optional<String>`. |
| `game/entities/Enemy.java` | 215 | Waypoint-following state machine (`moveAlongPath():104`), slow timer with auto-expiry, Lua-writable HP/speed setters. |
| `game/entities/Tower.java` | 159 | Abstract contract for 3 strategies; cooldown math guards `1.0/0` for IceTower (`:140`). |
| `game/entities/Entity.java` | 102 | Root of the whole hierarchy; AABB `collidesWith()` (`:79`) underpins all hits; defensive `Position` copy in ctor (`:38`). |
| `game/entities/Projectile.java` | 108 | Linear homing toward a fixed target + polymorphic `onHit()` (ray vs splash). |

### 🟡 Moderate (≈18 files)

Towers `ArrowTower`/`CannonTower`/`IceTower` (Streams targeting), `CannonProjectile` (splash via Streams),
`ArmoredEnemy` (resistance override), `FlyingEnemy` (air path), `ConfigManager`, `Position`,
`J2dEntityFactory`, `EntityFactory`, `FloatingTextWorld`, all `J2d*` renderers
(`J2dBase`, `J2dFlyingEnemy`, `J2dBasicEnemy`, `J2dArmoredEnemy`, `J2dIceTower`, …), `InputHandler`, `Wave`.

### 🟢 Straightforward (≈18 files)

Pure data / dispatch / glue: `Stopwatch`, `Main`, `SpriteManager`, `Path`, `Tile`, `Base`,
`BasicEnemy`, `RayProjectile`, `MovementSystem`, `LifetimeSystem`, the enums
(`GameState`, `TileType`, `EnemyType`, `TowerType`, `FloatingTextKind`), `GameView`.

**`enemy_ai.lua`** (46 lines): Straightforward — a per-type base-speed table plus a
"rage mode" that speeds an enemy up below 30 % HP.

---

## 3. Advanced feature catalog

Group by topic. Each row: **what the API is → where → what it does here.** These are the
terms a jury probes ("what is a BufferStrategy?"). Learn to say each one in a sentence.

### 3.1 Java2D rendering — all in `j2d/J2dGame.java`

| API / type | Line | One-sentence explanation |
|------------|-----:|--------------------------|
| `Graphics2D` | 81, 229 | The 2D drawing context; everything is drawn through it. Obtained fresh each frame from the buffer. |
| `BufferStrategy` | 62, 173–174 | Manages off-screen buffers. `canvas.createBufferStrategy(2)` = **double buffering**: draw to a hidden buffer, then flip — no flicker/tearing. |
| `bufferStrategy.getDrawGraphics()` | 229 | Hands you the `Graphics` for the *back* buffer to draw this frame. |
| `bufferStrategy.show()` | 295 | Flips the back buffer to the screen (the "page flip"). |
| `g2d.dispose()` | 294 | Releases the graphics context's native resources; done in a `finally` so it always runs. |
| `RenderingHints.KEY_ANTIALIASING` | 233–236 | Turns on antialiasing → smooth (non-jagged) edges on circles/lines/text. |
| `RenderingHints.KEY_INTERPOLATION` (`VALUE_INTERPOLATION_BILINEAR`) | 240–243 | Bilinear sampling so large source PNGs downscale to ~40 px smoothly instead of looking harsh. |
| `Canvas` | 61, 164 | A lightweight AWT component you can render to directly with a buffer strategy (faster than painting a `JComponent`). |
| `g2d.drawImage(img, x, y, w, h, null)` | 444, 453, 456 | Draws a sprite scaled to a tile; the `ImageObserver` arg is `null` because the image is already fully loaded. |
| `fillRect/fillOval/drawOval/drawString/drawRect` | 247, 363–365, 333… | Primitive shape + text drawing for tiles, range rings, HUD, overlays. |
| `getFontMetrics().stringWidth()` | 577 | Measures text width to centre overlay titles horizontally. |

### 3.2 Windowing & input

| API | Where | Explanation |
|-----|-------|-------------|
| `JFrame` + `EXIT_ON_CLOSE` | `J2dGame.java:159–160` | The OS window; closing it exits the JVM. |
| `frame.pack()` / `setLocationRelativeTo(null)` | `J2dGame.java:168–169` | Size to the canvas's preferred size, then centre on screen. |
| `KeyListener` / `MouseListener` / `MouseMotionListener` | `InputHandler.java:28` | One class implements all three input callback interfaces. |
| `KeyEvent.getKeyCode()` + `VK_1…VK_Q` | `InputHandler.java:74–84` | Maps physical keys to actions via a `switch`. |
| `MouseEvent.getButton() == BUTTON1` | `InputHandler.java:104` | Only left-clicks place towers. |
| **Consume-once flags** | `InputHandler.java:140–176` | `wasXPressed()` returns `true` once then resets — turns a held key into a single discrete event for the game loop. |

### 3.3 Game loop & timing

| API | Where | Explanation |
|-----|-------|-------------|
| `System.nanoTime()` | `Stopwatch.java:42, 56` | Monotonic high-resolution clock (nanoseconds); difference ÷ 1e9 = seconds elapsed. |
| `Thread.sleep(16)` | `Game.java:203` | Caps the loop near ~60 FPS by sleeping ~16 ms per frame. |
| `InterruptedException` + `Thread.currentThread().interrupt()` | `Game.java:204–206` | Correctly re-asserts the interrupt flag and exits the loop instead of swallowing the interrupt. |

### 3.4 Image loading & caching — `j2d/SpriteManager.java`

| API | Line | Explanation |
|-----|-----:|-------------|
| `ImageIO.read(InputStream)` | 43 | Decodes a PNG into a `BufferedImage`. |
| `getClassLoader().getResourceAsStream("images/…")` | 38 | Loads from the classpath, so it works from IDE *and* a JAR (no hard-coded paths). |
| try-with-resources `(InputStream is = …)` | 38 | Auto-closes the stream even on exception. |
| `static Map<String,BufferedImage> cache` | 20 | Each sprite is decoded once; subsequent calls hit the `HashMap`. Missing files cache `null` (drawn-shape fallback). |

### 3.5 luaj integration — `game/scripting/LuaScriptEngine.java`

The **Java → Lua → Java round trip** (the single most likely deep-dive topic):

| Step | API | Line | What happens |
|------|-----|-----:|--------------|
| Init | `JsePlatform.standardGlobals()` | 64, 126 | Creates a `Globals` with the standard Lua libs (math/string/table). Rebuilt on reload to clear old state. |
| Compile | `globals.load(new FileReader(file), name)` | 128 | Compiles the `.lua` text into a `LuaValue` chunk. |
| Define | `chunk.call()` | 129 | Runs the chunk so `function updateEnemy(...)` becomes a global. |
| Look up | `globals.get("updateEnemy")` + `.isnil()` | 179–180 | Fetches the Lua function; bails if the script didn't define it. |
| Marshal in | `new LuaTable()` + `enemyTable.set(k, LuaValue.valueOf(v))` | 183–188 | Copies the enemy's fields (type, HP, healthPercent, speedMul) into a Lua table. |
| Invoke | `func.call(enemyTable, LuaValue.valueOf(dt))` | 192 | Calls the Lua function with the table + delta-time. |
| Marshal out | `enemyTable.get("speedMul").todouble()` | 202–209 | Reads back the three writable fields and pushes changes to the Java `Enemy`. |
| Hot-reload | `Files.getLastModifiedTime(...).toMillis()` | 131, 156 | Every 500 ms (`CHECK_INTERVAL_MS`) compares mtime; reloads on change without restarting the game. |

Error handling: `LuaError` is caught separately for both compile (`:135`) and runtime
(`:193`) failures, so a broken script degrades gracefully instead of crashing the game.

### 3.6 Streams & functional style

| Pattern | Where | Explanation |
|---------|-------|-------------|
| `filter` + `min(Comparator.comparingDouble(...))` | `ArrowTower.java:61–64` | Keep alive in-range enemies, pick the **closest**. |
| `filter` + `max(Comparator.comparingDouble(Enemy::getCurrentHealth))` | `CannonTower.java:80–83` | Pick the **highest-HP** enemy (best splash value). |
| `filter` + `forEach(e -> e.applySlow(...))` | `IceTower.java:82–85` | Slow **every** in-range enemy (no return value). |
| Method reference `Enemy::isAlive` | `ArrowTower.java:62` | Shorthand for `e -> e.isAlive()`. |
| `List.removeIf(p -> !p.isAlive())` | `Game.java:444` | One-line cleanup of dead projectiles. |
| `Optional.ifPresent(lambda)` | `Game.java:468, 498`; `J2dGame.java:262` | Run code only if a value exists — e.g. spawn an enemy, damage the base, render the base. |
| `Optional.map(...).orElse("?")` | `J2dGame.java:507–509` | Build the base-HP HUD string without a null check. |

### 3.7 Enums with behaviour (polymorphic dispatch)

| Enum | Where | What's advanced |
|------|-------|-----------------|
| `EnemyType` | `enemies/EnemyType.java:26–28` | Each constant has a **constant-specific body** implementing `abstract Enemy create(factory, path)` → dispatches to the right factory method with no `switch`. `fromId()` (`:50`) maps config strings; `usesAirPath()` flag keeps the "is it flying?" question in one place. |
| `TowerType` | `towers/TowerType.java:22–24` | Same pattern for towers; carries a `range` field so the view can draw a placement preview *without* instantiating a tower (`getRange():41`). `fromHotkey()` maps keys 1/2/3. |
| `GameState`, `TileType`, `FloatingTextKind` | resp. files | Plain state/category enums (the state machine, terrain kinds, semantic text kind). |

This is the **Open/Closed Principle** in action: a new enemy/tower = one new enum constant +
one factory method; `Game.java` never changes.

### 3.8 OOP backbone & collections

| Feature | Where | Note |
|---------|-------|------|
| Abstract classes + `@Override` | `Entity`/`Enemy`/`Tower`/`Projectile` + subclasses | `render()` abstract in `Entity` is the hinge of the game/viz split. |
| Defensive copy | `Entity.java:38` | Copies the incoming `Position` so callers can't mutate an entity's location by reference. |
| `Iterator` + `it.remove()` | `Game.java:494, 579` | Safe removal while iterating (avoids `ConcurrentModificationException`). |
| Static nested class | `Wave.EnemyEntry` | Small immutable record of `(type, count)` for a wave. |
| Generics | `List<Tower>`, `Map<String,BufferedImage>`, `Optional<Enemy>` | Type-safe containers throughout. |

### 3.9 Config & resource I/O — `game/util/ConfigManager.java`

| API | Line | Note |
|-----|-----:|------|
| `java.util.Properties` + `.load(InputStream)` | 41, 67 | Parses `key=value` files. |
| `getResourceAsStream` (classpath) | 58 | Same portability story as sprites. |
| try-with-resources | 66 | Auto-close the config stream. |
| Typed getters with `NumberFormatException` fallback | 85–96, 108–118 | `getInt/getDouble/getString/getBoolean` always return a usable value → supports the "avoid nulls / never crash on bad config" requirement. |

---

## 4. Rehearsal Q&A

Practice these answers out loud. Each points to code you can open if pressed.

**Q: Why is `Game` a Singleton, and what are the downsides?**
There must be exactly one game world, score, and entity registry; a Singleton guarantees one
shared instance reachable from anywhere (`getInstance()`, `Game.java:153`) without threading the
object through every constructor. Downsides: it is effectively global state, which makes unit
testing harder (you can't easily swap a fresh `Game`) and hides dependencies. I mitigate this by
**injecting** the `EntityFactory` and `ConfigManager` through `start()` rather than hard-coding them.

**Q: Explain the Abstract Factory. What problem does it solve and how does it keep `game/` free of Java2D?**
`EntityFactory` (`game/factory/EntityFactory.java:38`) is an interface with `createArrowTower`,
`createBasicEnemy`, etc., all returning **abstract** game types. The concrete `J2dEntityFactory`
(`j2d/J2dEntityFactory.java`) returns `J2d*` objects that implement `render()` with `Graphics2D`,
but typed as `Tower`/`Enemy`. So `Game` only ever holds an `EntityFactory` reference and never
imports a single `J2d` class — verified: zero `java.awt`/`javax.swing` imports under `game/`. To
swap to a console renderer I'd write one new class implementing `EntityFactory` and change one line
in `Main`.

**Q: What is a `BufferStrategy` and why `createBufferStrategy(2)`? What do `show()` and `dispose()` do?**
A `BufferStrategy` (`J2dGame.java:173`) manages off-screen buffers for a `Canvas`. The `2` means
two buffers (double buffering): I draw the whole frame into the hidden back buffer, then
`bufferStrategy.show()` (`:295`) flips it to the screen in one step, so the user never sees a
half-drawn frame (no flicker/tearing). `g2d.dispose()` (`:294`) frees the native resources of that
frame's graphics context; it's in a `finally` block so it runs even if drawing throws.

**Q: What do the two `RenderingHints` do?**
`KEY_ANTIALIASING = ON` (`:233`) smooths the edges of shapes and text. `KEY_INTERPOLATION = BILINEAR`
(`:240`) controls how images are resampled when scaled — my source PNGs are large (e.g. 1024²) and
get drawn at ~40 px, so bilinear interpolation makes them downscale smoothly instead of looking
blocky.

**Q: Could you have simplified the rendering — dropped `BufferStrategy` and the hints — since the game is simple?**
I looked at this, and removing them is *not* a real simplification. The renderer uses **active
rendering**: the game loop calls `view.render()` every frame (`Game.java:199`); `J2dGame.render()`
(`:228`) pulls the frame's `Graphics2D` from `bufferStrategy.getDrawGraphics()` (`:229`), and all 9
`J2d*` entity classes read it back via `getGraphics2D()` (`:612`) before it's flipped with
`dispose()` + `show()` (`:295`). Given that:
- **Removing `BufferStrategy` makes things worse, not simpler.** The only alternatives are a
  single-buffered `canvas.getGraphics()`, which visibly flickers/tears every frame, or moving to a
  Swing `JPanel.paintComponent()`, which pushes rendering onto the EDT via `repaint()` coalescing —
  *more* complex to reason about and still double-buffered underneath. Double buffering is the thing
  that makes the animation look stable, so I keep it explicit.
- **The two `RenderingHints` are ~8 lines and earn their keep.** Bilinear interpolation (`:240`) is
  what stops the large source PNGs (~1024px) from looking blocky when downscaled to ~40px, and
  antialiasing (`:233`) smooths the range rings and health bars. The maintenance cost is negligible.

So the rendering is intentionally left as-is: the "advanced" pieces are the idiomatic, simplest way to
get flicker-free, crisp output for an actively-rendered game loop.

**Q: Walk me through how an enemy gets passed to Lua and the result read back.**
Each frame, for every living enemy, `LuaScriptEngine.callUpdateEnemy()` (`LuaScriptEngine.java:175`)
builds a `LuaTable`, copies type/HP/healthPercent/speedMul into it via `LuaValue.valueOf` (`:184–188`),
fetches the global `updateEnemy` function (`:179`), and calls it with the table and delta-time
(`func.call(...)`, `:192`). The Lua script mutates `enemy.speedMul`; back in Java I read
`enemyTable.get("speedMul").todouble()` (`:208`) and write it onto the `Enemy`. Only three fields are
writable (HP, maxHP, speed). It also checks the file's modified time every 500 ms and recompiles on
change (hot-reload).

**Q: Why double world coordinates instead of pixels? Where do you convert?**
Game logic (movement, range, collision) is resolution-independent: the world is 20×15 units
regardless of window size (`Position.java`, doubles). Pixels only appear at the rendering boundary —
`J2dGame.toScreenX/Y` (`:189`) convert world→pixels to draw, and `toGameX/Y` (`:209`) convert the
mouse pixel position back to world units for tower placement. Change the window size and gameplay is
unaffected.

**Q: Why `Optional` instead of `null`? Give an example.**
`Optional` makes "might be absent" explicit in the type, so the compiler/reader can't forget the
empty case. Example: `tower.findTarget(enemies)` returns `Optional<Enemy>` (`Tower.java:95`); the
loop does `if (target.isPresent())` before firing (`Game.java:523`). The base is `Optional<Base>`
(`Game.java:85`) and I use `base.ifPresent(b -> b.takeDamage(1))` (`:498`) so there's no null check.

**Q: Explain `ArrowTower.findTarget()` line by line.**
(`ArrowTower.java:61–64`) `enemies.stream()` opens a pipeline; `.filter(Enemy::isAlive)` drops dead
enemies; `.filter(e -> towerPos.distanceTo(e.getPosition()) <= range)` keeps only those in range;
`.min(Comparator.comparingDouble(e -> towerPos.distanceTo(...)))` returns the closest one as an
`Optional<Enemy>` (empty if none in range). `CannonTower` is identical but uses `.max(... by
currentHealth)`; `IceTower` uses `.forEach(... applySlow)`.

**Q: Why is your ECS data-oriented and not OOP? What is swap-remove?**
The ECS (`game/ecs/`) stores floating combat text as **parallel arrays** (`FloatingTextWorld.java:46–52`),
one array per field — Structure-of-Arrays — with the "entity" being just an index. The
`MovementSystem`/`LifetimeSystem` are stateless and sweep those arrays by index. It's deliberately the
opposite style of the OOP entities to demonstrate the data-oriented approach on cheap, numerous,
short-lived objects. **Swap-remove** (`LifetimeSystem.java:25`): to delete index `i` in O(1), copy the
last live element into slot `i` and decrement the count, keeping all live elements packed in `[0,count)`
with no gaps and no array shifting.

**Q: How does delta-time make movement frame-rate independent?**
`Stopwatch.tick()` (`Stopwatch.java:55`) returns seconds since the last frame. Movement is
`speed * deltaTime` (`Enemy.java:110–111`), so on a slow PC (big `deltaTime`) an entity takes a bigger
step and on a fast PC a smaller one — the real-world speed is identical. Same idea for tower cooldowns
and the slow timer.

**Q: How are the two levels configured and loaded, and how do waves get parsed?**
`game.properties` sets `levels.count=2`. `loadLevel(n)` (`Game.java:220`) loads
`levels/level{n}.properties` into a `GameMap` (map size, spawn/base, waypoints, build spots) and a
`WaveManager`. The wave config is lines like `wave.2.enemies=basic:8,armored:2`;
`WaveManager.parseEnemyLine()` (`:178`) splits on `,` then `:` into `EnemyEntry(type,count)`, skipping
malformed tokens. On `WON`, `handleInput` calls `nextLevel()` (`:264`) unless it was the last level.

**Q: Prove `game/` has no visualization dependency.**
Run `grep -rE "import (java\.awt|javax\.swing)" src/.../game/` → no matches. The only bridge is the
`GameView` interface (`game/GameView.java`), implemented by `J2dGame`; `Game` gets it from
`factory.getView()` (`Game.java:179`) and calls `view.render()` without knowing the concrete type.

**Q: Why does `IceTower.findTarget()` return empty? Isn't that wrong?**
No — the IceTower never fires a projectile. It overrides `applyAreaEffect()` (`IceTower.java:79`) to
slow every enemy in range each frame, and `isReadyToFire()` returns `false` (`:121`) so the firing
path is skipped entirely. `findTarget()` returns `Optional.empty()` only to satisfy the abstract
contract in `Tower`. (Note: the class-level comment in `Tower.java:30` about "fastest enemy" is stale —
trust the method.)

**Q: How do you avoid a `ConcurrentModificationException` when enemies die mid-loop?**
Removal that happens during iteration uses an explicit `Iterator` and `it.remove()`
(`Game.java:494, 579`); the stream-based reads never mutate the list. Projectile cleanup uses
`removeIf` (`:444`) which is also safe.

---

## 5. One-paragraph elevator pitch (for the opening)

> "It's a Java2D top-down tower defence. The architecture is built around two patterns: a **Singleton**
> `Game` that owns the world and runs an 11-step game loop, and an **Abstract Factory** that creates all
> entities, which lets me keep the `game/` logic package completely free of any Java2D import — rendering
> lives only in `j2d/`. Game logic runs in double-precision world coordinates and is frame-rate independent
> via a delta-time `Stopwatch`. Levels, waves, and tuning come from `.properties` files; enemy speed
> behaviour is scripted in **Lua** (luaj) with hot-reload; and I included a small **data-oriented ECS** for
> floating combat text to contrast with the OOP entity hierarchy."
