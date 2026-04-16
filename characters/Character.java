package sk.stuba.fiit.characters;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.Collidable;
import sk.stuba.fiit.core.GravityStrategy;
import sk.stuba.fiit.core.Movable;
import sk.stuba.fiit.core.Updatable;
import sk.stuba.fiit.util.Vector2D;

import java.util.Collections;
import java.util.List;

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
    private float deathTimer = -1f;

    protected int armor;
    protected int maxArmor;

    public Character(String name, int hp, int attackPower, float speed, Vector2D position) {
        this(name, hp, attackPower, speed, position, 0, 0);
    }

    public Character(String name, int hp, int attackPower, float speed,
                     Vector2D position, int armor, int maxArmor) {
        this.name     = name;
        this.hp       = hp;
        this.maxHp    = hp;
        this.attackPower = attackPower;
        this.speed    = speed;
        this.position = position;
        this.hitbox   = new Rectangle(position.getX(), position.getY(), 64, 64);
        this.armor    = Math.min(armor, maxArmor);
        this.maxArmor = maxArmor;
    }

    public void updateHitbox() {
        hitbox.setPosition(position.getX(), position.getY());
    }

    /**
     * Aplikuje gravitáciu. Platformy sa predávajú zvonku – Character
     * nemusí vedieť nič o Level ani GameManager.
     *
     * @param deltaTime čas od posledného framu
     * @param platforms kolízne obdĺžniky mapy; null = žiadne platformy
     */
    public void applyGravity(float deltaTime, List<Rectangle> platforms) {
        if (gravityStrategy != null) {
            gravityStrategy.apply(this, deltaTime, platforms);
        }
    }

    public void startDeathAnimation() {
        if (deathTimer != -1f) return;
        AnimationManager am = getAnimationManager();
        float duration = (am != null && am.hasAnimation("death"))
            ? am.getAnimationDuration("death")
            : 1.0f;
        deathTimer = duration;
        if (am != null) am.play("death");
    }

    public void updateDeathTimer(float deltaTime) {
        if (deathTimer > 0f) {
            deathTimer = Math.max(0f, deathTimer - deltaTime);
        }
    }

    public boolean isDeathAnimationDone() {
        return !isAlive() && deathTimer == 0f;
    }

    public void jump(float jumpForce) {
        if (isOnGround) {
            velocityY  = jumpForce;
            isOnGround = false;
        }
    }

    public void revive() {
        this.hp        = this.maxHp;
        this.velocityY = 0f;
        this.isOnGround = false;
        this.deathTimer = -1f;
    }

    /**
     * Aplikuje poškodenie s odpočítaním brnenia.
     * Záporný dmg = liečenie; brnenie sa vtedy neaplikuje.
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

    // --- gettery / settery ---
    public String      getName()        { return name; }
    public int         getHp()          { return hp; }
    public int         getMaxHp()       { return maxHp; }
    public int         getAttackPower() { return attackPower; }
    public float       getSpeed()       { return speed; }
    public Vector2D    getPosition()    { return position; }
    public void        setPosition(Vector2D position) { this.position = position; }
    public void        setHitboxSize(Vector2D size)   { this.hitbox.setSize(size.getX(), size.getY()); }
    public Rectangle   getHitbox()      { return hitbox; }
    public float       getVelocityY()   { return velocityY; }
    public void        setVelocityY(float v) { this.velocityY = v; }
    public boolean     isOnGround()     { return isOnGround; }
    public void        setOnGround(boolean b) { this.isOnGround = b; }
    public boolean     isFacingRight()  { return facingRight; }
    public void        setFacingRight(boolean b) { this.facingRight = b; }
    public float       getVelocityX()   { return velocityX; }
    public void        setVelocityX(float v) { this.velocityX = v; }
    public int         getArmor()       { return armor; }
    public int         getMaxArmor()    { return maxArmor; }
    public abstract AnimationManager getAnimationManager();
}
