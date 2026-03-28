package be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.entities;

import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.entities.Obstacle;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.game.util.Position;
import be.uantwerpen.fti.ei.geavanceerde.towerdefence.j2d.J2dGame;

import java.awt.Color;
import java.awt.Graphics2D;

/*
 * Concrete Obstacle with Java2D rendering.
 *
 * Drawn as a brown rounded rectangle to suggest a rock or tree stump.
 * Obstacles occupy one tile and block tower placement.
 *
 * Default size is 0.8 x 0.8 game-world units (slightly smaller than a tile).
 */
public class J2dObstacle extends Obstacle {

    private static final Color FILL   = new Color(139, 119, 101);  // rosy brown
    private static final Color BORDER = new Color(101, 67, 33);    // dark brown
    private static final double OBSTACLE_SIZE = 0.8;

    private final J2dGame j2dGame;

    public J2dObstacle(Position position, J2dGame j2dGame) {
        super(position, OBSTACLE_SIZE, OBSTACLE_SIZE);
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

        // Rounded rectangle — suggests a natural obstacle
        g.setColor(FILL);
        g.fillRoundRect(sx, sy, sw, sh, 6, 6);
        g.setColor(BORDER);
        g.drawRoundRect(sx, sy, sw, sh, 6, 6);
    }
}
