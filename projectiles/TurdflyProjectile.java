package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.Renderable;
import sk.stuba.fiit.util.Vector2D;

/**
 * Projektil vystrelený keď hráč použije FriendlyDuck z inventára.
 *
 * Implementuje {@link Renderable} – GameRenderer nemusí poznať tento typ.
 */
public class TurdflyProjectile extends Projectile implements Renderable {

    private static final int   TURDFLY_DAMAGE = 25;
    private static final float TURDFLY_SPEED  = 7.0f;
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
