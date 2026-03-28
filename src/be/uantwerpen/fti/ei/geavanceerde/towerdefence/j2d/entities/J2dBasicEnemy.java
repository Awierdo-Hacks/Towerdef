package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.enemies.BasicEnemy;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/*
 * Concrete BasicEnemy with Java2D rendering.
 *
 * Drawn as a red circle with a health bar above it.
 * Red = standard threat, small size, moderate speed.
 *
 * Start position is the first waypoint of the path.
 * Uses default stats from BasicEnemy.
 */
public class J2dBasicEnemy extends BasicEnemy {

    private static final Color FILL        = new Color(220, 50, 50);   // red
    private static final Color BORDER      = new Color(139, 0, 0);     // dark red
    private static final Color HEALTH_BG   = new Color(60, 60, 60);    // dark gray bar background
    private static final Color HEALTH_FILL = new Color(0, 200, 0);     // green health

    private final J2dGame j2dGame;

    public J2dBasicEnemy(List<Position> path, J2dGame j2dGame) {
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

        // Enemy body — filled circle
        g.setColor(FILL);
        g.fillOval(sx, sy, sw, sh);
        g.setColor(BORDER);
        g.drawOval(sx, sy, sw, sh);

        // Health bar above the enemy
        drawHealthBar(g, sx, sy, sw);
    }

    /*
     * Draws a small health bar above the enemy.
     * Background is gray, fill is green scaled by health percentage.
     */
    private void drawHealthBar(Graphics2D g, int sx, int sy, int sw) {
        int barH = 4;
        int barY = sy - barH - 2;

        // Background
        g.setColor(HEALTH_BG);
        g.fillRect(sx, barY, sw, barH);

        // Health fill
        int fillW = (int) (sw * getHealthPercent());
        g.setColor(HEALTH_FILL);
        g.fillRect(sx, barY, fillW, barH);
    }
}
