package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.Poolable;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.Renderable;
import sk.stuba.fiit.util.Vector2D;

/**
 * Projectile fired when the player uses a {@link sk.stuba.fiit.items.FriendlyDuck} item.
 *
 * <p>Pooled via {@link ProjectilePool}. After {@code obtainTurdfly()}, callers
 * must call {@link #reset(Vector2D, Vector2D)} to configure position and direction
 * before adding the projectile to the level.
 *
 * <p>Implements {@link Renderable} so {@code GameRenderer} does not need to know
 * this concrete type.
 */
public class TurdflyProjectile extends Projectile implements Renderable, Poolable {

    private static final int   TURDFLY_DAMAGE = 25;
    private static final float TURDFLY_SPEED  = 2.5f;
    private static final float RENDER_W       = 46f;
    private static final float RENDER_H       = 33f;

    private final AnimationManager animationManager;

    public TurdflyProjectile(Vector2D position, Vector2D direction) {
        super(TURDFLY_DAMAGE, TURDFLY_SPEED, position, direction);
        animationManager = new AnimationManager("atlas/turdfly/turdfly.atlas");
        animationManager.addAnimation("fly", "TURDFLY/TURDFLY", 0.1f);
        animationManager.play("fly");
        setHitboxSize(animationManager.getAnimationSize("fly"));
    }


    @Override
    public void returnToPool() {
        ProjectilePool.getInstance().free(this);
    }

    // -------------------------------------------------------------------------
    //  Pool reset
    // -------------------------------------------------------------------------

    /**
     * Reinitialises this instance with new game values so it can be reused from
     * the pool without allocating a new object.
     *
     * <p>Must be called immediately after
     * {@link ProjectilePool#obtainTurdfly()} before adding the projectile to the level.
     * Damage and speed are fixed constants and are restored automatically;
     * only position, direction, and transient physics state are reset here.
     * {@link #resetEffects()} is called to ensure no on-hit effects leak from a
     * previous use of this instance.
     *
     * @param position  spawn position in world coordinates
     * @param direction normalised direction vector; {@code (1, 0)} = right, {@code (-1, 0)} = left
     */
    public void reset(Vector2D position, Vector2D direction) {
        this.damage    = TURDFLY_DAMAGE;
        this.speed     = TURDFLY_SPEED;
        this.position  = new Vector2D(position.getX(), position.getY());
        this.direction = direction;
        this.active    = true;
        this.setVelocityY(0f);
        this.setOnGround(false);
        this.setOwner(ProjectileOwner.PLAYER);
        hitbox.setPosition(position.getX(), position.getY());
        setHitboxSize(animationManager.getAnimationSize("fly"));
        animationManager.play("fly");
    }

    @Override
    public void update(UpdateContext ctx) {
        move();
        hitbox.setPosition(position.getX(), position.getY());
        animationManager.update(ctx.deltaTime);
    }

    // -------------------------------------------------------------------------
    //  Renderable
    // -------------------------------------------------------------------------

    @Override public AnimationManager getAnimationManager() { return animationManager; }
    @Override public boolean isFlippedX()    { return direction.getX() < 0; }
    @Override public float   getRenderWidth()  { return RENDER_W; }
    @Override public float   getRenderHeight() { return RENDER_H; }
}
