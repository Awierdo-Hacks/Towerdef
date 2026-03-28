package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.ArrowTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;

import java.awt.Color;
import java.awt.Graphics2D;

/*
 * Concrete ArrowTower with Java2D rendering.
 *
 * Drawn as a green square with a dark border and a small triangle on top
 * to suggest an arrow/turret. Green = fast, cheap, accurate.
 *
 * Uses default stats from ArrowTower. The J2dEntityFactory creates these.
 */
public class J2dArrowTower extends ArrowTower {

    private static final Color FILL   = new Color(34, 139, 34);   // forest green
    private static final Color BORDER = new Color(0, 100, 0);     // dark green

    // Reference to the visualization layer for Graphics2D and coordinate conversion
    private final J2dGame j2dGame;

    public J2dArrowTower(Position position, J2dGame j2dGame) {
        super(position, DEFAULT_RANGE, DEFAULT_DAMAGE, DEFAULT_FIRE_RATE, DEFAULT_COST);
        this.j2dGame = j2dGame;
    }

    @Override
    public void render() {
        Graphics2D g = j2dGame.getGraphics2D();
        if (g == null) return;

        // Convert game-world coordinates to screen pixels (centred on position)
        int sx = j2dGame.toScreenX(position.getX() - width / 2);
        int sy = j2dGame.toScreenY(position.getY() - height / 2);
        int sw = j2dGame.toScreenWidth(width);
        int sh = j2dGame.toScreenHeight(height);

        // Tower body
        g.setColor(FILL);
        g.fillRect(sx, sy, sw, sh);
        g.setColor(BORDER);
        g.drawRect(sx, sy, sw, sh);

        // Small triangle on top to suggest a turret
        int cx = sx + sw / 2;
        int[] xPoints = {cx - sw / 4, cx + sw / 4, cx};
        int[] yPoints = {sy, sy, sy - sh / 3};
        g.setColor(BORDER);
        g.fillPolygon(xPoints, yPoints, 3);
    }
}
