package sk.stuba.fiit.attacks;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.exceptions.InvalidAttackException;
import sk.stuba.fiit.projectiles.MagicSpell;
import sk.stuba.fiit.projectiles.ProjectileOwner;
import sk.stuba.fiit.projectiles.ProjectilePool;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Ranged spell attack that spawns a {@link MagicSpell} AOE projectile.
 *
 * <p>Returns the spawned projectile so that decorators can attach on-hit effects
 * without knowing the concrete type or modifying this class's internals.
 */
public class SpellAttack implements Attack {

    private final float aoeRadius;
    private final float projectileSpeed;
    private final int   manaCost;

    private static final Logger log = GameLogger.get(SpellAttack.class);

    public SpellAttack(float projectileSpeed, float aoeRadius, int manaCost) {
        this.projectileSpeed = projectileSpeed;
        this.aoeRadius       = aoeRadius;
        this.manaCost        = manaCost;
    }

    /**
     * Obtains a {@link MagicSpell} from the pool, resets it, and adds it to the level.
     *
     * @param attacker the character performing the attack; must not be {@code null}
     * @param level    the active level; must not be {@code null}
     * @return the spawned {@link MagicSpell}
     * @throws InvalidAttackException if {@code attacker} is {@code null}
     */
    @Override
    public MagicSpell execute(Character attacker, Level level) {
        if (attacker == null) {
            throw new InvalidAttackException("unknown",
                "SpellAttack.execute called with null attacker");
        }
        if (level == null) {
            log.warn("SpellAttack.execute skipped – level is null: attacker={}",
                attacker.getName());
            return null;
        }
        boolean facingRight = attacker.isFacingRight();
        float dirX = facingRight ? 1f : -1f;

        Vector2D spawnPos = new Vector2D(
            attacker.getPosition().getX() + (dirX * 24f) + 1,
            attacker.getPosition().getY() + 32f
        );
        Vector2D direction = new Vector2D(dirX, 0);

        MagicSpell spell = ProjectilePool.getInstance().obtainSpell();
        spell.reset(attacker.getAttackPower(), projectileSpeed, spawnPos, direction, aoeRadius);

        spell.setOwner(attacker instanceof EnemyCharacter
            ? ProjectileOwner.ENEMY
            : ProjectileOwner.PLAYER);

        level.addProjectile(spell);

        if (log.isDebugEnabled()) {
            log.debug("Spell spawned: owner={}, AOE radius={}, dmg={}, pos=({},{})",
                spell.getOwner(), aoeRadius,
                attacker.getAttackPower(),
                String.format("%.1f", spawnPos.getX()),
                String.format("%.1f", spawnPos.getY()));
        }

        return spell;
    }

    @Override
    public String getAnimationName() { return "cast"; }

    @Override
    public float getAnimationDuration(AnimationManager am) {
        String anim = getAnimationName();
        return am != null && am.hasAnimation(anim)
            ? am.getAnimationDuration(anim)
            : 0.6f;
    }

    @Override
    public int getManaCost() { return manaCost; }
}
