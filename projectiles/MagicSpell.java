package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.Renderable;
import sk.stuba.fiit.util.Vector2D;

/**
 * Magický projektil s plošným poškodením (AOE).
 *
 * Implementuje {@link AoeProjectile} – CollisionManager automaticky
 * spracuje AOE dopad bez instanceof checkov na túto triedu.
 *
 * Implementuje {@link Renderable} – GameRenderer nemusí poznať tento
 * konkrétny typ; zoberie vizuálne parametre priamo z objektu.
 */
public class MagicSpell extends Projectile implements AoeProjectile, Renderable {

    private static final float RENDER_W = 64f;
    private static final float RENDER_H = 36f;

    private final float aoeRadius;
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
    public void update(UpdateContext ctx) {
        move();
        hitbox.setPosition(position.getX(), position.getY());
        animationManager.update(ctx.deltaTime);
    }

    // -------------------------------------------------------------------------
    //  AoeProjectile
    // -------------------------------------------------------------------------

    @Override public int   getDamage()    { return damage; }
    @Override public float getAoeRadius() { return aoeRadius; }

    // -------------------------------------------------------------------------
    //  Renderable
    // -------------------------------------------------------------------------

    @Override public AnimationManager getAnimationManager() { return animationManager; }
    @Override public boolean isFlippedX()    { return direction.getX() < 0; }
    @Override public float   getRenderWidth()  { return RENDER_W; }
    @Override public float   getRenderHeight() { return RENDER_H; }
}
