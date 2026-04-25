package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.core.engine.Physicable;
import sk.stuba.fiit.util.Vector2D;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class FlyingGravityTest {

    static class StubBody implements Physicable {
        Vector2D pos;
        float vy;
        boolean onGround;
        Rectangle hitbox;

        StubBody(float x, float y, float vy) {
            this.pos = new Vector2D(x, y);
            this.vy = vy;
            this.hitbox = new Rectangle(x, y, 32, 32);
        }

        @Override public Vector2D  getPosition()           { return pos; }
        @Override public void      setPosition(Vector2D p) { this.pos = p; }
        @Override public float     getVelocityY()          { return vy; }
        @Override public void      setVelocityY(float v)   { this.vy = v; }
        @Override public Rectangle getHitbox()             { return hitbox; }
        @Override public boolean   isOnGround()            { return onGround; }
        @Override public void      setOnGround(boolean b)  { this.onGround = b; }
        @Override public void      updateHitbox()          { hitbox.setPosition(pos.getX(), pos.getY()); }
    }

    @Test
    void apply_changesVelocityY() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(0, 100, 0);
        g.apply(body, 0.1f, Collections.emptyList());
        // velocity must change (positive or negative random increment)
        assertNotEquals(0f, body.vy, 1f); // allows tiny float rounding
    }

    @Test
    void apply_nullPlatforms_doesNotThrow() {
        FlyingGravity g = new FlyingGravity();
        assertDoesNotThrow(() -> g.apply(new StubBody(0, 50, 0), 0.016f, null));
    }

    @Test
    void apply_velocityClampedBelowMax() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(0, 500, 200f); // above MAX_DIFFERENCE=150
        g.apply(body, 0.1f, Collections.emptyList());
        assertTrue(body.vy <= 150f);
    }

    @Test
    void apply_velocityClampedAboveMin() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(0, 500, -200f); // below -MAX_DIFFERENCE
        g.apply(body, 0.1f, Collections.emptyList());
        assertTrue(body.vy >= -150f);
    }

    @Test
    void apply_withPlatformBelow_setsOnGround() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(0, 100, -50f); // moving down
        Rectangle platform = new Rectangle(0, 90, 100, 20); // platform at y=90-110
        g.apply(body, 0.1f, java.util.List.of(platform));
        // after collision body should be on ground
        assertTrue(body.onGround);
        assertEquals(0f, body.vy, 0.001f);
    }

    @Test
    void apply_updatesHitboxPosition() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(10, 200, 0);
        g.apply(body, 0.1f, Collections.emptyList());
        assertEquals(body.pos.getY(), body.hitbox.y, 0.5f);
    }
}
