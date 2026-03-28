package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;

import java.awt.Color;
import java.awt.Graphics2D;

/*
 * Concrete Base with Java2D rendering.
 *
 * Drawn as a gold-coloured castle shape — a rectangle with two small
 * turrets on the corners. A health bar below shows remaining HP.
 *
 * Default size is 1.0 x 1.0 game-world units (one tile).
 */
public class J2dBase extends Base {

    private static final Color FILL       = new Color(218, 165, 32);   // goldenrod
    private static final Color BORDER     = new Color(139, 101, 8);    // dark goldenrod
    private static final Color TURRET     = new Color(160, 120, 20);   // turret accent
    private static final Color HEALTH_BG  = new Color(60, 60, 60);
    private static final Color HEALTH_OK  = new Color(0, 200, 0);
    private static final Color HEALTH_LOW = new Color(200, 0, 0);
    private static final double BASE_SIZE = 1.0;

    private final J2dGame j2dGame;

    public J2dBase(Position position, int maxHealth, J2dGame j2dGame) {
        super(position, BASE_SIZE, BASE_SIZE, maxHealth);
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

        // Main body
        g.setColor(FILL);
        g.fillRect(sx, sy, sw, sh);
        g.setColor(BORDER);
        g.drawRect(sx, sy, sw, sh);

        // Two small turret blocks on top corners
        int turretW = sw / 4;
        int turretH = sh / 4;
        g.setColor(TURRET);
        g.fillRect(sx, sy - turretH, turretW, turretH);
        g.fillRect(sx + sw - turretW, sy - turretH, turretW, turretH);

        // Health bar below the base
        int barH = 5;
        int barY = sy + sh + 3;
        g.setColor(HEALTH_BG);
        g.fillRect(sx, barY, sw, barH);

        // Green when above 30%, red when below
        double pct = getHealthPercent();
        int fillW = (int) (sw * pct);
        g.setColor(pct > 0.3 ? HEALTH_OK : HEALTH_LOW);
        g.fillRect(sx, barY, fillW, barH);
    }
}
