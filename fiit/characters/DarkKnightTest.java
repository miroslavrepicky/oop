package sk.stuba.fiit.characters;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DarkKnightTest extends GdxTest {

    private static final Vector2D ORIGIN = new Vector2D(0, 0);

    private MockedConstruction.MockInitializer<AnimationManager> animStub() {
        return (mock, ctx) -> {
            when(mock.getFirstFrameSize("idle")).thenReturn(new Vector2D(40, 80));
            // hasAnimation returns false by default → attack strategies use their fallback durations
        };
    }

    /** Minimal Level stub that collects projectiles without loading atlas. */
    static class FakeLevel extends Level {
        FakeLevel() { super(1); }
        @Override public sk.stuba.fiit.world.MapManager getMapManager() { return null; }
    }

    /** Minimal PlayerCharacter stub for ctx.player. */
    static class StubPlayer extends PlayerCharacter {
        StubPlayer(float x, float y) {
            super("P", 100, 10, 1f, new Vector2D(x, y), 0);
            enemy = false;
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
    }

    // ── Constructor / stats ────────────────────────────────────────────────────

    @Test
    void name_isDarkKnight() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertEquals("DarkKnight", new DarkKnight(ORIGIN).getName());
        }
    }

    @Test
    void hp_is500() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            assertEquals(500, dk.getHp());
            assertEquals(500, dk.getMaxHp());
        }
    }

    @Test
    void attackPower_is50() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertEquals(50, new DarkKnight(ORIGIN).getAttackPower());
        }
    }

    @Test
    void speed_is2() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertEquals(2.0f, new DarkKnight(ORIGIN).getSpeed(), 0.001f);
        }
    }

    @Test
    void armor_is30() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            assertEquals(30, dk.getMaxArmor());
            assertEquals(30, dk.getArmor());
        }
    }

    // ── Identity ──────────────────────────────────────────────────────────────

    @Test
    void isEnemy_true() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertTrue(new DarkKnight(ORIGIN).isEnemy());
        }
    }

    @Test
    void isAlive_initially() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertTrue(new DarkKnight(ORIGIN).isAlive());
        }
    }

    @Test
    void isAttacking_initiallyFalse() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertFalse(new DarkKnight(ORIGIN).isAttacking());
        }
    }

    // ── Animation name ────────────────────────────────────────────────────────

    @Test
    void getAttackAnimationName_default_isCast() {
        // usingMeleeAttack starts as false → "cast"
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            // getAttackAnimationName is protected – accessible from same package
            assertEquals("cast", dk.getAttackAnimationName());
        }
    }

    @Test
    void getAttackAnimationName_afterMeleeTrigger_isAttack() {
        // When lastKnownPlayer is null, triggerAttack falls back to melee
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            dk.triggerAttack(); // null player → melee path → usingMeleeAttack = true
            assertEquals("attack", dk.getAttackAnimationName());
        }
    }

    // ── triggerAttack ─────────────────────────────────────────────────────────

    @Test
    void triggerAttack_withNullPlayer_setsAttacking() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            dk.triggerAttack();
            assertTrue(dk.isAttacking());
        }
    }

    @Test
    void triggerAttack_withNullPlayer_selectsMelee() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            dk.triggerAttack();
            assertEquals("attack", dk.getAttackAnimationName());
        }
    }

    @Test
    void triggerAttack_whileAlreadyAttacking_ignored() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            dk.triggerAttack();
            // Second call while isAttacking=true must be a no-op
            assertDoesNotThrow(() -> dk.triggerAttack());
            // Still attacking from first trigger
            assertTrue(dk.isAttacking());
        }
    }

    @Test
    void triggerAttack_withClosePlayer_selectsMelee() {
        // Player within MELEE_THRESHOLD (26 * 1.2 = 31.2 px)
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            StubPlayer closePlayer = new StubPlayer(10f, 0f); // 10 px away

            // Feed player into DarkKnight via update()
            UpdateContext ctx = new UpdateContext(0.016f, null, new FakeLevel(), closePlayer, null);
            dk.update(ctx);

            // Reset attack state so we can trigger fresh
            dk.isAttacking = false;
            dk.attackCooldown = 0f;

            dk.triggerAttack();
            assertEquals("attack", dk.getAttackAnimationName());
        }
    }

    @Test
    void triggerAttack_withFarPlayer_selectsSpell() {
        // Player beyond MELEE_THRESHOLD (26 * 1.2 = 31.2 px)
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            StubPlayer farPlayer = new StubPlayer(200f, 0f);

            UpdateContext ctx = new UpdateContext(0.016f, null, new FakeLevel(), farPlayer, null);
            dk.update(ctx);

            dk.isAttacking = false;
            dk.attackCooldown = 0f;

            dk.triggerAttack();
            assertEquals("cast", dk.getAttackAnimationName());
        }
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_withNullPlayer_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            UpdateContext ctx = new UpdateContext(0.016f, null, null, null, null);
            assertDoesNotThrow(() -> dk.update(ctx));
        }
    }

    @Test
    void update_withPlayer_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            StubPlayer player = new StubPlayer(100f, 0f);
            UpdateContext ctx = new UpdateContext(0.016f, null, new FakeLevel(), player, null);
            assertDoesNotThrow(() -> dk.update(ctx));
        }
    }

    // ── Behaviour ─────────────────────────────────────────────────────────────

    @Test
    void takeDamage_kills() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            dk.takeDamage(9999);
            assertFalse(dk.isAlive());
        }
    }

    @Test
    void revive_restoresHp() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            dk.takeDamage(9999);
            dk.revive();
            assertEquals(500, dk.getHp());
            assertTrue(dk.isAlive());
        }
    }

    @Test
    void applySlow_reducesSpeed() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            float base = dk.getSpeed();
            dk.applySlow(0.5f, 5f);
            assertEquals(base * 0.5f, dk.getSpeed(), 0.001f);
        }
    }

    @Test
    void getAnimationManager_notNull() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertNotNull(new DarkKnight(ORIGIN).getAnimationManager());
        }
    }

    @Test
    void move_updatesPosition() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            dk.move(new Vector2D(10f, 0f));
            assertEquals(10f, dk.getPosition().getX(), 0.001f);
        }
    }

    @Test
    void attackCooldown_preventsRetrigger() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            dk.triggerAttack();
            assertTrue(dk.isAttacking());

            // Manually clear isAttacking but leave cooldown active
            dk.isAttacking = false;
            dk.attackCooldown = 1.0f;

            dk.triggerAttack(); // should be blocked by cooldown
            assertFalse(dk.isAttacking());
        }
    }

    @Test
    void deathAnimation_startsOnDeath() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            DarkKnight dk = new DarkKnight(ORIGIN);
            dk.takeDamage(9999);
            dk.startDeathAnimation();
            dk.updateDeathTimer(100f);
            assertTrue(dk.isDeathAnimationDone());
        }
    }
}
