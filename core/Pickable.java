package sk.stuba.fiit.core;

import sk.stuba.fiit.characters.PlayerCharacter;

public interface Pickable {
    void onPickup(PlayerCharacter character);
}
