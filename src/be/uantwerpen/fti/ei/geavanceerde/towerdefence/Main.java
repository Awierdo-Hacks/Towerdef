package be.uantwerpen.fti.ei.geavanceerde.towerdefence;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.Game;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dEntityFactory;

/*
 * Entry point — only wires up the Abstract Factory and starts the game.
 * All game logic, input handling, and rendering live in Game.start().
 */
public class Main {

    public static void main(String[] args) {
        ConfigManager config = new ConfigManager("config/game.properties");
        EntityFactory factory = new J2dEntityFactory(config);
        Game.getInstance().start(factory, config);
    }
}
