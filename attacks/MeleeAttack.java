package sk.stuba.fiit.attacks;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.projectiles.MeleeHitbox;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.projectiles.ProjectileOwner;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Melee attack that spawns an invisible {@link MeleeHitbox} in the level.
 *
 * <p>Previously this class resolved collisions and dealt damage directly in
 * {@code execute()}, coupling attack logic to character lists. After refactoring:
 * <ol>
 *   <li>{@code execute()} computes the hit area in front of the attacker and
 *       adds a {@link MeleeHitbox} to the level (same as any other projectile).</li>
 *   <li>{@code CollisionManager} picks up the hitbox in the same frame and deals
 *       damage to whatever it overlaps – enemies, ducks, or the player.</li>
 *   <li>Because {@link MeleeHitbox#isSingleUse()} is {@code true}, the hitbox is
 *       always deactivated by {@code CollisionManager} after one pass, whether it
 *       hit something or not.</li>
 * </ol>
 *
 * <p>Duck killing (with item drop) is now handled uniformly by
 * {@code CollisionManager.applySingleHit()} – no special case needed here.
 */
public class MeleeAttack implements Attack {

    private final float rangeTiles;
    private static final Logger log = GameLogger.get(MeleeAttack.class);

    /**
     * @param rangeTiles horizontal reach in tiles; one tile ≈ 52 px
     */
    public MeleeAttack(float rangeTiles) {
        this.rangeTiles = rangeTiles;
    }

    /**
     * Spawns a {@link MeleeHitbox} covering the melee reach in front of the attacker.
     *
     * <p>Hit-area geometry:
     * <ul>
     *   <li><b>Width</b> – {@code rangeTiles * 26} px</li>
     *   <li><b>Height</b> – matches the attacker's current hitbox height</li>
     *   <li><b>X</b> – starts at the leading edge of the attacker's hitbox
     *       (right when facing right, left-minus-reach when facing left)</li>
     *   <li><b>Y</b> – aligned with the bottom of the attacker's hitbox</li>
     * </ul>
     *
     * @return the spawned {@link MeleeHitbox} (never {@code null})
     */
    @Override
    public Projectile execute(Character attacker, Level level) {
        float reach = rangeTiles * 26f;

        boolean facingRight = attacker.isFacingRight();
        float   hitboxH     = attacker.getHitbox().height;

        // Leading edge of the attacker's hitbox
        float leadX = attacker.getHitbox().x
            + (facingRight ? attacker.getHitbox().width : 0f);

        float hitX = facingRight ? leadX : leadX - reach;
        float hitY = attacker.getHitbox().y;

        ProjectileOwner owner = (attacker instanceof EnemyCharacter)
            ? ProjectileOwner.ENEMY
            : ProjectileOwner.PLAYER;

        MeleeHitbox box = new MeleeHitbox(
            attacker.getAttackPower(),
            new Vector2D(hitX, hitY),
            reach, hitboxH,
            owner
        );

        level.addProjectile(box);

        if (log.isDebugEnabled()) {
            log.debug("MeleeHitbox spawned: owner={}, reach={}, x={}, y={}, h={}",
                owner, reach,
                String.format("%.1f", hitX),
                String.format("%.1f", hitY),
                String.format("%.1f", hitboxH));
        }

        return box;
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
