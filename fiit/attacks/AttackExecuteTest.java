package sk.stuba.fiit.attacks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.core.exceptions.InvalidAttackException;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.projectiles.ProjectileOwner;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttackExecuteTest extends GdxTest {

    // ── Stubs ─────────────────────────────────────────────────────────────────

    static class StubPlayer extends PlayerCharacter {
        private final boolean fr;
        StubPlayer(boolean facingRight) {
            super("TestAttacker", 100, 25, 1f, new Vector2D(100f, 50f), 0);
            this.fr = facingRight; this.enemy = false;
            hitbox.setSize(30, 60); hitbox.setPosition(100, 50);
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
        @Override public boolean isFacingRight() { return fr; }
    }

    static class StubEnemy extends Character {
        StubEnemy() {
            super("EnemyAttacker", 100, 20, 1f, new Vector2D(100f, 50f));
            this.enemy = true; hitbox.setSize(30, 60); hitbox.setPosition(100, 50);
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
        @Override public void onCollision(Object o) {}
    }

    static class FakeLevel extends Level {
        final List<Projectile> added = new ArrayList<>();
        FakeLevel() { super(1); }
        @Override public void addProjectile(Projectile p) { added.add(p); }
        @Override public List<Projectile> getProjectiles() { return added; }
        @Override public sk.stuba.fiit.world.MapManager getMapManager() { return null; }
    }

    private FakeLevel level;

    @BeforeEach void setUp() { level = new FakeLevel(); }

    // ── ArrowAttack ───────────────────────────────────────────────────────────

    @Test void arrowAttack_nullAttacker_throws() {
        assertThrows(InvalidAttackException.class, () -> new ArrowAttack().execute(null, level));
    }

    @Test void arrowAttack_nullLevel_returnsNull() {
        assertNull(new ArrowAttack().execute(new StubPlayer(true), null));
    }

    @Test void arrowAttack_facingRight_addsToLevel() {
        Projectile p = new ArrowAttack().execute(new StubPlayer(true), level);
        assertNotNull(p);
        assertEquals(1, level.added.size());
    }

    @Test void arrowAttack_facingLeft_addsToLevel() {
        Projectile p = new ArrowAttack().execute(new StubPlayer(false), level);
        assertNotNull(p);
        assertEquals(1, level.added.size());
    }

    @Test void arrowAttack_playerOwned() {
        assertTrue(new ArrowAttack().execute(new StubPlayer(true), level).isPlayerProjectile());
    }

    @Test void arrowAttack_enemyOwned() {
        assertEquals(ProjectileOwner.ENEMY, new ArrowAttack().execute(new StubEnemy(), level).getOwner());
    }

    @Test void arrowAttack_damage_matchesAttackPower() {
        StubPlayer a = new StubPlayer(true);
        assertEquals(a.getAttackPower(), new ArrowAttack().execute(a, level).getDamage());
    }

    @Test void arrowAttack_movesRight_whenFacingRight() {
        Projectile p = new ArrowAttack().execute(new StubPlayer(true), level);
        float x = p.getPosition().getX(); p.move();
        assertTrue(p.getPosition().getX() > x);
    }

    @Test void arrowAttack_movesLeft_whenFacingLeft() {
        Projectile p = new ArrowAttack().execute(new StubPlayer(false), level);
        float x = p.getPosition().getX(); p.move();
        assertTrue(p.getPosition().getX() < x);
    }

    @Test void arrowAttack_animationName() { assertEquals("attack", new ArrowAttack().getAnimationName()); }
    @Test void arrowAttack_manaCost_zero() { assertEquals(0, new ArrowAttack().getManaCost()); }
    @Test void arrowAttack_animDuration_positive() { assertTrue(new ArrowAttack().getAnimationDuration(null) > 0f); }

    // ── SpellAttack ───────────────────────────────────────────────────────────

    @Test void spellAttack_nullAttacker_throws() {
        assertThrows(InvalidAttackException.class, () -> new SpellAttack(5f, 80f, 0).execute(null, level));
    }

    @Test void spellAttack_nullLevel_returnsNull() {
        assertNull(new SpellAttack(5f, 80f, 0).execute(new StubPlayer(true), null));
    }

    @Test void spellAttack_addsToLevel() {
        assertNotNull(new SpellAttack(5f, 80f, 0).execute(new StubPlayer(true), level));
        assertEquals(1, level.added.size());
    }

    @Test void spellAttack_playerOwned() {
        assertTrue(new SpellAttack(5f, 80f, 0).execute(new StubPlayer(true), level).isPlayerProjectile());
    }

    @Test void spellAttack_enemyOwned() {
        assertEquals(ProjectileOwner.ENEMY, new SpellAttack(5f, 80f, 0).execute(new StubEnemy(), level).getOwner());
    }

    @Test void spellAttack_damage_matchesAttackPower() {
        StubPlayer a = new StubPlayer(true);
        assertEquals(a.getAttackPower(), new SpellAttack(5f, 80f, 0).execute(a, level).getDamage());
    }

    @Test void spellAttack_movesLeft_whenFacingLeft() {
        Projectile p = new SpellAttack(5f, 80f, 0).execute(new StubPlayer(false), level);
        float x = p.getPosition().getX(); p.move();
        assertTrue(p.getPosition().getX() < x);
    }

    @Test void spellAttack_animationName() { assertEquals("cast", new SpellAttack(5f, 80f, 0).getAnimationName()); }
    @Test void spellAttack_manaCost() { assertEquals(20, new SpellAttack(5f, 80f, 20).getManaCost()); }
    @Test void spellAttack_animDuration_positive() { assertTrue(new SpellAttack(5f, 80f, 0).getAnimationDuration(null) > 0f); }

    // ── Decorators cez SpellAttack ────────────────────────────────────────────

    @Test void fireDecorator_addsDot() {
        Projectile p = new FireDecorator(new SpellAttack(5f, 80f, 0)).execute(new StubPlayer(true), level);
        assertTrue(p.hasDotEffect());
        assertEquals(1f, p.getTintR(), 0.01f); // orange tint
    }

    @Test void freezeDecorator_addsSlow() {
        Projectile p = new FreezeDecorator(new SpellAttack(5f, 80f, 0)).execute(new StubPlayer(true), level);
        assertTrue(p.hasSlowEffect());
        assertEquals(0.3f, p.getTintR(), 0.01f); // blue tint
    }

    @Test void stackedDecorators_bothEffects() {
        Projectile p = new FreezeDecorator(new FireDecorator(new SpellAttack(5f, 80f, 0)))
                .execute(new StubPlayer(true), level);
        assertTrue(p.hasDotEffect());
        assertTrue(p.hasSlowEffect());
    }

    @Test void fireDecorator_manaCostHigherThanBase() {
        int base = new SpellAttack(5f, 80f, 20).getManaCost();
        assertTrue(new FireDecorator(new SpellAttack(5f, 80f, 20)).getManaCost() > base);
    }

    @Test void freezeDecorator_manaCostHigherThanBase() {
        int base = new SpellAttack(5f, 80f, 20).getManaCost();
        assertTrue(new FreezeDecorator(new SpellAttack(5f, 80f, 20)).getManaCost() > base);
    }
}
