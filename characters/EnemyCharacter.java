package sk.stuba.fiit.characters;

import sk.stuba.fiit.attacks.Attack;
import sk.stuba.fiit.core.engine.AIControllable;
import sk.stuba.fiit.core.AIController;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.MovementResolver;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;

/**
 * Base class for all AI-controlled enemy characters.
 *
 * <p>On-hit effects (DOT, slow) received from projectiles are ticked each frame
 * via {@link #tickEffects(float)} inherited from {@link Character}.
 *
 * <p>Attack timing hook: the single-hit damage logic is isolated in
 * {@link #dealAttackDamage(UpdateContext)}, which subclasses can override to
 * implement multi-hit or frame-specific attack timing (e.g. {@link DarkKnight}).
 */
public abstract class EnemyCharacter extends Character implements AIControllable {
    protected float     patrolRange;
    protected float     detectionRange;
    protected Inventory inventory;
    private   AIController     aiController;
    private   MovementResolver movementResolver;

    protected Attack attack;

    // ── Attack state ─────────────────────────────────────────────────────────
    // Fields are protected so subclasses (DarkKnight) can read/write them
    // for multi-hit timing and early animation cancellation.
    protected float   attackCooldown              = 0f;
    private static final float ATTACK_COOLDOWN_MAX = 1.5f;

    protected boolean isAttacking     = false;
    protected float   attackAnimTimer = 0f;
    /** Set to {@code true} once damage has been dealt for this attack swing. */
    protected boolean damageDealt     = false;

    private   boolean lastMoveBlocked = false;

    public EnemyCharacter(String name, int hp, int attackPower, float speed,
                          Vector2D position, float patrolRange, float detectionRange) {
        this(name, hp, attackPower, speed, position, patrolRange, detectionRange, 0, 0);
    }

    public EnemyCharacter(String name, int hp, int attackPower, float speed,
                          Vector2D position, float patrolRange, float detectionRange,
                          int armor, int maxArmor) {
        super(name, hp, attackPower, speed, position, armor, maxArmor);
        this.patrolRange    = patrolRange;
        this.detectionRange = detectionRange;
        this.inventory      = new Inventory();
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

    public void initAI(Vector2D patrolStart, Vector2D patrolEnd,
                       float attackRange, float preferredRange) {
        this.aiController = new AIController(
            this, patrolStart, patrolEnd, attackRange, preferredRange);
    }

    public void setMovementResolver(MovementResolver resolver) {
        this.movementResolver = resolver;
    }

    // -------------------------------------------------------------------------
    //  Movement
    // -------------------------------------------------------------------------

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
     * Starts an attack sequence: sets cooldown, marks {@code isAttacking},
     * and plays the attack animation.
     *
     * <p>Subclasses must set {@link #attack} before calling {@code super.triggerAttack()}.
     * The actual projectile / hit-box is spawned later in {@link #dealAttackDamage(UpdateContext)}.
     */
    @Override
    public void triggerAttack() {
        if (attackCooldown > 0 || isAttacking || attack == null) return;

        attackCooldown  = ATTACK_COOLDOWN_MAX;
        isAttacking     = true;
        damageDealt     = false;

        AnimationManager am = getAnimationManager();
        attackAnimTimer = attack.getAnimationDuration(am);
        if (am != null) am.play(attack.getAnimationName());
    }

    protected String getAttackAnimationName() { return "attack"; }

    // -------------------------------------------------------------------------
    //  Damage-dealing hook
    // -------------------------------------------------------------------------

    /**
     * Called every frame while {@link #isAttacking} is {@code true}.
     * Default behaviour: execute the attack once, near the end of the animation
     * (within the last two animation frames).
     *
     * <p>Subclasses may override this to implement:
     * <ul>
     *   <li>Multi-hit at specific frame numbers (see {@link DarkKnight}).</li>
     *   <li>Early cancellation when the player leaves range mid-swing.</li>
     * </ul>
     *
     * @param ctx current frame context (contains {@code level} for spawning hitboxes)
     */
    protected void dealAttackDamage(UpdateContext ctx) {
        if (damageDealt || attack == null) return;
        AnimationManager am = getAnimationManager();
        float frameDuration = attack.getFrameDuration(am);
        if (attackAnimTimer <= frameDuration * 2) {
            if (ctx.level != null) {
                attack.execute(this, ctx.level);
            }
            damageDealt = true;
        }
    }

    // -------------------------------------------------------------------------
    //  Update
    // -------------------------------------------------------------------------

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
            dealAttackDamage(ctx);          // polymorphic – DarkKnight overrides this
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

    public boolean isAttacking() { return isAttacking; }
}
