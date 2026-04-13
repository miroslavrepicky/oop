package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import sk.stuba.fiit.attacks.SpellAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.util.Vector2D;

public class EnemyWizzard extends EnemyCharacter {
    private static final int ARMOR = 0; // carodejnik bez brnenia

    private AnimationManager animationManager;

    public EnemyWizzard(Vector2D position) {
        super("EnemyWizzard", 60, 35, 1.5f, position, 100f, 350f, ARMOR, ARMOR);
        this.gravityStrategy = new NormalGravity();
        this.attack          = new SpellAttack(4.0f, 50.0f, 0); // mana cost 0 – enemy nema manu
        initAnimations();
        Vector2D idleSize = animationManager.getFirstFrameSize("idle");
        this.hitbox.setSize(idleSize.getX(), idleSize.getY());
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/wizzard/wizzard.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",   0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",   0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",   0.1f);
        animationManager.addAnimation("cast",   "CAST/CAST",   0.08f);
        animationManager.addAnimation("death",  "DEATH/DEATH", 0.3f, Animation.PlayMode.NORMAL);
    }

    @Override
    public void performAttack() {
        // SpellAttack.execute() spawnuje MagicSpell do levelu
    }

    @Override
    protected String getAttackAnimationName() {
        return "cast";
    }

    @Override
    public AnimationManager getAnimationManager() { return animationManager; }
}
