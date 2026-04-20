package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import sk.stuba.fiit.attacks.ArrowAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.NormalGravity;
import sk.stuba.fiit.util.Vector2D;

public class EnemyArcher extends EnemyCharacter {
    private static final int ARMOR = 5; // łucznik ma len malu obranu

    private int arrowCount;
    private AnimationManager animationManager;

    public EnemyArcher(Vector2D position) {
        super("EnemyArcher", 70, 15, 2.0f, position, 150f, 300f, ARMOR, ARMOR);
        this.arrowCount      = 20;
        this.gravityStrategy = new NormalGravity();
        this.attack          = new ArrowAttack(false);
        initAnimations();
        Vector2D idleSize = animationManager.getFirstFrameSize("idle");
        this.hitbox.setSize(idleSize.getX(), idleSize.getY());
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/archer/archer.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",     0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",     0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",     0.1f);
        animationManager.addAnimation("attack", "ATTACK/ATTACK", 0.07f);
        animationManager.addAnimation("death",  "DEATH/DEATH",   0.3f, Animation.PlayMode.NORMAL);
    }

    @Override
    public void triggerAttack() {
        if (arrowCount <= 0) return;
        arrowCount--;
        super.triggerAttack();
    }

    @Override
    public AnimationManager getAnimationManager() { return animationManager; }

    public int getArrowCount() { return arrowCount; }
}
