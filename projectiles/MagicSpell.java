package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.Poolable;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.Renderable;
import sk.stuba.fiit.util.Vector2D;

/**
 * AOE magic projectile fired by spell attacks.
 *
 * <p>On-hit effects (DOT, slow) are attached by decorators after {@code execute()}
 * returns this instance, and cleared in {@link #reset(int, float, Vector2D, Vector2D, float)}
 * so they never leak between pool reuse cycles.
 */
public class MagicSpell extends Projectile implements AoeProjectile, Renderable, Poolable {

    private static final float RENDER_W = 64f;
    private static final float RENDER_H = 36f;

    private float aoeRadius;
    private final AnimationManager animationManager;

    public MagicSpell(int damage, float speed, Vector2D position,
                      Vector2D direction, float aoeRadius) {
        super(damage, speed, position, direction);
        this.aoeRadius        = aoeRadius;
        this.animationManager = new AnimationManager("atlas/firespell/firespell.atlas");
        animationManager.addAnimation("fly", "FIRESPELL/FIRESPELL", 0.08f);
        animationManager.play("fly");
        hitbox.setSize(RENDER_W, RENDER_H);
    }

    @Override
    public void returnToPool() {
        ProjectilePool.getInstance().free(this);
    }

    public void reset(int damage, float speed, Vector2D position,
                      Vector2D direction, float aoeRadius) {
        this.damage    = damage;
        this.speed     = speed;
        this.position  = new Vector2D(position.getX(), position.getY());
        this.direction = direction;
        this.aoeRadius = aoeRadius;
        this.active    = true;
        this.setVelocityY(0f);
        this.setOnGround(false);
        this.setOwner(ProjectileOwner.PLAYER);
        resetEffects();
        hitbox.setPosition(position.getX(), position.getY());
        hitbox.setSize(RENDER_W, RENDER_H);
        animationManager.play("fly");
    }

    @Override
    public void update(UpdateContext ctx) {
        move();
        hitbox.setPosition(position.getX(), position.getY());
        animationManager.update(ctx.deltaTime);
    }

    @Override public int   getDamage()    { return damage; }
    @Override public float getAoeRadius() { return aoeRadius; }

    @Override public AnimationManager getAnimationManager() { return animationManager; }
    @Override public boolean isFlippedX()      { return direction.getX() < 0; }
    @Override public float   getRenderWidth()  { return RENDER_W; }
    @Override public float   getRenderHeight() { return RENDER_H; }
}
