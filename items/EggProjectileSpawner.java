package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;

/**
 * Marker item vytvorený keď kačka padne a vylosuje sa vajce (50 %).
 *
 * Tento item sa NEDÁ zobrať do inventára (onPickup nič nerobí).
 * Level.update() ho zachytí cez instanceof check, vytvorí EggProjectile
 * na jeho pozícii a tento item okamžite odstráni zo scény.
 *
 * Prečo marker a nie priamy spawn?
 *   Duck.onKilled() vracia Item – aby sme nemuseli meniť signatúru a CollisionManager.
 *   Level si potom sám rozhodne čo s markerom spraviť.
 */
public class EggProjectileSpawner extends Item {

    public EggProjectileSpawner(Vector2D position) {
        super(0, position); // 0 slotov – neberie sa do inventára
    }

    @Override
    public void use(PlayerCharacter character) {
        // nič – nikdy sa nepoužíva priamo
    }

    /**
     * Zablokuj pickup – vajce sa nedá zobrať, iba spawnuje výbuch.
     */
    @Override
    public String getIconPath() { return null; }

    @Override
    public void onPickup(PlayerCharacter character) {
        // nič
    }
}
