package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.projectiles.TurdflyProjectile;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Pickable item získaný zabitím kačky (50 % šanca).
 *
 * Keď hráč použije tento item (napr. klávesa E), vystrelí TurdflyProjectile
 * v smere ktorým hráč práve stojí.
 * Item sa po použití spotrebuje (odoberie sa z inventára).
 */
public class FriendlyDuck extends Item {

    private int damage; // zalozna hodnota; TurdflyProjectile ma vlastny damage
    private AnimationManager animationManager;

    public FriendlyDuck(int damage, Vector2D position) {
        super(1, position); // 1 slot
        this.damage = damage;
        initAnimations();
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/turdfly/turdfly.atlas");
        animationManager.addAnimation("fly", "TURDFLY/TURDFLY", 0.1f);
        animationManager.play("fly");
    }

    /**
     * Použitie: vystrelí TurdflyProjectile a odoberie item z inventára.
     */
    @Override
    public void use(PlayerCharacter character) {
        Level level = GameManager.getInstance().getCurrentLevel();
        if (level == null) return;

        float dirX = character.isFacingRight() ? 1f : -1f;
        Vector2D direction = new Vector2D(dirX, 0);
        Vector2D spawnPos  = new Vector2D(
            character.getPosition().getX() + dirX * 20f,
            character.getPosition().getY() + 10f
        );

        TurdflyProjectile turdfly = new TurdflyProjectile(spawnPos, direction);
        level.addProjectile(turdfly);

        // spotrebuj item
        character.getInventory().removeItem(this);
        System.out.println("FriendlyDuck pouzita – turdfly vystreleny!");
    }

    @Override
    public void update(float deltaTime) {
        animationManager.update(deltaTime);
    }

    public int getDamage()  { return damage; }
    public AnimationManager getAnimationManager() { return animationManager; }
}
