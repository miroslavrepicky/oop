package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.Renderable;
import sk.stuba.fiit.util.Vector2D;

// TODO: consider removing piercing – currently not used anywhere

public class Arrow extends Projectile implements Renderable {

    private static final float RENDER_W = 32f;
    private static final float RENDER_H = 16f;

    private final boolean        piercing;
    private final AnimationManager animationManager;

    public Arrow(int damage, float speed, Vector2D position,
                 Vector2D direction, boolean piercing) {
        super(damage, speed, position, direction);
        this.piercing         = piercing;
        this.animationManager = new AnimationManager("atlas/arrow/arrow.atlas");
        animationManager.addAnimation("fly", "ARROW/ARROW", 0.05f);
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

    // -------------------------------------------------------------------------
    //  Gettery
    // -------------------------------------------------------------------------

    public boolean isPiercing() { return piercing; }
}
