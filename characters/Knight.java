package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import sk.stuba.fiit.attacks.MeleeAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.NormalGravity;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;


/**
 * The default player character and the only character that cannot be removed from
 * the inventory.
 *
 * <p>A melee-focused tank with high HP ({@code 1500}) and the highest armour
 * ({@value #MAX_ARMOR}) of all player characters. Its only available attack is a
 * 1-tile melee swing mapped to the primary attack key (SPACE).
 *
 * <p>The hitbox is sized from the "idle" animation's first frame so it matches the
 * sprite exactly. The update method is intentionally left empty – all per-frame logic
 * (gravity, animation) is driven by {@link PlayerCharacter#updateAnimation(UpdateContext)}
 * called from {@code PlayerController}.
 */
public class Knight extends PlayerCharacter {
    private static final int MAX_ARMOR = 80;

    private AnimationManager animationManager;

    public Knight(Vector2D position) {
        super("Knight", 150, 30, 2.0f, position, MAX_ARMOR);
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

    /**
     * No per-frame character logic – gravity and animation are handled externally
     * by {@code PlayerController} and {@link #updateAnimation(UpdateContext)}.
     */
    @Override
    public void update(UpdateContext ctx) {

    }

    @Override
    public AnimationManager getAnimationManager() { return animationManager; }
}
