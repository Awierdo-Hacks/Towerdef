package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.IceTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;

import java.awt.Color;
import java.awt.Graphics2D;

/*
 * Concrete IceTower with Java2D rendering.
 *
 * Drawn as a cyan diamond shape to distinguish it from the square
 * damage towers. A translucent range circle shows the slow aura.
 *
 * Uses default stats from IceTower (slow factor, slow duration, range).
 */
public class J2dIceTower extends IceTower {

    private static final Color FILL       = new Color(0, 206, 209);    // dark turquoise
    private static final Color BORDER     = new Color(0, 139, 139);    // dark cyan
    private static final Color AURA_COLOR = new Color(0, 206, 209, 40); // translucent aura

    private final J2dGame j2dGame;

    public J2dIceTower(Position position, J2dGame j2dGame) {
        super(position, DEFAULT_RANGE, DEFAULT_SLOW_FACTOR, DEFAULT_SLOW_DURATION, DEFAULT_COST);
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

        // Translucent aura circle showing slow range
        int rangeW = j2dGame.toScreenWidth(range * 2);
        int rangeH = j2dGame.toScreenHeight(range * 2);
        int rangeX = j2dGame.toScreenX(position.getX() - range);
        int rangeY = j2dGame.toScreenY(position.getY() - range);
        g.setColor(AURA_COLOR);
        g.fillOval(rangeX, rangeY, rangeW, rangeH);

        // Diamond shape (rotated square)
        int cx = sx + sw / 2;
        int cy = sy + sh / 2;
        int[] xPoints = {cx, cx + sw / 2, cx, cx - sw / 2};
        int[] yPoints = {cy - sh / 2, cy, cy + sh / 2, cy};
        g.setColor(FILL);
        g.fillPolygon(xPoints, yPoints, 4);
        g.setColor(BORDER);
        g.drawPolygon(xPoints, yPoints, 4);
    }
}
