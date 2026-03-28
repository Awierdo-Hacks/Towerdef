package be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Enemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Tower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/*
 * A slow, high-damage tower that fires cannonballs at the HIGHEST HP enemy in range.
 *
 * Stats (defaults):
 *   range    : 2.5 game-world units
 *   damage   : 60 per direct hit
 *   fireRate : 0.6 shots per second
 *   cost     : 100 gold
 *   size     : 0.9 x 0.9 units
 *
 * SPLASH DAMAGE:
 *   hasSplashDamage = true. The game loop checks this flag and calls
 *   applySplashDamage() when the projectile hits, damaging all enemies
 *   within splashRadius of the impact point.
 *   splashDamage is a fraction of the base damage (default: 40%).
 *
 * TARGETING STRATEGY (Java Streams):
 *   Targets the enemy with the HIGHEST current HP in range.
 *   This maximises the value of splash damage against clustered tanky enemies
 *   and ensures armoured enemies are prioritised.
 *
 * ABSTRACT because render() from Entity is not implemented here.
 * J2dCannonTower (Fase 5) extends this and implements render().
 */
public abstract class CannonTower extends Tower {

    public static final double DEFAULT_RANGE       = 2.5;
    public static final int    DEFAULT_DAMAGE      = 60;
    public static final double DEFAULT_FIRE_RATE   = 0.6;
    public static final int    DEFAULT_COST        = 100;
    public static final double SIZE                = 0.9;

    // Splash damage values — read by the game loop after a projectile lands
    public static final double DEFAULT_SPLASH_RADIUS = 1.2;  // game-world units
    public static final double DEFAULT_SPLASH_DAMAGE_FRACTION = 0.4;  // 40% of main damage

    // Whether this tower's projectile explodes on impact
    protected boolean hasSplashDamage;
    protected double  splashRadius;
    protected int     splashDamage;

    public CannonTower(Position position, double range, int damage, double fireRate, int cost,
                       double splashRadius, double splashDamageFraction) {
        super(position, SIZE, SIZE, range, damage, fireRate, cost);
        this.hasSplashDamage = true;
        this.splashRadius    = splashRadius;
        // Splash damage is a fraction of the main hit damage
        this.splashDamage    = (int)(damage * splashDamageFraction);
    }

    // -------------------------------------------------------------------------
    // Targeting — HIGHEST HP alive enemy within range (Java Streams)
    // -------------------------------------------------------------------------

    /*
     * Finds the alive enemy with the most current HP within this tower's range.
     *
     * STREAMS USAGE:
     *   1. filter: alive enemies within range
     *   2. max:    pick the one with the highest currentHealth
     *
     * Prioritising high-HP targets maximises splash effectiveness and
     * ensures ArmoredEnemies are dealt with before they reach the base.
     */
    @Override
    public Optional<Enemy> findTarget(List<Enemy> enemies) {
        Position towerPos = this.position;

        return enemies.stream()
            .filter(Enemy::isAlive)
            .filter(e -> towerPos.distanceTo(e.getPosition()) <= this.range)
            .max(Comparator.comparingDouble(Enemy::getCurrentHealth));
    }

    // -------------------------------------------------------------------------
    // Splash logic — called by the game loop when a cannonball lands
    // -------------------------------------------------------------------------

    /*
     * Deals splash damage to all alive enemies within splashRadius of the impact point.
     *
     * Called by the game loop AFTER the direct hit has already been applied:
     *   1. Projectile hits primary target → projectile.onHit(target)
     *   2. Game loop calls cannonTower.applySplashDamage(impactPoint, allEnemies)
     *
     * STREAMS USAGE: filters by alive + within splash radius, applies damage to each.
     */
    public void applySplashDamage(Position impactPoint, List<Enemy> enemies) {
        enemies.stream()
            .filter(Enemy::isAlive)
            .filter(e -> impactPoint.distanceTo(e.getPosition()) <= splashRadius)
            .forEach(e -> e.takeDamage(splashDamage));
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public boolean hasSplashDamage() { return hasSplashDamage; }
    public double  getSplashRadius() { return splashRadius; }
    public int     getSplashDamage() { return splashDamage; }
}
