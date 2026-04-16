package sk.stuba.fiit.core;

import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public class MovementResolver {
    private final List<Rectangle> walls;

    public MovementResolver(List<Rectangle> walls) {
        this.walls = walls;
    }

    /**
     * Skontroluje ci je horizontalny pohyb o dx blokovany stenou.
     * Vracia skutocny posun (0 ak blokovany, dx inak).
     */
    public float resolveX(Rectangle hitbox, float dx) {
        if (walls == null || dx == 0f) return dx;

        Rectangle test = new Rectangle(
            hitbox.x + dx, hitbox.y,
            hitbox.width, hitbox.height
        );
        for (Rectangle wall : walls) {
            if (!test.overlaps(wall)) continue;
            float overlapX = Math.min(test.x + test.width,  wall.x + wall.width)
                - Math.max(test.x, wall.x);
            float overlapY = Math.min(test.y + test.height, wall.y + wall.height)
                - Math.max(test.y, wall.y);
            if (overlapX < overlapY) return 0f; // bocna stena
        }
        return dx;
    }
}
