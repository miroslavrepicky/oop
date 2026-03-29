package sk.stuba.fiit.characters;

import sk.stuba.fiit.attacks.Attack;
import sk.stuba.fiit.attacks.SpellAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public abstract class PlayerCharacter extends Character {
    protected Attack primaryAttack;
    protected Attack secondaryAttack;
    protected boolean isAttacking = false;
    protected float attackAnimTimer = 0f;
    protected Attack currentAttack;

    /**
     * Základný konštruktor – armor = 0, maxArmor = 0.
     * Podtriedy môžu zavolať rozšírený konštruktor s maxArmor.
     */
    public PlayerCharacter(String name, int hp, int attackPower, float speed, Vector2D position) {
        this(name, hp, attackPower, speed, position, 0);
    }

    /**
     * Rozšírený konštruktor – armor štartuje na 0, maxArmor je strop.
     *
     * @param maxArmor maximálna hodnota brnenia ktorú hráč môže nazbierať
     */
    public PlayerCharacter(String name, int hp, int attackPower, float speed,
                           Vector2D position, int maxArmor) {
        super(name, hp, attackPower, speed, position, 0, maxArmor);
    }

    // mana — defaultne nepotrebná, Wizzard override-ne
    protected int getMana() { return Integer.MAX_VALUE; }
    protected void spendMana(int amount) {}

    protected void executeAttack(Attack attack) {
        if (attack == null) return;

        if (attack instanceof SpellAttack) {
            SpellAttack spell = (SpellAttack) attack;
            if (getMana() < spell.getManaCost()) return;
            spendMana(spell.getManaCost());
        }

        Level level = GameManager.getInstance().getCurrentLevel();
        if (level == null) return;

        AnimationManager am = getAnimationManager();
        if (am != null) {
            isAttacking    = true;
            currentAttack  = attack;
            attackAnimTimer = attack.getAnimationDuration(am);
        }

        attack.execute(this, level);
    }

    public void performPrimaryAttack()   { executeAttack(primaryAttack); }
    public void performSecondaryAttack() { executeAttack(secondaryAttack); }

    @Override
    public void performAttack() { performPrimaryAttack(); }

    @Override
    public void updateAnimation(float deltaTime) {
        if (getAnimationManager() == null) return;

        if (isAttacking) {
            attackAnimTimer -= deltaTime;
            if (attackAnimTimer <= 0f) isAttacking = false;
        }

        String anim;
        if (!isAlive()) {
            anim = "death";
        } else if (isAttacking) {
            anim = currentAttack != null ? currentAttack.getAnimationName() : "attack";
        } else if (!isOnGround()) {
            anim = hasAnimation("jump") ? "jump" : "idle";
        } else if (Math.abs(velocityX) > 0.1f) {
            anim = "walk";
        } else {
            anim = "idle";
        }

        getAnimationManager().play(anim);
        getAnimationManager().update(deltaTime);
    }

    protected boolean hasAnimation(String name) {
        return getAnimationManager() != null && getAnimationManager().hasAnimation(name);
    }

    public Inventory getInventory() {
        return GameManager.getInstance().getInventory();
    }

    public abstract void handleInput();

    @Override
    public void move(Vector2D direction) {
        position = position.add(direction);
        updateHitbox();
    }

    @Override
    public void onCollision(Object other) {}
}
