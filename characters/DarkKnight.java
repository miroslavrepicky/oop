package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import sk.stuba.fiit.attacks.*;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.NormalGravity;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Boss-tier enemy with two attack modes selected dynamically by distance.
 *
 * <h2>Attack selection strategy – distance-based</h2>
 * <p>When {@link #triggerAttack()} is called by the AI controller, DarkKnight
 * checks the current distance to the player:
 * <ul>
 *   <li><b>Close range</b> (≤ {@value #MELEE_THRESHOLD} px) → random attack from
 *       {@link #meleeAttacks}. Uses a wide sweep hitbox via {@link MeleeAttack}.</li>
 *   <li><b>Long range</b> (&gt; {@value #MELEE_THRESHOLD} px) → random attack from
 *       {@link #spellAttacks}. Can carry Fire or Freeze on-hit effects.</li>
 * </ul>
 * <p>This approach was chosen over health-threshold phases because it requires no
 * reconfiguration of {@code AIController} ranges and produces more reactive gameplay.
 *
 * <h2>Multi-hit melee</h2>
 * <p>The melee attack animation has 20 frames at 0.07 s/frame (total 1.4 s).
 * Hit-boxes are spawned at frames 4, 8, 13 and 17.  Each spawn is independent –
 * it creates a fresh {@link sk.stuba.fiit.projectiles.MeleeHitbox} so the player
 * can be hit up to four times per swing.
 *
 * <h2>Early animation cancellation</h2>
 * <p>At each scheduled hit frame, if the player is no longer within
 * {@value #MELEE_THRESHOLD} px, DarkKnight sets {@link #isAttacking}{@code = false}
 * and {@link #attackAnimTimer}{@code = 0}, immediately ending the animation.
 * All remaining hit frames are skipped.
 */
public class DarkKnight extends EnemyCharacter {

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final int   ARMOR           = 30;

    /**
     * Distance threshold (px) that determines attack type.
     * Below → melee swing; at or above → ranged spell.
     */
    private static final float MELEE_THRESHOLD = 130f;

    /**
     * 1-based frame indices within the 20-frame melee animation at which a
     * {@link sk.stuba.fiit.projectiles.MeleeHitbox} is spawned.
     */
    private static final int[] HIT_FRAME_NUMBERS = {4, 8, 13, 17};

    // ── Attack lists ──────────────────────────────────────────────────────────

    /** Attacks available in close-range mode. */
    final List<Attack> meleeAttacks = new ArrayList<>();

    /** Attacks available in long-range mode. */
    final List<Attack> spellAttacks = new ArrayList<>();

    // ── Runtime state ─────────────────────────────────────────────────────────

    private final Random rng = new Random();

    /** Last known player reference, updated every {@link #update(UpdateContext)}. */
    private PlayerCharacter lastKnownPlayer;

    /** True when the current attack was selected from {@link #meleeAttacks}. */
    private boolean usingMeleeAttack = false;

    /**
     * Which of the {@link #HIT_FRAME_NUMBERS} have already been fired this swing.
     * Reset in {@link #triggerAttack()} before each new swing.
     */
    private boolean[] hitsFired = new boolean[HIT_FRAME_NUMBERS.length];

    /**
     * Pre-computed {@code attackAnimTimer} thresholds for each hit frame.
     * Threshold[i] = total anim duration − HIT_FRAME_NUMBERS[i] × frame duration.
     * A hit fires the first time {@code attackAnimTimer} drops ≤ threshold[i].
     */
    private float[] hitThresholds = new float[HIT_FRAME_NUMBERS.length];

    private AnimationManager animationManager;

    // ── Constructor ───────────────────────────────────────────────────────────

    public DarkKnight(Vector2D position) {
        super("DarkKnight", 500, 50, 2.0f, position, 200f, 400f, ARMOR, ARMOR);
        this.gravityStrategy = new NormalGravity();
        initAnimations();

        Vector2D idleSize = animationManager.getFirstFrameSize("idle");
        this.hitbox.setSize(idleSize.getX(), idleSize.getY());

        // Melee: 2-tile (~104 px) and 3-tile (~156 px) swings
        meleeAttacks.add(new MeleeAttack(2));
        meleeAttacks.add(new MeleeAttack(3));

        // Spells: plain, fire-DoT, freeze-slow, and combined
        meleeAttacks.add(new MeleeAttack(2)); // 3rd variant for more weight on 2-tile

        spellAttacks.add(new SpellAttack(5.0f, 0f, 0));
        spellAttacks.add(new FireDecorator(new SpellAttack(5.0f, 0f, 0)));
        spellAttacks.add(new FreezeDecorator(new SpellAttack(5.0f, 0f, 0)));
        spellAttacks.add(new FreezeDecorator(new FireDecorator(new SpellAttack(5.0f, 0f, 0))));

        // Required non-null initial value for the parent's null guard
        this.attack = meleeAttacks.get(0);
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/dark_knight/dark_knight.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",     0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",     0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",     0.1f);
        // 20 frames × 0.07 s = 1.4 s total melee animation
        animationManager.addAnimation("attack", "ATTACK/ATTACK", 0.07f);
        // Spell / cast animation
        animationManager.addAnimation("cast",   "PRAY/PRAY",     0.07f);
        animationManager.addAnimation("death",  "DEATH/DEATH",   0.3f, Animation.PlayMode.NORMAL);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Caches the player reference for use in {@link #triggerAttack()} and
     * {@link #dealAttackDamage(UpdateContext)}, then delegates to the parent.
     */
    @Override
    public void update(UpdateContext ctx) {
        if (ctx.player != null) {
            lastKnownPlayer = ctx.player;
        }
        super.update(ctx); // gravity, AI controller (→ triggerAttack), animation
    }

    // ── Attack selection ──────────────────────────────────────────────────────

    /**
     * Selects the attack type based on current distance to the player, initialises
     * the multi-hit tracking arrays for melee, then delegates to the parent's
     * cooldown logic and animation trigger.
     */
    @Override
    public void triggerAttack() {
        if (isAttacking) return; // guard against re-entry (also checked by super)

        // ── Choose attack ──────────────────────────────────────────────────
        if (lastKnownPlayer != null) {
            double dist = position.distanceTo(lastKnownPlayer.getPosition());
            if (dist <= MELEE_THRESHOLD) {
                attack = meleeAttacks.get(rng.nextInt(meleeAttacks.size()));
                usingMeleeAttack = true;
            } else {
                attack = spellAttacks.get(rng.nextInt(spellAttacks.size()));
                usingMeleeAttack = false;
            }
        } else {
            attack = meleeAttacks.get(0);
            usingMeleeAttack = true;
        }

        // ── Reset multi-hit tracking for melee ────────────────────────────
        if (usingMeleeAttack) {
            hitsFired = new boolean[HIT_FRAME_NUMBERS.length];
            computeHitThresholds();
        }

        super.triggerAttack(); // sets isAttacking, resets damageDealt, plays anim
    }

    /**
     * Pre-computes {@link #hitThresholds} from the "attack" animation metadata.
     * Called once per swing start so frame timings are always in sync with the
     * actual atlas, even if frameDuration changes in the future.
     */
    private void computeHitThresholds() {
        AnimationManager am = getAnimationManager();
        float totalDuration = (am != null && am.hasAnimation("attack"))
            ? am.getAnimationDuration("attack") : 1.4f;
        int   totalFrames   = (am != null && am.hasAnimation("attack"))
            ? am.getFrameCount("attack") : 20;
        float frameDuration = totalDuration / totalFrames;

        for (int i = 0; i < HIT_FRAME_NUMBERS.length; i++) {
            // time remaining in the countdown timer when frame N is first shown
            hitThresholds[i] = totalDuration - HIT_FRAME_NUMBERS[i] * frameDuration;
        }
    }

    // ── Damage-dealing hook ───────────────────────────────────────────────────

    /**
     * For <b>spell</b> attacks delegates to the standard single-hit logic.
     *
     * <p>For <b>melee</b> attacks fires a {@link sk.stuba.fiit.projectiles.MeleeHitbox}
     * at each of the four scheduled frames (4, 8, 13, 17).  At each scheduled frame,
     * if the player is no longer within {@value #MELEE_THRESHOLD} px the remaining
     * animation is cancelled immediately.
     */
    @Override
    protected void dealAttackDamage(UpdateContext ctx) {
        if (!usingMeleeAttack) {
            super.dealAttackDamage(ctx); // single-hit at end of anim
            return;
        }

        if (ctx.level == null) return;

        boolean allFired = true;

        for (int i = 0; i < HIT_FRAME_NUMBERS.length; i++) {
            if (hitsFired[i]) continue;

            allFired = false; // at least one hit still pending

            if (attackAnimTimer > hitThresholds[i]) continue; // not yet this frame

            // ── Time to fire hit i ──────────────────────────────────────────
            if (isPlayerInMeleeReach()) {
                attack.execute(this, ctx.level);
                hitsFired[i] = true;
            } else {
                // Player left range – cancel animation and skip remaining hits
                isAttacking     = false;
                attackAnimTimer = 0f;
                for (int j = i; j < HIT_FRAME_NUMBERS.length; j++) {
                    hitsFired[j] = true; // mark remaining as done so the loop exits
                }
                break;
            }
        }

        if (allFired) {
            damageDealt = true; // tell parent no further processing needed
        }
    }

    /**
     * Returns {@code true} when the last known player position is within melee reach.
     */
    private boolean isPlayerInMeleeReach() {
        return lastKnownPlayer != null
            && lastKnownPlayer.isAlive()
            && position.distanceTo(lastKnownPlayer.getPosition()) <= MELEE_THRESHOLD;
    }

    // ── Animation name ────────────────────────────────────────────────────────

    /**
     * Routes to "attack" for melee or "cast" for spells so that
     * {@link EnemyCharacter#updateAnimation(float)} uses the correct clip.
     */
    @Override
    protected String getAttackAnimationName() {
        return usingMeleeAttack ? "attack" : "cast";
    }

    @Override
    public AnimationManager getAnimationManager() { return animationManager; }
}
