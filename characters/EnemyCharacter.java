package sk.stuba.fiit.characters;

import org.slf4j.Logger;
import sk.stuba.fiit.attacks.Attack;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.engine.AIControllable;
import sk.stuba.fiit.core.AIController;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.MovementResolver;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

/**
 * Base class for all AI-controlled enemy characters.
 *
 * <h2>On-hit effects</h2>
 * <p>DOT and slow effects received from player projectiles are ticked each frame via
 * {@link #tickEffects(float)} inherited from {@link Character}.
 *
 * <h2>Attack timing hook</h2>
 * <p>Single-hit damage logic is isolated in {@link #dealAttackDamage(UpdateContext)}.
 * Subclasses override this method to implement multi-hit or frame-specific attack
 * timing (e.g. {@link DarkKnight}).
 *
 * <h2>Movement collision</h2>
 * <p>Horizontal movement is resolved against the map's wall hitboxes through a
 * {@link MovementResolver} set via {@link #setMovementResolver(MovementResolver)}.
 * The {@link #wasLastMoveBlocked()} flag is read by {@link AIController} to decide
 * when to jump over obstacles or reverse a patrol direction.
 */
public abstract class EnemyCharacter extends Character implements AIControllable {
    private static final Logger log = GameLogger.get(EnemyCharacter.class);

    protected float     patrolRange;
    protected float     detectionRange;
    private   AIController     aiController;
    private   MovementResolver movementResolver;

    /** The attack strategy used by this enemy. Must be set before the first {@link #triggerAttack()}. */
    protected Attack attack;

    //  Attack state
    /** Remaining cooldown in seconds before the next attack may be triggered. */
    protected float   attackCooldown              = 0f;
    private static final float ATTACK_COOLDOWN_MAX = 1.5f;

    /** {@code true} while an attack animation is playing. */
    protected boolean isAttacking     = false;

    /** Countdown (in seconds) remaining in the current attack animation. */
    protected float   attackAnimTimer = 0f;

    /** Set to {@code true} once damage has been dealt for this attack swing. */
    protected boolean damageDealt     = false;

    private   boolean lastMoveBlocked = false;

    /**
     * Constructor without armour.
     *
     * @param name            display name
     * @param hp              starting and maximum HP
     * @param attackPower     base damage value
     * @param speed           movement speed in world units per second
     * @param position        initial world position
     * @param patrolRange     half-width of the default patrol route in world units
     * @param detectionRange  maximum distance at which the player is detected
     */
    public EnemyCharacter(String name, int hp, int attackPower, float speed,
                          Vector2D position, float patrolRange, float detectionRange) {
        this(name, hp, attackPower, speed, position, patrolRange, detectionRange, 0, 0);
    }

    /**
     * @param name            display name
     * @param hp              starting and maximum HP
     * @param attackPower     base damage value forwarded to attack strategies
     * @param speed           movement speed in world units per second
     * @param position        initial world position
     * @param patrolRange     half-width of the default patrol route in world units
     * @param detectionRange  maximum distance at which the player is detected
     * @param armor           starting armour value
     * @param maxArmor        maximum armour value
     */
    public EnemyCharacter(String name, int hp, int attackPower, float speed,
                          Vector2D position, float patrolRange, float detectionRange,
                          int armor, int maxArmor) {
        super(name, hp, attackPower, speed, position, armor, maxArmor);
        this.patrolRange    = patrolRange;
        this.detectionRange = detectionRange;
    }

    // -------------------------------------------------------------------------
    //  AIControllable
    // -------------------------------------------------------------------------

    @Override
    public boolean detectPlayer(PlayerCharacter player) {
        return position.distanceTo(player.getPosition()) <= detectionRange;
    }

    @Override
    public boolean wasLastMoveBlocked() { return lastMoveBlocked; }

    /**
     * Initializes the {@link AIController} with patrol boundaries and combat ranges.
     *
     * @param patrolStart   left the patrol waypoint
     * @param patrolEnd     right patrol waypoint
     * @param attackRange   distance at which the AI starts attacking
     * @param preferredRange ideal combat distance (ranged enemies keep this gap)
     */
    public void initAI(Vector2D patrolStart, Vector2D patrolEnd,
                       float attackRange, float preferredRange) {
        this.aiController = new AIController(
            this, patrolStart, patrolEnd, attackRange, preferredRange);
    }

    /**
     * Sets the {@link MovementResolver} used to resolve horizontal wall collisions.
     * Must be called after the map is loaded so the resolver has access to wall hitboxes.
     *
     * @param resolver resolver configured with the current map's hitboxes
     */
    public void setMovementResolver(MovementResolver resolver) {
        this.movementResolver = resolver;
    }

    // -------------------------------------------------------------------------
    //  Movement
    // -------------------------------------------------------------------------

    /**
     * Moves the enemy by {@code direction}, resolving horizontal wall collisions if a
     * {@link MovementResolver} is configured. Updates {@link #lastMoveBlocked}.
     *
     * @param direction desired displacement for this frame
     */
    @Override
    public void move(Vector2D direction) {
        float dx = direction.getX();
        float dy = direction.getY();

        if (movementResolver != null && dx != 0f) {
            float allowed   = movementResolver.resolveX(hitbox, dx);
            lastMoveBlocked = (allowed == 0f);
            dx              = allowed;
        } else {
            lastMoveBlocked = false;
        }

        position.setX(position.getX() + dx);
        position.setY(position.getY() + dy);
        updateHitbox();
    }

    // -------------------------------------------------------------------------
    //  Attack trigger
    // -------------------------------------------------------------------------

    /**
     * Starts an attack sequence: sets the cooldown, marks {@code isAttacking},
     * and plays the attack animation.
     *
     * <p>Subclasses must set {@link #attack} before calling {@code super.triggerAttack()}.
     * The actual projectile or hitbox is spawned later in {@link #dealAttackDamage(UpdateContext)}.
     * Guard conditions: the attack is skipped if the cooldown is active, another attack is
     * already in progress, or no attack strategy has been assigned.
     */
    @Override
    public void triggerAttack() {
        if (attack == null) {
            log.warn("triggerAttack called but no attack strategy assigned: enemy={}", name);
            return;
        }
        if (attackCooldown > 0f) {
            if (log.isDebugEnabled()) {
                log.debug("triggerAttack skipped – cooldown active: enemy={}, cooldown={}",
                    name, String.format("%.2f", attackCooldown));
            }
            return;
        }
        if (isAttacking) {
            if (log.isDebugEnabled()) {
                log.debug("triggerAttack skipped – attack already in progress: enemy={}", name);
            }
            return;
        }

        attackCooldown  = ATTACK_COOLDOWN_MAX;
        isAttacking     = true;
        damageDealt     = false;

        AnimationManager am = getAnimationManager();
        attackAnimTimer = attack.getAnimationDuration(am);
        if (am != null) am.play(attack.getAnimationName());
        log.info("Attack triggered: enemy={}, attack={}, animDuration={}",
            name, attack.getAnimationName(),
            String.format("%.2f", attackAnimTimer));
    }

    /**
     * Returns the animation name used for the current attack.
     * Override in subclasses that have multiple attack animations (e.g. {@link DarkKnight}).
     *
     * @return animation key understood by {@link AnimationManager}; default is {@code "attack"}
     */
    protected String getAttackAnimationName() { return "attack"; }

    // -------------------------------------------------------------------------
    //  Damage-dealing hook
    // -------------------------------------------------------------------------

    /**
     * Called every frame while {@link #isAttacking} is {@code true}.
     *
     * <p>Default behavior: executes the attack once, near the end of the animation
     * (within the last two animation frames). Subclasses may override this to:
     * <ul>
     *   <li>Spawn hitboxes at multiple specific frame numbers (see {@link DarkKnight}).</li>
     *   <li>Cancel the animation early when the player leaves range mid-swing.</li>
     * </ul>
     *
     * @param ctx current frame context; {@code ctx.level} is used to spawn hitboxes
     */
    protected void dealAttackDamage(UpdateContext ctx) {
        if (damageDealt || attack == null) return;
        AnimationManager am = getAnimationManager();
        float frameDuration = attack.getFrameDuration(am);
        if (attackAnimTimer <= frameDuration * 2) {
            if (ctx.level != null) {
                attack.execute(this, ctx.level);
            }else {
                log.warn("dealAttackDamage: level is null, skipping projectile spawn: enemy={}", name);
            }
            damageDealt = true;
        }
    }

    // -------------------------------------------------------------------------
    //  Update
    // -------------------------------------------------------------------------
    /**
     * Advances the enemy for one frame:
     * <ol>
     *   <li>If dead: plays the death animation and exits early.</li>
     *   <li>Decrements the attack cooldown.</li>
     *   <li>If attacking: decrements the animation timer and calls {@link #dealAttackDamage}.</li>
     *   <li>Applies gravity and ticks active effects.</li>
     *   <li>Delegates to {@link AIController#update(float, PlayerCharacter)} for state-machine logic.</li>
     *   <li>Updates the animation state.</li>
     * </ol>
     *
     * @param ctx frame context including {@code deltaTime}, platforms, level, and the active player
     */
    @Override
    public void update(UpdateContext ctx) {
        if (!isAlive()) {
            startDeathAnimation();
            updateDeathTimer(ctx.deltaTime);
            updateAnimation(ctx.deltaTime);
            return;
        }

        attackCooldown -= ctx.deltaTime;

        if (isAttacking) {
            attackAnimTimer -= ctx.deltaTime;
            dealAttackDamage(ctx);
            if (attackAnimTimer <= 0f) {
                isAttacking = false;
            }
        }

        applyGravity(ctx.deltaTime, ctx.platforms);
        tickEffects(ctx.deltaTime);

        if (aiController != null && ctx.player != null) {
            aiController.update(ctx.deltaTime, ctx.player);
        }
        updateAnimation(ctx.deltaTime);
    }

    // -------------------------------------------------------------------------
    //  Animation
    // -------------------------------------------------------------------------
    /**
     * Selects and plays the appropriate animation clip based on the current state:
     * {@code "death"}, {@code "attack"}/{@code "cast"}, {@code "jump"}, {@code "walk"},
     * or {@code "idle"}.
     *
     * @param deltaTime elapsed time in seconds
     */
    @Override
    public void updateAnimation(float deltaTime) {
        AnimationManager am = getAnimationManager();
        if (am == null) return;

        String anim;
        if (!isAlive()) {
            anim = "death";
        } else if (isAttacking()) {
            anim = getAttackAnimationName();
        } else if (!isOnGround()) {
            anim = am.hasAnimation("jump") ? "jump" : "idle";
        } else if (Math.abs(getVelocityX()) > 0.1f) {
            anim = "walk";
        } else {
            anim = "idle";
        }
        am.play(anim);
        am.update(deltaTime);
    }

    @Override
    public void onCollision(Object other) {}

    /** Returns {@code true} while an attack animation is playing. */
    public boolean isAttacking() { return isAttacking; }
}
