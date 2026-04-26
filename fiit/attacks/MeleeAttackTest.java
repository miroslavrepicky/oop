package sk.stuba.fiit.attacks;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.exceptions.InvalidAttackException;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MeleeAttackTest {

    @Mock Character attacker;
    @Mock Level level;

    //  Konstruktor

    @Test
    void constructor_negativeRange_throwsInvalidAttack() {
        assertThrows(InvalidAttackException.class, () -> new MeleeAttack(-1f));
    }

    @Test
    void constructor_zeroRange_throwsInvalidAttack() {
        assertThrows(InvalidAttackException.class, () -> new MeleeAttack(0f));
    }

    @Test
    void constructor_validRange_doesNotThrow() {
        assertDoesNotThrow(() -> new MeleeAttack(1f));
    }

    //  execute()

    @Test
    void execute_nullAttacker_throwsInvalidAttack() {
        assertThrows(InvalidAttackException.class,
            () -> new MeleeAttack(1f).execute(null, level));
    }

    @Test
    void execute_nullLevel_returnsNull() {
        // getName() je volana len pri warn logu – stub ho preventivne
        when(attacker.getName()).thenReturn("Knight");
        assertNull(new MeleeAttack(1f).execute(attacker, null));
    }

    @Test
    void execute_facingRight_addsProjectileToLevel() {
        setupAttacker(true);
        Projectile result = new MeleeAttack(1f).execute(attacker, level);
        assertNotNull(result);
        verify(level).addProjectile(any());
    }

    @Test
    void execute_facingLeft_addsProjectileToLevel() {
        setupAttacker(false);
        Projectile result = new MeleeAttack(1f).execute(attacker, level);
        assertNotNull(result);
        verify(level).addProjectile(any());
    }

    @Test
    void execute_enemyAttacker_setsEnemyOwner() {
        setupAttacker(true);
        when(attacker.isEnemy()).thenReturn(true);
        Projectile p = new MeleeAttack(1f).execute(attacker, level);
        assertFalse(p.isPlayerProjectile());
    }

    @Test
    void execute_playerAttacker_setsPlayerOwner() {
        setupAttacker(true);
        when(attacker.isEnemy()).thenReturn(false);
        Projectile p = new MeleeAttack(1f).execute(attacker, level);
        assertTrue(p.isPlayerProjectile());
    }

    @Test
    void execute_largerRange_producesBiggerHitbox() {
        setupAttacker(true);
        Projectile small = new MeleeAttack(1f).execute(attacker, level);
        reset(level); // reset verify count

        setupAttacker(true);
        Projectile large = new MeleeAttack(2f).execute(attacker, level);

        assertTrue(large.getHitbox().width > small.getHitbox().width);
    }

    //  Metadata

    @Test
    void getAnimationName_returnsAttack() {
        assertEquals("attack", new MeleeAttack(1f).getAnimationName());
    }

    @Test
    void getAnimationDuration_nullAm_returnsPositiveDefault() {
        assertTrue(new MeleeAttack(1f).getAnimationDuration(null) > 0f);
    }

    @Test
    void getManaCost_returnsZero() {
        assertEquals(0, new MeleeAttack(1f).getManaCost());
    }

    //  Helper

    /** Nastavi mock attacker-a s beznymi hodnotami. */
    private void setupAttacker(boolean facingRight) {
        when(attacker.isFacingRight()).thenReturn(facingRight);
        when(attacker.getPosition()).thenReturn(new Vector2D(100f, 50f));
        when(attacker.getHitbox()).thenReturn(new Rectangle(100, 50, 30, 60));
        when(attacker.getAttackPower()).thenReturn(25);
        when(attacker.isEnemy()).thenReturn(false);
    }
}
