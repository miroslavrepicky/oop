package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.world.Level;

public class MeleeAttack implements Attack {
    private final int rangeTiles; // počet dlaždíc dosahu (1 = blízky melee)

    public MeleeAttack(int rangeTiles) {
        this.rangeTiles = rangeTiles;
    }

    @Override
    public void execute(Character attacker, Level level) {
        float reach = rangeTiles * 64f; // každá dlaždica = 64 px

        if (attacker instanceof PlayerCharacter) {
            // hráč trafí nepriateľov v dosahu
            PlayerCharacter player = (PlayerCharacter) attacker;
            float ax = player.getPosition().getX();
            float dirX = player.isFacingRight() ? 1f : -1f;

            for (EnemyCharacter enemy : level.getEnemies()) {
                if (!enemy.isAlive()) continue;
                float ex = enemy.getPosition().getX();
                float dist = (ex - ax) * dirX; // kladné = pred hráčom
                if (dist >= 0 && dist <= reach) {
                    enemy.takeDamage(attacker.getAttackPower());
                }
            }

        } else if (attacker instanceof EnemyCharacter) {
            // nepriateľ trafí aktívneho hráča ak je v dosahu
            PlayerCharacter player = level.getActivePlayer();
            if (player == null || !player.isAlive()) return;

            double dist = attacker.getPosition().distanceTo(player.getPosition());
            if (dist <= reach) {
                player.takeDamage(attacker.getAttackPower());
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
