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
        for (Item item : level.getItems()) {
            if (player.getHitbox().overlaps(item.getHitbox())) {
                item.onPickup(player);        // FriendlyDuck sa pridá do inventára;
                // EggProjectileSpawner.onPickup() nič nerobí
                level.getItems().remove(item);
                break;
            }
        }
    }

    private void checkPlayerVsDucks(PlayerCharacter player, Level level) {
        for (Duck duck : level.getDucks()) {
            if (!duck.isAlive()) continue;
            if (player.getHitbox().overlaps(duck.getHitbox())) {
                duck.takeDamage(duck.getHp()); // zabi kačku

                // DROP: FriendlyDuck alebo EggProjectileSpawner
                // Oboje sa pridáva do LEVELU (nie priamo do inventára).
                // Level.update() si potom EggProjectileSpawner skonvertuje na EggProjectile.
                // FriendlyDuck ostane v items a hráč si ho zdvihne dotykom.
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
}
