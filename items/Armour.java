package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class Armour extends Item {
    private final int defenseBonus;

    public Armour(int defenseBonus, Vector2D position) {
        super(1, position);
        this.defenseBonus = defenseBonus;
    }

    @Override
    public void use(PlayerCharacter character, Level level) {
        character.addArmor(defenseBonus);
        character.getInventory().removeItem(this);
    }

    @Override
    public String getIconPath() { return "icons/armour.png"; }

    public int getDefenseBonus() { return defenseBonus; }
}
