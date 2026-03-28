package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies.ArmoredEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/*
 * Concrete ArmoredEnemy with Java2D rendering.
 *
 * Drawn as a larger dark-maroon square to show it's heavier and armoured.
 * The square shape and darker colour distinguish it from the round BasicEnemy.
 * Health bar turns yellow when damage resistance is active.
 *
 * Uses default stats from ArmoredEnemy (high HP, slow, 50% resistance).
 */
public class J2dArmoredEnemy extends ArmoredEnemy {

    private static final Color FILL        = new Color(128, 0, 0);     // maroon
    private static final Color BORDER      = new Color(80, 0, 0);      // darker maroon
    private static final Color HEALTH_BG   = new Color(60, 60, 60);
    private static final Color HEALTH_FILL = new Color(255, 200, 0);   // yellow — armoured indicator

    private final J2dGame j2dGame;

    public J2dArmoredEnemy(List<Position> path, J2dGame j2dGame) {
        super(path.get(0), DEFAULT_HEALTH, DEFAULT_SPEED,
              DEFAULT_REWARD, DEFAULT_SCORE, DEFAULT_DAMAGE_RESISTANCE, path);
        this.j2dGame = j2dGame;
    }

    @Override
    public void render() {
        Graphics2D g = j2dGame.getGraphics2D();
        if (g == null) return;

        int sx = j2dGame.toScreenX(position.getX() - width / 2);
        int sy = j2dGame.toScreenY(position.getY() - height / 2);
        int sw = j2dGame.toScreenWidth(width);
        int sh = j2dGame.toScreenHeight(height);

        // Enemy body — filled square (armoured = boxy shape)
        g.setColor(FILL);
        g.fillRect(sx, sy, sw, sh);
        g.setColor(BORDER);
        g.drawRect(sx, sy, sw, sh);

        // Armour cross — two thin lines across the body to suggest a shield
        g.setColor(new Color(200, 200, 200, 100));
        g.drawLine(sx, sy, sx + sw, sy + sh);
        g.drawLine(sx + sw, sy, sx, sy + sh);

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
