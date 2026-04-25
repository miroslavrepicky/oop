package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.stuba.fiit.core.engine.Physicable;
import sk.stuba.fiit.util.Vector2D;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GravityStrategyTest {

    @Mock Physicable body;

    // ── NoGravity ─────────────────────────────────────────────────────────────

    @Test
    void noGravity_doesNotTouchBody() {
        GravityStrategy strategy = new NoGravity();
        strategy.apply(body, 0.016f, Collections.emptyList());
        verifyNoInteractions(body);
    }

    // ── NormalGravity ─────────────────────────────────────────────────────────

    @Test
    void normalGravity_decreasesVelocityY() {
        GravityStrategy strategy = new NormalGravity();
        Vector2D pos = new Vector2D(0f, 100f);

        when(body.getVelocityY()).thenReturn(0f);
        when(body.getPosition()).thenReturn(pos);
        when(body.getHitbox()).thenReturn(new Rectangle(0, 100, 32, 64));

        strategy.apply(body, 0.1f, Collections.emptyList());

        // NormalGravity: velocityY += -500 * 0.1 = -50
        verify(body).setVelocityY(-50f);
    }

    @Test
    void normalGravity_setsOnGroundFalse_whenNoPlatforms() {
        GravityStrategy strategy = new NormalGravity();
        Vector2D pos = new Vector2D(0f, 100f);

        when(body.getVelocityY()).thenReturn(0f);
        when(body.getPosition()).thenReturn(pos);
        when(body.getHitbox()).thenReturn(new Rectangle(0, 100, 32, 64));

        strategy.apply(body, 0.1f, null);

        verify(body).setOnGround(false);
    }

    // ── FloatingGravity ───────────────────────────────────────────────────────

    @Test
    void floatingGravity_appliesSmallGravity() {
        GravityStrategy strategy = new FloatingGravity();
        Vector2D pos = new Vector2D(0f, 100f);

        when(body.getVelocityY()).thenReturn(0f);
        when(body.getPosition()).thenReturn(pos);
        when(body.getHitbox()).thenReturn(new Rectangle(0, 100, 32, 32));

        strategy.apply(body, 0.1f, Collections.emptyList());

        // FloatingGravity: GRAVITY = -50, delta=0.1 → velocityY += -5
        verify(body).setVelocityY(-5f);
    }

    @Test
    void floatingGravity_nullPlatforms_doesNotThrow() {
        GravityStrategy strategy = new FloatingGravity();
        Vector2D pos = new Vector2D(0f, 50f);

        when(body.getVelocityY()).thenReturn(0f);
        when(body.getPosition()).thenReturn(pos);
        when(body.getHitbox()).thenReturn(new Rectangle(0, 50, 32, 32));

        assertDoesNotThrow(() -> strategy.apply(body, 0.016f, null));
    }
}
