package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.core.engine.Physicable;
import sk.stuba.fiit.util.Vector2D;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class NormalGravityPlatformTest {

    static class StubBody implements Physicable {
        Vector2D pos; float vy; boolean onGround; Rectangle hitbox;
        StubBody(float x, float y, float vy) {
            pos = new Vector2D(x, y); this.vy = vy;
            hitbox = new Rectangle(x, y, 32, 64);
        }
        @Override public Vector2D getPosition()           { return pos; }
        @Override public void     setPosition(Vector2D p) { pos = p; }
        @Override public float    getVelocityY()          { return vy; }
        @Override public void     setVelocityY(float v)   { vy = v; }
        @Override public Rectangle getHitbox()            { return hitbox; }
        @Override public boolean  isOnGround()            { return onGround; }
        @Override public void     setOnGround(boolean b)  { onGround = b; }
        @Override public void     updateHitbox()          { hitbox.setPosition(pos.getX(), pos.getY()); }
    }

    @Test
    void landOnPlatform_setsOnGroundAndZerosVelocity() {
        NormalGravity g = new NormalGravity();
        StubBody body = new StubBody(0, 105, -50f); // falling
        Rectangle platform = new Rectangle(-10, 90, 100, 20); // top at y=110

        g.apply(body, 0.1f, List.of(platform));

        assertTrue(body.onGround);
        assertEquals(0f, body.vy, 0.001f);
    }

    @Test
    void hitCeiling_zerosVelocityAndMovesDown() {
        NormalGravity g = new NormalGravity();
        StubBody body = new StubBody(0, 180, 50f); // moving up
        Rectangle ceiling = new Rectangle(-10, 190, 100, 20); // floor at y=190

        g.apply(body, 0.1f, List.of(ceiling));

        assertEquals(0f, body.vy, 0.001f);
        assertFalse(body.onGround);
    }

    @Test
    void noOverlap_onGroundFalse() {
        NormalGravity g = new NormalGravity();
        StubBody body = new StubBody(0, 300, 0f);
        Rectangle farPlatform = new Rectangle(500, 0, 100, 10);

        g.apply(body, 0.016f, List.of(farPlatform));

        assertFalse(body.onGround);
    }

    @Test
    void emptyPlatforms_noGroundContact() {
        NormalGravity g = new NormalGravity();
        StubBody body = new StubBody(0, 100, 0f);
        g.apply(body, 0.016f, List.of());
        assertFalse(body.onGround);
    }
}
