package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies.FlyingEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.SpriteManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/*
 * Concrete FlyingEnemy with Java2D rendering.
 *
 * Uses the enemy_flying.png sprite with a drop shadow beneath it
 * to visually indicate flight. Falls back to an orange triangle
 * if the sprite cannot be loaded.
 */
public class J2dFlyingEnemy extends FlyingEnemy {

    private static final Color FILL        = new Color(255, 165, 0);
    private static final Color BORDER      = new Color(200, 100, 0);
    private static final Color SHADOW      = new Color(0, 0, 0, 50);
    private static final Color HEALTH_BG   = new Color(60, 60, 60);
    private static final Color HEALTH_FILL = new Color(0, 200, 0);

    private final J2dGame j2dGame;
    private final BufferedImage sprite;

    public J2dFlyingEnemy(List<Position> path, J2dGame j2dGame) {
        super(path.get(0), DEFAULT_HEALTH, DEFAULT_SPEED,
              DEFAULT_REWARD, DEFAULT_SCORE, path);
        this.j2dGame = j2dGame;
        this.sprite  = SpriteManager.getSprite("enemy_flying.png");
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

        // Drop shadow to simulate flight height
        int shadowOff = 4;
        g.setColor(SHADOW);
        g.fillOval(sx + shadowOff, sy + shadowOff, sw, sh / 2);

        if (sprite != null) {
            g.drawImage(sprite, sx, sy, sw, sh, null);
        } else {
            int cx = sx + sw / 2;
            int cy = sy + sh / 2;
            int[] xPoints = {cx, cx + sw / 2, cx - sw / 2};
            int[] yPoints = {cy - sh / 2, cy + sh / 2, cy + sh / 2};
            g.setColor(FILL);
            g.fillPolygon(xPoints, yPoints, 3);
            g.setColor(BORDER);
            g.drawPolygon(xPoints, yPoints, 3);
        }

        // Health bar
        int barH = 3;
        int barY = sy - barH - 2;
        g.setColor(HEALTH_BG);
        g.fillRect(sx, barY, sw, barH);
        int fillW = (int) (sw * getHealthPercent());
        g.setColor(HEALTH_FILL);
        g.fillRect(sx, barY, fillW, barH);
    }
}
