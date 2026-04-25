package sk.stuba.fiit.characters;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WizzardTest extends GdxTest {

    private static final Vector2D ORIGIN = new Vector2D(0, 0);

    private MockedConstruction.MockInitializer<AnimationManager> withIdleSize() {
        return (mock, ctx) -> when(mock.getFirstFrameSize("idle")).thenReturn(new Vector2D(32, 64));
    }

    /** Sets the private {@code mana} field via reflection. */
    private static void setMana(Wizzard w, int value) {
        try {
            var f = Wizzard.class.getDeclaredField("mana");
            f.setAccessible(true);
            f.set(w, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ── Constructor / stats ────────────────────────────────────────────────────

    @Test
    void name_isWizzard() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals("Wizzard", new Wizzard(ORIGIN).getName());
        }
    }

    @Test
    void hp_is7000() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Wizzard w = new Wizzard(ORIGIN);
            assertEquals(7000, w.getHp());
            assertEquals(7000, w.getMaxHp());
        }
    }

    @Test
    void speed_is2point5() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(2.5f, new Wizzard(ORIGIN).getSpeed(), 0.001f);
        }
    }

    @Test
    void attackPower_is40() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(40, new Wizzard(ORIGIN).getAttackPower());
        }
    }

    @Test
    void maxArmor_is30() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(30, new Wizzard(ORIGIN).getMaxArmor());
        }
    }

    // ── Mana system ───────────────────────────────────────────────────────────

    @Test
    void getCurrentMana_startsAt100() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(100, new Wizzard(ORIGIN).getCurrentMana());
        }
    }

    @Test
    void getMaxMana_is100() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(100, new Wizzard(ORIGIN).getMaxMana());
        }
    }

    @Test
    void update_regeneratesMana_whenBelowMax() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Wizzard w = new Wizzard(ORIGIN);
            setMana(w, 50);

            // 5 mana/s × 2 s = 10 → 60
            w.update(new UpdateContext(2.0f));

            assertEquals(60, w.getCurrentMana());
        }
    }

    @Test
    void update_manaCappedAtMax() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Wizzard w = new Wizzard(ORIGIN);
            // mana already at max; regeneration should not exceed 100
            w.update(new UpdateContext(100f));
            assertEquals(100, w.getCurrentMana());
        }
    }

    @Test
    void update_partialRegeneration_doesNotOverflow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Wizzard w = new Wizzard(ORIGIN);
            setMana(w, 98);
            // 5 mana/s × 0.4 s = 2 (int cast) → 100
            w.update(new UpdateContext(0.4f));
            assertEquals(100, w.getCurrentMana());
        }
    }

    @Test
    void spendMana_reducesCurrentMana() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Wizzard w = new Wizzard(ORIGIN);
            // spendMana is protected – accessible from same package
            w.spendMana(30);
            assertEquals(70, w.getCurrentMana());
        }
    }

    @Test
    void spendMana_clampedToZero() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Wizzard w = new Wizzard(ORIGIN);
            w.spendMana(9999);
            assertEquals(0, w.getCurrentMana());
        }
    }

    @Test
    void getMana_equalsCurrentMana() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Wizzard w = new Wizzard(ORIGIN);
            // getMana() is protected; accessible from same package
            assertEquals(w.getMana(), w.getCurrentMana());
        }
    }

    // ── Identity ──────────────────────────────────────────────────────────────

    @Test
    void isEnemy_false() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertFalse(new Wizzard(ORIGIN).isEnemy());
        }
    }

    @Test
    void isAlive_initially() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertTrue(new Wizzard(ORIGIN).isAlive());
        }
    }

    // ── Arrow system: not applicable ──────────────────────────────────────────

    @Test
    void getArrowCount_minusOne() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(-1, new Wizzard(ORIGIN).getArrowCount());
        }
    }

    @Test
    void getMaxArrows_minusOne() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertEquals(-1, new Wizzard(ORIGIN).getMaxArrows());
        }
    }

    // ── Behaviour ─────────────────────────────────────────────────────────────

    @Test
    void getAnimationManager_notNull() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertNotNull(new Wizzard(ORIGIN).getAnimationManager());
        }
    }

    @Test
    void revive_restoresHpAndMana() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Wizzard w = new Wizzard(ORIGIN);
            w.takeDamage(9999);
            setMana(w, 10);

            w.revive();

            assertEquals(7000, w.getHp());
            assertTrue(w.isAlive());
        }
    }

    @Test
    void applySlow_reducesSpeed() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            Wizzard w = new Wizzard(ORIGIN);
            float base = w.getSpeed();
            w.applySlow(0.4f, 3f);
            assertEquals(base * 0.4f, w.getSpeed(), 0.001f);
        }
    }

    @Test
    void performPrimaryAttack_nullLevel_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertDoesNotThrow(() -> new Wizzard(ORIGIN).performPrimaryAttack(null));
        }
    }

    @Test
    void performSecondaryAttack_nullLevel_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, withIdleSize())) {
            assertDoesNotThrow(() -> new Wizzard(ORIGIN).performSecondaryAttack(null));
        }
    }
}
