package sk.stuba.fiit.core;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.EggProjectile;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

import java.util.List;

/**
 * Riadi vsetky kolizie v aktualnom leveli.
 *
 * Zodpovednosti:
 *   - projektily hraca vs. nepriatelia a kacky
 *   - projektily nepriatelov vs. hrac
 *   - vsetky projektily vs. steny  (okrem EggProjectile)
 *   - hrac vs. itemy na zemi  (proximity pickup)
 *
 * Kolizna odozva vzdy ide cez {@link Collidable#onCollision(Object)},
 * takze jednotlive triedy si samy riadia co sa s nimi stane.
 */
public class CollisionManager {

    /** Item ktory je momentalne v dosahu hraca (pre HUD hint). */
    private Item nearbyItem = null;

    // -------------------------------------------------------------------------
    //  Hlavny vstupny bod - vola sa každý frame z GameScreen
    // -------------------------------------------------------------------------

    public void update(Level level) {
        PlayerCharacter player = GameManager.getInstance()
            .getInventory().getActive();
        if (player == null || level == null) return;

        checkNearbyItems(player, level);
        checkPlayerProjectilesVsEnemies(player, level);
        checkPlayerProjectilesVsDucks(player, level);
        checkEnemyProjectilesVsPlayer(player, level);
        checkProjectilesVsWalls(level);
    }

    // -------------------------------------------------------------------------
    //  Pickup
    // -------------------------------------------------------------------------

    /**
     * Najde item v tesnej blizkosti hraca a ulozi ho ako {@code nearbyItem}.
     * Samotne zdvihnutie spusta {@link #pickupNearbyItem} (klavesa E).
     */
    private void checkNearbyItems(PlayerCharacter player, Level level) {
        nearbyItem = null;
        for (Item item : level.getItems()) {
            if (player.getHitbox().overlaps(item.getHitbox())) {
                nearbyItem = item;
                return; // stačí prvý nájdený
            }
        }
    }

    /** Vola sa z {@link PlayerController} pri stlacení E. */
    public void pickupNearbyItem(PlayerCharacter player, Level level) {
        if (nearbyItem == null) return;
        nearbyItem.onPickup(player);
        level.getItems().remove(nearbyItem);
        nearbyItem = null;
    }

    // -------------------------------------------------------------------------
    //  Hracske projektily vs. nepriatelia
    // -------------------------------------------------------------------------

    private void checkPlayerProjectilesVsEnemies(PlayerCharacter player, Level level) {
        for (Projectile projectile : level.getProjectiles()) {
            if (!isPlayerProjectile(projectile)) continue;

            for (EnemyCharacter enemy : level.getEnemies()) {
                if (!enemy.isAlive()) continue;
                if (projectile.getHitbox().overlaps(enemy.getHitbox())) {
                    projectile.onCollision(enemy);  // Projectile.onHit() -> takeDamage + setActive(false)
                    break; // jeden zasah = koniec pre tento projektil
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Hracske projektily vs. kacky
    // -------------------------------------------------------------------------

    /**
     * Kacka sa zabije jedinym zasahom.
     * Projektil sa deaktivuje ihned, aby druhy projektil v tom istom frame
     * nemohol zabit tu istu kacku znova.
     */
    private void checkPlayerProjectilesVsDucks(PlayerCharacter player, Level level) {
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            if (!isPlayerProjectile(projectile)) continue;

            for (Duck duck : level.getDucks()) {
                if (!duck.isAlive()) continue;
                if (projectile.getHitbox().overlaps(duck.getHitbox())) {
                    killDuck(duck, projectile, level);
                    break; // projektil je mŕtvy, ďalšie kačky preskočíme
                }
            }
        }
    }

    /** Zabije kacku, spawnuje drop a deaktivuje projektil. */
    private void killDuck(Duck duck, Projectile projectile, Level level) {
        duck.takeDamage(duck.getHp());          // instant kill
        Item drop = duck.onKilled();
        if (drop != null) level.addItem(drop);
        projectile.setActive(false);            // jeden projektil, jeden kill
    }

    // -------------------------------------------------------------------------
    //  Nepriatelské projektily vs. hrac
    // -------------------------------------------------------------------------

    private void checkEnemyProjectilesVsPlayer(PlayerCharacter player, Level level) {
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            if (isPlayerProjectile(projectile)) continue;

            if (projectile.getHitbox().overlaps(player.getHitbox())) {
                projectile.onCollision(player);
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Projektily vs. steny
    // -------------------------------------------------------------------------

    /**
     * EggProjectile je stacionarny - steny ho nezaujimaju.
     * Vsetky ostatne aktivne projektily sa pri zasahu steny deaktivuju.
     */
    private void checkProjectilesVsWalls(Level level) {
        if (level.getMapManager() == null) return;
        List<Rectangle> walls = level.getMapManager().getHitboxes();

        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            if (projectile instanceof EggProjectile) continue;

            for (Rectangle wall : walls) {
                if (projectile.getHitbox().overlaps(wall)) {
                    projectile.setActive(false);
                    break;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Pomocne metody
    // -------------------------------------------------------------------------

    /**
     * Projektil je „hracsky" ak ho vystrelil PlayerCharacter.
     * Null shooter sa povazuje za hracsky.
     */
    private boolean isPlayerProjectile(Projectile projectile) {
        return !(projectile.getShooter() instanceof EnemyCharacter);
    }

    // -------------------------------------------------------------------------
    //  Gettery pre HUD
    // -------------------------------------------------------------------------

    public Item getNearbyItem() { return nearbyItem; }
}
