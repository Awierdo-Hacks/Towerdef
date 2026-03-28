package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.CannonTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;

import java.awt.Color;
import java.awt.Graphics2D;

/*
 * Concrete CannonTower with Java2D rendering.
 *
 * Drawn as a dark gray square with a cannon barrel (thick line).
 * Larger than the ArrowTower to show it's heavier and more expensive.
 *
 * Uses default stats from CannonTower including splash damage.
 */
public class J2dCannonTower extends CannonTower {

    private static final Color FILL   = new Color(105, 105, 105);  // dim gray
    private static final Color BORDER = new Color(50, 50, 50);     // darker gray
    private static final Color BARREL = new Color(30, 30, 30);     // near-black barrel

    private final J2dGame j2dGame;

    public J2dCannonTower(Position position, J2dGame j2dGame) {
        super(position, DEFAULT_RANGE, DEFAULT_DAMAGE, DEFAULT_FIRE_RATE, DEFAULT_COST,
              DEFAULT_SPLASH_RADIUS, DEFAULT_SPLASH_DAMAGE_FRACTION);
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

        // Tower body
        g.setColor(FILL);
        g.fillRect(sx, sy, sw, sh);
        g.setColor(BORDER);
        g.drawRect(sx, sy, sw, sh);

        // Cannon barrel — thick line sticking out to the right
        int cx = sx + sw / 2;
        int cy = sy + sh / 2;
        g.setColor(BARREL);
        g.fillRect(cx, cy - sh / 6, sw / 2 + 4, sh / 3);
    }
}
