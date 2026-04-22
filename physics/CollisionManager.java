package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import org.slf4j.Logger;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.AoeProjectile;
import sk.stuba.fiit.projectiles.EggProjectile;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Manages all collision detection and response for the active level.
 *
 * <p>On-hit effects (DOT, slow) carried by projectiles are applied here directly
 * to the hit {@link Character} via {@link Character#applyDot} and
 * {@link Character#applySlow}. No separate status-effect list is needed.
 */
public class CollisionManager {

    private Item nearbyItem = null;
    private static final Logger log = GameLogger.get(CollisionManager.class);

    public void update(Level level, PlayerCharacter player) {
        if (player == null || level == null) return;

        checkNearbyItems(player, level);
        checkPlayerProjectiles(level);
        checkEnemyProjectiles(player, level);
        checkEggExplosions(player, level);
        checkPlayerVsEnemyPush(player, level);
    }

    // -------------------------------------------------------------------------
    //  Pickup
    // -------------------------------------------------------------------------

    private void checkNearbyItems(PlayerCharacter player, Level level) {
        nearbyItem = null;
        for (Item item : level.getItems()) {
            if (player.getHitbox().overlaps(item.getHitbox())) {
                nearbyItem = item;
                return;
            }
        }
    }

    public void pickupNearbyItem(PlayerCharacter player, Level level, Inventory inventory) {
        if (nearbyItem == null) {
            log.debug("pickupNearbyItem called but no nearby item");
            return;
        }
        boolean picked = nearbyItem.onPickup(player, inventory);
        if (picked) {
            log.info("Item picked up: item={}, player={}",
                nearbyItem.getClass().getSimpleName(), player.getName());
            level.getItems().remove(nearbyItem);
            nearbyItem = null;
        } else {
            log.warn("Item pickup failed – inventory full: item={}, player={}",
                nearbyItem.getClass().getSimpleName(), player.getName());
            // item zostáva v leveli
        }
    }

    // -------------------------------------------------------------------------
    //  Player projectiles
    // -------------------------------------------------------------------------

    private void checkPlayerProjectiles(Level level) {
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            if (!projectile.isPlayerProjectile()) continue;
            if (projectile instanceof EggProjectile) continue;

            Object hitTarget = resolveHit(projectile, level);
            if (hitTarget != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Projectile hit: target={}, pos=({},{})",
                        hitTarget.getClass().getSimpleName(),
                        projectile.getPosition().getX(),
                        projectile.getPosition().getY());
                }
                triggerImpact(projectile, hitTarget, level);
            } else if (projectile.isSingleUse()) {
                // Single-use projectile missed everything (e.g., MeleeHitbox) – deactivate.
                projectile.setActive(false);
            }
        }
    }

    private Object resolveHit(Projectile projectile, Level level) {
        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;
            if (projectile.getHitbox().overlaps(enemy.getHitbox())) return enemy;
        }
        for (Duck duck : level.getDucks()) {
            if (!duck.isAlive()) continue;
            if (projectile.getHitbox().overlaps(duck.getHitbox())) return duck;
        }
        if (level.getMapManager() != null) {
            for (Rectangle wall : level.getMapManager().getHitboxes()) {
                if (projectile.getHitbox().overlaps(wall)) return wall;
            }
        }
        return null;
    }

    private void triggerImpact(Projectile projectile, Object hitTarget, Level level) {
        if (projectile instanceof AoeProjectile) {
            AoeProjectile aoe = (AoeProjectile) projectile;

            if (hitTarget instanceof EnemyCharacter) {
                EnemyCharacter directHit = (EnemyCharacter) hitTarget;
                directHit.takeDamage(aoe.getDamage());
                applyOnHitEffects(projectile, directHit);
                applyAoeExcluding(
                    projectile.getPosition().getX(),
                    projectile.getPosition().getY(),
                    aoe.getAoeRadius(), aoe.getDamage(), level, directHit);
            } else if (hitTarget instanceof Duck) {
                killDuck((Duck) hitTarget, projectile, level);
                applyAoe(
                    projectile.getPosition().getX(),
                    projectile.getPosition().getY(),
                    aoe.getAoeRadius(), aoe.getDamage(), level);
            } else {
                applyAoe(
                    projectile.getPosition().getX(),
                    projectile.getPosition().getY(),
                    aoe.getAoeRadius(), aoe.getDamage(), level);
            }
        } else {
            applySingleHit(projectile, hitTarget, level);
        }
        projectile.setActive(false);
    }

    /**
     * Applies DOT and slow effects carried by the projectile to the hit character.
     */
    private void applyOnHitEffects(Projectile projectile, Character target) {
        if (projectile.hasDotEffect()) {
            target.applyDot(projectile.getDotDps(), projectile.getDotDuration());
        }
        if (projectile.hasSlowEffect()) {
            target.applySlow(projectile.getSlowMultiplier(), projectile.getSlowDuration());
        }
    }

    private void applyAoe(float cx, float cy, float radius, int damage, Level level) {
        applyAoeExcluding(cx, cy, radius, damage, level, null);
    }

    private void applyAoeExcluding(float cx, float cy, float radius, int damage,
                                   Level level, EnemyCharacter excludedEnemy) {
        Vector2D centre = new Vector2D(cx, cy);

        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive() || enemy == excludedEnemy) continue;
            float ex    = enemy.getPosition().getX() + enemy.getHitbox().width  / 2f;
            float ey    = enemy.getPosition().getY() + enemy.getHitbox().height / 2f;
            double dist = centre.distanceTo(new Vector2D(ex, ey));
            if (dist <= radius) {
                float falloff = 1f - (float)(dist / radius);
                enemy.takeDamage(Math.max(1, (int)(damage * falloff)));
            }
        }

        for (Duck duck : level.getDucks()) {
            if (!duck.isAlive()) continue;
            float dx    = duck.getPosition().getX() + duck.getHitbox().width  / 2f;
            float dy    = duck.getPosition().getY() + duck.getHitbox().height / 2f;
            double dist = centre.distanceTo(new Vector2D(dx, dy));
            if (dist <= radius) {
                duck.takeDamage(duck.getHp());
                Item drop = duck.onKilled();
                if (drop != null) level.addItem(drop);
            }
        }
    }

    private void applySingleHit(Projectile projectile, Object hitTarget, Level level) {
        if (hitTarget instanceof EnemyCharacter) {
            EnemyCharacter enemy = (EnemyCharacter) hitTarget;
            projectile.onCollision(enemy);
            applyOnHitEffects(projectile, enemy);
        } else if (hitTarget instanceof Duck) {
            killDuck((Duck) hitTarget, projectile, level);
        }
    }

    // -------------------------------------------------------------------------
    //  Enemy projectiles
    // -------------------------------------------------------------------------

    private void checkEnemyProjectiles(PlayerCharacter player, Level level) {
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            if (projectile.isPlayerProjectile()) continue;
            if (projectile instanceof EggProjectile) continue;

            if (projectile.getHitbox().overlaps(player.getHitbox())) {
                projectile.onCollision(player);
            } else if (hitsWall(projectile, level)) {
                projectile.setActive(false);
            }

            // Single-use projectile (e.g. enemy MeleeHitbox) must be deactivated
            // after one pass regardless of whether it connected or not.
            if (projectile.isSingleUse()) {
                projectile.setActive(false);
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Egg explosions
    // -------------------------------------------------------------------------

    private void checkEggExplosions(PlayerCharacter player, Level level) {
        for (Projectile p : level.getProjectiles()) {
            if (!(p instanceof EggProjectile)) continue;
            EggProjectile egg = (EggProjectile) p;
            if (egg.getEggState() != EggProjectile.EggState.BLASTING) continue;
            if (egg.isDamageDealt()) continue;

            applyAoe(egg.getPosition().getX(), egg.getPosition().getY(),
                egg.getAoeRadius(), egg.getDamage(), level);

            if (player != null) {
                double dist = egg.getPosition().distanceTo(player.getPosition());
                if (dist <= egg.getAoeRadius()) {
                    float falloff = 1f - (float)(dist / egg.getAoeRadius());
                    player.takeDamage(Math.max(1, (int)(egg.getDamage() * falloff)));
                }
            }

            egg.markDamageDealt();
        }
    }

    // -------------------------------------------------------------------------
    //  Player vs enemy push
    // -------------------------------------------------------------------------

    private void checkPlayerVsEnemyPush(PlayerCharacter player, Level level) {
        Rectangle playerBox = player.getHitbox();
        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;
            Rectangle enemyBox = enemy.getHitbox();
            if (!playerBox.overlaps(enemyBox)) continue;

            float overlapX = Math.min(playerBox.x + playerBox.width,  enemyBox.x + enemyBox.width)
                - Math.max(playerBox.x, enemyBox.x);
            float overlapY = Math.min(playerBox.y + playerBox.height, enemyBox.y + enemyBox.height)
                - Math.max(playerBox.y, enemyBox.y);

            if (overlapX <= overlapY) {
                float push = playerBox.x < enemyBox.x ? -overlapX : overlapX;
                player.getPosition().setX(player.getPosition().getX() + push);
                player.updateHitbox();
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Helpers
    // -------------------------------------------------------------------------

    private void killDuck(Duck duck, Projectile projectile, Level level) {
        duck.takeDamage(duck.getHp());
        Item drop = duck.onKilled();
        if (drop != null) level.addItem(drop);
        projectile.setActive(false);
    }

    private boolean hitsWall(Projectile projectile, Level level) {
        if (level.getMapManager() == null) return false;
        for (Rectangle wall : level.getMapManager().getHitboxes()) {
            if (projectile.getHitbox().overlaps(wall)) return true;
        }
        return false;
    }

    public Item getNearbyItem() { return nearbyItem; }
}
