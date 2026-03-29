package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.projectiles.Arrow;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class ArrowAttack implements Attack {
    private final boolean piercing;

    public ArrowAttack(boolean piercing) {
        this.piercing = piercing;
    }

    @Override
    public void execute(Character attacker, Level level) {
        boolean facingRight = attacker.isFacingRight();
        float dirX = facingRight ? 1f : -1f;

        Vector2D spawnPos = new Vector2D(
            attacker.getPosition().getX() + dirX * 20f,
            attacker.getPosition().getY() + 10f
        );
        Vector2D direction = new Vector2D(dirX, 0);

        Arrow arrow = new Arrow(
            attacker.getAttackPower(),
            5.0f,
            spawnPos,
            direction,
            piercing
        );

        // nepriateľské šípy budú kolídovať s hráčom – to rieši CollisionManager
        arrow.setShooter(attacker);
        level.addProjectile(arrow);
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
