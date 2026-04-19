package sk.stuba.fiit.projectiles;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.engine.Collidable;
import sk.stuba.fiit.core.engine.Physicable;
import sk.stuba.fiit.core.engine.Updatable;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.physics.GravityStrategy;
import sk.stuba.fiit.physics.NoGravity;
import sk.stuba.fiit.util.Vector2D;

/**
 * Base class for all projectiles in the game.
 *
 * <p>On-hit effects: a projectile can carry a DOT (damage over time) effect
 * and/or a slow effect. These are attached by decorators ({@link sk.stuba.fiit.attacks.FireDecorator},
 * {@link sk.stuba.fiit.attacks.FreezeDecorator}) after {@code execute()} returns the projectile,
 * and applied by {@code CollisionManager} when the projectile hits a character.
 *
 * <p>Pool reuse: {@link #resetEffects()} must be called in each subclass {@code reset()}
 * so effects never leak between shots when the projectile is recycled from the pool.
 */
public abstract class Projectile implements Updatable, Collidable, Physicable {
    protected int      damage;
    protected float    speed;
    protected Vector2D position;
    protected Vector2D direction;
    protected boolean  active;
    protected Rectangle hitbox;

    // On-hit effects attached by FireDecorator / FreezeDecorator
    private int   dotDps        = 0;
    private float dotDuration   = 0f;
    private float slowMultiplier = 0f;
    private float slowDuration   = 0f;

    private GravityStrategy gravityStrategy = new NoGravity();
    private float   velocityY = 0f;
    private boolean onGround  = false;
    private ProjectileOwner owner = ProjectileOwner.PLAYER;

    private float tintR = 1f;
    private float tintG = 1f;
    private float tintB = 1f;

    public Projectile(int damage, float speed, Vector2D position, Vector2D direction) {
        this.damage    = damage;
        this.speed     = speed;
        this.position  = position;
        this.direction = direction;
        this.active    = true;
        this.hitbox    = new Rectangle(position.getX(), position.getY(), 16, 8);
    }

    public void setTint(float r, float g, float b) {
        this.tintR = r;
        this.tintG = g;
        this.tintB = b;
    }

    // -------------------------------------------------------------------------
    //  On-hit effect API – set by decorators, read by CollisionManager
    // -------------------------------------------------------------------------

    public void setDotEffect(int dps, float duration) {
        this.dotDps      = dps;
        this.dotDuration = duration;
    }

    public void setSlowEffect(float multiplier, float duration) {
        this.slowMultiplier = multiplier;
        this.slowDuration   = duration;
    }

    public boolean hasDotEffect()      { return dotDps > 0; }
    public boolean hasSlowEffect()     { return slowMultiplier > 0; }
    public int     getDotDps()         { return dotDps; }
    public float   getDotDuration()    { return dotDuration; }
    public float   getSlowMultiplier() { return slowMultiplier; }
    public float   getSlowDuration()   { return slowDuration; }
    public float getTintR() { return tintR; }
    public float getTintG() { return tintG; }
    public float getTintB() { return tintB; }

    /**
     * Clears all on-hit effects. Must be called from subclass {@code reset()}
     * to prevent effects leaking across pool reuse cycles.
     */
    protected void resetEffects() {
        dotDps        = 0;
        dotDuration   = 0f;
        slowMultiplier = 0f;
        slowDuration   = 0f;
        tintR = 1f;
        tintG = 1f;
        tintB = 1f;
    }

    // -------------------------------------------------------------------------
    //  Physicable
    // -------------------------------------------------------------------------

    @Override public Vector2D  getPosition()           { return position; }
    @Override public void      setPosition(Vector2D p) { this.position = p; }
    @Override public float     getVelocityY()          { return velocityY; }
    @Override public void      setVelocityY(float vy)  { this.velocityY = vy; }
    @Override public Rectangle getHitbox()             { return hitbox; }
    @Override public boolean   isOnGround()            { return onGround; }
    @Override public void      setOnGround(boolean b)  { this.onGround = b; }
    @Override public void      updateHitbox()          { hitbox.setPosition(position.getX(), position.getY()); }

    // -------------------------------------------------------------------------
    //  Movement and update
    // -------------------------------------------------------------------------

    public void move() {
        position = position.add(direction.scale(speed));
    }

    @Override
    public void update(UpdateContext ctx) {
        gravityStrategy.apply(this, ctx.deltaTime, ctx.platforms);
        move();
        hitbox.setPosition(position.getX(), position.getY());
    }

    // -------------------------------------------------------------------------
    //  Collision
    // -------------------------------------------------------------------------

    public void onHit(Character target) {
        target.takeDamage(damage);
        active = false;
    }

    @Override
    public void onCollision(Object other) {
        if (other instanceof Character) {
            onHit((Character) other);
        }
    }

    // -------------------------------------------------------------------------
    //  Getters / setters
    // -------------------------------------------------------------------------

    public void setGravityStrategy(GravityStrategy strategy) {
        this.gravityStrategy = (strategy != null) ? strategy : new NoGravity();
    }

    public boolean         isPlayerProjectile()        { return owner == ProjectileOwner.PLAYER; }
    public void            setActive(boolean active)   { this.active = active; }
    public boolean         isActive()                  { return active; }
    public Vector2D        getDirection()              { return direction; }
    public ProjectileOwner getOwner()                  { return owner; }
    public void            setOwner(ProjectileOwner o) { this.owner = o; }
    public void            setHitboxSize(Vector2D size){ this.hitbox.setSize(size.getX(), size.getY()); }
    public int             getDamage()                 { return damage; }
}
