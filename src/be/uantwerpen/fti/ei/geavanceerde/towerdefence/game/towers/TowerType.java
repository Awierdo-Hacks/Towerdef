package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.factory.EntityFactory;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

/*
 * The playable tower types, as a polymorphic dispatcher over the Abstract Factory.
 *
 * Each constant knows which factory method creates its concrete tower. This keeps
 * the type -> creation mapping in ONE place and keeps the game loop (Game.java)
 * free of any switch/instanceof and free of imports of concrete tower classes.
 *
 * OPEN/CLOSED: adding a new tower type means adding a constant here (and a factory
 * method) — Game.java is never touched.
 *
 * Note this enum depends only on the abstract EntityFactory + Tower, never on the
 * concrete J2d* classes, so the game/visualization separation is preserved.
 */
public enum TowerType {

    ARROW (ArrowTower.DEFAULT_RANGE)  { public Tower create(EntityFactory f, Position p) { return f.createArrowTower(p);  } },
    CANNON(CannonTower.DEFAULT_RANGE) { public Tower create(EntityFactory f, Position p) { return f.createCannonTower(p); } },
    ICE   (IceTower.DEFAULT_RANGE)    { public Tower create(EntityFactory f, Position p) { return f.createIceTower(p);    } };

    // Detection/effect radius of this tower type, in game-world units. Mirrors the
    // DEFAULT_RANGE the factory builds each tower with, kept here so the type can
    // answer "how far do I reach?" WITHOUT creating an instance — used by the
    // visualization layer to draw the placement range preview. Pure game-data
    // (no awt/swing), so the game/visualization separation is preserved.
    private final double range;

    TowerType(double range) {
        this.range = range;
    }

    /* Creates this tower type at the given position via the abstract factory. */
    public abstract Tower create(EntityFactory f, Position p);

    /* Detection radius (game-world units) this tower type will be built with. */
    public double getRange() {
        return range;
    }

    /*
     * Maps a player hotkey (1 = arrow, 2 = cannon, 3 = ice) to a tower type.
     * Returns null for 0 (nothing selected) or any unknown key.
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
