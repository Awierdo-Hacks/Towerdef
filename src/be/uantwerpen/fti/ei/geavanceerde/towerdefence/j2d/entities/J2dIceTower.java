package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.IceTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.SpriteManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Concrete {@link IceTower} with Java2D rendering.
 *
 * <p>Uses the {@code tower_ice.png} sprite, falling back to a cyan diamond if the
 * sprite cannot be loaded. The slow-range ring is drawn generically for every tower by
 * {@code J2dGame.renderTowerRanges()}.</p>
 *
 * @author Tower Defence team
 */
public class J2dIceTower extends IceTower {

    /** Fallback fill colour when the sprite cannot be loaded. */
    private static final Color FILL   = new Color(0, 206, 209);
    /** Fallback border colour when the sprite cannot be loaded. */
    private static final Color BORDER = new Color(0, 139, 139);

    /** The view used for graphics access and coordinate conversion. */
    private final J2dGame j2dGame;
    /** The tower sprite, or {@code null} if it could not be loaded. */
    private final BufferedImage sprite;

    /**
     * Creates a Java2D ice tower at the given position.
     *
     * @param position the build position in game-world coordinates
     * @param j2dGame  the view used for rendering and coordinate conversion
     */
    public J2dIceTower(Position position, J2dGame j2dGame) {
        super(position, DEFAULT_RANGE, DEFAULT_SLOW_FACTOR, DEFAULT_SLOW_DURATION, DEFAULT_COST);
        this.j2dGame = j2dGame;
        this.sprite  = SpriteManager.getSprite("tower_ice.png");
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
