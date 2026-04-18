package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.Renderable;
import sk.stuba.fiit.util.Vector2D;

// TODO: consider removing piercing – currently not used anywhere

public class Arrow extends Projectile implements Renderable, Poolable {

    private static final float RENDER_W = 32f;
    private static final float RENDER_H = 16f;

    private boolean        piercing;
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

    // -------------------------------------------------------------------------
    //  Reset pre ObjectPool – nastaví nové herné hodnoty pred opätovným použitím
    // -------------------------------------------------------------------------

    /**
     * Reinicializuje šíp na nové herné hodnoty.
     * Volá sa ihneď po {@code ProjectilePool.getInstance().obtainArrow()}.
     *
     * <p>AnimationManager sa NEresetuje – atlas je zdieľaný cez AtlasCache
     * a animácia "fly" je vždy rovnaká.
     *
     * @param damage    poškodenie šípu
     * @param speed     rýchlosť pohybu za snímok
     * @param position  štartovacia pozícia (skopíruje sa, nezdieľa)
     * @param direction normalizovaný smer
     * @param piercing  true = šíp prechádza cez viacero cieľov
     */
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
        this.setOwner(ProjectileOwner.PLAYER);   // default; volajúci môže prepísať
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
