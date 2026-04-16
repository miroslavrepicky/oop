package sk.stuba.fiit.core.engine;

import sk.stuba.fiit.characters.PlayerCharacter;

public interface Pickable {
    void onPickup(PlayerCharacter character);
}
