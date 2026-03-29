package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;

public class EnemyDuck extends Item {
    private int damage;

    public EnemyDuck(int damage, Vector2D position) {
        super(0, position); // 0 slotov – aktivuje sa automaticky
        this.damage = damage;
    }

    @Override
    public void use(PlayerCharacter character) {
        character.takeDamage(damage); // utoci na hraca
    }

    @Override
    public String getIconPath() {
        return "";
    }

    public int getDamage() { return damage; }
}
