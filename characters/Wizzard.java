package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import sk.stuba.fiit.attacks.MeleeAttack;
import sk.stuba.fiit.attacks.SpellAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.NormalGravity;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;


/**
 * Player character specialised in ranged magic attacks.
 *
 * <h2>Attacks</h2>
 * <ul>
 *   <li><b>Primary (SPACE)</b> – {@link SpellAttack}: fast AOE magic projectile
 *       with a mana cost of 20.</li>
 *   <li><b>Secondary (V)</b>   – {@link MeleeAttack}: melee backup with 1-tile reach.</li>
 * </ul>
 *
 * <h2>Mana system</h2>
 * <p>Unlike other characters, the Wizzard has a finite mana pool that regenerates
 * passively at 5 mana per second. The template methods {@link #getMana()} and
 * {@link #spendMana(int)} from {@link PlayerCharacter} are overridden here to
 * connect the base class attack-check logic to the real mana pool.
 *
 * <h2>Armour</h2>
 * <p>Low armour ({@value #MAX_ARMOR}) compared to {@link Knight}, reflecting the
 * archetype's "glass cannon" role.
 */
public class Wizzard extends PlayerCharacter {

    /** Maximum armour value – Wizzard has low physical defense. */
    private static final int MAX_ARMOR = 30;

    private int mana;
    private int maxMana;
    private AnimationManager animationManager;

    /**
     * @param position initial world position; overwritten by Tiled spawn data when the level loads
     */
    public Wizzard(Vector2D position) {
        super("Wizzard", 7000, 40, 2.5f, position, MAX_ARMOR);
        this.mana = 100;
        this.maxMana = 100;
        this.gravityStrategy = new NormalGravity();
        initAnimations();
        Vector2D idleSize = animationManager.getFirstFrameSize("idle");
        this.hitbox.setSize(idleSize.getX(), idleSize.getY());
        primaryAttack   = new SpellAttack(6.0f, 100f, 20);   // SPACE - rychle kuzlo
        secondaryAttack = new MeleeAttack(1);                // V - melee zaloha
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/wizzard/wizzard.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",     0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",     0.1f);
        animationManager.addAnimation("attack", "ATTACK/ATTACK", 0.07f);
        animationManager.addAnimation("cast",   "CAST/CAST",     0.08f);
        animationManager.addAnimation("death",  "DEATH/DEATH",   0.3f, Animation.PlayMode.NORMAL);
        animationManager.addAnimation("hurt",   "HURT/HURT",     0.08f);
    }

    /**
     * Returns the Wizzard's current mana, used by the base class to gate spell attacks.
     */
    @Override
    protected int getMana() { return mana; }

    /**
     * Deducts {@code amount} from the mana pool, clamped to zero.
     *
     * @param amount mana points consumed by the attack
     */
    @Override
    protected void spendMana(int amount) {
        mana = Math.max(0, mana - amount);
    }

    /**
     * Passive mana regeneration: restores 5 mana per second, capped at {@link #maxMana}.
     *
     * @param ctx frame context; only {@code deltaTime} is used here
     */
    @Override
    public void update(UpdateContext ctx) {
        regenerateMana(ctx.deltaTime);
    }

    /**
     * Increases mana by {@code 5 * deltaTime}, capped at {@link #maxMana}.
     *
     * @param deltaTime elapsed time in seconds
     */
    private void regenerateMana(float deltaTime) {
        mana = Math.min(maxMana, mana + (int)(5 * deltaTime));
    }


    @Override
    public AnimationManager getAnimationManager() { return animationManager; }

    // -------------------------------------------------------------------------
    //  HUD data – publicly expose mana for SnapshotBuilder
    // -------------------------------------------------------------------------

    /** Returns current mana; used by HUD rendering. */
    @Override
    public int getCurrentMana() { return mana; }

    /** Returns maximum mana; used by HUD rendering. */
    @Override
    public int getMaxMana() { return maxMana; }
}
