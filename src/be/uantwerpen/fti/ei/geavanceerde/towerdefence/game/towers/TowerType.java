package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/**
 * The playable tower types, as a polymorphic dispatcher over the Abstract Factory.
 *
 * <p>Each constant knows which factory method creates its concrete tower. This keeps
 * the type → creation mapping in one place and keeps the game loop ({@code Game})
 * free of any {@code switch}/{@code instanceof} and free of imports of concrete tower
 * classes.</p>
 *
 * <p>Open/closed: adding a new tower type means adding a constant here (and a factory
 * method) — {@code Game} is never touched. This enum depends only on the abstract
 * {@link EntityFactory} and {@link Tower}, never on the concrete {@code J2d*} classes,
 * so the game/visualization separation is preserved.</p>
 *
 * @author Tower Defence team
 */
public enum TowerType {

    /** Fast, low-damage tower — creates an {@code ArrowTower}. */
    ARROW (ArrowTower.DEFAULT_RANGE)  { public Tower create(EntityFactory f, Position p) { return f.createArrowTower(p);  } },
    /** Slow, high-damage splash tower — creates a {@code CannonTower}. */
    CANNON(CannonTower.DEFAULT_RANGE) { public Tower create(EntityFactory f, Position p) { return f.createCannonTower(p); } },
    /** Support tower that slows enemies — creates an {@code IceTower}. */
    ICE   (IceTower.DEFAULT_RANGE)    { public Tower create(EntityFactory f, Position p) { return f.createIceTower(p);    } };

    /**
     * Detection/effect radius of this tower type, in game-world units. Mirrors the
     * {@code DEFAULT_RANGE} the factory builds each tower with, so the type can answer
     * "how far do I reach?" without creating an instance. Pure game data (no awt/swing).
     */
    private final double range;

    /**
     * Associates this tower type with its build range.
     *
     * @param range the detection/effect radius in game-world units
     */
    TowerType(double range) {
        this.range = range;
    }

    /**
     * Creates this tower type at the given position via the abstract factory.
     *
     * @param f the abstract factory used to create the tower
     * @param p the build position in game-world coordinates
     * @return the newly created tower
     */
    public abstract Tower create(EntityFactory f, Position p);

    /**
     * Returns the detection radius (game-world units) this tower type will be built
     * with.
     *
     * @return the build range
     */
    public double getRange() {
        return range;
    }

    /**
     * Maps a player hotkey to a tower type ({@code 1} = arrow, {@code 2} = cannon,
     * {@code 3} = ice).
     *
     * @param hotkey the pressed hotkey number
     * @return the matching tower type, or {@code null} for {@code 0} (nothing selected)
     *         or any unknown key
     */
    public static TowerType fromHotkey(int hotkey) {
        switch (hotkey) {
            case 1:  return ARROW;
            case 2:  return CANNON;
            case 3:  return ICE;
            default: return null;
        }
    }
}
