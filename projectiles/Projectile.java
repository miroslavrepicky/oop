package sk.stuba.fiit.projectiles;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.core.Collidable;
import sk.stuba.fiit.core.GravityStrategy;
import sk.stuba.fiit.core.NoGravity;
import sk.stuba.fiit.core.Physicable;
import sk.stuba.fiit.core.Updatable;
import sk.stuba.fiit.core.UpdateContext;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.util.Vector2D;

/**
 * Základná trieda pre všetky projektily.
 *
 * Implementuje {@link Physicable} – vďaka tomu môže voliteľne využívať
 * {@link GravityStrategy} (napr. parabolický let šípu) bez dedičnosti od
 * {@code Character}. Predvolená stratégia je {@link NoGravity}.
 *
 * Implementuje {@link Updatable} cez {@link #update(UpdateContext)} –
 * rovnaký kontrakt ako ostatné objekty v hernej slučke.
 */
public abstract class Projectile implements Updatable, Collidable, Physicable {
    protected int      damage;
    protected float    speed;
    protected Vector2D position;
    protected Vector2D direction;
    protected boolean  active;
    protected Rectangle hitbox;

    /**
     * Gravitačná stratégia. Predvolene {@link NoGravity} – projektily
     * letia horizontálne. Podtrieda (napr. parabolický šíp) môže nastaviť
     * {@link sk.stuba.fiit.core.NormalGravity} cez konštruktor.
     */
    private GravityStrategy gravityStrategy = new NoGravity();

    // Physicable – projektily nemajú vertikálnu rýchlosť v pôvodnom zmysle,
    // ale Physicable.velocityY sa využíva keď je nastavená gravitácia.
    private float velocityY = 0f;
    private boolean onGround = false;

    private ProjectileOwner owner = ProjectileOwner.PLAYER;

    public Projectile(int damage, float speed, Vector2D position, Vector2D direction) {
        this.damage    = damage;
        this.speed     = speed;
        this.position  = position;
        this.direction = direction;
        this.active    = true;
        this.hitbox    = new Rectangle(position.getX(), position.getY(), 16, 8);
    }

    // -------------------------------------------------------------------------
    //  Physicable implementácia
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
    //  Pohyb a update
    // -------------------------------------------------------------------------

    public void move() {
        position = position.add(direction.scale(speed));
    }

    /**
     * Predvolená implementácia: pohyb + voliteľná gravitácia + update hitboxu.
     * Podtriedy override-ujú ak potrebujú animáciu alebo špeciálnu logiku.
     */
    @Override
    public void update(UpdateContext ctx) {
        gravityStrategy.apply(this, ctx.deltaTime, ctx.platforms);
        move();
        hitbox.setPosition(position.getX(), position.getY());
    }

    // -------------------------------------------------------------------------
    //  Kolízie
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
    //  Gettery / settery
    // -------------------------------------------------------------------------

    public void setGravityStrategy(GravityStrategy strategy) {
        this.gravityStrategy = (strategy != null) ? strategy : new NoGravity();
    }

    public boolean         isPlayerProjectile()         { return owner == ProjectileOwner.PLAYER; }
    public void            setActive(boolean active)    { this.active = active; }
    public boolean         isActive()                   { return active; }
    public Vector2D        getDirection()               { return direction; }
    public ProjectileOwner getOwner()                   { return owner; }
    public void            setOwner(ProjectileOwner o)  { this.owner = o; }
    public void            setHitboxSize(Vector2D size) { this.hitbox.setSize(size.getX(), size.getY()); }
    public int             getDamage()                  { return damage; }
}
