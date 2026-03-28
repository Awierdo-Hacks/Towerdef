# TODO - Tower Defence Project

---

## Fase 1: Foundation
- [x] `Position.java` — double x,y coordinaat met distanceTo()
- [x] `Stopwatch.java` — delta time in milliseconden
- [x] `ConfigManager.java` — laadt .properties bestanden
- [x] `GameState.java` — enum (MENU, PLAYING, PAUSED, GAME_OVER, WON)
- [x] `Game.java` — Singleton skeleton (private constructor, getInstance, entity lists)

## Fase 2: Entity Hierarchy
- [x] `Entity.java` — abstract basis (position, size, alive, update, collidesWith)
- [x] `Tower.java` — abstract (range, damage, fireRate, findTarget, applyAreaEffect)
- [x] `Enemy.java` — abstract (health, speed, path, moveAlongPath, takeDamage, applySlow)
- [x] `Projectile.java` — abstract (damage, speed, target)
- [x] `Base.java` — abstract (health points, takeDamage)
- [x] `Obstacle.java` — abstract (blokkeert plaatsing)
- [x] `Bonus.java` — abstract (effect, duur)

## Fase 3: Abstract Factory
- [x] `EntityFactory.java` — interface met alle create-methodes
- [x] `ArrowTower.java` — snel, lage schade (Streams: closest enemy)
- [x] `CannonTower.java` — langzaam, hoge schade, splash (Streams: highest HP)
- [x] `IceTower.java` — vertraagt vijanden (Streams: area slow + fastest enemy)
- [x] `BasicEnemy.java` — standaard vijand
- [x] `ArmoredEnemy.java` — veel HP, langzaam, damage resistance
- [x] `FlyingEnemy.java` — negeert terrein, optional flyingPath

## Fase 4: Map & Pathfinding
- [x] `TileType.java` — enum (GRASS, PATH, WATER, BUILD_SPOT, SPAWN, BASE)
- [x] `Tile.java` — tegel type + positie, isWalkable(), isBuildable()
- [x] `Path.java` — lijst van waypoints (unmodifiable)
- [x] `GameMap.java` — grid, spawn, build spots, pad, level laden uit .properties
- [x] `level1.properties` — level 1 definitie (winding road)
- [x] `level2.properties` — level 2 definitie (the gauntlet)

## Fase 5: J2D Visualization
- [x] `J2dGame.java` — JFrame, Canvas, BufferStrategy, render pipeline, coord mapping
- [x] `J2dEntityFactory.java` — concrete factory implements EntityFactory
- [x] `InputHandler.java` — KeyListener + MouseListener + tower selection
- [x] `J2dArrowTower.java` — groene toren, driehoek turret
- [x] `J2dCannonTower.java` — grijze toren, kanon barrel
- [x] `J2dIceTower.java` — cyaan diamant, range aura
- [x] `J2dBasicEnemy.java` — rode cirkel, health bar
- [x] `J2dArmoredEnemy.java` — maroon vierkant, gele health bar
- [x] `J2dFlyingEnemy.java` — oranje driehoek, drop shadow
- [x] `J2dProjectile.java` — gele cirkel
- [x] `J2dBase.java` — goud kasteel met turrets, health bar
- [x] `J2dObstacle.java` — bruin afgerond vierkant
- [x] `J2dBonus.java` — geel diamant, pulse effect

## Fase 6: Game Loop & Mechanics
- [x] `Main.java` — entry point, wiring, game loop starten
- [x] Game loop implementeren in Game.update(deltaTime)
- [x] Tower targeting (Streams API)
- [x] Enemy movement langs pad
- [x] Projectile movement + collision
- [x] Gold/score systeem
- [x] Win/lose condities

## Fase 7: Waves & Levels
- [ ] `Wave.java` — enemy spawn definitie
- [ ] `WaveManager.java` — wave progressie, spawn timing
- [ ] `level1.properties` — level 1 definitie
- [ ] `level2.properties` — level 2 definitie
- [ ] Moeilijkheidsgraad stijging per wave

## Fase 8: Lua & Config
- [ ] `LuaScriptEngine.java` — LuaJ integratie
- [ ] `enemy_ai.lua` — enemy AI script
- [ ] `game.properties` — game configuratie
- [ ] Lua script koppelen aan enemy update

## Fase 9: Polish
- [ ] Score/gold/lives HUD overlay
- [ ] Game over scherm
- [ ] Victory scherm
- [ ] Menu scherm
- [ ] Health bars boven vijanden
- [ ] Tower range indicator bij plaatsen
- [ ] Balancing (tower/enemy stats)

## Fase 10: Extras (optioneel)
- [ ] ECS systeem (min. 1 system — verplicht)
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
- [ ] Singleton pattern (Game)
- [ ] Abstract Factory pattern (EntityFactory + J2dEntityFactory)
- [ ] Game/visualisatie scheiding (geen awt imports in game/)
- [ ] Double coordinaten (Position)
- [ ] Stopwatch (delta time)
- [ ] Score zichtbaar
- [ ] Geen null waardes in game logic (Optional)
- [ ] Java Streams API (min. 1x)
- [ ] Lua script (min. 1x)
- [ ] Configuration file (.properties)
- [ ] Min. 2 levels
- [ ] Java2D rendering (Graphics2D)
- [ ] ECS systeem (min. 1x)
- [ ] Draait op andere systemen zonder code wijzigingen
- [ ] LLM portfolio bijhouden
