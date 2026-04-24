package sk.stuba.fiit.attacks;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.exceptions.InvalidAttackException;
import sk.stuba.fiit.projectiles.Arrow;
import sk.stuba.fiit.projectiles.ProjectileOwner;
import sk.stuba.fiit.projectiles.ProjectilePool;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Ranged attack that spawns an {@link Arrow} projectile from the {@link ProjectilePool}.
 *
 * <p>Returns the spawned projectile so that decorators can attach on-hit effects
 * without knowing the concrete type or modifying this class's internals.
 */
public class ArrowAttack implements Attack {


    private static final Logger log = GameLogger.get(ArrowAttack.class);

    public ArrowAttack() {
    }

    /**
     * Obtains an {@link Arrow} from the pool, resets it, and adds it to the level.
     *
     * @param attacker the character performing the attack; must not be {@code null}
     * @param level    the active level; must not be {@code null}
     * @return the spawned {@link Arrow}
     * @throws InvalidAttackException if {@code attacker} is {@code null}
     */
    @Override
    public Arrow execute(Character attacker, Level level) {
        if (attacker == null) {
            throw new InvalidAttackException("unknown",
                "ArrowAttack.execute called with null attacker");
        }
        if (level == null) {
            log.warn("ArrowAttack.execute skipped – level is null: attacker={}",
                attacker.getName());
            return null;
        }
        boolean facingRight = attacker.isFacingRight();
        float dirX = facingRight ? 1f : -1f;

        Vector2D spawnPos = new Vector2D(
            attacker.getPosition().getX() + dirX * 20f,
            attacker.getPosition().getY() + 20f
        );
        Vector2D direction = new Vector2D(dirX, 0);

        Arrow arrow = ProjectilePool.getInstance().obtainArrow();
        arrow.reset(attacker.getAttackPower(), 5.0f, spawnPos, direction);

        arrow.setOwner(attacker.isEnemy()
            ? ProjectileOwner.ENEMY
            : ProjectileOwner.PLAYER);

        level.addProjectile(arrow);

        if (log.isDebugEnabled()) {
            log.debug("Arrow spawned: owner={}, dmg={}, pos=({},{})",
                arrow.getOwner(),
                attacker.getAttackPower(),
                String.format("%.1f", spawnPos.getX()),
                String.format("%.1f", spawnPos.getY()));
        }
        if (log.isDebugEnabled()) {
            log.debug("Arrow spawned: owner={}, dmg={}, pos=({},{})",
                arrow.getOwner(),
                attacker.getAttackPower(),
                String.format("%.1f", spawnPos.getX()),
                String.format("%.1f", spawnPos.getY()));
        }
        return arrow;
    }

    @Override
    public String getAnimationName() { return "attack"; }

    @Override
    public float getAnimationDuration(AnimationManager am) {
        return am != null && am.hasAnimation("attack")
            ? am.getAnimationDuration("attack")
            : 0.5f;
    }
}
