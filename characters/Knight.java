package sk.stuba.fiit.characters;

import sk.stuba.fiit.attacks.MeleeAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.util.Vector2D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Knight extends PlayerCharacter {
    private static final int MAX_ARMOR = 80;

    private AnimationManager animationManager;

    public Knight(Vector2D position) {
        super("Knight", 150, 30, 2.0f, position, MAX_ARMOR);
        this.gravityStrategy = new NormalGravity();
        initAnimations();

        primaryAttack = new MeleeAttack(1);
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/knight/knight.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",     0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",     0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",     0.1f);
        animationManager.addAnimation("death",  "DEATH/DEATH",   0.1f);
        animationManager.addAnimation("attack", "ATTACK/ATTACK", 0.07f);
    }

    @Override
    public void handleInput() {}

    @Override
    public void update(float deltaTime) {
        handleInput();
    }

    @Override
    public AnimationManager getAnimationManager() { return animationManager; }
}
