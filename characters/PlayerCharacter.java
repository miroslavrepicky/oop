package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;

public abstract class PlayerCharacter extends Character {


    public PlayerCharacter(String name, int hp, int attackPower, float speed, Vector2D position) {
        super(name, hp, attackPower, speed, position);
    }

    public Inventory getInventory() {
        return GameManager.getInstance().getInventory();
    }

    public abstract void handleInput();

    @Override
    public void move(Vector2D direction) {
        position = position.add(direction);
        updateHitbox();
    }

    @Override
    public void onCollision(Object other) {
        // spracovanie kolízie
    }
}
