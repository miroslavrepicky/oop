package sk.stuba.fiit.core;

import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

public class CollisionManager {

    // Item v dosahu hráča – PlayerController ho zdvihne po stlačení E
    private Item nearbyItem = null;

    public void update(Level level) {
        PlayerCharacter player = GameManager.getInstance()
            .getInventory().getActive();
        if (player == null || level == null) return;

        checkPlayerVsEnemies(player, level);
        checkPlayerVsItems(player, level);
        checkPlayerVsDucks(player, level);
        checkProjectilesVsEnemies(level);
    }

    private void checkPlayerVsEnemies(PlayerCharacter player, Level level) {
        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;
            if (player.getHitbox().overlaps(enemy.getHitbox())) {
                enemy.attack(player);
            }
        }
    }

    private void checkPlayerVsItems(PlayerCharacter player, Level level) {
        nearbyItem = null;
        for (Item item : level.getItems()) {
            if (player.getHitbox().overlaps(item.getHitbox())) {
                nearbyItem = item; // uloží referenciu, ale NEZDVIHNE automaticky
                break;
            }
        }
    }

    /**
     * Volá PlayerController po stlačení E.
     * Zdvihne nearbyItem ak existuje a je miesto v inventári.
     */
    public void pickupNearbyItem(PlayerCharacter player, Level level) {
        if (nearbyItem == null) return;
        nearbyItem.onPickup(player);
        level.getItems().remove(nearbyItem);
        nearbyItem = null;
    }

    private void checkPlayerVsDucks(PlayerCharacter player, Level level) {
        for (Duck duck : level.getDucks()) {
            if (!duck.isAlive()) continue;
            if (player.getHitbox().overlaps(duck.getHitbox())) {
                duck.takeDamage(duck.getHp());
                Item result = duck.onKilled();
                level.addItem(result);
                level.getDucks().remove(duck);
                break;
            }
        }
    }

    private void checkProjectilesVsEnemies(Level level) {
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            for (EnemyCharacter enemy : level.getEnemies()) {
                if (!enemy.isAlive()) continue;
                if (projectile.getHitbox().overlaps(enemy.getHitbox())) {
                    projectile.onCollision(enemy);
                }
            }
        }
    }

    public Item getNearbyItem() { return nearbyItem; }
}
