package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.FloatingGravity;
import sk.stuba.fiit.items.EnemyDuck;
import sk.stuba.fiit.items.FriendlyDuck;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.util.Vector2D;

import java.util.Random;

public class Duck extends Character {
    private static final int DUCK_HP = 20;
    private static final int DUCK_DAMAGE = 10;

    public Duck(Vector2D position) {

        super("Duck", DUCK_HP, 0, 1.0f, position);
        this.gravityStrategy = new FloatingGravity();
    }

    @Override
    public void performAttack() {
        // Duck neútočí
    }

    @Override
    public AnimationManager getAnimationManager() {
        return null;
    }

    @Override
    public void move(Vector2D direction) {
        position = position.add(direction);
        updateHitbox();
    }

    @Override
    public void update(float deltaTime) {
        // jednoduchý pohyb po teréne
    }

    @Override
    public void onCollision(Object other) {
        // kolízia s hráčom – onPickup
    }

    public Item onKilled() {
        Random random = new Random();
        if (random.nextBoolean()) {
            return new FriendlyDuck(DUCK_DAMAGE, position);
        } else {
            return new EnemyDuck(DUCK_DAMAGE, position);
        }
    }
}
