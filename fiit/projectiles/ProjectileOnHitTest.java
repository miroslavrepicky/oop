package sk.stuba.fiit.projectiles;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectileOnHitTest {

    // Minimal concrete projectile
    static class SimpleProjectile extends Projectile {
        SimpleProjectile(int damage) {
            super(damage, 1f, new Vector2D(0, 0), new Vector2D(1, 0));
        }
        @Override public void update(UpdateContext ctx) { move(); }
    }

    @Mock Character target;

    @Test
    void onHit_dealsDamageAndDeactivates() {
        SimpleProjectile p = new SimpleProjectile(25);
        p.onHit(target);
        verify(target).takeDamage(25);
        assertFalse(p.isActive());
    }

    @Test
    void onCollision_withCharacter_callsOnHit() {
        SimpleProjectile p = new SimpleProjectile(10);
        p.onCollision(target);
        verify(target).takeDamage(10);
        assertFalse(p.isActive());
    }

    @Test
    void onCollision_withNonCharacter_doesNotDeactivate() {
        SimpleProjectile p = new SimpleProjectile(10);
        p.onCollision("not a character");
        assertTrue(p.isActive()); // unchanged
    }

    @Test
    void move_updatesPosition() {
        SimpleProjectile p = new SimpleProjectile(5);
        float startX = p.getPosition().getX();
        p.move();
        assertTrue(p.getPosition().getX() > startX);
    }

    @Test
    void setGravityStrategy_null_usesNoGravity() {
        SimpleProjectile p = new SimpleProjectile(5);
        assertDoesNotThrow(() -> p.setGravityStrategy(null));
    }
}
