package sk.stuba.fiit.characters;

import sk.stuba.fiit.attacks.MeleeAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.util.Vector2D;

public class EnemyKnight extends EnemyCharacter {
    private boolean shield;
    private AnimationManager animationManager;

    public EnemyKnight(Vector2D position) {
        super("EnemyKnight", 120, 25, 1.5f, position, 100f, 200f);
        this.shield          = true;
        this.gravityStrategy = new NormalGravity();
        this.attack          = new MeleeAttack(1);
        initAnimations();
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/knight/knight.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",     0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",     0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",     0.1f);
        animationManager.addAnimation("attack", "ATTACK/ATTACK", 0.07f);
        animationManager.addAnimation("death",  "DEATH/DEATH",   0.1f);
    }

    @Override
    public void performAttack() {
        // logika rieši MeleeAttack.execute() + EnemyCharacter.performAttack(player)
    }

    @Override
    public void updateAnimation(float deltaTime) {
        if (!isAlive()) {
            animationManager.play("death");
        } else if (isAttacking()) {
            animationManager.play("attack");
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
    public AnimationManager getAnimationManager() { return animationManager; }

    public boolean hasShield() { return shield; }
}
