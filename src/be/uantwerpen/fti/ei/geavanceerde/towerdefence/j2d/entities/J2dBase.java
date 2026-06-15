package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Base;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.SpriteManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/*
 * Concrete Base with Java2D rendering.
 *
 * Uses the nurse_base.png sprite. Falls back to a gold-coloured castle
 * shape (a rectangle with two small turrets on the corners) if the
 * sprite cannot be loaded. A health bar below shows remaining HP.
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

    // Visuele vergroting van de base-sprite t.o.v. zijn logische tile (1x1). Puur
    // cosmetisch en gecentreerd op de positie: de sprite mag over de tile-rand komen
    // zodat de nurse goed zichtbaar is i.p.v. miniatuur. De logische width/height —
    // en dus gameplay/hitbox — blijven onaangeroerd. Verhoog/verlaag naar smaak.
    private static final double VISUAL_SCALE = 1.6;

    private final J2dGame j2dGame;
    private final BufferedImage sprite;

    public J2dBase(Position position, int maxHealth, J2dGame j2dGame) {
        super(position, BASE_SIZE, BASE_SIZE, maxHealth);
        this.j2dGame = j2dGame;
        this.sprite  = SpriteManager.getSprite("nurse_base.png");
    }

    @Override
    public void render() {
        Graphics2D g = j2dGame.getGraphics2D();
        if (g == null) return;

        // Visuele grootte (groter dan de logische hitbox), gecentreerd op de positie.
        // De health-bar hieronder anchort op sx/sy/sw/sh en schuift dus mee.
        double vw = width  * VISUAL_SCALE;
        double vh = height * VISUAL_SCALE;
        int sx = j2dGame.toScreenX(position.getX() - vw / 2);
        int sy = j2dGame.toScreenY(position.getY() - vh / 2);
        int sw = j2dGame.toScreenWidth(vw);
        int sh = j2dGame.toScreenHeight(vh);

        if (sprite != null) {
            // De sprite (768x768) en het vak zijn beide vierkant, dus dit is gewoon
            // cover-scaling zonder squish: vul het vak volledig.
            g.drawImage(sprite, sx, sy, sw, sh, null);
        } else {
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
        }

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
