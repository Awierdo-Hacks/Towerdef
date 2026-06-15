package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies.ArmoredEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.SpriteManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/*
 * Concrete ArmoredEnemy with Java2D rendering.
 *
 * Uses the enemy_armored.png sprite with a yellow health bar.
 * Falls back to a maroon square with armour cross if the sprite cannot be loaded.
 */
public class J2dArmoredEnemy extends ArmoredEnemy {

    private static final Color FILL        = new Color(128, 0, 0);
    private static final Color BORDER      = new Color(80, 0, 0);
    private static final Color HEALTH_BG   = new Color(60, 60, 60);
    private static final Color HEALTH_FILL = new Color(255, 200, 0);

    private final J2dGame j2dGame;
    private final BufferedImage sprite;

    public J2dArmoredEnemy(List<Position> path, J2dGame j2dGame) {
        super(path.get(0), DEFAULT_HEALTH, DEFAULT_SPEED,
              DEFAULT_REWARD, DEFAULT_SCORE, DEFAULT_DAMAGE_RESISTANCE, path);
        this.j2dGame = j2dGame;
        this.sprite  = SpriteManager.getSprite("enemy_armored.png");
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
            g.fillRect(sx, sy, sw, sh);
            g.setColor(BORDER);
            g.drawRect(sx, sy, sw, sh);
            g.setColor(new Color(200, 200, 200, 100));
            g.drawLine(sx, sy, sx + sw, sy + sh);
            g.drawLine(sx + sw, sy, sx, sy + sh);
        }

        // Health bar (yellow to indicate armour)
        int barH = 4;
        int barY = sy - barH - 2;
        g.setColor(HEALTH_BG);
        g.fillRect(sx, barY, sw, barH);
        int fillW = (int) (sw * getHealthPercent());
        g.setColor(HEALTH_FILL);
        g.fillRect(sx, barY, fillW, barH);
    }
}
