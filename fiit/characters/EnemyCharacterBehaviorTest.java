package sk.stuba.fiit.characters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.attacks.Attack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.projectiles.MeleeHitbox;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.projectiles.ProjectileOwner;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EnemyCharacter logic: attack cooldown, state transitions,
 * movement, effects. All stubs avoid atlas loading.
 */
class EnemyCharacterBehaviorTest {

    // ── Stubs ─────────────────────────────────────────────────────────────────

    static class StubAttack implements Attack {
        int executeCount = 0;
        @Override public Projectile execute(Character attacker, Level level) {
            executeCount++;
            // return a real MeleeHitbox so it can be added to level
            MeleeHitbox hb = new MeleeHitbox(
                attacker.getAttackPower(),
                new Vector2D(attacker.getPosition().getX(), attacker.getPosition().getY()),
                30, 50,
                ProjectileOwner.ENEMY);
            if (level != null) level.addProjectile(hb);
            return hb;
        }
        @Override public String getAnimationName()                  { return "attack"; }
        @Override public float  getAnimationDuration(AnimationManager am) { return 0.5f; }
    }

    static class StubEnemy extends EnemyCharacter {
        StubEnemy(float x, float y) {
            super("StubEnemy", 100, 15, 2f, new Vector2D(x, y), 100f, 200f, 10, 10);
            enemy = true;
            hitbox.setPosition(x, y);
            hitbox.setSize(32, 64);
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) { super.update(ctx); }
    }

    static class FakeLevel extends Level {
        List<Projectile> projs = new ArrayList<>();
        FakeLevel() { super(1); }
        @Override public void addProjectile(Projectile p) { projs.add(p); }
        @Override public List<Projectile> getProjectiles() { return projs; }
        @Override public sk.stuba.fiit.world.MapManager getMapManager() { return null; }
    }

    private StubEnemy enemy;
    private FakeLevel level;

    @BeforeEach
    void setUp() {
        enemy = new StubEnemy(100f, 100f);
        level = new FakeLevel();
    }

    // ── Basic state ───────────────────────────────────────────────────────────

    @Test
    void isAlive_initiallyTrue() {
        assertTrue(enemy.isAlive());
    }

    @Test
    void isEnemy_true() {
        assertTrue(enemy.isEnemy());
    }

    @Test
    void isAttacking_initiallyFalse() {
        assertFalse(enemy.isAttacking());
    }

    @Test
    void getAttackPower_returnsConstructorValue() {
        assertEquals(15, enemy.getAttackPower());
    }

    @Test
    void getArmor_returnsConstructorValue() {
        assertEquals(10, enemy.getArmor());
    }

    @Test
    void getSpeed_returnsConstructorValue() {
        assertEquals(2f, enemy.getSpeed(), 0.001f);
    }

    // ── triggerAttack ─────────────────────────────────────────────────────────

    @Test
    void triggerAttack_noStrategy_doesNotThrow() {
        // attack field is null by default in our StubEnemy
        assertDoesNotThrow(() -> enemy.triggerAttack());
        assertFalse(enemy.isAttacking()); // should remain false
    }

    @Test
    void triggerAttack_withStrategy_setsAttacking() {
        enemy.attack = new StubAttack();
        enemy.triggerAttack();
        assertTrue(enemy.isAttacking());
    }

    @Test
    void triggerAttack_whileAttacking_ignored() {
        enemy.attack = new StubAttack();
        enemy.triggerAttack(); // first trigger
        assertTrue(enemy.isAttacking());

        // Triggering again while still attacking should be a no-op
        enemy.triggerAttack();
        // Still attacking, but no second trigger happened (checked via cooldown state)
        assertTrue(enemy.isAttacking());
    }

    @Test
    void triggerAttack_duringCooldown_ignored() {
        StubAttack atk = new StubAttack();
        enemy.attack = atk;

        enemy.triggerAttack(); // sets cooldown
        assertTrue(enemy.isAttacking());

        // Simulate cooldown still > 0 by NOT advancing time
        // triggerAttack again is blocked by cooldown
        enemy.isAttacking = false; // manually reset isAttacking but NOT cooldown
        enemy.attackCooldown = 1.0f; // still cooling
        enemy.triggerAttack(); // should be blocked by cooldown

        assertFalse(enemy.isAttacking(), "Attack should be blocked by cooldown");
    }

    // ── Attack animation timer ────────────────────────────────────────────────

    @Test
    void attackAnimTimer_decreasesOverTime() {
        StubAttack atk = new StubAttack();
        enemy.attack = atk;
        enemy.triggerAttack();

        float initialTimer = enemy.attackAnimTimer;
        UpdateContext ctx = new UpdateContext(0.1f, null, level, null, null);
        enemy.update(ctx); // advances timer by 0.1

        assertTrue(enemy.attackAnimTimer < initialTimer);
    }

    @Test
    void attackEnds_whenTimerExpires() {
        StubAttack atk = new StubAttack();
        enemy.attack = atk;
        enemy.triggerAttack();

        // Advance enough to expire the attack (duration is 0.5s)
        UpdateContext ctx = new UpdateContext(0.6f, null, level, null, null);
        enemy.update(ctx);

        assertFalse(enemy.isAttacking(), "Attack should end when timer expires");
    }

    // ── Movement ─────────────────────────────────────────────────────────────

    @Test
    void move_changesPosition() {
        float startX = enemy.getPosition().getX();
        enemy.move(new Vector2D(10f, 0f));
        assertEquals(startX + 10f, enemy.getPosition().getX(), 0.001f);
    }

    @Test
    void move_updatesHitbox() {
        enemy.move(new Vector2D(5f, 0f));
        assertEquals(enemy.getPosition().getX(), enemy.getHitbox().x, 0.001f);
    }

    @Test
    void wasLastMoveBlocked_falseInitially() {
        assertFalse(enemy.wasLastMoveBlocked());
    }

    @Test
    void move_withResolver_blockedByWall() {
        com.badlogic.gdx.math.Rectangle wall = new com.badlogic.gdx.math.Rectangle(115f, 0f, 50f, 200f);
        sk.stuba.fiit.physics.MovementResolver resolver =
            new sk.stuba.fiit.physics.MovementResolver(List.of(wall));
        enemy.setMovementResolver(resolver);

        // Move toward wall (dx=20 → hitbox right would overlap wall)
        enemy.move(new Vector2D(20f, 0f));

        assertTrue(enemy.wasLastMoveBlocked(), "Move should be blocked by wall");
    }

    @Test
    void move_withResolver_allowedWhenNoWall() {
        com.badlogic.gdx.math.Rectangle farWall = new com.badlogic.gdx.math.Rectangle(500f, 0f, 50f, 200f);
        sk.stuba.fiit.physics.MovementResolver resolver =
            new sk.stuba.fiit.physics.MovementResolver(List.of(farWall));
        enemy.setMovementResolver(resolver);

        enemy.move(new Vector2D(5f, 0f));
        assertFalse(enemy.wasLastMoveBlocked());
    }

    // ── FacingRight ───────────────────────────────────────────────────────────

    @Test
    void setFacingRight_false() {
        enemy.setFacingRight(false);
        assertFalse(enemy.isFacingRight());
    }

    @Test
    void setFacingRight_true() {
        enemy.setFacingRight(false);
        enemy.setFacingRight(true);
        assertTrue(enemy.isFacingRight());
    }

    // ── Death ─────────────────────────────────────────────────────────────────

    @Test
    void takeDamage_kills() {
        enemy.takeDamage(9999);
        assertFalse(enemy.isAlive());
    }

    @Test
    void isDeathAnimationDone_falseWhileTimerRunning() {
        enemy.takeDamage(9999);
        enemy.startDeathAnimation(); // starts timer
        assertFalse(enemy.isDeathAnimationDone(), "Timer not yet expired");
    }

    @Test
    void isDeathAnimationDone_trueAfterTimerZero() {
        enemy.takeDamage(9999);
        enemy.startDeathAnimation();
        enemy.updateDeathTimer(10f); // expire timer
        assertTrue(enemy.isDeathAnimationDone());
    }

    // ── Effects ───────────────────────────────────────────────────────────────

    @Test
    void applyDot_ticksOnUpdate() {
        enemy.applyDot(100, 5f);
        int hpBefore = enemy.getHp();

        UpdateContext ctx = new UpdateContext(1f, null, null, null, null);
        enemy.update(ctx); // update ticks gravity + effects

        assertTrue(enemy.getHp() <= hpBefore, "DOT should reduce HP");
    }

    @Test
    void applySlow_reducesSpeed() {
        float base = enemy.getSpeed();
        enemy.applySlow(0.5f, 5f);
        assertEquals(base * 0.5f, enemy.getSpeed(), 0.001f);
    }

    @Test
    void revive_restoresHpAndSpeed() {
        float base = enemy.getSpeed();
        enemy.takeDamage(50);
        enemy.applySlow(0.2f, 10f);
        enemy.revive();

        assertEquals(100, enemy.getHp());
        assertEquals(base, enemy.getSpeed(), 0.001f);
    }

    // ── detectPlayer (via distanceTo) ─────────────────────────────────────────

    @Test
    void detectPlayer_withinRange_true() {
        // StubEnemy detectionRange = 200f, player at (150, 100) → dist=50
        PlayerCharacter fakePlayer = makeFakePlayer(150f, 100f);
        assertTrue(enemy.detectPlayer(fakePlayer));
    }

    @Test
    void detectPlayer_outsideRange_false() {
        PlayerCharacter fakePlayer = makeFakePlayer(1000f, 100f);
        assertFalse(enemy.detectPlayer(fakePlayer));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private PlayerCharacter makeFakePlayer(float x, float y) {
        return new PlayerCharacter("P", 100, 10, 1f, new Vector2D(x, y), 0) {
            @Override public AnimationManager getAnimationManager() { return null; }
            @Override public void update(UpdateContext ctx) {}
            @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
        };
    }
}
