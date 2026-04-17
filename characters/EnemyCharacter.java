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
 * Základná trieda pre všetkých nepriateľov.
 *
 * Update logika prešla na {@link #update(UpdateContext)} –
 * všetky kontextové dáta (platformy, level, hráč) sú v {@code ctx}.
 * Trieda nemusí volať {@code GameManager} vôbec.
 */
public abstract class EnemyCharacter extends Character implements AIControllable {
    protected float patrolRange;
    protected float detectionRange;
    protected Inventory inventory;
    private AIController aiController;
    private MovementResolver movementResolver;

    protected Attack attack;

    private float attackCooldown             = 0f;
    private static final float ATTACK_COOLDOWN_MAX = 1.5f;

    protected boolean isAttacking        = false;
    private   float   attackAnimTimer    = 0f;
    private   float   attackAnimDuration = 0f;
    private   boolean damageDealt        = false;
    private   PlayerCharacter pendingTarget = null;
    private   boolean lastMoveBlocked    = false;

    // -------------------------------------------------------------------------
    //  Konštruktory
    // -------------------------------------------------------------------------

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
    //  AIControllable implementácia
    // -------------------------------------------------------------------------

    @Override
    public boolean detectPlayer(PlayerCharacter player) {
        return position.distanceTo(player.getPosition()) <= detectionRange;
    }

    @Override
    public boolean wasLastMoveBlocked() { return lastMoveBlocked; }

    // -------------------------------------------------------------------------
    //  AI inicializácia
    // -------------------------------------------------------------------------

    public void initAI(Vector2D patrolStart, Vector2D patrolEnd,
                       float attackRange, float preferredRange) {
        this.aiController = new AIController(
            this, patrolStart, patrolEnd, attackRange, preferredRange);
    }

    public void setMovementResolver(MovementResolver resolver) {
        this.movementResolver = resolver;
    }

    // -------------------------------------------------------------------------
    //  Pohyb
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
    //  Útok
    // -------------------------------------------------------------------------

    @Override
    public void performAttack(PlayerCharacter player) {
        if (attackCooldown > 0 || isAttacking || attack == null) return;

        attackCooldown    = ATTACK_COOLDOWN_MAX;
        isAttacking       = true;
        damageDealt       = false;
        pendingTarget     = player;

        AnimationManager am = getAnimationManager();
        attackAnimDuration  = attack.getAnimationDuration(am);
        attackAnimTimer     = attackAnimDuration;

        if (am != null) am.play(attack.getAnimationName());

        performAttack();
    }

    protected String getAttackAnimationName() { return "attack"; }

    // -------------------------------------------------------------------------
    //  Updatable – hlavná update metóda
    // -------------------------------------------------------------------------

    /**
     * Všetky potrebné dáta sú v {@code ctx}.
     * Nepriateľ si zoberie {@code ctx.platforms} pre gravitáciu,
     * {@code ctx.level} pre útok a {@code ctx.player} pre AI.
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

            if (!damageDealt && attack != null) {
                AnimationManager am = getAnimationManager();
                float frameDuration = attack.getFrameDuration(am);
                if (attackAnimTimer <= frameDuration * 2) {   // ← predposledný frame
                    if (ctx.level != null) {
                        attack.execute(this, ctx.level);
                    }
                    damageDealt = true;
                }
            }

            if (attackAnimTimer <= 0f) {
                isAttacking   = false;
                pendingTarget = null;
            }
        }

        applyGravity(ctx.deltaTime, ctx.platforms);

        if (aiController != null && ctx.player != null) {
            aiController.update(ctx.deltaTime, ctx.player);
        }
        updateAnimation(ctx.deltaTime);
    }

    // -------------------------------------------------------------------------
    //  Animácia
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
