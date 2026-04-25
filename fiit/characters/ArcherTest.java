package sk.stuba.fiit.characters;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArcherTest extends GdxTest {

    private static final Vector2D ORIGIN = new Vector2D(0, 0);

    private MockedConstruction.MockInitializer<AnimationManager> withIdleSize() {
        return (mock, ctx) -> when(mock.getFirstFrameSize("idle")).thenReturn(new Vector2D(32, 64));
    }

    // ── Constructor / stats ────────────────────────────────────────────────────

    @Test
    void name_isArcher() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals("Archer", new Archer(ORIGIN).getName());
        }
    }

    @Test
    void hp_is800() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Archer a = new Archer(ORIGIN);
            assertEquals(800, a.getHp());
            assertEquals(800, a.getMaxHp());
        }
    }

    @Test
    void speed_is3point5() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(3.5f, new Archer(ORIGIN).getSpeed(), 0.001f);
        }
    }

    @Test
    void attackPower_is20() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(20, new Archer(ORIGIN).getAttackPower());
        }
    }

    @Test
    void maxArmor_is50() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(50, new Archer(ORIGIN).getMaxArmor());
        }
    }

    // ── Arrow economy ─────────────────────────────────────────────────────────

    @Test
    void arrowCount_startsAt30() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(30, new Archer(ORIGIN).getArrowCount());
        }
    }

    @Test
    void maxArrows_is30() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(30, new Archer(ORIGIN).getMaxArrows());
        }
    }

    @Test
    void performPrimaryAttack_whenArrowsZero_doesNotStartAttack() throws Exception {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Archer a = new Archer(ORIGIN);
            // Force arrowCount = 0 via reflection
            var field = Archer.class.getDeclaredField("arrowCount");
            field.setAccessible(true);
            field.set(a, 0);

            a.performPrimaryAttack(null);

            // isAttacking is protected – accessible from same package
            assertFalse(a.isAttacking,
                "Attack must not start when arrowCount is 0");
        }
    }

    @Test
    void performPrimaryAttack_nullLevel_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertDoesNotThrow(() -> new Archer(ORIGIN).performPrimaryAttack(null));
        }
    }

    // ── Identity ──────────────────────────────────────────────────────────────

    @Test
    void isEnemy_false() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertFalse(new Archer(ORIGIN).isEnemy());
        }
    }

    @Test
    void isAlive_initially() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertTrue(new Archer(ORIGIN).isAlive());
        }
    }

    // ── HUD data: mana not applicable ─────────────────────────────────────────

    @Test
    void getCurrentMana_minusOne() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(-1, new Archer(ORIGIN).getCurrentMana());
        }
    }

    @Test
    void getMaxMana_minusOne() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(-1, new Archer(ORIGIN).getMaxMana());
        }
    }

    // ── Behaviour ────────────────────────────────────────────────────────────

    @Test
    void update_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertDoesNotThrow(() -> new Archer(ORIGIN).update(new UpdateContext(0.016f)));
        }
    }

    @Test
    void getAnimationManager_notNull() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertNotNull(new Archer(ORIGIN).getAnimationManager());
        }
    }

    @Test
    void move_updatesPosition() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Archer a = new Archer(ORIGIN);
            a.move(new Vector2D(10f, 5f));
            assertEquals(10f, a.getPosition().getX(), 0.001f);
            assertEquals(5f,  a.getPosition().getY(), 0.001f);
        }
    }

    @Test
    void revive_restoresHp() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Archer a = new Archer(ORIGIN);
            a.takeDamage(9999);
            a.revive();
            assertEquals(800, a.getHp());
            assertTrue(a.isAlive());
        }
    }

    @Test
    void takeDamage_kills() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Archer a = new Archer(ORIGIN);
            a.takeDamage(9999);
            assertFalse(a.isAlive());
        }
    }

    @Test
    void applySlow_reducesSpeed() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Archer a = new Archer(ORIGIN);
            float base = a.getSpeed();
            a.applySlow(0.5f, 5f);
            assertEquals(base * 0.5f, a.getSpeed(), 0.001f);
        }
    }
}
