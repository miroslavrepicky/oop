package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.projectiles.ProjectileOwner;
import sk.stuba.fiit.projectiles.ProjectilePool;
import sk.stuba.fiit.projectiles.TurdflyProjectile;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Consumable item obtained by killing a duck (50 % drop chance).
 *
 * <p>When used, fires a {@link TurdflyProjectile} in the direction the player
 * is currently facing. Gets the projectile from {@link ProjectilePool} and
 * resets it before adding it to the level. Removes itself from the inventory
 * after a single use.
 *
 * <p>The level is passed as a parameter so this item does not depend on {@code GameManager}.
 */
public class FriendlyDuck extends Item {
    private AnimationManager animationManager;

    public FriendlyDuck(Vector2D position) {
        super(1, position);

        initAnimations();
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/turdfly/turdfly.atlas");
        animationManager.addAnimation("fly", "TURDFLY/TURDFLY", 0.1f);
        animationManager.play("fly");
    }

    /**
     * Shoot TurdflyProjectile in the direction the player is currently facing.
     */
    @Override
    public void use(PlayerCharacter character, Level level, Inventory inventory) {
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

        inventory.removeItem(this);
        System.out.println("FriendlyDuck used -> turdfly shot!");
    }

    @Override
    public void update(UpdateContext ctx) {
        animationManager.update(ctx.deltaTime);
    }

    @Override
    public String getIconPath() { return "icons/duck.png"; }

}
