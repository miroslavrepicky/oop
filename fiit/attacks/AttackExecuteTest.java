package sk.stuba.fiit.attacks;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.core.exceptions.InvalidAttackException;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttackExecuteTest {

    static class StubProjectile extends Projectile {
        StubProjectile(int dmg, Vector2D pos, Vector2D dir) {
            super(dmg, 1f, pos, dir);
        }
        @Override public void update(UpdateContext ctx) { move(); }
    }

    static class FakeLevel extends Level {
        final List<Projectile> added = new ArrayList<>();
        FakeLevel() { super(1); }
        @Override public void addProjectile(Projectile p) { added.add(p); }
        @Override public List<Projectile> getProjectiles() { return added; }
        @Override public sk.stuba.fiit.world.MapManager getMapManager() { return null; }
    }

    @Mock Character attacker;
    @Mock Projectile mockProjectile;

    private FakeLevel level;

    @BeforeEach
    void setUp() {
        level = new FakeLevel();
        when(attacker.getPosition()).thenReturn(new Vector2D(100f, 50f));
        when(attacker.getHitbox()).thenReturn(new Rectangle(100, 50, 30, 60));
        when(attacker.getAttackPower()).thenReturn(25);
        when(attacker.isFacingRight()).thenReturn(true);
        when(attacker.isEnemy()).thenReturn(false);
        when(attacker.getName()).thenReturn("TestAttacker");
    }

    //  MeleeAttack

    @Test
    void meleeAttack_nullAttacker_throws() {
        assertThrows(InvalidAttackException.class, () -> new MeleeAttack(1f).execute(null, level));
    }

    @Test
    void meleeAttack_nullLevel_returnsNull() {
        assertNull(new MeleeAttack(1f).execute(attacker, null));
    }

    @Test
    void meleeAttack_facingRight_addsProjectile() {
        assertNotNull(new MeleeAttack(1f).execute(attacker, level));
        assertEquals(1, level.added.size());
    }

    @Test
    void meleeAttack_facingLeft_addsProjectile() {
        when(attacker.isFacingRight()).thenReturn(false);
        assertNotNull(new MeleeAttack(1f).execute(attacker, level));
        assertEquals(1, level.added.size());
    }

    @Test
    void meleeAttack_playerOwner() {
        when(attacker.isEnemy()).thenReturn(false);
        assertTrue(new MeleeAttack(1f).execute(attacker, level).isPlayerProjectile());
    }

    @Test
    void meleeAttack_enemyOwner() {
        when(attacker.isEnemy()).thenReturn(true);
        assertFalse(new MeleeAttack(1f).execute(attacker, level).isPlayerProjectile());
    }

    @Test
    void meleeAttack_largerRange_biggerHitbox() {
        Projectile small = new MeleeAttack(1f).execute(attacker, level);
        level.added.clear();
        Projectile large = new MeleeAttack(2f).execute(attacker, level);
        assertTrue(large.getHitbox().width > small.getHitbox().width);
    }

    @Test
    void meleeAttack_animationName() {
        assertEquals("attack", new MeleeAttack(1f).getAnimationName());
    }

    @Test
    void meleeAttack_animDuration_positive() {
        assertTrue(new MeleeAttack(1f).getAnimationDuration(null) > 0f);
    }

    @Test
    void meleeAttack_manaCost_zero() {
        assertEquals(0, new MeleeAttack(1f).getManaCost());
    }

    @Test
    void meleeAttack_zeroRange_throws() {
        assertThrows(InvalidAttackException.class, () -> new MeleeAttack(0f));
    }

    @Test
    void meleeAttack_negativeRange_throws() {
        assertThrows(InvalidAttackException.class, () -> new MeleeAttack(-1f));
    }

    //  FireDecorator over MeleeAttack

    @Test
    void fireDecorator_overMelee_setsDot() {
        Projectile p = new FireDecorator(new MeleeAttack(1f)).execute(attacker, level);
        assertNotNull(p);
        assertTrue(p.hasDotEffect());
    }

    @Test
    void freezeDecorator_overMelee_setsSlow() {
        Projectile p = new FreezeDecorator(new MeleeAttack(1f)).execute(attacker, level);
        assertNotNull(p);
        assertTrue(p.hasSlowEffect());
    }

    @Test
    void stackedDecorators_overMelee_bothEffects() {
        Projectile p = new FreezeDecorator(new FireDecorator(new MeleeAttack(1f))).execute(attacker, level);
        assertNotNull(p);
        assertTrue(p.hasDotEffect());
        assertTrue(p.hasSlowEffect());
    }

    //  Decorator over mock Attack

    @Test
    void fireDecorator_nullWrapped_throws() {
        assertThrows(InvalidAttackException.class, () -> new FireDecorator(null));
    }

    @Test
    void freezeDecorator_nullWrapped_throws() {
        assertThrows(InvalidAttackException.class, () -> new FreezeDecorator(null));
    }

    @Test
    void fireDecorator_nullProjectile_doesNotThrow() {
        Attack base = mock(Attack.class);
        when(base.execute(any(), any())).thenReturn(null);
        assertDoesNotThrow(() -> new FireDecorator(base).execute(attacker, level));
    }

    @Test
    void freezeDecorator_nullProjectile_doesNotThrow() {
        Attack base = mock(Attack.class);
        when(base.execute(any(), any())).thenReturn(null);
        assertDoesNotThrow(() -> new FreezeDecorator(base).execute(attacker, level));
    }

    @Test
    void fireDecorator_setsDot_onMockProjectile() {
        Attack base = mock(Attack.class);
        when(base.execute(any(), any())).thenReturn(mockProjectile);
        new FireDecorator(base).execute(attacker, level);
        verify(mockProjectile).setDotEffect(anyInt(), anyFloat());
        verify(mockProjectile).setTint(1f, 0.3f, 0f);
    }

    @Test
    void freezeDecorator_setsSlow_onMockProjectile() {
        Attack base = mock(Attack.class);
        when(base.execute(any(), any())).thenReturn(mockProjectile);
        new FreezeDecorator(base).execute(attacker, level);
        verify(mockProjectile).setSlowEffect(anyFloat(), anyFloat());
        verify(mockProjectile).setTint(0.3f, 0.7f, 1f);
    }

    @Test
    void stackedDecorators_bothEffects_onMockProjectile() {
        Attack base = mock(Attack.class);
        when(base.execute(any(), any())).thenReturn(mockProjectile);
        new FreezeDecorator(new FireDecorator(base)).execute(attacker, level);
        verify(mockProjectile).setDotEffect(anyInt(), anyFloat());
        verify(mockProjectile).setSlowEffect(anyFloat(), anyFloat());
    }

    @Test
    void fireDecorator_manaCostAddsExtra() {
        Attack base = mock(Attack.class);
        when(base.getManaCost()).thenReturn(20);
        assertTrue(new FireDecorator(base).getManaCost() > 20);
    }

    @Test
    void freezeDecorator_manaCostAddsExtra() {
        Attack base = mock(Attack.class);
        when(base.getManaCost()).thenReturn(20);
        assertTrue(new FreezeDecorator(base).getManaCost() > 20);
    }

    @Test
    void stackedDecorators_manaCostCumulative() {
        Attack base = mock(Attack.class);
        when(base.getManaCost()).thenReturn(20);
        int fire    = new FireDecorator(base).getManaCost();
        when(base.getManaCost()).thenReturn(20);
        int stacked = new FreezeDecorator(new FireDecorator(base)).getManaCost();
        assertTrue(stacked > fire);
    }

    @Test
    void decorator_delegatesAnimationName() {
        Attack base = mock(Attack.class);
        when(base.getAnimationName()).thenReturn("cast");
        assertEquals("cast", new FireDecorator(base).getAnimationName());
    }

    @Test
    void decorator_delegatesAnimationDuration() {
        Attack base = mock(Attack.class);
        AnimationManager am = mock(AnimationManager.class);
        when(base.getAnimationDuration(am)).thenReturn(0.8f);
        assertEquals(0.8f, new FireDecorator(base).getAnimationDuration(am), 0.001f);
    }
}
