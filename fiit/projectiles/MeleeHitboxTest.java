package sk.stuba.fiit.projectiles;

import org.junit.jupiter.api.Test;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;

class MeleeHitboxTest {

    @Test
    void isSingleUse_returnsTrue() {
        MeleeHitbox hb = new MeleeHitbox(10, new Vector2D(0, 0), 50, 60, ProjectileOwner.PLAYER);
        assertTrue(hb.isSingleUse());
    }

    @Test
    void hitbox_sizedCorrectly() {
        MeleeHitbox hb = new MeleeHitbox(10, new Vector2D(5, 10), 50, 60, ProjectileOwner.ENEMY);
        assertEquals(50f, hb.getHitbox().width,  0.001f);
        assertEquals(60f, hb.getHitbox().height, 0.001f);
        assertEquals(5f,  hb.getHitbox().x,      0.001f);
        assertEquals(10f, hb.getHitbox().y,      0.001f);
    }

    @Test
    void update_doesNotChangePosition() {
        MeleeHitbox hb = new MeleeHitbox(10, new Vector2D(100, 200), 30, 40, ProjectileOwner.PLAYER);
        hb.update(null); // intentionally empty, should not throw
        assertEquals(100f, hb.getPosition().getX(), 0.001f);
    }

    @Test
    void owner_setCorrectly() {
        MeleeHitbox player = new MeleeHitbox(5, new Vector2D(0, 0), 10, 10, ProjectileOwner.PLAYER);
        assertTrue(player.isPlayerProjectile());

        MeleeHitbox enemy = new MeleeHitbox(5, new Vector2D(0, 0), 10, 10, ProjectileOwner.ENEMY);
        assertFalse(enemy.isPlayerProjectile());
    }

    @Test
    void damage_setCorrectly() {
        MeleeHitbox hb = new MeleeHitbox(42, new Vector2D(0, 0), 10, 10, ProjectileOwner.PLAYER);
        assertEquals(42, hb.getDamage());
    }
}
