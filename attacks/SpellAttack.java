package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.projectiles.MagicSpell;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class SpellAttack implements Attack {
    private float spellSpeed;
    private float aoeRadius;
    private int manaCost;

    public SpellAttack(float spellSpeed, float aoeRadius, int manaCost) {
        this.spellSpeed = spellSpeed;
        this.aoeRadius = aoeRadius;
        this.manaCost = manaCost;
    }

    @Override
    public void execute(Character caster, Level level) {
        float dirX = caster.isFacingRight() ? 1 : -1;
        Vector2D direction = new Vector2D(dirX, 0);
        MagicSpell spell = new MagicSpell(
            caster.getAttackPower(), spellSpeed,
            new Vector2D(caster.getPosition().getX(), caster.getPosition().getY()),
            direction, aoeRadius
        );
        level.addProjectile(spell);
    }

    @Override
    public String getAnimationName() { return "cast"; }

    @Override
    public float getAnimationDuration(AnimationManager animManager) {
        return animManager.getAnimationDuration("cast");
    }

    public int getManaCost() { return manaCost; }
}
