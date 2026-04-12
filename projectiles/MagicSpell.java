package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.util.Vector2D;

public class MagicSpell extends Projectile {
    private float aoeRadius;
    private AnimationManager animationManager;

    public MagicSpell(int damage, float speed, Vector2D position, Vector2D direction, float aoeRadius) {
        super(damage, speed, position, direction);
        this.aoeRadius = aoeRadius;
        this.animationManager = new AnimationManager("atlas/firespell/firespell.atlas");
        animationManager.addAnimation("fly", "FIRESPELL/FIRESPELL", 0.08f);
        animationManager.play("fly");
        hitbox.setSize(64, 36);
    }

    @Override
    public void update(float deltaTime) {
        move();
        hitbox.setPosition(position.getX(), position.getY());
        animationManager.update(deltaTime);
    }

    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, boolean flipX) {
        animationManager.render(batch, position.getX(), position.getY(), 64, 36, flipX);
    }

    public AnimationManager getAnimationManager() { return animationManager; }
    public float getAoeRadius() { return aoeRadius; }
}
