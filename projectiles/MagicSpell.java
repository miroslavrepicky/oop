package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.util.Vector2D;

public class MagicSpell extends Projectile {
    private float aoeRadius;

    public MagicSpell(int damage, float speed, Vector2D position, Vector2D direction, float aoeRadius) {
        super(damage, speed, position, direction);
        this.aoeRadius = aoeRadius;
    }

    @Override
    public void update(float deltaTime) {
        move();
    }

    public float getAoeRadius() { return aoeRadius; }
}
