package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;

public class Armour extends Item {
    private int defenseBonus;

    public Armour(int defenseBonus, Vector2D position) {
        super(1, position); // 1 slot
        this.defenseBonus = defenseBonus;
    }

    /**
     * Pouzitie brnenia – zvysi aktualny armor hraca o defenseBonus,
     * maximalne do jeho maxArmor.
     */
    @Override
    public void use(PlayerCharacter character) {
        character.addArmor(defenseBonus);
        character.getInventory().removeItem(this);
    }

    @Override
    public String getIconPath() { return "icons/armour.png"; }

    public int getDefenseBonus() { return defenseBonus; }
}
