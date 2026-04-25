package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

/**
 * Invisible, stationary, single-pass hitbox spawned by {@link sk.stuba.fiit.attacks.MeleeAttack}.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@code MeleeAttack.execute()} creates this hitbox at the attacker's position,
 *       sized to cover the melee reach in the facing direction, and adds it to the level.</li>
 *   <li>{@code CollisionManager} processes it in the same frame – on hit it deals damage
 *       via {@link #onCollision(Object)}, on miss it is deactivated because
 *       {@link #isSingleUse()} returns {@code true}.</li>
 *   <li>On the next {@code Level.update()}, the deactivated hitbox is removed by
 *       {@code projectiles.removeIf(p -> !p.isActive())}.</li>
 * </ol>
 *
 * <p>No animation, no movement, no rendering (null {@code AnimationManager}).
 * In debug mode (F1) it appears as a cyan rectangle matching the hitbox.
 *
 * <p>The attack sets owner:
 * <ul>
 *   <li>{@link ProjectileOwner#PLAYER} -> {@code CollisionManager.checkPlayerProjectiles()}
 *       handles it and can hit enemies and ducks.</li>
 *   <li>{@link ProjectileOwner#ENEMY} -> {@code CollisionManager.checkEnemyProjectiles()}
 *       handles it and can hit the player.</li>
 * </ul>
 */
public class MeleeHitbox extends Projectile {

    /**
     * @param damage   attack power of the attacker at the moment of the swing
     * @param position bottom-left corner of the hit area in world coordinates
     * @param width    horizontal reach of the swing (pixels)
     * @param height   vertical extent matching the attacker's hitbox height (pixels)
     * @param owner    who fired this – determines which collision handler picks it up
     */
    public MeleeHitbox(int damage, Vector2D position,
                       float width, float height, ProjectileOwner owner) {
        super(damage, 0f, position, new Vector2D(0, 0));
        this.hitbox.setSize(width, height);
        this.hitbox.setPosition(position.getX(), position.getY());
        setOwner(owner);
    }

    /**
     * Stationary – no movement, no internal state change.
     * Deactivation is handled exclusively by {@code CollisionManager}
     * via {@link #isSingleUse()}.
     */
    @Override
    public void update(UpdateContext ctx) {
        // intentionally empty – MeleeHitbox exists only for one collision pass
    }

    /**
     * Signals to {@code CollisionManager} that this projectile must be deactivated
     * after a single collision check, even when no target was hit (avoids lingering).
     *
     * @return {@code true} always
     */
    @Override
    public boolean isSingleUse() {
        return true;
    }
}
