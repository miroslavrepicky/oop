package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.AIController;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;

public abstract class EnemyCharacter extends Character{
    protected float patrolRange;
    protected float detectionRange;
    protected Inventory inventory;
    private AIController aiController;
    private float attackCooldown = 0f;
    private static final float ATTACK_COOLDOWN_MAX = 1.0f; // utok raz za sekundu

    public EnemyCharacter(String name, int hp, int attackPower, float speed,
                          Vector2D position, float patrolRange, float detectionRange) {
        super(name, hp, attackPower, speed, position);
        this.patrolRange = patrolRange;
        this.detectionRange = detectionRange;
        this.inventory = new Inventory();
    }

    public void patrol() {
        // pohyb po patrolovacej trase
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

    public void attack(PlayerCharacter player) {
        if (attackCooldown <= 0) {
            player.takeDamage(attackPower);
            attackCooldown = ATTACK_COOLDOWN_MAX;
            System.out.println("Nepriatel zautocil! Hrac HP: " + player.getHp());
        }
    }


    @Override
    public void onCollision(Object other) {
        // spracovanie kolizie
    }

    @Override
    public void update(float deltaTime) {
        //System.out.println(getPosition().getX() + " " + getPosition().getY());
        attackCooldown -= deltaTime;
        applyGravity(deltaTime);
        if (aiController != null) {
            PlayerCharacter player = GameManager.getInstance()
                .getInventory()
                .getActive();
            if (player != null) {
                aiController.update(deltaTime, player);
            }
        }
    }


}
