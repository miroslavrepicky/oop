package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.projectiles.Arrow;
import sk.stuba.fiit.util.Vector2D;

public class EnemyArcher extends EnemyCharacter {
    private int arrowCount;

    public EnemyArcher(Vector2D position) {
        super("EnemyArcher", 70, 15, 2.0f, position, 150f, 300f);
        this.arrowCount = 20;
        this.gravityStrategy = new NormalGravity();

    }

    @Override
    public void performAttack() {
        if (arrowCount > 0) {
            shootArrow();
        }
    }

    @Override
    public AnimationManager getAnimationManager() {
        return null;
    }

    public Arrow shootArrow() {
        arrowCount--;
        Vector2D direction = new Vector2D(-1, 0); // strieľa smerom k hráčovi
        return new Arrow(attackPower, 5.0f, position, direction, false);
    }

}
