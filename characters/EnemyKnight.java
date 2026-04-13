package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import sk.stuba.fiit.attacks.MeleeAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.util.Vector2D;

public class EnemyKnight extends EnemyCharacter {
    private static final int ARMOR = 15;

    private AnimationManager animationManager;

    public EnemyKnight(Vector2D position) {
        super("EnemyKnight", 120, 25, 1.5f, position, 100f, 200f, ARMOR, ARMOR);
        this.gravityStrategy = new NormalGravity();
        this.attack          = new MeleeAttack(1);
        initAnimations();
        Vector2D idleSize = animationManager.getFirstFrameSize("idle");
        this.hitbox.setSize(idleSize.getX(), idleSize.getY());
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/knight/knight.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",     0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",     0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",     0.1f);
        animationManager.addAnimation("attack", "ATTACK/ATTACK", 0.07f);
        animationManager.addAnimation("death",  "DEATH/DEATH",   0.2f, Animation.PlayMode.NORMAL);
    }

    @Override
    public void performAttack() {
        // logika riesi MeleeAttack.execute() + EnemyCharacter.performAttack(player)
    }

    @Override
    public AnimationManager getAnimationManager() { return animationManager; }
}
