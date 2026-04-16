package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import sk.stuba.fiit.attacks.ArrowAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.NormalGravity;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;


public class Archer extends PlayerCharacter {
    private static final int MAX_ARMOR = 50; // stredna obrana
    private static final int MAX_ARROWS    = 30;

    private int arrowCount;
    private AnimationManager animationManager;

    public Archer(Vector2D position) {
        super("Archer", 80, 20, 3.5f, position, MAX_ARMOR);
        this.arrowCount = 30;
        this.gravityStrategy = new NormalGravity();
        initAnimations();
        Vector2D idleSize = animationManager.getFirstFrameSize("idle");
        this.hitbox.setSize(idleSize.getX(), idleSize.getY());
        primaryAttack = new ArrowAttack(true); // SPACE - normalna sipka
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/archer/archer.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",     0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",     0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",     0.1f);
        animationManager.addAnimation("death",  "DEATH/DEATH",   0.3f, Animation.PlayMode.NORMAL);
        animationManager.addAnimation("attack", "ATTACK/ATTACK", 0.07f);
    }

    @Override
    public void performPrimaryAttack() {
        if (arrowCount <= 0) return;
        arrowCount--;
        super.performPrimaryAttack();
    }

    private void handleInput() {}

    @Override
    public void update(UpdateContext ctx) {

    }

    public int getArrowCount() {
        return arrowCount;
    }
    public int getMaxArrows()   { return MAX_ARROWS; }

    @Override
    public AnimationManager getAnimationManager() {
        return animationManager;
    }
}
