package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.projectiles.ProjectileOwner;
import sk.stuba.fiit.projectiles.ProjectilePool;
import sk.stuba.fiit.projectiles.TurdflyProjectile;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Pickable item získaný zabitím kačky (50 % šanca).
 *
 * Keď hráč použije tento item, vystrelí {@link TurdflyProjectile}
 * v smere ktorým hráč práve stojí.
 * Item sa po použití spotrebuje.
 */
public class FriendlyDuck extends Item {

    private final int damage;
    private AnimationManager animationManager;

    public FriendlyDuck(int damage, Vector2D position) {
        super(1, position);
        this.damage = damage;
        initAnimations();
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/turdfly/turdfly.atlas");
        animationManager.addAnimation("fly", "TURDFLY/TURDFLY", 0.1f);
        animationManager.play("fly");
    }

    /**
     * Vystrelí TurdflyProjectile do aktuálneho levelu.
     * Level je predaný zvonku – item nemusí volať GameManager.
     */
    @Override
    public void use(PlayerCharacter character, Level level) {
        if (level == null) return;

        float     dirX      = character.isFacingRight() ? 1f : -1f;
        Vector2D  direction = new Vector2D(dirX, 0);
        Vector2D  spawnPos  = new Vector2D(
            character.getPosition().getX() + dirX * 20f,
            character.getPosition().getY() + 10f
        );

        TurdflyProjectile turdfly = ProjectilePool.getInstance().obtainTurdfly();
        turdfly.reset(spawnPos, direction);
        turdfly.setOwner(ProjectileOwner.PLAYER);
        level.addProjectile(turdfly);

        character.getInventory().removeItem(this);
        System.out.println("FriendlyDuck použitá -> turdfly vystrelený!");
    }

    @Override
    public void update(UpdateContext ctx) {
        animationManager.update(ctx.deltaTime);
    }

    @Override
    public String getIconPath() { return "icons/duck.png"; }

    public int             getDamage()            { return damage; }
    public AnimationManager getAnimationManager() { return animationManager; }
}
