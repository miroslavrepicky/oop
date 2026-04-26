package sk.stuba.fiit.characters;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KnightTest extends GdxTest {

    private static final Vector2D ORIGIN = new Vector2D(0, 0);

    /** Helper: stub AnimationManager so getFirstFrameSize("idle") returns a usable size. */
    private MockedConstruction.MockInitializer<AnimationManager> withIdleSize() {
        return (mock, ctx) -> when(mock.getFirstFrameSize("idle")).thenReturn(new Vector2D(32, 64));
    }

    //  Identity / type

    @Test
    void isEnemy_false() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertFalse(new Knight(ORIGIN).isEnemy());
        }
    }

    @Test
    void isAlive_initially() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertTrue(new Knight(ORIGIN).isAlive());
        }
    }

    //  Animation

    @Test
    void getAnimationManager_notNull() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertNotNull(new Knight(ORIGIN).getAnimationManager());
        }
    }

    //  HUD data

    @Test
    void currentMana_minusOne_noManaSystem() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(-1, new Knight(ORIGIN).getCurrentMana());
        }
    }

    @Test
    void maxMana_minusOne_noManaSystem() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(-1, new Knight(ORIGIN).getMaxMana());
        }
    }

    @Test
    void arrowCount_minusOne_noArrowSystem() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(-1, new Knight(ORIGIN).getArrowCount());
        }
    }

    @Test
    void maxArrows_minusOne_noArrowSystem() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(-1, new Knight(ORIGIN).getMaxArrows());
        }
    }

    //  Behaviour

    @Test
    void update_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertDoesNotThrow(() -> new Knight(ORIGIN).update(new UpdateContext(0.016f)));
        }
    }

    @Test
    void performPrimaryAttack_nullLevel_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertDoesNotThrow(() -> new Knight(ORIGIN).performPrimaryAttack(null));
        }
    }

    @Test
    void performSecondaryAttack_nullLevel_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertDoesNotThrow(() -> new Knight(ORIGIN).performSecondaryAttack(null));
        }
    }

    @Test
    void move_updatesPosition() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Knight k = new Knight(ORIGIN);
            k.move(new Vector2D(10f, 5f));
            assertEquals(10f, k.getPosition().getX(), 0.001f);
            assertEquals(5f,  k.getPosition().getY(), 0.001f);
        }
    }

    @Test
    void move_updatesHitbox() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Knight k = new Knight(ORIGIN);
            k.move(new Vector2D(15f, 0f));
            assertEquals(15f, k.getHitbox().x, 0.001f);
        }
    }

    @Test
    void jump_onGround_setsVelocity() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Knight k = new Knight(ORIGIN);
            k.setOnGround(true);
            k.jump(300f);
            assertEquals(300f, k.getVelocityY(), 0.001f);
        }
    }

    @Test
    void revive_restoresFullHp() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Knight k = new Knight(ORIGIN);
            k.takeDamage(9999);
            k.revive();
            assertEquals(350, k.getHp());
            assertTrue(k.isAlive());
        }
    }

    @Test
    void addArmor_cappedAtMax() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Knight k = new Knight(ORIGIN);
            k.addArmor(200);
            assertEquals(80, k.getArmor());
        }
    }

    @Test
    void position_setCorrectlyInConstructor() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Knight k = new Knight(new Vector2D(100f, 200f));
            assertEquals(100f, k.getPosition().getX(), 0.001f);
            assertEquals(200f, k.getPosition().getY(), 0.001f);
        }
    }
}
