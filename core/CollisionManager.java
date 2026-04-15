package sk.stuba.fiit.core;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.AoeProjectile;
import sk.stuba.fiit.projectiles.EggProjectile;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;


/**
 * Riadi vsetky kolizie v aktualnom leveli.
 *
 * Zodpovednosti:
 *   - projektily hraca vs. nepriatelia, kacky a steny  (single-hit aj AOE)
 *   - projektily nepriatelov vs. hrac a steny
 *   - EggProjectile AOE vybuchy
 *   - hrac vs. itemy na zemi  (proximity pickup)
 *   - hrac vs. nepriatelia  (push)
 *
 * Kolizna odozva vzdy ide cez {@link Collidable#onCollision(Object)},
 * takze jednotlive triedy si samy riadia co sa s nimi stane.
 */
public class CollisionManager {

    /** Item ktory je momentalne v dosahu hraca (pre HUD hint). */
    private Item nearbyItem = null;

    // -------------------------------------------------------------------------
    //  Hlavny vstupny bod - vola sa kazdy frame z GameScreen
    // -------------------------------------------------------------------------

    public void update(Level level) {
        PlayerCharacter player = GameManager.getInstance()
            .getInventory().getActive();
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

    /**
     * Najde item v tesnej blizkosti hraca a ulozi ho ako {@code nearbyItem}.
     * Samotne zdvihnutie spusta {@link #pickupNearbyItem} (klavesa E).
     */
    private void checkNearbyItems(PlayerCharacter player, Level level) {
        nearbyItem = null;
        for (Item item : level.getItems()) {
            if (player.getHitbox().overlaps(item.getHitbox())) {
                nearbyItem = item;
                return; // staci prvy najdeny
            }
        }
    }

    /** Vola sa z {@link PlayerController} pri stlaceni E. */
    public void pickupNearbyItem(PlayerCharacter player, Level level) {
        if (nearbyItem == null) return;
        nearbyItem.onPickup(player);
        level.getItems().remove(nearbyItem);
        nearbyItem = null;
    }

    // -------------------------------------------------------------------------
    //  Hracske projektily  (nepriatelia + kacky + steny, single-hit aj AOE)
    // -------------------------------------------------------------------------

    private void checkPlayerProjectiles(Level level) {
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            if (!isPlayerProjectile(projectile)) continue;
            if (projectile instanceof EggProjectile) continue; // riesi checkEggExplosions

            Object hitTarget = resolveHit(projectile, level);
            if (hitTarget != null) {
                triggerImpact(projectile, hitTarget, level);
            }
        }
    }

    /**
     * Zisti co projektil zasiahol – nepriatel, kacka alebo stena.
     * Vracia prvy najdeny ciel, alebo null ak projektil nic nezasiahol.
     * Priorita: nepriatelia > kacky > steny.
     */
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

    /**
     * Rozhodne ci ide o AOE alebo single-hit dopad a aplikuje poskodenie.
     *
     * OPRAVA: AOE projektil pri priamom zasahu nepriatela:
     *   1) priamy zasah dostane plny damage (bez falloff)
     *   2) ostatni v radiuse dostanu AOE damage s falloff (okrem priameho cela)
     *
     * Pri zasahu steny dostanu nepriatelia len AOE falloff damage.
     */
    private void triggerImpact(Projectile projectile, Object hitTarget, Level level) {
        if (projectile instanceof AoeProjectile) {
            AoeProjectile aoe = (AoeProjectile) projectile;

            if (hitTarget instanceof EnemyCharacter) {
                // PRIAMY ZASAH nepriatela: plny damage + AOE na ostatnych
                EnemyCharacter directHit = (EnemyCharacter) hitTarget;
                directHit.takeDamage(aoe.getDamage());
                applyAoeExcluding(
                    projectile.getPosition().getX(),
                    projectile.getPosition().getY(),
                    aoe.getAoeRadius(),
                    aoe.getDamage(),
                    level,
                    directHit  // tento nepriatel uz dostal plny damage
                );
            } else if (hitTarget instanceof Duck) {
                // Kacky sa zabiaju jednym zasahom vzdy
                killDuck((Duck) hitTarget, projectile, level);
                applyAoe(
                    projectile.getPosition().getX(),
                    projectile.getPosition().getY(),
                    aoe.getAoeRadius(),
                    aoe.getDamage(),
                    level
                );
            } else {
                // Stena: len AOE falloff pre vsetkych v radiuse
                applyAoe(
                    projectile.getPosition().getX(),
                    projectile.getPosition().getY(),
                    aoe.getAoeRadius(),
                    aoe.getDamage(),
                    level
                );
            }
        } else {
            applySingleHit(projectile, hitTarget, level);
        }
        projectile.setActive(false);
    }

    /**
     * Plosne poskodenie – zasiahne vsetkych nepriatelov a kacky v polomere
     * s poklesom poskodenia podla vzdialenosti (falloff).
     * Pouziva stred hitboxu namiesto pozicie (lavy dolny roh) pre presnejsiu detekciu.
     */
    private void applyAoe(float cx, float cy, float radius, int damage, Level level) {
        applyAoeExcluding(cx, cy, radius, damage, level, null);
    }

    /**
     * AOE damage pre vsetkych nepriatelov v radiuse, okrem excludedEnemy
     * (ktory uz dostal priamy damage).
     * Meria vzdialenost od stredu hitboxu nepriatela, nie od jeho pozicie.
     */
    private void applyAoeExcluding(float cx, float cy, float radius, int damage,
                                   Level level, EnemyCharacter excludedEnemy) {
        Vector2D centre = new Vector2D(cx, cy);

        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;
            if (enemy == excludedEnemy) continue;

            // Meriame od stredu hitboxu nepriatela (nie lavy dolny roh)
            float enemyCenterX = enemy.getPosition().getX() + enemy.getHitbox().width / 2f;
            float enemyCenterY = enemy.getPosition().getY() + enemy.getHitbox().height / 2f;
            Vector2D enemyCenter = new Vector2D(enemyCenterX, enemyCenterY);

            double dist = centre.distanceTo(enemyCenter);
            if (dist <= radius) {
                float falloff = 1f - (float)(dist / radius);
                enemy.takeDamage(Math.max(1, (int)(damage * falloff)));
            }
        }

        for (Duck duck : level.getDucks()) {
            if (!duck.isAlive()) continue;
            float duckCenterX = duck.getPosition().getX() + duck.getHitbox().width / 2f;
            float duckCenterY = duck.getPosition().getY() + duck.getHitbox().height / 2f;
            Vector2D duckCenter = new Vector2D(duckCenterX, duckCenterY);

            double dist = centre.distanceTo(duckCenter);
            if (dist <= radius) {
                duck.takeDamage(duck.getHp());
                Item drop = duck.onKilled();
                if (drop != null) level.addItem(drop);
            }
        }
    }

    /**
     * Single-hit dopad – poskodi prave zasiahnute zariadenie.
     * Stena sposobi len deaktivaciu projektilu, ziadny damage.
     */
    private void applySingleHit(Projectile projectile, Object hitTarget, Level level) {
        if (hitTarget instanceof EnemyCharacter) {
            projectile.onCollision(hitTarget);
        } else if (hitTarget instanceof Duck) {
            killDuck((Duck) hitTarget, projectile, level);
        }
        // Rectangle (stena) -> len setActive(false) v triggerImpact, ziadny damage
    }

    // -------------------------------------------------------------------------
    //  Nepriatelske projektily  (hrac + steny)
    // -------------------------------------------------------------------------

    /**
     * Nepriatelske projektily sa spracovavaju spolocne – hrac aj steny
     * v jednej iteracii, aby sme cez zoznam projektily nechodili dvakrat.
     */
    private void checkEnemyProjectiles(PlayerCharacter player, Level level) {
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            if (isPlayerProjectile(projectile)) continue;
            if (projectile instanceof EggProjectile) continue;

            if (projectile.getHitbox().overlaps(player.getHitbox())) {
                projectile.onCollision(player);
            } else if (hitsWall(projectile, level)) {
                projectile.setActive(false);
            }
        }
    }

    // -------------------------------------------------------------------------
    //  EggProjectile AOE vybuchy
    //
    //  EggProjectile si riadi vlastny casovac a animacie (to je jeho
    //  zodpovednost). CollisionManager len sleduje prechod do stavu BLASTING
    //  a aplikuje AOE damage – bez akejkolvek referencie na GameManager
    //  v samotnom projektile.
    // -------------------------------------------------------------------------

    private void checkEggExplosions(PlayerCharacter player, Level level) {
        for (Projectile p : level.getProjectiles()) {
            if (!(p instanceof EggProjectile)) continue;
            EggProjectile egg = (EggProjectile) p;
            if (egg.getEggState() != EggProjectile.EggState.BLASTING) continue;
            if (egg.isDamageDealt()) continue;

            // AOE na nepriatelov a kacky
            applyAoe(egg.getPosition().getX(),
                egg.getPosition().getY(),
                egg.getAoeRadius(),
                egg.getDamage(),
                level);

            // AOE aj na hraca – vajce je nepriatelsky "projektil"
            if (player != null) {
                double dist = egg.getPosition()
                    .distanceTo(player.getPosition());
                if (dist <= egg.getAoeRadius()) {
                    float falloff = 1f - (float)(dist / egg.getAoeRadius());
                    player.takeDamage(Math.max(1,
                        (int)(egg.getDamage() * falloff)));
                }
            }

            egg.markDamageDealt();
        }
    }

    // -------------------------------------------------------------------------
    //  Hrac vs. nepriatelia – odsun (push)
    // -------------------------------------------------------------------------

    private void checkPlayerVsEnemyPush(PlayerCharacter player, Level level) {
        Rectangle playerBox = player.getHitbox();

        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;
            Rectangle enemyBox = enemy.getHitbox();
            if (!playerBox.overlaps(enemyBox)) continue;

            float overlapX =
                Math.min(playerBox.x + playerBox.width,  enemyBox.x + enemyBox.width)
                    - Math.max(playerBox.x, enemyBox.x);
            float overlapY =
                Math.min(playerBox.y + playerBox.height, enemyBox.y + enemyBox.height)
                    - Math.max(playerBox.y, enemyBox.y);

            if (overlapX <= overlapY) {
                float push = playerBox.x < enemyBox.x ? -overlapX : overlapX;
                player.getPosition().setX(player.getPosition().getX() + push);
                player.updateHitbox();
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Pomocne metody
    // -------------------------------------------------------------------------

    /** Zabije kacku, prida drop do levelu a deaktivuje projektil. */
    private void killDuck(Duck duck, Projectile projectile, Level level) {
        duck.takeDamage(duck.getHp());
        Item drop = duck.onKilled();
        if (drop != null) level.addItem(drop);
        projectile.setActive(false);
    }

    /** Skontroluje ci projektil narazil do niektorej steny v mape. */
    private boolean hitsWall(Projectile projectile, Level level) {
        if (level.getMapManager() == null) return false;
        for (Rectangle wall : level.getMapManager().getHitboxes()) {
            if (projectile.getHitbox().overlaps(wall)) return true;
        }
        return false;
    }

    /**
     * Projektil je "hracsky" ak ho vystrelil PlayerCharacter.
     * Null shooter sa povazuje za hracsky (napr. debug projektily).
     */
    private boolean isPlayerProjectile(Projectile projectile) {
        return !(projectile.getShooter() instanceof EnemyCharacter);
    }

    // -------------------------------------------------------------------------
    //  Gettery pre HUD
    // -------------------------------------------------------------------------

    public Item getNearbyItem() { return nearbyItem; }
}
