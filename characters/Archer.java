package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import sk.stuba.fiit.attacks.ArrowAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.NormalGravity;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;


/**
 * Player character specialized in ranged physical attacks.
 *
 * <h2>Attacks</h2>
 * <ul>
 *   <li><b>Primary (SPACE)</b> – {@link ArrowAttack}: piercing arrow projectile
 *       with a limited supply of {@value #MAX_ARROWS} arrows.</li>
 * </ul>
 *
 * <h2>Arrow economy</h2>
 * <p>{@link #performPrimaryAttack(Level)} guards against firing when the arrow count
 * reaches zero. The remaining count can be queried via {@link #getArrowCount()}.
 *
 * <h2>Stats</h2>
 * <ul>
 *   <li>High movement speed ({@code 3.5}) – fastest of all player characters.</li>
 *   <li>Medium armour ({@value #MAX_ARMOR}).</li>
 *   <li>Moderate HP ({@code 800}).</li>
 * </ul>
 */
public class Archer extends PlayerCharacter {
    private static final int MAX_ARMOR = 50; // stredna obrana
    private static final int MAX_ARROWS    = 30;

    private int arrowCount;
    private AnimationManager animationManager;

    public Archer(Vector2D position) {
        super("Archer", 800, 20, 3.5f, position, MAX_ARMOR);
        this.arrowCount = 30;
        this.gravityStrategy = new NormalGravity();
        initAnimations();
        Vector2D idleSize = animationManager.getFirstFrameSize("idle");
        this.hitbox.setSize(idleSize.getX(), idleSize.getY());
        primaryAttack = new ArrowAttack();
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
    public void performPrimaryAttack(Level level) {
        if (arrowCount <= 0 || isAttacking) return;
        arrowCount--;
        super.performPrimaryAttack(level);
    }

    /**
     * No per-frame character logic – gravity and animation are handled externally
     * by {@code PlayerController} and {@link #updateAnimation(UpdateContext)}.
     */
    @Override
    public void update(UpdateContext ctx) {

    }

    @Override
    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    // -------------------------------------------------------------------------
    //  HUD data – publicly expose arrow count for SnapshotBuilder
    // -------------------------------------------------------------------------

    /** Returns remaining arrows; used by HUD rendering. */
    @Override
    public int getArrowCount() { return arrowCount; }

    /** Returns maximum arrows; used by HUD rendering. */
    @Override
    public int getMaxArrows() { return MAX_ARROWS; }
}
