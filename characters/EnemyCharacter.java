package sk.stuba.fiit.characters;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.attacks.Attack;
import sk.stuba.fiit.core.AIController;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public abstract class EnemyCharacter extends Character {
    protected float patrolRange;
    protected float detectionRange;
    protected Inventory inventory;
    private AIController aiController;

    // utok – nastavuju podtriedy v konstruktore (ako PlayerCharacter)
    protected Attack attack;

    private float attackCooldown             = 0f;
    private static final float ATTACK_COOLDOWN_MAX = 1.5f;

    // stav utocnej animacie
    protected boolean isAttacking        = false;
    private   float   attackAnimTimer    = 0f;
    private   float   attackAnimDuration = 0f;
    private   boolean damageDealt        = false;
    private   PlayerCharacter pendingTarget = null;
    private boolean lastMoveBlocked = false;

    /**
     * Zakladny konstruktor – armor = 0, maxArmor = 0.
     */
    public EnemyCharacter(String name, int hp, int attackPower, float speed,
                          Vector2D position, float patrolRange, float detectionRange) {
        this(name, hp, attackPower, speed, position, patrolRange, detectionRange, 0, 0);
    }

    /**
     * Rozsireny konstruktor s pevnou hodnotou brnenia.
     *
     * @param armor    pociatocne (a zaroven maximalne) brnenie nepriatela
     * @param maxArmor strop brnenia (zvycajne rovnaky ako armor)
     */
    public EnemyCharacter(String name, int hp, int attackPower, float speed,
                          Vector2D position, float patrolRange, float detectionRange,
                          int armor, int maxArmor) {
        super(name, hp, attackPower, speed, position, armor, maxArmor);
        this.patrolRange    = patrolRange;
        this.detectionRange = detectionRange;
        this.inventory      = new Inventory();
    }

    public boolean detectPlayer(PlayerCharacter player) {
        return position.distanceTo(player.getPosition()) <= detectionRange;
    }

    public void initAI(Vector2D patrolStart, Vector2D patrolEnd,
                       float attackRange, float preferredRange) {
        this.aiController = new AIController(
            this, patrolStart, patrolEnd, attackRange, preferredRange);
    }

    @Override
    public void move(Vector2D direction) {
        Level level = GameManager.getInstance().getCurrentLevel();

        if (level == null || level.getMapManager() == null) {
            position.setX(position.getX() + direction.getX());
            position.setY(position.getY() + direction.getY());
            updateHitbox();
            return;
        }

        if (direction.getX() == 0f && direction.getY() == 0f) {
            lastMoveBlocked = false;
            return;
        }

        float newX = position.getX() + direction.getX();
        float newY = position.getY() + direction.getY();
        lastMoveBlocked = false;

        // Test horizontálneho pohybu
        if (direction.getX() != 0f) {
            Rectangle testBox = new Rectangle(
                newX,
                position.getY(),
                hitbox.width,
                hitbox.height
            );
            for (Rectangle wall : level.getMapManager().getHitboxes()) {
                if (testBox.overlaps(wall)) {
                    float overlapX = Math.min(testBox.x + testBox.width, wall.x + wall.width)
                        - Math.max(testBox.x, wall.x);
                    float overlapY = Math.min(testBox.y + testBox.height, wall.y + wall.height)
                        - Math.max(testBox.y, wall.y);
                    if (overlapX < overlapY) {
                        // bočná stena – zablokuj horizontálny pohyb
                        newX = position.getX();
                        lastMoveBlocked = true;
                        break;
                    }
                }
            }
        }
        position.setX(newX);
        position.setY(newY);
        updateHitbox();
    }

    /**
     * Volana z AIController ked je hrac v ATTACK_RANGE.
     * Spusti utocnu animaciu; damage pride na KONCI animacie (posledny frame).
     */
    public void performAttack(PlayerCharacter player) {
        if (attackCooldown > 0 || isAttacking || attack == null) return;

        attackCooldown  = ATTACK_COOLDOWN_MAX;
        isAttacking     = true;
        damageDealt     = false;
        pendingTarget   = player;

        AnimationManager am = getAnimationManager();
        attackAnimDuration = attack.getAnimationDuration(am);
        attackAnimTimer    = attackAnimDuration;

        // spusti animaciu
        if (am != null) am.play(attack.getAnimationName());

        // spusti vlastnu logiku podtriedy (napr. DarkKnight prepinanie faz)
        performAttack();
    }

    protected String getAttackAnimationName() {
        return "attack";
    }

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
        //this.setHitboxSize(am.getAnimationSize(anim));
        am.play(anim);
        am.update(deltaTime);
    }

    @Override
    public void onCollision(Object other) {}

    @Override
    public void update(float deltaTime) {
        if (!isAlive()) {
            startDeathAnimation();
            updateDeathTimer(deltaTime);
            return;
        }
        attackCooldown -= deltaTime;

        if (isAttacking) {
            attackAnimTimer -= deltaTime;

            // damage na konci animacie (posledny frame)
            if (!damageDealt && attackAnimTimer <= 0f) {
                Level level = GameManager.getInstance().getCurrentLevel();
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

        applyGravity(deltaTime);

        if (aiController != null) {
            PlayerCharacter player = GameManager.getInstance()
                .getInventory().getActive();
            if (player != null) {
                aiController.update(deltaTime, player);
            }
        }
    }

    public boolean isAttacking() { return isAttacking; }
    public boolean wasLastMoveBlocked() { return lastMoveBlocked; }
}
