package sk.stuba.fiit.characters;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EnemyArcher.
 * Covers: stats, arrow economy, triggerAttack override, detection, effects.
 * AnimationManager je mockovaný aby sa nezaťažoval atlas z disku.
 */
class EnemyArcherTest extends GdxTest {

    private static final Vector2D ORIGIN = new Vector2D(0, 0);

    /** Stub: getFirstFrameSize("idle") vracia validný vektor, ostatné volania majú rozumné defaulty. */
    private MockedConstruction.MockInitializer<AnimationManager> animStub() {
        return (mock, ctx) -> {
            when(mock.getFirstFrameSize("idle")).thenReturn(new Vector2D(32, 64));
            when(mock.getAnimationDuration(anyString())).thenReturn(0.5f);
            when(mock.getFrameCount(anyString())).thenReturn(5);
            when(mock.hasAnimation(anyString())).thenReturn(false);
            when(mock.getAnimationSize(anyString())).thenReturn(new Vector2D(32, 64));
        };
    }

    // ── Základné štatistiky ───────────────────────────────────────────────────

    @Test
    void name_isEnemyArcher() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertEquals("EnemyArcher", new EnemyArcher(ORIGIN).getName());
        }
    }

    @Test
    void hp_is70() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            assertEquals(70, a.getHp());
            assertEquals(70, a.getMaxHp());
        }
    }

    @Test
    void attackPower_is15() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertEquals(15, new EnemyArcher(ORIGIN).getAttackPower());
        }
    }

    @Test
    void speed_is2point0() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertEquals(2.0f, new EnemyArcher(ORIGIN).getSpeed(), 0.001f);
        }
    }

    @Test
    void armor_is5() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            assertEquals(5, a.getArmor());
            assertEquals(5, a.getMaxArmor());
        }
    }

    // ── Identita ──────────────────────────────────────────────────────────────

    @Test
    void isEnemy_true() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertTrue(new EnemyArcher(ORIGIN).isEnemy());
        }
    }

    @Test
    void isAlive_initially() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertTrue(new EnemyArcher(ORIGIN).isAlive());
        }
    }

    @Test
    void isAttacking_initiallyFalse() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertFalse(new EnemyArcher(ORIGIN).isAttacking());
        }
    }

    @Test
    void getAnimationManager_notNull() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertNotNull(new EnemyArcher(ORIGIN).getAnimationManager());
        }
    }

    // ── Ekonómia šípov ────────────────────────────────────────────────────────

    @Test
    void arrowCount_initiallyIs20() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertEquals(20, new EnemyArcher(ORIGIN).getArrowCount());
        }
    }

    @Test
    void triggerAttack_withArrows_setsAttacking() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            a.triggerAttack();
            assertTrue(a.isAttacking());
        }
    }

    @Test
    void triggerAttack_withArrows_decrementsCount() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            int before = a.getArrowCount();
            a.triggerAttack();
            assertEquals(before - 1, a.getArrowCount());
        }
    }

    @Test
    void triggerAttack_noArrows_doesNotSetAttacking() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            setArrowCount(a, 0);
            a.triggerAttack();
            assertFalse(a.isAttacking());
        }
    }

    @Test
    void triggerAttack_noArrows_countRemainsZero() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            setArrowCount(a, 0);
            a.triggerAttack();
            assertEquals(0, a.getArrowCount());
        }
    }

    /**
     * Keď je isAttacking == true, super.triggerAttack() nezmení stav
     * (cooldown/guard), takže šíp sa nesmie spotrebovať druhýkrát.
     */
    @Test
    void triggerAttack_whileAlreadyAttacking_arrowNotConsumedAgain() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            a.triggerAttack();               // prvý útok
            int afterFirst = a.getArrowCount();
            a.triggerAttack();               // ignorovaný (isAttacking = true)
            assertEquals(afterFirst, a.getArrowCount());
        }
    }

    @Test
    void triggerAttack_onlyOneArrowRemaining_consumesItAndStillAttacks() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            setArrowCount(a, 1);
            a.triggerAttack();
            assertTrue(a.isAttacking());
            assertEquals(0, a.getArrowCount());
        }
    }

    // ── Pohyb ─────────────────────────────────────────────────────────────────

    @Test
    void move_updatesPosition() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            a.move(new Vector2D(15f, 0f));
            assertEquals(15f, a.getPosition().getX(), 0.001f);
        }
    }

    @Test
    void move_updatesHitbox() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            a.move(new Vector2D(15f, 0f));
            assertEquals(a.getPosition().getX(), a.getHitbox().x, 0.001f);
        }
    }

    // ── Poškodenie / smrť / oživenie ─────────────────────────────────────────

    @Test
    void takeDamage_kills() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            a.takeDamage(9999);
            assertFalse(a.isAlive());
        }
    }

    @Test
    void revive_restoresHp() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            a.takeDamage(9999);
            a.revive();
            assertEquals(70, a.getHp());
            assertTrue(a.isAlive());
        }
    }

    @Test
    void takeDamage_armorAbsorbsFirst() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN); // armor = 5
            a.takeDamage(3);  // absorbuje armor celé, hp nezmenené
            assertEquals(70, a.getHp());
            assertEquals(2, a.getArmor());
        }
    }

    // ── Efekty ────────────────────────────────────────────────────────────────

    @Test
    void applySlow_reducesSpeed() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            float base = a.getSpeed();
            a.applySlow(0.5f, 3f);
            assertEquals(base * 0.5f, a.getSpeed(), 0.001f);
        }
    }

    @Test
    void revive_clearsSlow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            float base = a.getSpeed();
            a.applySlow(0.2f, 100f);
            a.revive();
            assertEquals(base, a.getSpeed(), 0.001f);
        }
    }

    // ── Detekcia hráča ───────────────────────────────────────────────────────

    @Test
    void detectPlayer_withinDetectionRange_true() {
        // detectionRange EnemyArcher = 300f
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            assertTrue(a.detectPlayer(stubPlayer(100f, 0f)));
        }
    }

    @Test
    void detectPlayer_outsideDetectionRange_false() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            EnemyArcher a = new EnemyArcher(ORIGIN);
            assertFalse(a.detectPlayer(stubPlayer(9999f, 0f)));
        }
    }

    // ── Halper metódy ─────────────────────────────────────────────────────────

    private static void setArrowCount(EnemyArcher archer, int count) {
        try {
            var f = EnemyArcher.class.getDeclaredField("arrowCount");
            f.setAccessible(true);
            f.set(archer, count);
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }
    }

    private PlayerCharacter stubPlayer(float x, float y) {
        return new PlayerCharacter("P", 100, 10, 1f, new Vector2D(x, y), 0) {
            @Override public AnimationManager getAnimationManager() { return null; }
            @Override public void update(UpdateContext ctx) {}
            @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
        };
    }
}
