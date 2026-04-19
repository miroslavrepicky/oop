package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * Resolves horizontal movement against a list of wall rectangles.
 *
 * <p>Used by {@code EnemyCharacter.move()} to prevent enemies from walking
 * through walls. The resolver checks whether moving by {@code dx} would cause
 * the hitbox to overlap any wall; if so, it returns {@code 0f} (blocked).
 *
 * <p>Only lateral (side) collisions are considered: a collision is treated as
 * a wall hit only when the horizontal overlap is smaller than the vertical overlap.
 */
public class MovementResolver {
    private final List<Rectangle> walls;

    /**
     * @param walls list of collision rectangles representing walls and platforms
     */
    public MovementResolver(List<Rectangle> walls) {
        this.walls = walls;
    }

    /**
     * Checks whether horizontal movement by {@code dx} is blocked by any wall.
     *
     * @param hitbox the moving object's current hitbox
     * @param dx     desired horizontal displacement (positive = right)
     * @return {@code dx} if the move is unobstructed, {@code 0f} if blocked
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
