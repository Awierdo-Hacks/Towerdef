package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.CannonProjectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.SpriteManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Concrete {@link CannonProjectile} with Java2D rendering.
 *
 * <p>Uses the {@code projectile_cannon.png} sprite, falling back to an orange circle
 * if the sprite cannot be loaded. The splash behaviour itself lives in the game-logic
 * superclass {@code CannonProjectile.onHit()}.</p>
 *
 * @author Tower Defence team
 */
public class J2dCannonProjectile extends CannonProjectile {

    /** Fallback fill colour when the sprite cannot be loaded. */
    private static final Color FILL   = new Color(255, 150, 50);
    /** Fallback border colour when the sprite cannot be loaded. */
    private static final Color BORDER = new Color(150, 80, 0);
    /** Travel speed of the projectile in game-world units per second. */
    private static final double PROJECTILE_SPEED = 8.0;

    /** The view used for graphics access and coordinate conversion. */
    private final J2dGame j2dGame;

    /**
     * Creates a Java2D cannon projectile flying from {@code start} to {@code target}.
     *
     * @param start        the position the projectile is fired from
     * @param target       the world position the projectile flies toward
     * @param damage       the direct-hit damage
     * @param splashRadius the splash radius in game-world units
     * @param splashDamage the damage applied to other enemies within the splash radius
     * @param j2dGame      the view used for rendering and coordinate conversion
     */
    public J2dCannonProjectile(Position start, Position target, int damage,
                               double splashRadius, int splashDamage, J2dGame j2dGame) {
        super(start, target, PROJECTILE_SPEED, damage, splashRadius, splashDamage);
        this.j2dGame = j2dGame;
    }

    @Override
    public void render() {
        Graphics2D g = j2dGame.getGraphics2D();
        if (g == null) return;

        // Visuele grootte (groter dan de logische hitbox), gecentreerd op de positie.
        double vw = width  * J2dGame.SPRITE_SCALE;
        double vh = height * J2dGame.SPRITE_SCALE;
        int sx = j2dGame.toScreenX(position.getX() - vw / 2);
        int sy = j2dGame.toScreenY(position.getY() - vh / 2);
        int sw = j2dGame.toScreenWidth(vw);
        int sh = j2dGame.toScreenHeight(vh);

        BufferedImage sprite = SpriteManager.getSprite("projectile_cannon.png");
        if (sprite != null) {
            g.drawImage(sprite, sx, sy, sw, sh, null);
        } else {
            g.setColor(FILL);
            g.fillOval(sx, sy, sw, sh);
            g.setColor(BORDER);
            g.drawOval(sx, sy, sw, sh);
        }
    }
}
