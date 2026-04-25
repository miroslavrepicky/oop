package sk.stuba.fiit.projectiles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.util.Vector2D;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeleeHitboxCollisionTest {

    @Mock Character target;

    @Test
    void onCollision_withCharacter_dealsDamage() {
        MeleeHitbox hb = new MeleeHitbox(30, new Vector2D(0,0), 50, 60, ProjectileOwner.PLAYER);
        hb.onCollision(target);
        verify(target).takeDamage(30);
    }

    @Test
    void onCollision_withCharacter_deactivates() {
        MeleeHitbox hb = new MeleeHitbox(30, new Vector2D(0,0), 50, 60, ProjectileOwner.PLAYER);
        hb.onCollision(target);
        assertFalse(hb.isActive());
    }

    @Test
    void onCollision_withNonCharacter_noInteraction() {
        MeleeHitbox hb = new MeleeHitbox(10, new Vector2D(0,0), 10, 10, ProjectileOwner.PLAYER);
        hb.onCollision("wall");
        assertTrue(hb.isActive()); // walls don't deactivate
        verifyNoInteractions(target);
    }

    @Test
    void update_withNullContext_doesNotThrow() {
        MeleeHitbox hb = new MeleeHitbox(10, new Vector2D(5,5), 20, 30, ProjectileOwner.ENEMY);
        assertDoesNotThrow(() -> hb.update(null));
        // Position must be unchanged
        assertEquals(5f, hb.getPosition().getX(), 0.001f);
    }
}
