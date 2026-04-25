package sk.stuba.fiit.core.engine;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.util.Vector2D;

/**
 * Contract for objects on which a GravityStrategy can act.
 *
 * <p>Originally {@code GravityStrategy} depended on {@code Character}, which
 * made it impossible to apply gravity to projectiles or other objects without
 * unnecessary inheritance. This interface exposes only what gravity truly needs:
 * position, vertical velocity, hitbox, and the on-ground flag.
 *
 * <p>Implemented by: {@code Character} and optionally {@code Projectile} and
 * other physical objects.
 */
public interface Physicable {

    Vector2D getPosition();
    void     setPosition(Vector2D position);

    float getVelocityY();
    void  setVelocityY(float vy);

    Rectangle getHitbox();

    /**
     * Synchronizes the hitbox position with the object's current world position.
     * Must be called after any position change.
     */
    void      updateHitbox();

    boolean isOnGround();
    void    setOnGround(boolean onGround);
}
