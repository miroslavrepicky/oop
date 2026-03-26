package sk.stuba.fiit.core;

import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

public class CollisionManager {

    public void update(Level level) {
        PlayerCharacter player = GameManager.getInstance()
            .getInventory()
            .getActive();
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
        for (Item item : level.getItems()) {
            if (player.getHitbox().overlaps(item.getHitbox())) {
                item.onPickup(player);
                level.getItems().remove(item);
                break; // vyhni sa ConcurrentModificationException
            }
        }
    }

    private void checkPlayerVsDucks(PlayerCharacter player, Level level) {
        for (Duck duck : level.getDucks()) {
            if (!duck.isAlive()) continue;
            if (player.getHitbox().overlaps(duck.getHitbox())) {
                // hráč sa dotkol kačky – pickup
                duck.takeDamage(duck.getHp()); // zabi kačku
                Item result = duck.onKilled();
                player.getInventory().addItem(result);
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
}
