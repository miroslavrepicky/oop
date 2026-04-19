package sk.stuba.fiit.characters;

import com.badlogic.gdx.math.Rectangle;
import org.slf4j.Logger;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.engine.Collidable;
import sk.stuba.fiit.physics.GravityStrategy;
import sk.stuba.fiit.core.engine.Movable;
import sk.stuba.fiit.core.engine.Physicable;
import sk.stuba.fiit.core.engine.Updatable;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import java.util.List;

/**
 * Abstract base class for all characters (player and enemy alike).
 *
 * <p>Damage model: {@link #takeDamage(int)} first drains armour, then HP.
 * A negative damage value heals the character up to {@code maxHp}.
 *
 * <p>On-hit effects: {@link #applyDot(int, float)} and {@link #applySlow(float, float)}
 * are called by {@code CollisionManager} when a projectile carrying an effect hits this
 * character. Effects tick internally each frame via {@link #tickEffects(float)}, which
 * subclasses must call from their {@code update()} implementation.
 */
public abstract class Character implements Updatable, Movable, Collidable, Physicable {
    protected String    name;
    protected int       hp;
    protected int       maxHp;
    protected int       attackPower;
    protected float     speed;
    protected Vector2D  position;
    protected Rectangle hitbox;
    protected GravityStrategy gravityStrategy;
    protected float   velocityY  = 0f;
    protected boolean isOnGround = false;
    protected boolean facingRight = true;
    protected float   velocityX  = 0f;
    private   float   deathTimer = -1f;
    protected int     armor;
    protected int     maxArmor;

    // DOT state
    private int   dotDps         = 0;
    private float dotRemaining   = 0f;
    private float dotAccumulator = 0f;

    // Slow state
    private float originalSpeed  = -1f;
    private float slowRemaining  = 0f;

    private static final Logger log = GameLogger.get(Character.class);

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

    // -------------------------------------------------------------------------
    //  Physicable
    // -------------------------------------------------------------------------

    @Override public Vector2D  getPosition()           { return position; }
    @Override public void      setPosition(Vector2D p) { this.position = p; }
    @Override public float     getVelocityY()          { return velocityY; }
    @Override public void      setVelocityY(float vy)  { this.velocityY = vy; }
    @Override public Rectangle getHitbox()             { return hitbox; }
    @Override public boolean   isOnGround()            { return isOnGround; }
    @Override public void      setOnGround(boolean b)  { this.isOnGround = b; }

    @Override
    public void updateHitbox() {
        hitbox.setPosition(position.getX(), position.getY());
    }

    // -------------------------------------------------------------------------
    //  Gravity
    // -------------------------------------------------------------------------

    public void applyGravity(float deltaTime, List<Rectangle> platforms) {
        if (gravityStrategy != null) {
            gravityStrategy.apply(this, deltaTime, platforms);
        }
    }

    // -------------------------------------------------------------------------
    //  On-hit effects – called by CollisionManager on impact
    // -------------------------------------------------------------------------

    /**
     * Applies a damage-over-time effect. Replaces any active DOT.
     *
     * @param dps      damage per second
     * @param duration total duration in seconds
     */
    public void applyDot(int dps, float duration) {
        this.dotDps         = dps;
        this.dotRemaining   = duration;
        this.dotAccumulator = 0f;
        log.info("DOT applied: target={}, dps={}, duration={}", name, dps, duration);
    }

    /**
     * Applies a slow effect, reducing speed to {@code multiplier * originalSpeed}.
     * Replaces any active slow (original speed is restored first).
     *
     * @param multiplier fraction of original speed (e.g. 0.3 = 30%)
     * @param duration   total duration in seconds
     */
    public void applySlow(float multiplier, float duration) {
        if (originalSpeed >= 0f) {
            speed = originalSpeed;
        }
        originalSpeed = speed;
        speed         = speed * multiplier;
        slowRemaining = duration;
        log.info("Slow applied: target={}, multiplier={}, duration={}, speed={}->{}",
            name, multiplier, duration, originalSpeed, speed);
    }

    /**
     * Ticks active DOT and slow effects. Must be called once per frame from
     * each subclass {@code update()} implementation.
     *
     * @param deltaTime time since last frame in seconds
     */
    protected void tickEffects(float deltaTime) {
        if (dotRemaining > 0f && isAlive()) {
            dotRemaining    -= deltaTime;
            dotAccumulator  += dotDps * deltaTime;
            int dmg = (int) dotAccumulator;
            if (dmg > 0) {
                takeDamage(dmg);
                dotAccumulator -= dmg;
            }
            if (dotRemaining <= 0f) {
                dotDps       = 0;
                dotRemaining = 0f;
            }
        }

        if (slowRemaining > 0f) {
            slowRemaining -= deltaTime;
            if (slowRemaining <= 0f && originalSpeed >= 0f) {
                speed         = originalSpeed;
                originalSpeed = -1f;
                log.info("Slow expired: target={}, speedRestored={}", name, speed);
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Updatable
    // -------------------------------------------------------------------------

    @Override
    public abstract void update(UpdateContext ctx);

    // -------------------------------------------------------------------------
    //  Death animation
    // -------------------------------------------------------------------------

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
        this.hp           = this.maxHp;
        this.velocityY    = 0f;
        this.isOnGround   = false;
        this.deathTimer   = -1f;
        this.dotDps       = 0;
        this.dotRemaining = 0f;
        this.slowRemaining = 0f;
        if (originalSpeed >= 0f) {
            speed         = originalSpeed;
            originalSpeed = -1f;
        }
    }

    public void takeDamage(int dmg) {
        if (dmg > 0) {
            int armorAbsorb = Math.min(armor, dmg);
            armor = Math.max(0, armor - armorAbsorb);
            int reduced = Math.max(0, dmg - armorAbsorb);
            this.hp = Math.max(0, this.hp - reduced);

            if (log.isDebugEnabled()) {
                log.debug("Damage taken: target={}, rawDmg={}, armorAbsorb={}, reduced={}, hpAfter={}/{}",
                    name, dmg, armorAbsorb, reduced, this.hp, this.maxHp);
            }
            if (!isAlive()) {
                log.info("Character died: name={}, killedBy={}dmg", name, dmg);
            }
        } else {
            int healAmount = -dmg;
            int actualHeal = Math.min(healAmount, maxHp - this.hp);
            this.hp = Math.min(maxHp, this.hp - dmg);
            if (log.isDebugEnabled()) {
                log.debug("Healing applied: target={}, healAmount={}, actualHeal={}, hpAfter={}/{}",
                    name, healAmount, actualHeal, this.hp, this.maxHp);
            }
        }
    }

    public void addArmor(int amount) {
        armor = Math.min(maxArmor, armor + amount);
    }

    public boolean isAlive() { return hp > 0; }

    public abstract void performAttack();

    public void updateAnimation(float deltaTime) { }

    public String    getName()             { return name; }
    public int       getHp()              { return hp; }
    public int       getMaxHp()           { return maxHp; }
    public int       getAttackPower()     { return attackPower; }
    public float     getSpeed()           { return speed; }
    public void      setSpeed(float s)    { this.speed = s; }
    public void      setHitboxSize(Vector2D size) { this.hitbox.setSize(size.getX(), size.getY()); }
    public boolean   isFacingRight()      { return facingRight; }
    public void      setFacingRight(boolean b) { this.facingRight = b; }
    public float     getVelocityX()       { return velocityX; }
    public void      setVelocityX(float v){ this.velocityX = v; }
    public int       getArmor()           { return armor; }
    public int       getMaxArmor()        { return maxArmor; }
    public abstract AnimationManager getAnimationManager();
}
