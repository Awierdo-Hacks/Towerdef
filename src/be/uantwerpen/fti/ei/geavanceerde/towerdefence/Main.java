package be.uantwerpen.fti.ei.geavanceerde.towerdefence;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.Game;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.ConfigManager;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dEntityFactory;

/**
 * Application entry point.
 *
 * <p>{@code Main} only wires up the Abstract Factory and starts the game: it loads the
 * configuration, creates the concrete {@link J2dEntityFactory}, and hands both to the
 * {@link Game} singleton. All game logic, input handling, and rendering live in
 * {@link Game#start(EntityFactory, ConfigManager)}.</p>
 *
 * @author Tower Defence team
 */
public class Main {

    /** Utility entry-point class; not meant to be instantiated. */
    private Main() {
    }

    /**
     * Launches the Tower Defence game.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        ConfigManager config = new ConfigManager("config/game.properties");
        EntityFactory factory = new J2dEntityFactory(config);
        Game.getInstance().start(factory, config);
    }
}
