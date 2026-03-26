package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;

public class FriendlyDuck extends Item {
    private int damage;

    public FriendlyDuck(int damage, Vector2D position) {
        super(1, position); // 1 slot
        this.damage = damage;
    }

    @Override
    public void use(PlayerCharacter character) {
        // špeciálny útok – implementuj keď budeš mať útočnú logiku
    }

    public int getDamage() { return damage; }
}
