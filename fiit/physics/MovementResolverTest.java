package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MovementResolverTest {

    // NOTE: com.badlogic.gdx.math.Rectangle is a pure-Java data class with
    // no OpenGL dependency, so no mock/headless setup is needed.

    private Rectangle hitbox;

    @BeforeEach
    void setUp() {
        hitbox = new Rectangle(100, 50, 30, 60); // x=100, y=50, w=30, h=60
    }

    @Test
    void resolveX_noWalls_returnsDx() {
        MovementResolver resolver = new MovementResolver(List.of());
        assertEquals(5f, resolver.resolveX(hitbox, 5f), 0.001f);
    }

    @Test
    void resolveX_nullWalls_returnsDx() {
        MovementResolver resolver = new MovementResolver(null);
        assertEquals(5f, resolver.resolveX(hitbox, 5f), 0.001f);
    }

    @Test
    void resolveX_zeroDx_returnsZero() {
        Rectangle wall = new Rectangle(200, 0, 50, 200);
        MovementResolver resolver = new MovementResolver(List.of(wall));
        assertEquals(0f, resolver.resolveX(hitbox, 0f), 0.001f);
    }

    @Test
    void resolveX_wallToRight_blocksMovement() {
        // hitbox right edge = 130; wall starts at 132 -> overlap after dx=10 -> x=110, right=140, wall x=132
        Rectangle wall = new Rectangle(132, 50, 50, 60); // overlaps horizontally, same y-range
        MovementResolver resolver = new MovementResolver(List.of(wall));
        // After dx=10: test box x=110, right=140 -> overlap with wall x=132-182
        // overlapX = 140-132 = 8, overlapY = min(110,110)-max(50,50) = 60 -> overlapX < overlapY -> blocked
        float result = resolver.resolveX(hitbox, 10f);
        assertEquals(0f, result, 0.001f);
    }

    @Test
    void resolveX_wallFarAway_allowsMovement() {
        Rectangle wall = new Rectangle(500, 50, 50, 60);
        MovementResolver resolver = new MovementResolver(List.of(wall));
        assertEquals(10f, resolver.resolveX(hitbox, 10f), 0.001f);
    }

    @Test
    void resolveX_wallBelowHitbox_allowsMovement() {
        // Wall is at y=0, hitbox at y=50 – vertical platform, not a side wall
        // After moving dx=10, hitbox x=110-140, wall x=112-162, y=0-30
        // overlapY between y=50-110 and y=0-30 -> 0 (no vertical overlap) -> no collision
        Rectangle wall = new Rectangle(112, 0, 50, 30);
        MovementResolver resolver = new MovementResolver(List.of(wall));
        assertEquals(10f, resolver.resolveX(hitbox, 10f), 0.001f);
    }
}
