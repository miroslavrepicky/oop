package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.projectiles.Arrow;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class ArrowAttack implements Attack {
    private boolean piercing;

    public ArrowAttack(boolean piercing) {
        this.piercing = piercing;
    }

    @Override
    public void execute(Character caster, Level level) {
        float dirX = caster.isFacingRight() ? 1 : -1;
        Vector2D direction = new Vector2D(dirX, 0);
        Arrow arrow = new Arrow(
            caster.getAttackPower(), 8.0f,
            new Vector2D(caster.getPosition().getX(), caster.getPosition().getY()),
            direction, piercing
        );
        level.addProjectile(arrow);
    }

    @Override
    public String getAnimationName() { return "attack"; }

    @Override
    public float getAnimationDuration(AnimationManager animManager) {
        return animManager.getAnimationDuration("attack");
    }
}
