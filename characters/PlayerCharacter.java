package sk.stuba.fiit.characters;

import sk.stuba.fiit.attacks.Attack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Base class for all player-controlled characters.
 *
 * <h2>Attack lifecycle</h2>
 * <p>An attack is initiated via {@link #executeAttack(Attack)}, which:
 * <ol>
 *   <li>Checks that no other attack is in progress.</li>
 *   <li>Verifies that the character has enough mana (delegated to {@link #getMana()}).</li>
 *   <li>Spends mana via {@link #spendMana(int)}.</li>
 *   <li>Records the attack and starts the animation timer.</li>
 * </ol>
 * The actual projectile or hit-box is spawned inside {@link #updateAnimation(UpdateContext)}
 * at the correct animation frame (near the end of the animation), not at initiation time.
 *
 * <h2>Mana – template method</h2>
 * <p>The default implementation treats mana as unlimited ({@link Integer#MAX_VALUE}).
 * {@link Wizzard} overrides {@link #getMana()} and {@link #spendMana(int)} to use real
 * mana values, without requiring any changes to this base class.
 *
 * <h2>Physics</h2>
 * <p>Gravity is applied by {@code PlayerController}, which passes the platform list
 * directly – this class does not call {@code GameManager} for physics data.
 */
public abstract class PlayerCharacter extends Character {
    protected Attack primaryAttack;
    protected Attack secondaryAttack;
    protected boolean isAttacking       = false;
    protected float   attackAnimTimer   = 0f;
    protected Attack  currentAttack     = null;

    /** Guards against spawning the projectile more than once per attack swing. */
    protected boolean projectileSpawned = false;

    public PlayerCharacter(String name, int hp, int attackPower,
                           float speed, Vector2D position) {
        this(name, hp, attackPower, speed, position, 0);
    }

    /**
     * @param name      display name
     * @param hp        starting and maximum HP
     * @param attackPower base damage forwarded to attack strategies
     * @param speed     movement speed in world units per second
     * @param position  initial world position
     * @param maxArmor  maximum armour value (starting armour is 0)
     */
    public PlayerCharacter(String name, int hp, int attackPower, float speed,
                           Vector2D position, int maxArmor) {
        super(name, hp, attackPower, speed, position, 0, maxArmor);
    }

    // --- mana – default prázdna implementácia, Wizzard override-ne ---
    /**
     * Returns the character's current mana. Default: {@link Integer#MAX_VALUE} (unlimited).
     *
     * @return current mana points
     */
    protected int  getMana()             { return Integer.MAX_VALUE; }

    /**
     * Deducts {@code amount} mana points. Default: no-op (unlimited mana).
     *
     * @param amount mana points to spend
     */
    protected void spendMana(int amount) { }

    /**
     * Attempts to execute an attack. Silently ignored when:
     * <ul>
     *   <li>{@code attack} is {@code null}.</li>
     *   <li>Another attack is already in progress.</li>
     *   <li>The character has insufficient mana.</li>
     *   <li>No level is currently loaded.</li>
     * </ul>
     *
     * @param attack the attack strategy to execute
     */
    protected void executeAttack(Attack attack) {
        if (attack == null || isAttacking) {
            return;
        }

        int cost = attack.getManaCost();
        if (getMana() < cost) return;
        spendMana(cost);

        Level level = GameManager.getInstance().getCurrentLevel();
        if (level == null) return;

        AnimationManager am = getAnimationManager();
        if (am != null) {
            isAttacking       = true;
            currentAttack     = attack;
            attackAnimTimer   = attack.getAnimationDuration(am);
            projectileSpawned = false;
        }
    }

    /** Initiates the primary attack (typically mapped to {@code SPACE}). */
    public void performPrimaryAttack()   { executeAttack(primaryAttack); }

    /** Initiates the secondary attack (typically mapped to {@code V}). */
    public void performSecondaryAttack() { executeAttack(secondaryAttack); }

    /**
     * Drives the attack animation frame-by-frame.
     *
     * <ul>
     *   <li>If the character is dead: starts and ticks the death animation, then
     *       switches to the next living party member when the animation finishes.</li>
     *   <li>While attacking: spawns the projectile/hitbox at the correct frame (within
     *       the last three frame-durations), then counts down the animation timer.</li>
     *   <li>When not attacking: selects {@code "jump"}, {@code "walk"}, or {@code "idle"}
     *       based on the character's current movement state.</li>
     * </ul>
     *
     * @param ctx frame context including {@code deltaTime}, {@code level}, and {@code inventory}
     */
    public void updateAnimation(UpdateContext ctx) {
        if (getAnimationManager() == null) return;

        if (!isAlive()) {
            startDeathAnimation();
            updateDeathTimer(ctx.deltaTime);
            getAnimationManager().update(ctx.deltaTime);

            if (isDeathAnimationDone()) {
                if (ctx.inventory.getActive() == this && !ctx.inventory.switchToNextAlive()) {
                    // No living characters remain – PlayingState will detect isPartyDefeated().
                }
            }
            return;
        }

        // Spawn the projectile near the end of the attack animation.
        if (!projectileSpawned && currentAttack != null) {
            float frameDuration = currentAttack.getFrameDuration(getAnimationManager());
            if (attackAnimTimer <= frameDuration * 3) {
                currentAttack.execute(this, ctx.level);
                projectileSpawned = true;
            }
        }

        if (isAttacking) {
            attackAnimTimer -= ctx.deltaTime;
            if (attackAnimTimer <= 0f) isAttacking = false;
        }

        String anim;
        if (isAttacking) {
            anim = currentAttack != null ? currentAttack.getAnimationName() : "attack";
        } else if (!isOnGround()) {
            anim = hasAnimation("jump") ? "jump" : "idle";
        } else if (Math.abs(velocityX) > 0.1f) {
            anim = "walk";
        } else {
            anim = "idle";
        }

        getAnimationManager().play(anim);
        getAnimationManager().update(ctx.deltaTime);
    }

    /**
     * Returns {@code true} if the character's animation manager has an animation
     * registered under the given name.
     *
     * @param name the animation key to check
     * @return {@code true} when the animation exists
     */
    protected boolean hasAnimation(String name) {
        return getAnimationManager() != null && getAnimationManager().hasAnimation(name);
    }

    /**
     * Returns the shared {@link Inventory} via {@link GameManager}.
     * Items call this to remove themselves after use.
     */
    public Inventory getInventory() {
        return GameManager.getInstance().getInventory();
    }

    @Override
    public void move(Vector2D direction) {
        position = position.add(direction);
        updateHitbox();
    }

    @Override
    public void onCollision(Object other) { }
}
