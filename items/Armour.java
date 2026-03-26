package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;

public class Armour extends Item {
    private int defenseBonus;

    public Armour(int defenseBonus, Vector2D position) {
        super(1, position); // 1 slot
        this.defenseBonus = defenseBonus;
    }

    @Override
    public void use(PlayerCharacter character) {
        // zníži prijaté poškodenie – implementuj keď budeš mať obranný systém
    }

    public int getDefenseBonus() { return defenseBonus; }
}
