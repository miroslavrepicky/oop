package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.projectiles.MagicSpell;
import sk.stuba.fiit.util.Vector2D;

public class EnemyWizzard extends EnemyCharacter {
    private int mana;
    private static final int SPELL_MANA_COST = 20;
    private AnimationManager animationManager;

    public EnemyWizzard(Vector2D position) {
        super("EnemyWizzard", 60, 35, 1.5f, position, 100f, 350f);
        this.mana = 100;
        this.gravityStrategy = new NormalGravity();
        initAnimations();
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/wizzard/wizzard.atlas");
        animationManager.addAnimation("idle",  "IDLE/IDLE",   0.1f);
        animationManager.addAnimation("walk",  "WALK/WALK",   0.1f);
        animationManager.addAnimation("jump",  "JUMP/JUMP",   0.1f);
        animationManager.addAnimation("death", "DEATH/DEATH", 0.1f);
    }

    @Override
    public void performAttack() {
        if (mana >= SPELL_MANA_COST) {
            castSpell();
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

    public MagicSpell castSpell() {
        mana -= SPELL_MANA_COST;
        Vector2D direction = new Vector2D(-1, 0);
        return new MagicSpell(attackPower, 4.0f, position, direction, 50.0f);
    }

    public int getMana() { return mana; }
}
