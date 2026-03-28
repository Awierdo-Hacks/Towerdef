package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Bonus;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;

import java.awt.Color;
import java.awt.Graphics2D;

/*
 * Concrete Bonus with Java2D rendering.
 *
 * Drawn as a glowing yellow diamond that pulses (alpha changes) as it
 * approaches expiry. The player clicks on it to collect the bonus.
 *
 * Default size is 0.5 x 0.5 game-world units, lifetime is 10 seconds.
 *
 * The applyEffect() is a placeholder — concrete bonus types (GoldBonus,
 * DamageBoost, etc.) would extend this class or use a BonusType enum.
 * For now it grants 25 bonus gold when collected.
 */
public class J2dBonus extends Bonus {

    private static final Color BORDER    = new Color(200, 200, 0);
    private static final double BONUS_SIZE     = 0.5;
    private static final double BONUS_LIFETIME = 10.0;  // seconds on map
    private static final int    BONUS_GOLD     = 25;     // gold granted on pickup

    private final J2dGame j2dGame;

    public J2dBonus(Position position, J2dGame j2dGame) {
        super(position, BONUS_SIZE, BONUS_SIZE, BONUS_LIFETIME);
        this.j2dGame = j2dGame;
    }

    /*
     * Grants bonus gold to the player when collected.
     * In later phases, subclasses could provide different effects
     * (damage boost, slow all enemies, etc.).
     */
    @Override
    protected void applyEffect() {
        be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.Game.getInstance()
            .addGold(BONUS_GOLD);
    }

    @Override
    public void render() {
        Graphics2D g = j2dGame.getGraphics2D();
        if (g == null) return;

        int sx = j2dGame.toScreenX(position.getX() - width / 2);
        int sy = j2dGame.toScreenY(position.getY() - height / 2);
        int sw = j2dGame.toScreenWidth(width);
        int sh = j2dGame.toScreenHeight(height);

        // Pulse effect — alpha decreases as the bonus approaches expiry
        double remaining = getRemainingTime();
        int alpha = (int) (255 * Math.min(1.0, remaining / 3.0));  // fade in last 3 seconds
        alpha = Math.max(80, alpha);  // never fully invisible

        // Diamond shape
        int cx = sx + sw / 2;
        int cy = sy + sh / 2;
        int[] xPoints = {cx, cx + sw / 2, cx, cx - sw / 2};
        int[] yPoints = {cy - sh / 2, cy, cy + sh / 2, cy};

        g.setColor(new Color(255, 255, 0, alpha));
        g.fillPolygon(xPoints, yPoints, 4);
        g.setColor(BORDER);
        g.drawPolygon(xPoints, yPoints, 4);
    }
}
