package sk.stuba.fiit.characters;


import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.util.Vector2D;

public class EnemyKnight extends EnemyCharacter {
    private boolean shield;

    public EnemyKnight(Vector2D position) {
        super("EnemyKnight", 120, 25, 1.5f, position, 100f, 200f);
        this.shield = true;
        this.gravityStrategy = new NormalGravity();

    }

    @Override
    public void performAttack() {
        // melee útok na hráča v dosahu
    }

    @Override
    public AnimationManager getAnimationManager() {
        return null;
    }

}
