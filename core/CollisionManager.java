package sk.stuba.fiit.core;

import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

public class CollisionManager {

    private Item nearbyItem = null;

    public void update(Level level) {
        PlayerCharacter player = GameManager.getInstance()
            .getInventory().getActive();
        if (player == null || level == null) return;

        checkPlayerVsItems(player, level);
        checkPlayerVsDucks(player, level);
        checkProjectilesVsEnemies(level);
        checkProjectilesVsPlayer(player, level);  // nepriateľské projektily
    }

    private void checkPlayerVsItems(PlayerCharacter player, Level level) {
        nearbyItem = null;
        for (Item item : level.getItems()) {
            if (player.getHitbox().overlaps(item.getHitbox())) {
                nearbyItem = item;
                break;
            }
        }
    }

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

    /**
     * Nepriateľské projektily (šípy, kúzla) kolídujú s hráčom.
     * Projektil sám zavolá target.takeDamage() cez onCollision().
     */
    private void checkProjectilesVsPlayer(PlayerCharacter player, Level level) {
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            if (projectile.getHitbox().overlaps(player.getHitbox())) {
                projectile.onCollision(player);
            }
        }
    }

    public Item getNearbyItem() { return nearbyItem; }
}
