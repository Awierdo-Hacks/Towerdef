package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.towers.CannonTower;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.SpriteManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/*
 * Concrete CannonTower with Java2D rendering.
 *
 * Uses the tower_cannon.png sprite. Falls back to a gray rectangle
 * if the sprite cannot be loaded.
 */
public class J2dCannonTower extends CannonTower {

    private static final Color FILL   = new Color(105, 105, 105);
    private static final Color BORDER = new Color(50, 50, 50);

    private final J2dGame j2dGame;
    private final BufferedImage sprite;

    public J2dCannonTower(Position position, J2dGame j2dGame) {
        super(position, DEFAULT_RANGE, DEFAULT_DAMAGE, DEFAULT_FIRE_RATE, DEFAULT_COST,
              DEFAULT_SPLASH_RADIUS, DEFAULT_SPLASH_DAMAGE_FRACTION);
        this.j2dGame = j2dGame;
        this.sprite  = SpriteManager.getSprite("tower_cannon.png");
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
            g.setColor(FILL);
            g.fillRect(sx, sy, sw, sh);
            g.setColor(BORDER);
            g.drawRect(sx, sy, sw, sh);
        }
    }
}
