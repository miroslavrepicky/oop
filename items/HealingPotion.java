package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;

public class HealingPotion extends Item {
    private int healAmount;

    public HealingPotion(int healAmount, Vector2D position) {
        super(2, position); // 2 sloty
        this.healAmount = healAmount;
    }

    @Override
    public void use(PlayerCharacter character) {
        character.takeDamage(-healAmount); // záporný damage = heal
    }

    @Override
    public String getIconPath() { return "icons/potion.png"; }
}
