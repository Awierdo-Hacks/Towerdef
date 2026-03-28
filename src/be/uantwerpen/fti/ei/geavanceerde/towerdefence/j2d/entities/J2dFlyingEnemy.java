package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies.FlyingEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/*
 * Concrete FlyingEnemy with Java2D rendering.
 *
 * Drawn as an orange triangle (suggesting wings/flight) with a drop shadow
 * beneath it to visually indicate it's flying above ground level.
 *
 * Small and fast — uses default stats from FlyingEnemy.
 */
public class J2dFlyingEnemy extends FlyingEnemy {

    private static final Color FILL        = new Color(255, 165, 0);   // orange
    private static final Color BORDER      = new Color(200, 100, 0);   // darker orange
    private static final Color SHADOW      = new Color(0, 0, 0, 50);   // translucent shadow
    private static final Color HEALTH_BG   = new Color(60, 60, 60);
    private static final Color HEALTH_FILL = new Color(0, 200, 0);

    private final J2dGame j2dGame;

    public J2dFlyingEnemy(List<Position> path, J2dGame j2dGame) {
        super(path.get(0), DEFAULT_HEALTH, DEFAULT_SPEED,
              DEFAULT_REWARD, DEFAULT_SCORE, path);
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

        // Drop shadow — offset below and to the right to simulate height
        int shadowOff = 4;
        g.setColor(SHADOW);
        g.fillOval(sx + shadowOff, sy + shadowOff, sw, sh);

        // Flying body — triangle pointing in the direction of travel
        int cx = sx + sw / 2;
        int cy = sy + sh / 2;
        int[] xPoints = {cx, cx + sw / 2, cx - sw / 2};
        int[] yPoints = {cy - sh / 2, cy + sh / 2, cy + sh / 2};
        g.setColor(FILL);
        g.fillPolygon(xPoints, yPoints, 3);
        g.setColor(BORDER);
        g.drawPolygon(xPoints, yPoints, 3);

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
