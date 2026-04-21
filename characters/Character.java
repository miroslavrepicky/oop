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
 * Abstract base class for all characters – both player-controlled and AI-controlled.
 *
 * <h2>Damage model</h2>
 * <p>{@link #takeDamage(int)} first drains armour, then reduces HP.
 * A <em>negative</em> damage value heals the character up to {@code maxHp}.
 *
 * <h2>On-hit effects</h2>
 * <p>{@link #applyDot(int, float)} and {@link #applySlow(float, float)} are called by
 * {@code CollisionManager} when a projectile carrying an effect hits this character.
 * Effects tick each frame via {@link #tickEffects(float)}, which subclasses must call
 * from their own {@code update()} implementation.
 *
 * <h2>Death animation</h2>
 * <p>Calling {@link #startDeathAnimation()} switches to the {@code "death"} animation
 * and starts a countdown equal to the animation duration. Once the countdown reaches
 * zero, {@link #isDeathAnimationDone()} returns {@code true} and the object may be
 * removed from the level.
 *
 * <h2>Gravity</h2>
 * <p>Gravity is pluggable via the Strategy pattern: assign a {@link GravityStrategy}
 * implementation and call {@link #applyGravity(float, List)} each frame.
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

    /** Countdown for the death animation; -1 means the animation has not started yet. */
    private   float   deathTimer = -1f;
    protected int     armor;
    protected int     maxArmor;

    // DOT state
    private int   dotDps         = 0;
    private float dotRemaining   = 0f;
    private float dotAccumulator = 0f;

    // Slow state
    /** Saved speed before a slow was applied; -1 when no slow is active. */
    private float originalSpeed  = -1f;
    private float slowRemaining  = 0f;

    private static final Logger log = GameLogger.get(Character.class);

    /** Constructs a character with zero armour. */
    public Character(String name, int hp, int attackPower, float speed, Vector2D position) {
        this(name, hp, attackPower, speed, position, 0, 0);
    }

    /**
     * Full constructor used by armoured characters.
     *
     * @param name        display name
     * @param hp          starting and maximum HP
     * @param attackPower base damage value forwarded to attack strategies
     * @param speed       movement speed in world units per second
     * @param position    initial world position
     * @param armor       starting armour value (clamped to {@code maxArmor})
     * @param maxArmor    maximum armour value
     */
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

    /**
     * Applies the configured {@link GravityStrategy} for one frame.
     *
     * @param deltaTime elapsed time in seconds
     * @param platforms collision rectangles from the map
     */
    public void applyGravity(float deltaTime, List<Rectangle> platforms) {
        if (gravityStrategy != null) {
            gravityStrategy.apply(this, deltaTime, platforms);
        }
    }

    // -------------------------------------------------------------------------
    //  On-hit effects – called by CollisionManager on impact
    // -------------------------------------------------------------------------

    /**
     * Applies a damage-over-time burn effect, replacing any previously active DOT.
     *
     * @param dps      damage dealt per second
     * @param duration total effect duration in seconds
     */
    public void applyDot(int dps, float duration) {
        this.dotDps         = dps;
        this.dotRemaining   = duration;
        this.dotAccumulator = 0f;
        log.info("DOT applied: target={}, dps={}, duration={}", name, dps, duration);
    }

    /**
     * Applies a slow effect that reduces speed to {@code multiplier × originalSpeed}.
     * If a slow is already active the original speed is restored before the new
     * multiplier is applied, preventing multiplicative stacking.
     *
     * @param multiplier fraction of original speed (e.g. {@code 0.3f} = 30 %)
     * @param duration   effect duration in seconds
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
     * Ticks active DOT and slow effects. Must be called once per frame from each
     * subclass {@code update()} implementation.
     *
     * @param deltaTime elapsed time since the last frame, in seconds
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

    /**
     * Switches to the {@code "death"} animation and starts a countdown equal to its
     * duration. Has no effect if the death animation was already started.
     */
    public void startDeathAnimation() {
        if (deathTimer != -1f) return;
        AnimationManager am = getAnimationManager();
        float duration = (am != null && am.hasAnimation("death"))
            ? am.getAnimationDuration("death")
            : 1.0f;
        deathTimer = duration;
        if (am != null) am.play("death");
    }

    /**
     * Decrements the death-animation countdown by {@code deltaTime}, clamped to zero.
     *
     * @param deltaTime elapsed time in seconds
     */
    public void updateDeathTimer(float deltaTime) {
        if (deathTimer > 0f) {
            deathTimer = Math.max(0f, deathTimer - deltaTime);
        }
    }

    /**
     * Returns {@code true} when the character is dead <em>and</em> the death animation
     * countdown has reached zero. The character may then be removed from the level.
     */
    public boolean isDeathAnimationDone() {
        return !isAlive() && deathTimer == 0f;
    }

    /**
     * Applies an upward impulse if the character is on the ground.
     * Has no effect while airborne.
     *
     * @param jumpForce initial upward velocity in world units per second
     */
    public void jump(float jumpForce) {
        if (isOnGround) {
            velocityY  = jumpForce;
            isOnGround = false;
        }
    }

    /**
     * Restores full HP and clears all active effects (DOT, slow, death timer).
     * Called by {@code GameManager.reviveParty()} before retrying a failed level.
     */
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

    /**
     * Applies damage to the character. Armour absorbs damage first; any remainder
     * reduces HP. A negative {@code dmg} value heals up to {@code maxHp}.
     *
     * @param dmg raw damage value; negative values heal
     */
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

    /**
     * Priamo nastaví HP a armor na uložené hodnoty (používa SaveManager pri načítaní).
     * Na rozdiel od takeDamage() nepreteká cez armor absorpciu.
     */
    public void restoreStats(int hp, int armor) {
        this.hp    = Math.max(0, Math.min(hp,    maxHp));
        this.armor = Math.max(0, Math.min(armor, maxArmor));
    }

    /**
     * Permanently increases armour by {@code amount}, clamped to {@code maxArmor}.
     *
     * @param amount armour points to add
     */
    public void addArmor(int amount) {
        armor = Math.min(maxArmor, armor + amount);
    }

    /** Returns {@code true} when the character's HP is greater than zero. */
    public boolean isAlive() { return hp > 0; }

    /** No-op default; subclasses override to drive frame-by-frame animation logic. */
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

    /** Returns the character's {@link AnimationManager}; must not return {@code null}. */
    public abstract AnimationManager getAnimationManager();
}
