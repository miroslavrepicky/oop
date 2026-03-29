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
     * Použitie brnenia – zvýši aktuálny armor hráča o defenseBonus,
     * maximálne do jeho maxArmor.
     */
    @Override
    public void use(PlayerCharacter character) {
        character.addArmor(defenseBonus);
    }

    @Override
    public String getIconPath() { return "icons/armour.png"; }

    public int getDefenseBonus() { return defenseBonus; }
}
