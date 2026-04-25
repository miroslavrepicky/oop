package sk.stuba.fiit.attacks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.exceptions.InvalidAttackException;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttackDecoratorTest {

    @Mock
    Attack wrappedAttack;
    @Mock Character attacker;
    @Mock Level level;
    @Mock Projectile projectile;
    @Mock AnimationManager am;

    // Concrete subclass of abstract AttackDecorator
    static class PassThrough extends AttackDecorator {
        PassThrough(Attack w) { super(w); }
    }

    @Test
    void constructor_nullWrapped_throwsInvalidAttackException() {
        assertThrows(InvalidAttackException.class, () -> new PassThrough(null));
    }

    @Test
    void execute_delegatesToWrapped() {
        when(wrappedAttack.execute(attacker, level)).thenReturn(projectile);
        Attack decorator = new PassThrough(wrappedAttack);
        Projectile result = decorator.execute(attacker, level);
        assertSame(projectile, result);
    }

    @Test
    void getAnimationName_delegatesToWrapped() {
        when(wrappedAttack.getAnimationName()).thenReturn("cast");
        assertEquals("cast", new PassThrough(wrappedAttack).getAnimationName());
    }

    @Test
    void getAnimationDuration_delegatesToWrapped() {
        when(wrappedAttack.getAnimationDuration(am)).thenReturn(0.8f);
        assertEquals(0.8f, new PassThrough(wrappedAttack).getAnimationDuration(am), 0.001f);
    }

    @Test
    void getManaCost_delegatesToWrapped() {
        when(wrappedAttack.getManaCost()).thenReturn(20);
        assertEquals(20, new PassThrough(wrappedAttack).getManaCost());
    }

    // ── FireDecorator ─────────────────────────────────────────────────────────

    @Test
    void fireDecorator_addsDotEffect_toProjectile() {
        when(wrappedAttack.execute(attacker, level)).thenReturn(projectile);
        FireDecorator fire = new FireDecorator(wrappedAttack);
        fire.execute(attacker, level);
        verify(projectile).setDotEffect(anyInt(), anyFloat());
    }

    @Test
    void fireDecorator_setsTint_orange() {
        when(wrappedAttack.execute(attacker, level)).thenReturn(projectile);
        new FireDecorator(wrappedAttack).execute(attacker, level);
        verify(projectile).setTint(1f, 0.3f, 0f);
    }

    @Test
    void fireDecorator_nullProjectile_doesNotThrow() {
        when(wrappedAttack.execute(attacker, level)).thenReturn(null);
        assertDoesNotThrow(() -> new FireDecorator(wrappedAttack).execute(attacker, level));
    }

    @Test
    void fireDecorator_manaCostAddsExtra() {
        when(wrappedAttack.getManaCost()).thenReturn(20);
        assertTrue(new FireDecorator(wrappedAttack).getManaCost() > 20);
    }

    // ── FreezeDecorator ───────────────────────────────────────────────────────

    @Test
    void freezeDecorator_addsSlowEffect_toProjectile() {
        when(wrappedAttack.execute(attacker, level)).thenReturn(projectile);
        new FreezeDecorator(wrappedAttack).execute(attacker, level);
        verify(projectile).setSlowEffect(anyFloat(), anyFloat());
    }

    @Test
    void freezeDecorator_setsTint_blue() {
        when(wrappedAttack.execute(attacker, level)).thenReturn(projectile);
        new FreezeDecorator(wrappedAttack).execute(attacker, level);
        verify(projectile).setTint(0.3f, 0.7f, 1f);
    }

    @Test
    void freezeDecorator_nullProjectile_doesNotThrow() {
        when(wrappedAttack.execute(attacker, level)).thenReturn(null);
        assertDoesNotThrow(() -> new FreezeDecorator(wrappedAttack).execute(attacker, level));
    }

    @Test
    void freezeDecorator_manaCostAddsExtra() {
        when(wrappedAttack.getManaCost()).thenReturn(20);
        assertTrue(new FreezeDecorator(wrappedAttack).getManaCost() > 20);
    }

    // ── Stacking decorators ───────────────────────────────────────────────────

    @Test
    void stackedDecorators_bothEffectsApplied() {
        when(wrappedAttack.execute(attacker, level)).thenReturn(projectile);
        Attack stacked = new FreezeDecorator(new FireDecorator(wrappedAttack));
        stacked.execute(attacker, level);
        verify(projectile).setDotEffect(anyInt(), anyFloat());
        verify(projectile).setSlowEffect(anyFloat(), anyFloat());
    }

    @Test
    void stackedDecorators_manaCostCumulative() {
        when(wrappedAttack.getManaCost()).thenReturn(20);
        int base = new FireDecorator(wrappedAttack).getManaCost();
        when(wrappedAttack.getManaCost()).thenReturn(20);
        int stacked = new FreezeDecorator(new FireDecorator(wrappedAttack)).getManaCost();
        assertTrue(stacked > base);
    }
}
