package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.*;
import sk.stuba.fiit.util.Vector2D;

import com.badlogic.gdx.math.Rectangle;

public abstract class Character implements Updatable, Movable, Collidable {
    protected String name;
    protected int hp;
    protected int maxHp;
    protected int attackPower;
    protected float speed;
    protected Vector2D position;
    protected Rectangle hitbox;
    protected GravityStrategy gravityStrategy;
    protected float velocityY = 0f;
    protected boolean isOnGround = false;
    protected boolean facingRight = true;
    protected float velocityX = 0f;

    /** Aktualna hodnota brnenia. Znizuje prijate poskodenie. */
    protected int armor;
    /** Maximalna hodnota brnenia (strop pre armor). */
    protected int maxArmor;

    public Character(String name, int hp, int attackPower, float speed, Vector2D position) {
        this(name, hp, attackPower, speed, position, 0, 0);
    }

    public Character(String name, int hp, int attackPower, float speed,
                     Vector2D position, int armor, int maxArmor) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.attackPower = attackPower;
        this.speed = speed;
        this.position = position;
        this.hitbox = new Rectangle(position.getX(), position.getY(), 64, 64);
        this.armor = Math.min(armor, maxArmor);
        this.maxArmor = maxArmor;
    }

    public void updateHitbox() {
        hitbox.setPosition(position.getX(), position.getY());
    }

    public void applyGravity(float deltaTime) {
        if (gravityStrategy != null) {
            gravityStrategy.apply(this, deltaTime);
        }
    }

    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }

    public void jump(float jumpForce) {
        if (isOnGround) {
            velocityY = jumpForce;
            isOnGround = false;
        }
    }

    public void revive() {
        this.hp = this.maxHp;
        this.velocityY = 0f;
        this.isOnGround = false;
    }

    /**
     * Aplikuje poskodenie s odpocitanim brnenia.
     * Zaporny dmg = liecenie (heal), brnenie sa vtedy neaplikuje.
     */
    public void takeDamage(int dmg) {
        if (dmg > 0) {
            int armorAbsorb = Math.min(armor, dmg);
            armor = Math.max(0, armor - armorAbsorb);
            int reduced = Math.max(0, dmg - armorAbsorb);
            this.hp = Math.max(0, this.hp - reduced);
        } else {
            this.hp = Math.min(maxHp, this.hp - dmg);
        }
    }

    /**
     * Zvysi aktualny armor o {@code amount}, maximalne do maxArmor.
     */
    public void addArmor(int amount) {
        armor = Math.min(maxArmor, armor + amount);
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public abstract void performAttack();

    public void updateAnimation(float deltaTime) {
        // override v podtriedach
    }

    // gettery / settery
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttackPower() { return attackPower; }
    public float getSpeed() { return speed; }
    public Vector2D getPosition() { return position; }
    public void setPosition(Vector2D position) { this.position = position; }
    public void setHitboxSize(Vector2D size) { this.hitbox.setSize(size.getX(), size.getY()); }
    public Rectangle getHitbox() { return hitbox; }
    public float getVelocityY() { return velocityY; }
    public void setVelocityY(float velocityY) { this.velocityY = velocityY; }
    public boolean isOnGround() { return isOnGround; }
    public void setOnGround(boolean onGround) { this.isOnGround = onGround; }
    public abstract AnimationManager getAnimationManager();
    public float getVelocityX() { return velocityX; }
    public void setVelocityX(float velocityX) { this.velocityX = velocityX; }
    public int getArmor() { return armor; }
    public int getMaxArmor() { return maxArmor; }
}
