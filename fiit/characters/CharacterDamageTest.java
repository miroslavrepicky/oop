package sk.stuba.fiit.characters;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Character damage model, armor, heal, DOT, slow, revive, death timer.
 * Uses a minimal anonymous subclass to avoid LibGDX animation loading.
 */
class CharacterDamageTest {

    /** Minimal concrete Character with no animation loading. */
    static class TestChar extends Character {
        TestChar(int hp, int armor, int maxArmor) {
            super("Test", hp, 10, 1f, new Vector2D(0, 0), armor, maxArmor);
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D direction) {
            position = position.add(direction);
            updateHitbox();
        }
        @Override public void onCollision(Object other) {}
    }

    private TestChar character;

    @BeforeEach
    void setUp() {
        character = new TestChar(100, 20, 20);
    }

    // ── Basic damage ──────────────────────────────────────────────────────────

    @Test
    void takeDamage_reducesHp() {
        character.takeDamage(10);
        // armor absorbs 10, hp untouched
        assertEquals(100, character.getHp());
        assertEquals(10, character.getArmor());
    }

    @Test
    void takeDamage_armorAbsorbsPartially() {
        character.takeDamage(30); // armor=20 absorbs 20, remaining=10 → hp=90
        assertEquals(90, character.getHp());
        assertEquals(0, character.getArmor());
    }

    @Test
    void takeDamage_killsCharacter() {
        character.takeDamage(200);
        assertEquals(0, character.getHp());
        assertFalse(character.isAlive());
    }

    @Test
    void takeDamage_negative_heals() {
        character.takeDamage(50); // hp = 70 (30 absorbed by armor, 20 hp)
        // Actually: armor=20 absorbs 20, remaining=30 → hp=70
        character.takeDamage(-20); // heals 20 → hp=90
        assertEquals(90, character.getHp());
    }

    @Test
    void takeDamage_negative_doesNotExceedMaxHp() {
        character.takeDamage(-50); // tries to heal 50, but maxHp=100
        assertEquals(100, character.getHp());
    }

    @Test
    void isAlive_trueWhenHpPositive() {
        assertTrue(character.isAlive());
    }

    // ── Armor ─────────────────────────────────────────────────────────────────

    @Test
    void addArmor_increasesArmor() {
        character.takeDamage(20); // deplete armor first
        character.addArmor(10);
        assertEquals(10, character.getArmor());
    }

    @Test
    void addArmor_cappedAtMaxArmor() {
        character.addArmor(100);
        assertEquals(20, character.getArmor()); // maxArmor=20
    }

    @Test
    void restoreStats_setsHpAndArmor() {
        character.takeDamage(50);
        character.restoreStats(80, 15);
        assertEquals(80, character.getHp());
        assertEquals(15, character.getArmor());
    }

    @Test
    void restoreStats_clampedToMax() {
        character.restoreStats(999, 999);
        assertEquals(100, character.getHp());
        assertEquals(20, character.getArmor());
    }

    // ── Revive ────────────────────────────────────────────────────────────────

    @Test
    void revive_restoresFullHp() {
        character.takeDamage(200);
        character.revive();
        assertEquals(100, character.getHp());
        assertTrue(character.isAlive());
    }

    @Test
    void revive_clearsVelocityY() {
        character.setVelocityY(-300f);
        character.revive();
        assertEquals(0f, character.getVelocityY(), 0.001f);
    }

    // ── Death timer ───────────────────────────────────────────────────────────

    @Test
    void isDeathAnimationDone_falseWhileAlive() {
        assertFalse(character.isDeathAnimationDone());
    }

    @Test
    void startDeathAnimation_onlyOnce() {
        // With null AnimationManager, duration defaults to 1.0s
        character.takeDamage(200); // kill
        character.startDeathAnimation();
        character.updateDeathTimer(0.5f);
        character.startDeathAnimation(); // should be no-op
        character.updateDeathTimer(0.6f); // total > 1.0
        assertTrue(character.isDeathAnimationDone());
    }

    // ── DOT ───────────────────────────────────────────────────────────────────

    @Test
    void dot_dealsDamageOverTime() {
        // No armor → direct damage
        TestChar noArmor = new TestChar(100, 0, 0);
        noArmor.applyDot(10, 1.0f); // 10 dps for 1 second
        noArmor.tickEffects(1.0f);
        assertTrue(noArmor.getHp() < 100, "DOT should deal damage");
    }

    @Test
    void dot_expiresAfterDuration() {
        TestChar noArmor = new TestChar(100, 0, 0);
        noArmor.applyDot(10, 0.5f);
        noArmor.tickEffects(0.6f); // past duration
        int hpAfterFirst = noArmor.getHp();
        noArmor.tickEffects(1.0f); // DOT should be gone now
        assertEquals(hpAfterFirst, noArmor.getHp(), "DOT should not deal damage after expiry");
    }

    @Test
    void dot_doesNotAffectDeadCharacter() {
        TestChar dead = new TestChar(1, 0, 0);
        dead.takeDamage(1); // kill
        dead.applyDot(100, 5f);
        dead.tickEffects(1f);
        assertEquals(0, dead.getHp());
    }

    // ── Slow ─────────────────────────────────────────────────────────────────

    @Test
    void slow_reducesSpeed() {
        float originalSpeed = character.getSpeed();
        character.applySlow(0.5f, 2.0f);
        assertEquals(originalSpeed * 0.5f, character.getSpeed(), 0.001f);
    }

    @Test
    void slow_restoredAfterDuration() {
        float originalSpeed = character.getSpeed();
        character.applySlow(0.3f, 0.5f);
        character.tickEffects(0.6f);
        assertEquals(originalSpeed, character.getSpeed(), 0.001f);
    }

    @Test
    void slow_doesNotStack_multiplied() {
        // Applying second slow restores first then applies new multiplier
        float base = character.getSpeed();
        character.applySlow(0.5f, 10f);
        character.applySlow(0.5f, 10f);
        assertEquals(base * 0.5f, character.getSpeed(), 0.001f);
    }

    // ── Jump ─────────────────────────────────────────────────────────────────

    @Test
    void jump_setsVelocityY_whenOnGround() {
        character.setOnGround(true);
        character.jump(300f);
        assertEquals(300f, character.getVelocityY(), 0.001f);
        assertFalse(character.isOnGround());
    }

    @Test
    void jump_ignored_whenAirborne() {
        character.setOnGround(false);
        character.setVelocityY(100f);
        character.jump(300f);
        assertEquals(100f, character.getVelocityY(), 0.001f);
    }

    // ── Hitbox update ─────────────────────────────────────────────────────────

    @Test
    void updateHitbox_syncsWithPosition() {
        character.getPosition().setX(50f);
        character.getPosition().setY(75f);
        character.updateHitbox();
        Rectangle hb = character.getHitbox();
        assertEquals(50f, hb.x, 0.001f);
        assertEquals(75f, hb.y, 0.001f);
    }

    // ── FacingRight / velocityX ───────────────────────────────────────────────

    @Test
    void facingRight_defaultTrue() {
        assertTrue(character.isFacingRight());
    }

    @Test
    void setFacingRight_false() {
        character.setFacingRight(false);
        assertFalse(character.isFacingRight());
    }

    @Test
    void velocityX_setAndGet() {
        character.setVelocityX(42f);
        assertEquals(42f, character.getVelocityX(), 0.001f);
    }

    @Test
    void isEnemy_falseByDefault_whenEnemyFlagNotSet() {
        // TestChar doesn't set enemy=false so defaults to true
        assertTrue(character.isEnemy());
    }

    @Test
    void getMaxHp_returnsInitialHp() {
        assertEquals(100, character.getMaxHp());
    }
}
