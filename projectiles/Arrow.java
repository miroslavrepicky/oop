package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.Poolable;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.physics.FlyingGravity;
import sk.stuba.fiit.render.Renderable;
import sk.stuba.fiit.util.Vector2D;

// TODO: consider removing piercing – currently not used anywhere

/**
 * Arrow projectile fired by archer characters.
 *
 * <p>On-hit effects (DOT, slow) are attached by decorators after {@code execute()}
 * returns this instance, and cleared in {@link #reset(int, float, Vector2D, Vector2D, boolean)}
 * so they never leak between pool reuse cycles.
 */
public class Arrow extends Projectile implements Renderable, Poolable {

    private static final float RENDER_W = 32f;
    private static final float RENDER_H = 16f;

    private boolean piercing;
    private final AnimationManager animationManager;

    public Arrow(int damage, float speed, Vector2D position,
                 Vector2D direction, boolean piercing) {
        super(damage, speed, position, direction);
        this.piercing         = piercing;
        this.animationManager = new AnimationManager("atlas/arrow/arrow.atlas");
        animationManager.addAnimation("fly", "ARROW/ARROW", 0.05f);
        animationManager.play("fly");
        setHitboxSize(animationManager.getAnimationSize("fly"));
        setGravityStrategy(new FlyingGravity());
    }

    public void reset(int damage, float speed, Vector2D position,
                      Vector2D direction, boolean piercing) {
        this.damage    = damage;
        this.speed     = speed;
        this.position  = new Vector2D(position.getX(), position.getY());
        this.direction = direction;
        this.piercing  = piercing;
        this.active    = true;
        this.setVelocityY(0f);
        this.setOnGround(false);
        this.setOwner(ProjectileOwner.PLAYER);
        resetEffects();
        hitbox.setPosition(position.getX(), position.getY());
        setHitboxSize(animationManager.getAnimationSize("fly"));
        animationManager.play("fly");
    }

    @Override
    public void returnToPool() {
        ProjectilePool.getInstance().free(this);
    }

    @Override
    public void update(UpdateContext ctx) {
        super.update(ctx);
        animationManager.update(ctx.deltaTime);
    }

    @Override public AnimationManager getAnimationManager() { return animationManager; }
    @Override public boolean isFlippedX()      { return direction.getX() < 0; }
    @Override public float   getRenderWidth()  { return RENDER_W; }
    @Override public float   getRenderHeight() { return RENDER_H; }

    public boolean isPiercing() { return piercing; }
}
