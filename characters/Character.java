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
 * <p>Implements {@link Physicable} so that {@link GravityStrategy} can operate
 * on any character without knowing its concrete type. Gravity is decoupled from
 * the character hierarchy and can therefore also be applied to
 * {@link sk.stuba.fiit.projectiles.Projectile} instances.
 *
 * <p>Damage model: {@link #takeDamage(int)} first drains armour, then HP.
 * A negative damage value heals the character up to {@code maxHp}.
 *
 * <p>Death: when HP reaches zero {@link #isAlive()} returns {@code false}.
 * The death animation plays via {@link #startDeathAnimation()} and
 * {@link #isDeathAnimationDone()} signals when the character can be removed.
 */
public abstract class Character implements Updatable, Movable, Collidable, Physicable {
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

    private static final Logger log = GameLogger.get(Character.class);

    public Character(String name, int hp, int attackPower, float speed, Vector2D position) {
        this(name, hp, attackPower, speed, position, 0, 0);
    }

    public Character(String name, int hp, int attackPower, float speed,
                     Vector2D position, int armor, int maxArmor) {
        this.name        = name;
        this.hp          = hp;
        this.maxHp       = hp;
        this.attackPower = attackPower;
        this.speed       = speed;
        this.position    = position;
        this.hitbox      = new Rectangle(position.getX(), position.getY(), 64, 64);
        this.armor       = Math.min(armor, maxArmor);
        this.maxArmor    = maxArmor;
    }

    // -------------------------------------------------------------------------
    //  Physicable implementácia
    // -------------------------------------------------------------------------

    @Override public Vector2D  getPosition()              { return position; }
    @Override public void      setPosition(Vector2D p)    { this.position = p; }
    @Override public float     getVelocityY()             { return velocityY; }
    @Override public void      setVelocityY(float vy)     { this.velocityY = vy; }
    @Override public Rectangle getHitbox()                { return hitbox; }
    @Override public boolean   isOnGround()               { return isOnGround; }
    @Override public void      setOnGround(boolean b)     { this.isOnGround = b; }

    @Override
    public void updateHitbox() {
        hitbox.setPosition(position.getX(), position.getY());
    }

    // -------------------------------------------------------------------------
    //  Gravitácia – deleguje na GravityStrategy cez Physicable
    // -------------------------------------------------------------------------

    /**
     * Applies gravity using the configured {@link GravityStrategy}.
     * Platforms are passed in to avoid fetching them from {@code GameManager}.
     *
     * @param deltaTime time elapsed since the last frame in seconds
     * @param platforms collision rectangles from the map
     */
    public void applyGravity(float deltaTime, List<Rectangle> platforms) {
        if (gravityStrategy != null) {
            gravityStrategy.apply(this, deltaTime, platforms);
        }
    }

    // -------------------------------------------------------------------------
    //  Updatable – podtriedy musia implementovať update(UpdateContext)
    // -------------------------------------------------------------------------

    /**
     * Podtriedy implementujú túto metódu a z {@code ctx} si zoberú čo potrebujú.
     * {@code PlayerCharacter} berie {@code ctx.deltaTime} a {@code ctx.platforms}.
     * {@code EnemyCharacter} berie všetko vrátane {@code ctx.level} a {@code ctx.player}.
     */
    @Override
    public abstract void update(UpdateContext ctx);

    /**
     * Initiates the death animation. Has no effect if already started.
     * Sets the death timer to the duration of the "death" animation clip.
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

    public void updateDeathTimer(float deltaTime) {
        if (deathTimer > 0f) {
            deathTimer = Math.max(0f, deathTimer - deltaTime);
        }
    }

    /**
     * Returns {@code true} when the character is dead and the death animation
     * has fully played out. Used by {@code Level.update()} to decide when to
     * remove the character from the scene.
     *
     * @return {@code true} if the death animation is complete
     */
    public boolean isDeathAnimationDone() {
        return !isAlive() && deathTimer == 0f;
    }

    public void jump(float jumpForce) {
        if (isOnGround) {
            velocityY  = jumpForce;
            isOnGround = false;
        }
    }

    /**
     * Restores the character to full HP and resets velocity and death state.
     * Called by {@code GameManager.reviveParty()} on game-over retry.
     */
    public void revive() {
        this.hp         = this.maxHp;
        this.velocityY  = 0f;
        this.isOnGround = false;
        this.deathTimer = -1f;
    }

    /**
     * Applies damage with armour absorption. Negative {@code dmg} heals instead.
     *
     * <p>Damage flow: {@code armorAbsorb = min(armor, dmg)}, then
     * {@code hp -= max(0, dmg - armorAbsorb)}.
     *
     * @param dmg raw damage to apply; negative values heal
     */
    public void takeDamage(int dmg) {
        if (dmg > 0) {
            int armorAbsorb = Math.min(armor, dmg);
            armor = Math.max(0, armor - armorAbsorb);
            int reduced = Math.max(0, dmg - armorAbsorb);
            this.hp = Math.max(0, this.hp - reduced);

            // Guard check – takeDamage môže byť volaný každý frame (DoT, AOE)
            if (log.isDebugEnabled()) {
                log.debug("Damage taken: target={}, rawDmg={}, armorAbsorb={}, reduced={}, hpAfter={}/{}",
                    name, dmg, armorAbsorb, reduced, this.hp, this.maxHp);
            }

            // INFO len pri death – nie hot path
            if (!isAlive()) {
                log.info("Character died: name={}, killedBy={}dmg", name, dmg);
            }

        } else {
            // Healing
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
     * Increases the character's current armour by {@code amount}, capped at {@code maxArmor}.
     *
     * @param amount armour points to add
     */
    public void addArmor(int amount) {
        armor = Math.min(maxArmor, armor + amount);
    }

    public boolean isAlive() { return hp > 0; }

    public abstract void performAttack();

    public void updateAnimation(float deltaTime) {
        // override v podtriedach
    }

    // --- gettery / settery ---
    public String    getName()        { return name; }
    public int       getHp()          { return hp; }
    public int       getMaxHp()       { return maxHp; }
    public int       getAttackPower() { return attackPower; }
    public float     getSpeed()       { return speed; }
    public void      setSpeed(float s) { this.speed = s; }
    public void      setHitboxSize(Vector2D size)     { this.hitbox.setSize(size.getX(), size.getY()); }
    public boolean   isFacingRight()  { return facingRight; }
    public void      setFacingRight(boolean b) { this.facingRight = b; }
    public float     getVelocityX()   { return velocityX; }
    public void      setVelocityX(float v) { this.velocityX = v; }
    public int       getArmor()       { return armor; }
    public int       getMaxArmor()    { return maxArmor; }
    public abstract AnimationManager getAnimationManager();
}
