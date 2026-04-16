package sk.stuba.fiit.characters;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.attacks.Attack;
import sk.stuba.fiit.core.AIControllable;
import sk.stuba.fiit.core.AIController;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.MovementResolver;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.util.Collections;
import java.util.List;

/**
 * Základná trieda pre všetkých nepriateľov.
 *
 * Implementuje {@link AIControllable} – {@link AIController} závisí
 * len od tohto interface, nie od tejto konkrétnej triedy.
 *
 * Zmeny oproti pôvodnému kódu:
 *  - {@code update(float)} dostáva {@code Level} ako parameter namiesto
 *    volania {@code GameManager.getInstance().getCurrentLevel()}
 *  - gravitácia dostáva platformy priamo, nie cez GameManager
 *  - {@code AIController} pracuje cez {@code AIControllable} interface
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
    //  Útok (volaný z AIController cez AIControllable.performAttack)
    // -------------------------------------------------------------------------

    /**
     * Spustí útočnú animáciu; skutočný damage príde na konci animácie
     * v {@link #update(float, List, Level, PlayerCharacter)}.
     */
    @Override
    public void performAttack(PlayerCharacter player) {
        if (attackCooldown > 0 || isAttacking || attack == null) return;

        attackCooldown     = ATTACK_COOLDOWN_MAX;
        isAttacking        = true;
        damageDealt        = false;
        pendingTarget      = player;

        AnimationManager am = getAnimationManager();
        attackAnimDuration  = attack.getAnimationDuration(am);
        attackAnimTimer     = attackAnimDuration;

        if (am != null) am.play(attack.getAnimationName());

        performAttack(); // hook pre podtriedy (napr. DarkKnight zmena fázy)
    }

    protected String getAttackAnimationName() { return "attack"; }

    // -------------------------------------------------------------------------
    //  Update – Level predaný ako parameter, nie cez GameManager
    // -------------------------------------------------------------------------

    /**
     * Hlavný update. {@code Level} je predaný zvonku (z {@code Level.update()})
     * – EnemyCharacter nemusí volať {@code GameManager.getInstance()}.
     *
     * @param deltaTime čas od posledného framu
     * @param platforms kolízne obdĺžniky mapy pre gravitáciu
     * @param level     aktuálny level (potrebný pre útok)
     * @param player    aktívny hráč (potrebný pre AI)
     */
    public void update(float deltaTime, List<Rectangle> platforms,
                       Level level, PlayerCharacter player) {
        if (!isAlive()) {
            startDeathAnimation();
            updateDeathTimer(deltaTime);
            return;
        }

        attackCooldown -= deltaTime;

        if (isAttacking) {
            attackAnimTimer -= deltaTime;

            if (!damageDealt && attackAnimTimer <= 0f) {
                if (level != null && attack != null) {
                    attack.execute(this, level);
                }
                damageDealt = true;
            }

            if (attackAnimTimer <= 0f) {
                isAttacking   = false;
                pendingTarget = null;
            }
        }

        // Gravitácia – platformy predané priamo, bez GameManager
        applyGravity(deltaTime, platforms);

        if (aiController != null && player != null) {
            aiController.update(deltaTime, player);
        }
    }

    /**
     * Spätne kompatibilný update – používa GameManager ak ešte niekto volá
     * starý podpis. Interne deleguje na nový update.
     *
     * @deprecated Použi {@link #update(float, List, Level, PlayerCharacter)}
     */
    @Deprecated
    @Override
    public void update(float deltaTime) {
        Level level = GameManager.getInstance().getCurrentLevel();
        PlayerCharacter player = GameManager.getInstance().getInventory().getActive();
        List<Rectangle> platforms = (level != null && level.getMapManager() != null)
            ? level.getMapManager().getHitboxes()
            : Collections.emptyList();
        update(deltaTime, platforms, level, player);
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
