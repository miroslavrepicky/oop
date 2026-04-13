package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.world.Level;

public class MeleeAttack implements Attack {
    private final int rangeTiles; // pocet dlazdic dosahu (1 = blizky melee)

    public MeleeAttack(int rangeTiles) {
        this.rangeTiles = rangeTiles;
    }

    @Override
    public void execute(Character attacker, Level level) {
        float reach = rangeTiles * 52f; // kazda dlazdica = 64 px

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
                    enemy.takeDamage(attacker.getAttackPower());
                    return;
                }
            }
            for (Duck duck : level.getDucks()) {
                if (!duck.isAlive()) continue;
                float dx = duck.getPosition().getX();
                float dist = (dx - ax) * dirX;
                if (dist >= 0 && dist <= reach) {
                    duck.takeDamage(duck.getHp()); // jeden zasah = zabitie
                    Item result = duck.onKilled();
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
