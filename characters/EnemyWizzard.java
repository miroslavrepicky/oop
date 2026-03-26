package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.projectiles.MagicSpell;
import sk.stuba.fiit.util.Vector2D;

public class EnemyWizzard extends EnemyCharacter {
    private int mana;
    private static final int SPELL_MANA_COST = 20;

    public EnemyWizzard(Vector2D position) {
        super("EnemyWizzard", 60, 35, 1.5f, position, 100f, 350f);
        this.mana = 100;
        this.gravityStrategy = new NormalGravity();

    }

    @Override
    public void performAttack() {
        if (mana >= SPELL_MANA_COST) {
            castSpell();
        }
    }

    @Override
    public AnimationManager getAnimationManager() {
        return null;
    }

    public MagicSpell castSpell() {
        mana -= SPELL_MANA_COST;
        Vector2D direction = new Vector2D(-1, 0);
        return new MagicSpell(attackPower, 4.0f, position, direction, 50.0f);
    }

    public int getMana() { return mana; }
}
