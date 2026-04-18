package sk.stuba.fiit.attacks;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.projectiles.Arrow;
import sk.stuba.fiit.projectiles.ProjectileOwner;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class ArrowAttack implements Attack {
    private final boolean piercing;

    private static final Logger log = GameLogger.get(ArrowAttack.class);

    public ArrowAttack(boolean piercing) {
        this.piercing = piercing;
    }

    @Override
    public void execute(Character attacker, Level level) {
        boolean facingRight = attacker.isFacingRight();
        float dirX = facingRight ? 1f : -1f;

        Vector2D spawnPos = new Vector2D(
            attacker.getPosition().getX() + dirX * 20f,
            attacker.getPosition().getY() + 20f
        );
        Vector2D direction = new Vector2D(dirX, 0);

        Arrow arrow = new Arrow(
            attacker.getAttackPower(),
            5.0f,
            spawnPos,
            direction,
            piercing
        );

        // Nastav vlastníka podľa typu útočníka
        arrow.setOwner(attacker instanceof EnemyCharacter
            ? ProjectileOwner.ENEMY
            : ProjectileOwner.PLAYER);

        level.addProjectile(arrow);

        if (log.isDebugEnabled()) {
            log.debug("Arrow spawned: owner={}, piercing={}, dmg={}, pos=({},{})",
                arrow.getOwner(), piercing,
                attacker.getAttackPower(),
                String.format("%.1f", spawnPos.getX()),
                String.format("%.1f", spawnPos.getY()));
        }
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
