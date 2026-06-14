# Towerdef Agent Guide

## What this project is
- `Towerdef` is a Java 2D tower-defence game with a hard split between pure game logic and Java2D rendering/input.
- The main entry point is `src/.../Main.java`, which only wires `ConfigManager`, `J2dEntityFactory`, and `Game.getInstance().start(...)`.

## Architecture that matters
- Keep `src/.../game/` free of `java.awt` and `javax.swing`; all rendering and input live in `src/.../j2d/`.
- `Game` is a singleton and owns the whole runtime state: towers, enemies, projectiles, bonuses, base, score, gold, `GameState`.
- `EntityFactory` is the abstraction seam; `J2dEntityFactory` builds the concrete Java2D entities and also provides `GameView`.
- World logic uses double-precision game units (`Position`, `Entity`, `Enemy`, `Tower`); `J2dGame` converts to pixels.

## Runtime flow
- The frame loop in `Game.start()` is: input → `update(deltaTime)` → `view.render()` → sleep.
- `GameState` drives UI and updates: `MENU`, `PLAYING`, `PAUSED`, `GAME_OVER`, `WON`.
- Towers target enemies with `Optional<Enemy>`; e.g. `tower.findTarget(enemies)` returns `Optional.empty()` instead of `null`.

## Project-specific patterns
- Use `Optional` for absence (`Game.base`, tower targeting, script loading state) rather than nullable returns.
- Use `deltaTime` everywhere for movement/timers; do not hardcode frame-based motion.
- `Tower` subclasses implement targeting strategies with Streams and range checks; `IceTower` uses `applyAreaEffect(...)` instead of firing damage.
- `Enemy` movement follows waypoint lists from `GameMap`; `WaveManager` reads `wave.count` and `wave.N.enemies` from level `.properties` files like `basic:8,armored:2`.
- Lua AI is optional and hot-reloadable through `game/scripting/LuaScriptEngine` and `scripts/enemy_ai.lua`.

## Config and resources
- `ConfigManager` loads classpath resources such as `config/game.properties`, `levels/level1.properties`, and `scripts/enemy_ai.lua`.
- VS Code is configured with `src` and `resources` as source roots, `bin` as output, and `lib/*.jar` as referenced libraries (`.vscode/settings.json`).
- If you run manually, make sure `resources/` is on the classpath so config, levels, and scripts resolve.

## Files to use as references
- `src/.../game/Game.java` — singleton, game loop, level setup, spawn/update/cleanup order.
- `src/.../game/factory/EntityFactory.java` and `src/.../j2d/J2dEntityFactory.java` — abstraction boundary.
- `src/.../j2d/J2dGame.java` — windowing, input, rendering, coordinate conversion.
- `src/.../game/entities/{Entity,Tower,Enemy}.java` — core entity rules and conventions.
- `src/.../game/wave/WaveManager.java` and `src/.../game/scripting/LuaScriptEngine.java` — level spawning and Lua integration.

## Workflow notes
- There is no visible test suite in the repo; validate changes by compiling and launching `Main`.
- From the repo root on Windows, a typical manual run is:
  ```powershell
  javac -d bin -cp "lib/*" @sources.txt
  java -cp "bin;resources;lib/*" be.uantwerpen.fti.ei.geavanceerde.towerdefence.Main
  ```
- Preserve the existing package split and naming style when adding new content: logic in `game/`, visuals in `j2d/`.


