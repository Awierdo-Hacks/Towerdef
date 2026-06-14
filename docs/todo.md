# TODO - Tower Defence Project

_Laatst bijgewerkt: 2026-06-14 — afgevinkt op basis van de werkelijke codebase._

---

## Fase 1: Foundation
- [x] `Position.java` — double x,y coordinaat met distanceTo()
- [x] `Stopwatch.java` — delta time (seconden) tussen ticks
- [x] `ConfigManager.java` — laadt .properties bestanden
- [x] `GameState.java` — enum (MENU, PLAYING, PAUSED, GAME_OVER, WON)
- [x] `Game.java` — Singleton skeleton (private constructor, getInstance, entity lists)

## Fase 2: Entity Hierarchy
- [x] `Entity.java` — abstract basis (position, size, alive, update, collidesWith)
- [x] `Tower.java` — abstract (range, damage, fireRate, findTarget, applyAreaEffect)
- [x] `Enemy.java` — abstract (health, speed, path, moveAlongPath, takeDamage, applySlow)
- [x] `Projectile.java` — abstract (damage, speed, target, onHit)
- [x] `Base.java` — abstract (health points, takeDamage)
- [x] `Obstacle.java` — abstract (blokkeert plaatsing) ⚠️ later verwijderd (ongebruikt idee — zie ongebruikte-code-analyse.md)
- [x] `Bonus.java` — abstract (effect, duur) ⚠️ later verwijderd (ongebruikt idee — zie ongebruikte-code-analyse.md)

## Fase 3: Abstract Factory
- [x] `EntityFactory.java` — interface met alle create-methodes (incl. `createCannonProjectile`)
- [x] `ArrowTower.java` — snel, lage schade (Streams: closest enemy)
- [x] `CannonTower.java` — langzaam, hoge schade, splash (Streams: highest HP)
- [x] `IceTower.java` — vertraagt vijanden (Streams: area slow)
- [x] `BasicEnemy.java` — standaard vijand
- [x] `ArmoredEnemy.java` — veel HP, langzaam, damage resistance
- [x] `FlyingEnemy.java` — negeert terrein, snel/fragiel (vliegpad komt via constructor)

## Fase 4: Map & Pathfinding
- [x] `TileType.java` — enum (GRASS, PATH, WATER, BUILD_SPOT, SPAWN, BASE)
- [x] `Tile.java` — tegel type + positie, isBuildable()
- [x] `Path.java` — lijst van waypoints (unmodifiable)
- [x] `GameMap.java` — grid, spawn, build spots, pad, level laden uit .properties
- [x] `level1.properties` — level 1 definitie (winding road)
- [x] `level2.properties` — level 2 definitie (the gauntlet)

## Fase 5: J2D Visualization
- [x] `J2dGame.java` — JFrame, Canvas, BufferStrategy, render pipeline, coord mapping
- [x] `J2dEntityFactory.java` — concrete factory implements EntityFactory
- [x] `InputHandler.java` — KeyListener + MouseListener + tower selection
- [x] `J2dArrowTower.java` — raygun sprite / groene fallback
- [x] `J2dCannonTower.java` — cannon sprite / grijze fallback
- [x] `J2dIceTower.java` — ice sprite / cyaan diamant, range aura
- [x] `J2dBasicEnemy.java` — sprite / rode cirkel, health bar
- [x] `J2dArmoredEnemy.java` — sprite / maroon vierkant, gele health bar
- [x] `J2dFlyingEnemy.java` — sprite / oranje driehoek, drop shadow
- [x] `J2dRayProjectile.java` — ray sprite / gele cirkel
- [x] `J2dCannonProjectile.java` — cannon sprite (splash via onHit) — toegevoegd bij splash-refactor
- [x] `J2dBase.java` — goud kasteel met turrets, health bar
- [x] `J2dObstacle.java` — bruin afgerond vierkant ⚠️ later verwijderd (ongebruikt)
- [x] `J2dBonus.java` — geel diamant, pulse effect ⚠️ later verwijderd (ongebruikt)

## Fase 6: Game Loop & Mechanics
- [x] `Main.java` — entry point, wiring, game loop starten
- [x] Game loop implementeren in Game.update(deltaTime)
- [x] Tower targeting (Streams API)
- [x] Enemy movement langs pad
- [x] Projectile movement + collision (splash via CannonProjectile.onHit)
- [x] Gold/score systeem
- [x] Win/lose condities

## Fase 7: Waves & Levels
- [x] `Wave.java` — enemy spawn definitie
- [x] `WaveManager.java` — wave progressie, spawn timing
- [x] `level1.properties` — level 1 definitie
- [x] `level2.properties` — level 2 definitie
- [x] Moeilijkheidsgraad stijging per wave (oplopende counts + zwaardere types)

## Fase 8: Lua & Config
- [x] `LuaScriptEngine.java` — LuaJ integratie (incl. hot-reload)
- [x] `enemy_ai.lua` — enemy AI script
- [x] `game.properties` — game configuratie
- [x] Lua script koppelen aan enemy update (Game.update → callUpdateEnemy)

## Fase 9: Polish
- [x] Score/gold/lives HUD overlay (gold, score, HP, wave, enemies left)
- [x] Game over scherm
- [x] Victory scherm
- [ ] Menu scherm (nog niet: setupLevel springt direct naar PLAYING)
- [x] Health bars boven vijanden
- [ ] Tower range indicator bij plaatsen (enkel IceTower toont een vaste aura)
- [ ] Balancing (tower/enemy stats) — doorlopend

## Fase 10: Extras (optioneel)
- [x] ECS systeem (min. 1 system — verplicht) — floating combat text
      (`game/ecs/`: `FloatingTextWorld`, `MovementSystem`, `LifetimeSystem`, `FloatingTextKind`;
      render + kleur/formaat in `J2dGame`)
- [ ] Extra vijand types
- [ ] Extra tower types
- [ ] Tower upgrades
- [ ] Particle effects / animaties
- [ ] Geluid
- [ ] Collectables
- [ ] Moving obstacles
- [ ] Highscore systeem

---

## Verplichte Requirements Checklist
- [x] Singleton pattern (Game)
- [x] Abstract Factory pattern (EntityFactory + J2dEntityFactory)
- [x] Game/visualisatie scheiding (geen awt imports in game/ — geverifieerd)
- [x] Double coordinaten (Position)
- [x] Stopwatch (delta time)
- [x] Score zichtbaar
- [x] Geen null waardes in game logic (Optional)
- [x] Java Streams API (min. 1x — Arrow/Cannon findTarget, IceTower area slow)
- [x] Lua script (min. 1x — enemy_ai.lua)
- [x] Configuration file (.properties — game + 2 levels)
- [x] Min. 2 levels
- [x] Java2D rendering (Graphics2D)
- [x] ECS systeem (min. 1x — floating combat text)
- [x] Draait op andere systemen zonder code wijzigingen (classpath-resources + config)
- [ ] LLM portfolio bijhouden (extern/process — niet in code te verifiëren)
