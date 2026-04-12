package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.util.Vector2D;

public class Arrow extends Projectile {
    private boolean piercing;
    private AnimationManager animationManager;

    public Arrow(int damage, float speed, Vector2D position, Vector2D direction, boolean piercing) {
        super(damage, speed, position, direction);
        this.piercing = piercing;
        this.animationManager = new AnimationManager("atlas/arrow/arrow.atlas");
        animationManager.addAnimation("fly", "ARROW/ARROW", 0.05f);
        animationManager.play("fly");
        setHitboxSize( animationManager.getAnimationSize("fly"));
    }

    @Override
    public void update(float deltaTime) {
        move();
        hitbox.setPosition(position.getX(), position.getY());
        animationManager.update(deltaTime);
    }

    public boolean isPiercing() { return piercing; }
    public AnimationManager getAnimationManager() { return animationManager; }
}
