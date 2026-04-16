package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.util.Vector2D;

/**
 * Projektil vystreleny ked hrac pouzije FriendlyDuck z inventara.
 * Pouziva animaciu TURDFLY/TURDFLY.
 */
public class TurdflyProjectile extends Projectile {

    private static final int   TURDFLY_DAMAGE = 25;
    private static final float TURDFLY_SPEED  = 7.0f;

    private AnimationManager animationManager;

    public TurdflyProjectile(Vector2D position, Vector2D direction) {
        super(TURDFLY_DAMAGE, TURDFLY_SPEED, position, direction);
        initAnimations();
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/turdfly/turdfly.atlas");
        animationManager.addAnimation("fly", "TURDFLY/TURDFLY", 0.1f);
        animationManager.play("fly");
        setHitboxSize( animationManager.getAnimationSize("fly"));
    }

    @Override
    public void update(float deltaTime) {
        move();
        hitbox.setPosition(position.getX(), position.getY());
        animationManager.update(deltaTime);
    }

    public AnimationManager getAnimationManager() { return animationManager; }
}
