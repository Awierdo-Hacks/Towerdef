package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.RayProjectile;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.SpriteManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/*
 * Concrete RayProjectile with Java2D rendering — used for the raygun (ArrowTower)
 * single-target shots. Cannon shots use J2dCannonProjectile instead.
 *
 * Uses the projectile_ray.png sprite. Falls back to a bright yellow circle
 * if the sprite cannot be loaded. The single-target hit behaviour lives in the
 * game-logic superclass RayProjectile.onHit().
 */
public class J2dRayProjectile extends RayProjectile {

    private static final Color FILL   = new Color(255, 255, 50);
    private static final Color BORDER = new Color(200, 200, 0);
    private static final double PROJECTILE_SPEED = 8.0;

    private final J2dGame j2dGame;

    public J2dRayProjectile(Position start, Position target, int damage, J2dGame j2dGame) {
        super(start, target, PROJECTILE_SPEED, damage);
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

        BufferedImage sprite = SpriteManager.getSprite("projectile_ray.png");

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
