package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies.BasicEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.SpriteManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/*
 * Concrete BasicEnemy with Java2D rendering.
 *
 * Uses the enemy_basic.png sprite with a health bar above it.
 * Falls back to a red circle if the sprite cannot be loaded.
 */
public class J2dBasicEnemy extends BasicEnemy {

    private static final Color FILL        = new Color(220, 50, 50);
    private static final Color BORDER      = new Color(139, 0, 0);
    private static final Color HEALTH_BG   = new Color(60, 60, 60);
    private static final Color HEALTH_FILL = new Color(0, 200, 0);

    private final J2dGame j2dGame;
    private final BufferedImage sprite;

    public J2dBasicEnemy(List<Position> path, J2dGame j2dGame) {
        super(path.get(0), DEFAULT_HEALTH, DEFAULT_SPEED,
              DEFAULT_REWARD, DEFAULT_SCORE, path);
        this.j2dGame = j2dGame;
        this.sprite  = SpriteManager.getSprite("enemy_basic.png");
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

        if (sprite != null) {
            g.drawImage(sprite, sx, sy, sw, sh, null);
        } else {
            g.setColor(FILL);
            g.fillOval(sx, sy, sw, sh);
            g.setColor(BORDER);
            g.drawOval(sx, sy, sw, sh);
        }

        // Health bar above the enemy
        drawHealthBar(g, sx, sy, sw);
    }

    private void drawHealthBar(Graphics2D g, int sx, int sy, int sw) {
        int barH = 4;
        int barY = sy - barH - 2;

        g.setColor(HEALTH_BG);
        g.fillRect(sx, barY, sw, barH);

        int fillW = (int) (sw * getHealthPercent());
        g.setColor(HEALTH_FILL);
        g.fillRect(sx, barY, fillW, barH);
    }
}
