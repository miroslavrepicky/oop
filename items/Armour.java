package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Consumable item that permanently increases the character's armour value.
 *
 * <p>Calls {@link sk.stuba.fiit.characters.Character#addArmor(int)} on use
 * and removes itself from the inventory. Costs 1 inventory slot.
 */
public class Armour extends Item {
    private final int defenseBonus;

    public Armour(int defenseBonus, Vector2D position) {
        super(1, position);
        this.defenseBonus = defenseBonus;
    }

    @Override
    public void use(PlayerCharacter character, Level level, Inventory inventory) {
        character.addArmor(defenseBonus);
        inventory.removeItem(this);
    }

    @Override
    public String getIconPath() { return "icons/armour.png"; }

    public int getDefenseBonus() { return defenseBonus; }
}
