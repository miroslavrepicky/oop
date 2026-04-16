package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Marker item vytvoreny ked kacka padne a vylosuje sa vajce (50 %).
 * Tento item sa NEDa zobrat do inventara (onPickup nic nerobi).
 * Level.update() ho zachyti cez instanceof check, vytvori EggProjectile
 * na jeho pozicii a tento item okamzite odstrani zo sceny.
 * Preco marker a nie priamy spawn?
 *   Duck.onKilled() vracia Item – aby sme nemuseli menit signaturu a CollisionManager.
 *   Level si potom sam rozhodne co s markerom spravit.
 */
public class EggProjectileSpawner extends Item {

    public EggProjectileSpawner(Vector2D position) {
        super(0, position); // 0 slotov – neberie sa do inventara
    }

    @Override
    public void use(PlayerCharacter character, Level level) {

    }

    /**
     * Zablokuj pickup – vajce sa neda zobrat, iba spawnuje vybuch.
     */
    @Override
    public String getIconPath() { return null; }

    @Override
    public void onPickup(PlayerCharacter character) {
        // nic
    }
}
