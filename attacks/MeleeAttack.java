package sk.stuba.fiit.attacks;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.world.Level;


/**
 * Melee attack that damages the nearest target within a tile-based reach.
 *
 * <p>When executed by a {@code PlayerCharacter}, hits the closest living enemy
 * or duck in the direction the player is facing. When executed by an
 * {@code EnemyCharacter}, hits the active player if they are within range.
 *
 * <p>Range is expressed in tiles: {@code reach = rangeTiles * 52f} pixels.
 */
public class MeleeAttack implements Attack {
    /** Number of tiles that define the attack reach. */
    private final int rangeTiles;
    private static final Logger log = GameLogger.get(MeleeAttack.class);

    public MeleeAttack(int rangeTiles) {
        this.rangeTiles = rangeTiles;
    }

    @Override
    public void execute(Character attacker, Level level) {
        float reach = rangeTiles * 52f;

        if (attacker instanceof PlayerCharacter) {
            // hrac trafi nepriatelov v dosahu
            PlayerCharacter player = (PlayerCharacter) attacker;
            float ax = player.getPosition().getX();
            float dirX = player.isFacingRight() ? 1f : -1f;

            for (EnemyCharacter enemy : level.getEnemies()) {
                if (!enemy.isAlive()) continue;
                float ex = enemy.getPosition().getX();
                float dist = (ex - ax) * dirX; // kladne = pred hracom
                if (dist >= 0 && dist <= reach) {
                    if (log.isDebugEnabled()) {
                        log.debug("Melee hit enemy: attacker={}, target={}, dist={}, dmg={}",
                            attacker.getName(), enemy.getName(),
                            String.format("%.1f", dist),
                            attacker.getAttackPower());
                    }
                    enemy.takeDamage(attacker.getAttackPower());
                    return;
                }
            }
            for (Duck duck : level.getDucks()) {
                if (!duck.isAlive()) continue;
                float dx = duck.getPosition().getX();
                float dist = (dx - ax) * dirX;
                if (dist >= 0 && dist <= reach) {
                    if (log.isDebugEnabled()) {
                        log.debug("Melee hit duck: attacker={}, dist={}, dmg=instant kill",
                            attacker.getName(),
                            String.format("%.1f", dist));
                    }
                    duck.takeDamage(duck.getHp()); // jeden zasah = zabitie
                    Item result = duck.onKilled();
                    if (result != null) {
                        log.info("Duck killed – drop: item={}, pos=({},{})",
                            result.getClass().getSimpleName(),
                            String.format("%.1f", duck.getPosition().getX()),
                            String.format("%.1f", duck.getPosition().getY()));
                    }
                    level.addItem(result);
                    return;
                }
            }

        } else if (attacker instanceof EnemyCharacter) {
            // nepriatel trafi aktivneho hraca ak je v dosahu
            PlayerCharacter player = level.getActivePlayer();
            if (player == null || !player.isAlive()) return;

            double dist = attacker.getPosition().distanceTo(player.getPosition());
            if (dist <= reach) {
                if (log.isDebugEnabled()) {
                    log.debug("Enemy melee hit player: attacker={}, target={}, dist={}, dmg={}",
                        attacker.getName(), player.getName(),
                        String.format("%.1f", dist),
                        attacker.getAttackPower());
                }
                player.takeDamage(attacker.getAttackPower());
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("Enemy melee missed player: attacker={}, dist={}, reach={}",
                        attacker.getName(),
                        String.format("%.1f", dist),
                        reach);
                }
            }
        }
    }

    @Override
    public String getAnimationName() { return "attack"; }

    @Override
    public float getAnimationDuration(AnimationManager am) {
        return am != null && am.hasAnimation("attack")
            ? am.getAnimationDuration("attack")
            : 0.4f;
    }
}
