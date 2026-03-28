package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.world.Level;

public class MeleeAttack implements Attack {
    private int damageMultiplier;

    public MeleeAttack(int damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public void execute(Character caster, Level level) {
        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;
            if (caster.getHitbox().overlaps(enemy.getHitbox())) {
                enemy.takeDamage(caster.getAttackPower() * damageMultiplier);
            }
        }
    }

    @Override
    public String getAnimationName() { return "attack"; }

    @Override
    public float getAnimationDuration(AnimationManager animManager) {
        return animManager.getAnimationDuration("attack");
    }
}
