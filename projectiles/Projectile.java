package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.*;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.characters.Character;

import com.badlogic.gdx.math.Rectangle;

public abstract class Projectile implements Updatable, Collidable {
    protected int damage;
    protected float speed;
    protected Vector2D position;
    protected Vector2D direction;
    protected boolean active;
    protected Rectangle hitbox;
    protected GravityStrategy gravityStrategy;
    protected Character shooter = null;

    public Projectile(int damage, float speed, Vector2D position, Vector2D direction) {
        this.damage = damage;
        this.speed = speed;
        this.position = position;
        this.direction = direction;
        this.active = true;
        this.gravityStrategy = new NoGravity();
        this.hitbox = new Rectangle(position.getX(), position.getY(), 16, 8);
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

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void update(float deltaTime) {
        move();
        hitbox.setPosition(position.getX(), position.getY());
    }

    public boolean isActive() { return active; }
    public Vector2D getPosition() { return position; }
    public Rectangle getHitbox() { return hitbox; }
    public Vector2D getDirection() { return direction; }
    public Character getShooter() { return shooter; }
    public void setShooter(Character shooter) { this.shooter = shooter; }

}
