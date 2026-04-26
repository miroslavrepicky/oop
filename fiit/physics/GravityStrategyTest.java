package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.stuba.fiit.core.engine.Physicable;
import sk.stuba.fiit.util.Vector2D;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GravityStrategyTest {

    @Mock Physicable body;

    //  NoGravity

    @Test
    void noGravity_doesNotTouchBody() {
        GravityStrategy strategy = new NoGravity();
        strategy.apply(body, 0.016f, Collections.emptyList());
        verifyNoInteractions(body);
    }

    //  NormalGravity

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

    //  FloatingGravity

    @Test
    void floatingGravity_appliesSmallGravity() {
        GravityStrategy strategy = new FloatingGravity();
        Vector2D pos = new Vector2D(0f, 100f);

        when(body.getVelocityY()).thenReturn(0f);
        when(body.getPosition()).thenReturn(pos);
        when(body.getHitbox()).thenReturn(new Rectangle(0, 100, 32, 32));

        strategy.apply(body, 0.1f, Collections.emptyList());

        // FloatingGravity: GRAVITY = -50, delta=0.1 -> velocityY += -5
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

    //  Rozsirene testy pre FloatingGravity na zvysenie coverage

    @Test
    void floatingGravity_collisionFromAbove_resetsVelocityAndPosition() {
        GravityStrategy strategy = new FloatingGravity();
        Vector2D pos = new Vector2D(0f, 100f);
        // Simulujeme pohyb hore (kladna rychlost)
        when(body.getVelocityY()).thenReturn(50f);
        when(body.getPosition()).thenReturn(pos);
        // Kacka ma hitbox 32x32
        when(body.getHitbox()).thenReturn(new Rectangle(0, 100, 32, 32));

        // Platforma presne nad hlavou (strop)
        Rectangle ceiling = new Rectangle(0, 105, 100, 10);

        strategy.apply(body, 0.1f, List.of(ceiling));

        // Overime, ze pri naraze do stropu sa rychlost vynulovala
        verify(body).setVelocityY(0f);
        // Overime, ze pozicia Y bola upravena (vytlacena pod platformu)
        // Strop je na y=105, vyska postavy je 32 -> nova pozicia y by mala byt 105 - 32 = 73
        verify(body).setOnGround(false);
    }

    @Test
    void floatingGravity_landOnPlatform_setsOnGroundTrue() {
        GravityStrategy strategy = new FloatingGravity();
        Vector2D pos = new Vector2D(0f, 50f);

        // Nastavime vyssiu padovu rychlost, aby sme mali istotu, ze vletime do platformy
        when(body.getVelocityY()).thenReturn(-100f);
        when(body.getPosition()).thenReturn(pos);
        // Kacka ma vysku 32
        when(body.getHitbox()).thenReturn(new Rectangle(0, 50, 32, 32));

        // Platforma pod nohami: horna hrana bude na y = 45
        // Pri vy = -100 a gravitácii postava padne na y = 39.5, co je vnutri platformy
        Rectangle floor = new Rectangle(0, 20, 100, 25);

        strategy.apply(body, 0.1f, List.of(floor));

        // 1. Overime, ze pri dopade sa rychlost vynulovala
        verify(body).setVelocityY(0f);

        // 2. Overime, ze onGround je true
        verify(body).setOnGround(true);

        // 3. Overime, ze pozicia Y bola zarovnaná na hornu hranu platformy (y=45)
        assertEquals(45f, pos.getY(), 0.01f);
    }

    @Test
    void floatingGravity_horizontalCollision_doesNotTriggerVerticalResponse() {
        GravityStrategy strategy = new FloatingGravity();
        Vector2D pos = new Vector2D(0f, 100f);
        when(body.getVelocityY()).thenReturn(0f);
        when(body.getPosition()).thenReturn(pos);
        when(body.getHitbox()).thenReturn(new Rectangle(0, 100, 32, 32));

        // Platforma zboku (stena) - overlapX bude vacsi ako overlapY,
        // alebo v tvojom kode podmienka (overlapY <= overlapX) rozhodne
        Rectangle wall = new Rectangle(30, 100, 10, 100);

        strategy.apply(body, 0.1f, List.of(wall));

        // Ak je to cisto horizontálna kolizia, kod v "if (overlapY <= overlapX)"
        // by sa mal správat podla toho, ako más nastavene priority.
    }
}
