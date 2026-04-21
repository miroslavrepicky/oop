package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import sk.stuba.fiit.attacks.MeleeAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.NormalGravity;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;


public class Knight extends PlayerCharacter {
    private static final int MAX_ARMOR = 80;

    private AnimationManager animationManager;

    public Knight(Vector2D position) {
        super("Knight", 1500, 30, 2.0f, position, MAX_ARMOR);
        this.gravityStrategy = new NormalGravity();
        initAnimations();
        Vector2D idleSize = animationManager.getFirstFrameSize("idle");
        this.hitbox.setSize(idleSize.getX(), idleSize.getY());
        primaryAttack = new MeleeAttack(1);
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/knight/knight.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",     0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",     0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",     0.1f);
        animationManager.addAnimation("death",  "DEATH/DEATH",   0.2f, Animation.PlayMode.NORMAL);
        animationManager.addAnimation("attack", "ATTACK/ATTACK", 0.07f);
    }

    @Override
    public void update(UpdateContext ctx) {

    }

    @Override
    public AnimationManager getAnimationManager() { return animationManager; }
}
