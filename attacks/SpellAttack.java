package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.projectiles.MagicSpell;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class SpellAttack implements Attack {
    private final float aoeRadius;
    private final float projectileSpeed;
    private final int   manaCost;

    public SpellAttack(float projectileSpeed, float aoeRadius, int manaCost) {
        this.projectileSpeed = projectileSpeed;
        this.aoeRadius       = aoeRadius;
        this.manaCost        = manaCost;
    }

    @Override
    public void execute(Character attacker, Level level) {
        boolean facingRight = attacker.isFacingRight();
        float dirX = facingRight ? 1f : -1f;

        Vector2D spawnPos = new Vector2D(
            attacker.getPosition().getX() + (dirX * 24f) + 1,
            attacker.getPosition().getY() + 32f
        );
        Vector2D direction = new Vector2D(dirX, 0);

        MagicSpell spell = new MagicSpell(
            attacker.getAttackPower(),
            projectileSpeed,
            spawnPos,
            direction,
            aoeRadius
        );
        spell.setShooter(attacker);
        level.addProjectile(spell);
    }

    @Override
    public String getAnimationName() { return "cast"; }

    @Override
    public float getAnimationDuration(AnimationManager am) {
        String anim = getAnimationName();
        return am != null && am.hasAnimation(anim)
            ? am.getAnimationDuration(anim)
            : 0.6f;
    }

    @Override
    public int getManaCost() { return manaCost; }
}
