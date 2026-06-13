package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.IceTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.SpriteManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/*
 * Concrete IceTower with Java2D rendering.
 *
 * Uses the tower_ice.png sprite with a translucent aura circle
 * showing the slow range. Falls back to a cyan diamond if the
 * sprite cannot be loaded.
 */
public class J2dIceTower extends IceTower {

    private static final Color FILL       = new Color(0, 206, 209);
    private static final Color BORDER     = new Color(0, 139, 139);
    private static final Color AURA_COLOR = new Color(0, 206, 209, 40);

    private final J2dGame j2dGame;
    private final BufferedImage sprite;

    public J2dIceTower(Position position, J2dGame j2dGame) {
        super(position, DEFAULT_RANGE, DEFAULT_SLOW_FACTOR, DEFAULT_SLOW_DURATION, DEFAULT_COST);
        this.j2dGame = j2dGame;
        this.sprite  = SpriteManager.getSprite("tower_ice.png");
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

        if (sprite != null) {
            g.drawImage(sprite, sx, sy, sw, sh, null);
        } else {
            // Fallback diamond shape
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
}
