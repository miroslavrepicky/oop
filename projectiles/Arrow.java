package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.util.Vector2D;

public class Arrow extends Projectile {
    private boolean piercing;

    public Arrow(int damage, float speed, Vector2D position, Vector2D direction, boolean piercing) {
        super(damage, speed, position, direction);
        this.piercing = piercing;
    }

//    @Override
//    public void update(float deltaTime) {
//        move();
//    }

    public boolean isPiercing() { return piercing; }
}
