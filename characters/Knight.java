package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class Knight extends PlayerCharacter {
    private boolean shield;
    private AnimationManager animationManager;

    public Knight(Vector2D position) {
        super("Knight", 150, 30, 2.0f, position);
        this.shield = true;
        this.gravityStrategy = new NormalGravity();
        initAnimations();
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/knight/knight.atlas");
        //                        name      region    frames  origW  duration
        animationManager.addAnimation("idle",   "IDLE/IDLE",   0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",   0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",   0.1f);
        animationManager.addAnimation("death",  "DEATH/DEATH", 0.1f);
    }

    @Override
    public void performAttack() {
        Level level = GameManager.getInstance().getCurrentLevel();
        if (level == null) return;

        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;
            if (hitbox.overlaps(enemy.getHitbox())) {
                enemy.takeDamage(attackPower);
                System.out.println("Knight zautocil! Nepriatel HP: " + enemy.getHp());
            }
        }
    }

    public void specialAttack() {
        // silnejší úder, väčšie poškodenie
    }

    @Override
    public void updateAnimation(float deltaTime) {
        if (!isAlive()) {
            animationManager.play("death");
        } else if (!isOnGround()) {
            animationManager.play("jump");
        } else if (Math.abs(0) > 0.1f) {
            animationManager.play("walk");
        } else {
            animationManager.play("idle");
        }
        animationManager.update(deltaTime);
    }

    @Override
    public void handleInput() {
        // spracovanie vstupu hráča
    }

    @Override
    public void update(float deltaTime) {
        handleInput();
    }

    public boolean hasShield() { return shield; }
    public AnimationManager getAnimationManager() { return animationManager; }
}
