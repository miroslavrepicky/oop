package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.projectiles.MagicSpell;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class Wizzard extends PlayerCharacter {
    private int mana;
    private int maxMana;
    private static final int SPELL_MANA_COST = 20;

    public Wizzard(Vector2D position) {
        super("Wizzard", 70, 40, 2.5f, position);
        this.mana = 100;
        this.maxMana = 100;
        this.gravityStrategy = new NormalGravity();

    }

    @Override
    public void performAttack() {
        if (mana >= SPELL_MANA_COST) return;

        Level level = GameManager.getInstance().getCurrentLevel();
        if (level == null) return;

        mana -= SPELL_MANA_COST;
        float dirX = isFacingRight() ? 1 : -1;
        Vector2D direction = new Vector2D(dirX, 0);
        MagicSpell spell = new MagicSpell(attackPower, 6.0f,
            new Vector2D(position.getX(), position.getY()),
            direction, 50f);
        level.addProjectile(spell);
    }

    @Override
    public AnimationManager getAnimationManager() {
        return null;
    }

    public MagicSpell castSpell() {
        mana -= SPELL_MANA_COST;
        Vector2D direction = new Vector2D(1, 0);
        return new MagicSpell(attackPower, 4.0f, position, direction, 50.0f);
    }

    @Override
    public void handleInput() {
        // spracovanie vstupu hráča
    }

    @Override
    public void update(float deltaTime) {
        handleInput();
        regenerateMana(deltaTime);
    }

    private void regenerateMana(float deltaTime) {
        mana = Math.min(maxMana, mana + (int)(5 * deltaTime));
    }

    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
}
