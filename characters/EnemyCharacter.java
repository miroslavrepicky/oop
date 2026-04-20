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
 */
public abstract class EnemyCharacter extends Character implements AIControllable {
    protected float     patrolRange;
    protected float     detectionRange;
    protected Inventory inventory;
    private   AIController     aiController;
    private   MovementResolver movementResolver;

    protected Attack attack;

    private float   attackCooldown              = 0f;
    private static final float ATTACK_COOLDOWN_MAX = 1.5f;

    protected boolean isAttacking     = false;
    private   float   attackAnimTimer = 0f;
    private   boolean damageDealt     = false;
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
    //  Attack
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    //  Attack trigger – volaný z AIController, žiadny player parameter
    // -------------------------------------------------------------------------

    /**
     * Naštartuje útočnú sekvenciu: cooldown, animácia, timer.
     * Skutočné poškodenie/projektil vznikne neskôr v {@link #update(UpdateContext)}
     * cez {@code attack.execute(this, level)}.
     *
     * <p>Podtriedy môžu override-núť pre typ-špecifickú logiku pred spustením
     * (napr. {@code EnemyArcher} odpočítava šípy) a musia zavolať {@code super.triggerAttack()}.
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

            if (!damageDealt && attack != null) {
                AnimationManager am = getAnimationManager();
                float frameDuration = attack.getFrameDuration(am);
                if (attackAnimTimer <= frameDuration * 2) {
                    if (ctx.level != null) {
                        attack.execute(this, ctx.level);
                    }
                    damageDealt = true;
                }
            }

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
