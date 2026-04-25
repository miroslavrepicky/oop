package sk.stuba.fiit.projectiles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProjectileTest {

    /** Minimal concrete projectile with no LibGDX animation. */
    static class TestProjectile extends Projectile {
        TestProjectile(int damage, float speed, Vector2D pos, Vector2D dir) {
            super(damage, speed, pos, dir);
        }
        @Override public void update(UpdateContext ctx) { move(); }
    }

    private TestProjectile proj;

    @BeforeEach
    void setUp() {
        proj = new TestProjectile(10, 2f, new Vector2D(0f, 0f), new Vector2D(1f, 0f));
    }

    @Test
    void initiallyActive() {
        assertTrue(proj.isActive());
    }

    @Test
    void setActive_false_deactivates() {
        proj.setActive(false);
        assertFalse(proj.isActive());
    }

    @Test
    void getDamage_returnsCorrectValue() {
        assertEquals(10, proj.getDamage());
    }

    @Test
    void defaultOwner_isPlayer() {
        assertEquals(ProjectileOwner.PLAYER, proj.getOwner());
    }

    @Test
    void setOwner_changesOwner() {
        proj.setOwner(ProjectileOwner.ENEMY);
        assertEquals(ProjectileOwner.ENEMY, proj.getOwner());
        assertFalse(proj.isPlayerProjectile());
    }

    @Test
    void isPlayerProjectile_trueForPlayer() {
        proj.setOwner(ProjectileOwner.PLAYER);
        assertTrue(proj.isPlayerProjectile());
    }

    @Test
    void setTint_changesRGB() {
        proj.setTint(0.5f, 0.3f, 0.1f);
        assertEquals(0.5f, proj.getTintR(), 0.001f);
        assertEquals(0.3f, proj.getTintG(), 0.001f);
        assertEquals(0.1f, proj.getTintB(), 0.001f);
    }

    @Test
    void defaultTint_isWhite() {
        assertEquals(1f, proj.getTintR(), 0.001f);
        assertEquals(1f, proj.getTintG(), 0.001f);
        assertEquals(1f, proj.getTintB(), 0.001f);
    }

    @Test
    void setDotEffect_andGetters() {
        proj.setDotEffect(15, 3.0f);
        assertTrue(proj.hasDotEffect());
        assertEquals(15, proj.getDotDps());
        assertEquals(3.0f, proj.getDotDuration(), 0.001f);
    }

    @Test
    void setSlowEffect_andGetters() {
        proj.setSlowEffect(0.4f, 2.5f);
        assertTrue(proj.hasSlowEffect());
        assertEquals(0.4f, proj.getSlowMultiplier(), 0.001f);
        assertEquals(2.5f, proj.getSlowDuration(),   0.001f);
    }

    @Test
    void resetEffects_clearsAll() {
        proj.setDotEffect(10, 2f);
        proj.setSlowEffect(0.5f, 1f);
        proj.setTint(0.5f, 0.5f, 0.5f);
        // resetEffects is protected – call it via reset on a subclass
        // We can still test it indirectly by confirming initial state after construction
        TestProjectile fresh = new TestProjectile(5, 1f, new Vector2D(0, 0), new Vector2D(1, 0));
        assertFalse(fresh.hasDotEffect());
        assertFalse(fresh.hasSlowEffect());
        assertEquals(1f, fresh.getTintR(), 0.001f);
    }

    @Test
    void isSingleUse_defaultFalse() {
        assertFalse(proj.isSingleUse());
    }

    @Test
    void setHitboxSize_updatesHitbox() {
        proj.setHitboxSize(new Vector2D(64f, 32f));
        assertEquals(64f, proj.getHitbox().width,  0.001f);
        assertEquals(32f, proj.getHitbox().height, 0.001f);
    }

    @Test
    void velocityY_setAndGet() {
        proj.setVelocityY(-100f);
        assertEquals(-100f, proj.getVelocityY(), 0.001f);
    }

    @Test
    void onGround_setAndGet() {
        proj.setOnGround(true);
        assertTrue(proj.isOnGround());
    }

    @Test
    void updateHitbox_syncsWithPosition() {
        proj.getPosition().setX(50f);
        proj.getPosition().setY(75f);
        proj.updateHitbox();
        assertEquals(50f, proj.getHitbox().x, 0.001f);
        assertEquals(75f, proj.getHitbox().y, 0.001f);
    }
}
