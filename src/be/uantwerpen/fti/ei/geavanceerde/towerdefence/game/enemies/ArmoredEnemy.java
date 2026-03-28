package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.List;

/*
 * A heavily armoured enemy — high HP, slow movement, and physical resistance.
 *
 * Stats (defaults):
 *   health           : 220
 *   speed            : 1.2 game-world units per second
 *   reward           : 25 gold on kill
 *   scoreValue       : 20 points on kill
 *   damageResistance : 0.5 (takes 50% reduced damage from all sources)
 *   size             : 0.75 x 0.75 units
 *
 * DAMAGE RESISTANCE:
 *   Overrides takeDamage() to multiply incoming damage by (1 - damageResistance).
 *   This makes ArmoredEnemies noticeably tougher against ArrowTowers but still
 *   vulnerable to CannonTower splash (high base damage cuts through).
 *
 * TARGETING:
 *   CannonTower targets highest-HP enemies — ArmoredEnemies are therefore
 *   naturally prioritised by Cannon towers, which is the intended synergy.
 *
 * ABSTRACT because render() from Entity is not implemented here.
 * J2dArmoredEnemy (Fase 5) extends this and implements render().
 */
public abstract class ArmoredEnemy extends Enemy {

    public static final double DEFAULT_HEALTH              = 220.0;
    public static final double DEFAULT_SPEED               = 1.2;
    public static final int    DEFAULT_REWARD              = 25;
    public static final int    DEFAULT_SCORE               = 20;
    public static final double DEFAULT_DAMAGE_RESISTANCE   = 0.5;
    public static final double SIZE                        = 0.75;

    // Fraction of incoming damage that is absorbed (0.0 = no resistance, 1.0 = immune)
    protected double damageResistance;

    public ArmoredEnemy(Position startPosition, double health, double speed,
                        int reward, int scoreValue, double damageResistance,
                        List<Position> path) {
        super(startPosition, SIZE, SIZE, health, speed, reward, scoreValue, path);
        this.damageResistance = damageResistance;
    }

    // -------------------------------------------------------------------------
    // Damage resistance — reduces all incoming damage by the resistance fraction
    // -------------------------------------------------------------------------

    /*
     * Applies damage resistance before subtracting from health.
     *
     * Example with damageResistance = 0.5:
     *   ArrowTower fires 15 damage → only 7 effective damage (15 * 0.5)
     *   CannonTower fires 60 damage → only 30 effective damage (60 * 0.5)
     *
     * This makes the armoured enemy significantly tougher and forces the
     * player to invest in CannonTowers for later waves.
     */
    @Override
    public void takeDamage(double amount) {
        // Reduce incoming damage by the resistance fraction
        double effectiveDamage = amount * (1.0 - damageResistance);
        super.takeDamage(effectiveDamage);
    }

    @Override
    public String getType() {
        return "armored";
    }

    public double getDamageResistance() { return damageResistance; }
}
