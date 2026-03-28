package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.projectiles.Arrow;
import sk.stuba.fiit.util.Vector2D;

public class EnemyArcher extends EnemyCharacter {
    private int arrowCount;
    private AnimationManager animationManager;

    public EnemyArcher(Vector2D position) {
        super("EnemyArcher", 70, 15, 2.0f, position, 150f, 300f);
        this.arrowCount = 20;
        this.gravityStrategy = new NormalGravity();
        initAnimations();
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/archer/archer.atlas");
        animationManager.addAnimation("idle",  "IDLE/IDLE",   0.1f);
        animationManager.addAnimation("walk",  "WALK/WALK",   0.1f);
        animationManager.addAnimation("jump",  "JUMP/JUMP",   0.1f);
        animationManager.addAnimation("death", "DEATH/DEATH", 0.1f);
    }

    @Override
    public void performAttack() {
        if (arrowCount > 0) {
            shootArrow();
        }
    }

    @Override
    public void updateAnimation(float deltaTime) {
        if (!isAlive()) {
            animationManager.play("death");
        } else if (!isOnGround()) {
            animationManager.play("jump");
        } else if (Math.abs(getVelocityX()) > 0.1f) {
            animationManager.play("walk");
        } else {
            animationManager.play("idle");
        }
        animationManager.update(deltaTime);
    }

    @Override
    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    public Arrow shootArrow() {
        arrowCount--;
        Vector2D direction = new Vector2D(-1, 0);
        return new Arrow(attackPower, 5.0f, position, direction, false);
    }

    public int getArrowCount() { return arrowCount; }
}
