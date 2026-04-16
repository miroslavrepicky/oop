package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class HealingPotion extends Item {
    private final int healAmount;

    public HealingPotion(int healAmount, Vector2D position) {
        super(2, position);
        this.healAmount = healAmount;
    }

    @Override
    public void use(PlayerCharacter character, Level level) {
        if (character.getHp() >= character.getMaxHp()) return;
        character.takeDamage(-healAmount);
        character.getInventory().removeItem(this);
    }

    @Override
    public String getIconPath() { return "icons/potion.png"; }
}
