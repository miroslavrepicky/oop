package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.Collidable;
import sk.stuba.fiit.core.Updatable;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.util.Vector2D;

import com.badlogic.gdx.math.Rectangle;

public abstract class Projectile implements Updatable, Collidable {
    protected int damage;
    protected float speed;
    protected Vector2D position;
    protected Vector2D direction;
    protected boolean active;
    protected Rectangle hitbox;

    /**
     * Kto vystrelil projektil – PLAYER alebo ENEMY.
     * CollisionManager podľa toho rozhodne s kým koliduje.
     * Predvolená hodnota je PLAYER (napr. pre debug projektily).
     */
    private ProjectileOwner owner = ProjectileOwner.PLAYER;

    public Projectile(int damage, float speed, Vector2D position, Vector2D direction) {
        this.damage    = damage;
        this.speed     = speed;
        this.position  = position;
        this.direction = direction;
        this.active    = true;
        this.hitbox    = new Rectangle(position.getX(), position.getY(), 16, 8);
    }

    public void move() {
        position = position.add(direction.scale(speed));
    }

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

    @Override
    public void update(float deltaTime) {
        move();
        hitbox.setPosition(position.getX(), position.getY());
        // Projektily nemajú gravitáciu – pohybujú sa len podľa direction * speed.
        // Ak by konkrétny projektil potreboval gravitáciu, override-ne update().
    }

    public boolean isPlayerProjectile() {
        return owner == ProjectileOwner.PLAYER;
    }

    // --- gettery / settery ---
    public void            setActive(boolean active)    { this.active = active; }
    public boolean         isActive()                   { return active; }
    public Vector2D        getPosition()                { return position; }
    public Rectangle       getHitbox()                  { return hitbox; }
    public Vector2D        getDirection()               { return direction; }
    public ProjectileOwner getOwner()                   { return owner; }
    public void            setOwner(ProjectileOwner o)  { this.owner = o; }
    public void            setHitboxSize(Vector2D size) { this.hitbox.setSize(size.getX(), size.getY()); }
    public int             getDamage()                  { return damage; }
}
