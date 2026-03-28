package sk.stuba.fiit.characters;

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

    // útok – nastavujú podtriedy v konštruktore (ako PlayerCharacter)
    protected Attack attack;

    private float attackCooldown             = 0f;
    private static final float ATTACK_COOLDOWN_MAX = 1.5f;

    // stav útočnej animácie
    protected boolean isAttacking        = false;
    private   float   attackAnimTimer    = 0f;
    private   float   attackAnimDuration = 0f;
    private   boolean damageDealt        = false;
    private   PlayerCharacter pendingTarget = null;

    public EnemyCharacter(String name, int hp, int attackPower, float speed,
                          Vector2D position, float patrolRange, float detectionRange) {
        super(name, hp, attackPower, speed, position);
        this.patrolRange    = patrolRange;
        this.detectionRange = detectionRange;
        this.inventory      = new Inventory();
    }

    public boolean detectPlayer(PlayerCharacter player) {
        return position.distanceTo(player.getPosition()) <= detectionRange;
    }

    public void initAI(Vector2D patrolStart, Vector2D patrolEnd) {
        this.aiController = new AIController(this, patrolStart, patrolEnd);
    }

    @Override
    public void move(Vector2D direction) {
        position = position.add(direction);
        updateHitbox();
    }

    /**
     * Volaná z AIController keď je hráč v ATTACK_RANGE.
     * Spustí útočnú animáciu; damage príde na KONCI animácie (posledný frame).
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

        // spusti animáciu
        if (am != null) am.play(attack.getAnimationName());

        // spusti vlastnú logiku podtriedy (napr. DarkKnight prepínanie fáz)
        performAttack();
    }

    @Override
    public void onCollision(Object other) {}

    @Override
    public void update(float deltaTime) {
        attackCooldown -= deltaTime;

        if (isAttacking) {
            attackAnimTimer -= deltaTime;

            // damage na konci animácie (posledný frame)
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
}
