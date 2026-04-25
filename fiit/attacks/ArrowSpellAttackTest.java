package sk.stuba.fiit.attacks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.HeadlessGdxTest;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.AtlasCache;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.core.exceptions.InvalidAttackException;
import sk.stuba.fiit.projectiles.*;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ArrowAttack and SpellAttack using a real headless
 * LibGDX context so that atlas files can actually be loaded.
 *
 * <p>Requires {@code gdx-backend-headless} on the test classpath and the
 * working directory must contain the {@code atlas/} folder.
 */
class ArrowSpellAttackTest extends HeadlessGdxTest {

    // ── Stubs ─────────────────────────────────────────────────────────────────

    static class StubPlayer extends PlayerCharacter {
        StubPlayer(float x, float y, boolean facingRight) {
            super("Player", 100, 25, 1f, new Vector2D(x, y), 0);
            enemy = false;
            setFacingRight(facingRight);
            hitbox.setPosition(x, y);
            hitbox.setSize(32, 64);
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
    }

    static class StubEnemyChar extends PlayerCharacter {
        StubEnemyChar(float x, float y) {
            super("Enemy", 100, 20, 1f, new Vector2D(x, y), 0);
            this.enemy = true;
            hitbox.setPosition(x, y);
            hitbox.setSize(32, 64);
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); }
    }

    static class FakeLevel extends Level {
        List<Projectile> projs = new ArrayList<>();
        FakeLevel() { super(1); }
        @Override public void addProjectile(Projectile p) { projs.add(p); }
        @Override public List<Projectile> getProjectiles() { return projs; }
        @Override public sk.stuba.fiit.world.MapManager getMapManager() { return null; }
    }

    private FakeLevel level;

    @BeforeEach
    void resetPool() {
        // Clear pool between tests to avoid stale state
        ProjectilePool.getInstance().clearAll();
        AtlasCache.getInstance().dispose();
        level = new FakeLevel();
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  ArrowAttack
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    void arrowAttack_nullAttacker_throwsInvalidAttack() {
        assertThrows(InvalidAttackException.class,
            () -> new ArrowAttack().execute(null, level));
    }

    @Test
    void arrowAttack_nullLevel_returnsNull() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        assertNull(new ArrowAttack().execute(p, null));
    }

    @Test
    void arrowAttack_execute_addsArrowToLevel() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile result = new ArrowAttack().execute(p, level);

        assertNotNull(result, "execute() must return a non-null Arrow");
        assertEquals(1, level.projs.size(), "Arrow must be added to the level");
        assertSame(result, level.projs.get(0));
    }

    @Test
    void arrowAttack_returnsArrowInstance() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile result = new ArrowAttack().execute(p, level);
        assertInstanceOf(Arrow.class, result);
    }

    @Test
    void arrowAttack_playerOwner() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile result = new ArrowAttack().execute(p, level);
        assertTrue(result.isPlayerProjectile(), "Arrow from player should have PLAYER owner");
    }

    @Test
    void arrowAttack_enemyOwner() {
        StubEnemyChar e = new StubEnemyChar(100f, 100f);
        Projectile result = new ArrowAttack().execute(e, level);
        assertFalse(result.isPlayerProjectile(), "Arrow from enemy should have ENEMY owner");
    }

    @Test
    void arrowAttack_facingRight_positiveDirection() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile arrow = new ArrowAttack().execute(p, level);
        // Spawn position should be to the right of the player
        assertTrue(arrow.getPosition().getX() > p.getPosition().getX(),
            "Arrow should spawn to the right when facing right");
    }

    @Test
    void arrowAttack_facingLeft_negativeSpawnOffset() {
        StubPlayer p = new StubPlayer(100f, 100f, false);
        Projectile arrow = new ArrowAttack().execute(p, level);
        // Spawn position should be to the left
        assertTrue(arrow.getPosition().getX() < p.getPosition().getX(),
            "Arrow should spawn to the left when facing left");
    }

    @Test
    void arrowAttack_arrowIsActive() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile arrow = new ArrowAttack().execute(p, level);
        assertTrue(arrow.isActive(), "Arrow must be active after spawn");
    }

    @Test
    void arrowAttack_arrowDamage_matchesAttackerPower() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile arrow = new ArrowAttack().execute(p, level);
        assertEquals(p.getAttackPower(), arrow.getDamage(),
            "Arrow damage must equal attacker's attack power");
    }

    @Test
    void arrowAttack_animationName_isAttack() {
        assertEquals("attack", new ArrowAttack().getAnimationName());
    }

    @Test
    void arrowAttack_animDuration_positiveDefault() {
        assertTrue(new ArrowAttack().getAnimationDuration(null) > 0f);
    }

    @Test
    void arrowAttack_manaCost_zero() {
        assertEquals(0, new ArrowAttack().getManaCost());
    }

    @Test
    void arrowAttack_poolReuse_secondArrowIsSameObject() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Arrow first = (Arrow) new ArrowAttack().execute(p, level);
        first.setActive(false);
        first.returnToPool();

        FakeLevel level2 = new FakeLevel();
        Arrow second = (Arrow) new ArrowAttack().execute(p, level2);
        assertSame(first, second, "Pool should reuse the returned Arrow");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  SpellAttack
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    void spellAttack_nullAttacker_throwsInvalidAttack() {
        assertThrows(InvalidAttackException.class,
            () -> new SpellAttack(4f, 100f, 20).execute(null, level));
    }

    @Test
    void spellAttack_nullLevel_returnsNull() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        assertNull(new SpellAttack(4f, 100f, 20).execute(p, null));
    }

    @Test
    void spellAttack_execute_addsSpellToLevel() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile result = new SpellAttack(4f, 100f, 0).execute(p, level);

        assertNotNull(result);
        assertEquals(1, level.projs.size());
    }

    @Test
    void spellAttack_returnsMagicSpellInstance() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile result = new SpellAttack(4f, 100f, 0).execute(p, level);
        assertInstanceOf(MagicSpell.class, result);
    }

    @Test
    void spellAttack_playerOwner() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile spell = new SpellAttack(4f, 100f, 0).execute(p, level);
        assertTrue(spell.isPlayerProjectile());
    }

    @Test
    void spellAttack_enemyOwner() {
        StubEnemyChar e = new StubEnemyChar(100f, 100f);
        Projectile spell = new SpellAttack(4f, 100f, 0).execute(e, level);
        assertFalse(spell.isPlayerProjectile());
    }

    @Test
    void spellAttack_spellIsActive() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile spell = new SpellAttack(4f, 100f, 0).execute(p, level);
        assertTrue(spell.isActive());
    }

    @Test
    void spellAttack_damage_matchesAttackerPower() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile spell = new SpellAttack(4f, 100f, 0).execute(p, level);
        assertEquals(p.getAttackPower(), spell.getDamage());
    }

    @Test
    void spellAttack_manaCost_returned() {
        assertEquals(20, new SpellAttack(4f, 100f, 20).getManaCost());
        assertEquals(0,  new SpellAttack(4f, 100f, 0).getManaCost());
    }

    @Test
    void spellAttack_animationName_isCast() {
        assertEquals("cast", new SpellAttack(4f, 100f, 0).getAnimationName());
    }

    @Test
    void spellAttack_animDuration_positiveDefault() {
        assertTrue(new SpellAttack(4f, 100f, 0).getAnimationDuration(null) > 0f);
    }

    @Test
    void spellAttack_facingRight_spawnsToRight() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile spell = new SpellAttack(4f, 100f, 0).execute(p, level);
        assertTrue(spell.getPosition().getX() > p.getPosition().getX());
    }

    @Test
    void spellAttack_facingLeft_spawnsToLeft() {
        StubPlayer p = new StubPlayer(100f, 100f, false);
        Projectile spell = new SpellAttack(4f, 100f, 0).execute(p, level);
        assertTrue(spell.getPosition().getX() <= p.getPosition().getX());
    }

    @Test
    void spellAttack_isAoeProjectile() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile spell = new SpellAttack(4f, 100f, 0).execute(p, level);
        assertInstanceOf(AoeProjectile.class, spell);
    }

    @Test
    void spellAttack_aoeRadius_matchesConstructorParam() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        MagicSpell spell = (MagicSpell) new SpellAttack(4f, 75f, 0).execute(p, level);
        assertEquals(75f, spell.getAoeRadius(), 0.001f);
    }

    @Test
    void spellAttack_withFireDecorator_setsDot() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile spell = new FireDecorator(new SpellAttack(4f, 100f, 0)).execute(p, level);
        assertNotNull(spell);
        assertTrue(spell.hasDotEffect());
    }

    @Test
    void spellAttack_withFreezeDecorator_setsSlow() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Projectile spell = new FreezeDecorator(new SpellAttack(4f, 100f, 0)).execute(p, level);
        assertNotNull(spell);
        assertTrue(spell.hasSlowEffect());
    }

    @Test
    void spellAttack_poolReuse_secondSpellIsSameObject() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        MagicSpell first = (MagicSpell) new SpellAttack(4f, 100f, 0).execute(p, level);
        first.setActive(false);
        first.returnToPool();

        FakeLevel level2 = new FakeLevel();
        MagicSpell second = (MagicSpell) new SpellAttack(4f, 100f, 0).execute(p, level2);
        assertSame(first, second, "Pool should reuse the returned MagicSpell");
    }

    // ── Stacking decorators ───────────────────────────────────────────────────

    @Test
    void stacked_fireFreeze_bothEffects_onSpell() {
        StubPlayer p = new StubPlayer(100f, 100f, true);
        Attack stacked = new FreezeDecorator(new FireDecorator(new SpellAttack(4f, 100f, 0)));
        Projectile spell = stacked.execute(p, level);

        assertNotNull(spell);
        assertTrue(spell.hasDotEffect(),  "Should have DOT from FireDecorator");
        assertTrue(spell.hasSlowEffect(), "Should have slow from FreezeDecorator");
    }
}
