package sk.stuba.fiit.characters;

import sk.stuba.fiit.attacks.SpellAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.util.Vector2D;

public class EnemyWizzard extends EnemyCharacter {
    private AnimationManager animationManager;

    public EnemyWizzard(Vector2D position) {
        super("EnemyWizzard", 60, 35, 1.5f, position, 100f, 350f);
        this.gravityStrategy = new NormalGravity();
        this.attack          = new SpellAttack(4.0f, 50.0f, 0); // mana cost 0 – enemy nemá manu
        initAnimations();
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/wizzard/wizzard.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",   0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",   0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",   0.1f);
        animationManager.addAnimation("cast",   "CAST/CAST",   0.08f);
        animationManager.addAnimation("death",  "DEATH/DEATH", 0.1f);
    }

    @Override
    public void performAttack() {
        // SpellAttack.execute() spawnuje MagicSpell do levelu
    }

    @Override
    public void updateAnimation(float deltaTime) {
        if (!isAlive()) {
            animationManager.play("death");
        } else if (isAttacking()) {
            animationManager.play("cast");
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
}
