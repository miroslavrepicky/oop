package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.projectiles.TurdflyProjectile;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Pickable item ziskany zabitim kacky (50 % sanca).
 *
 * Ked hrac pouzije tento item (napr. klavesa E), vystreli TurdflyProjectile
 * v smere ktorym hrac prave stoji.
 * Item sa po pouziti spotrebuje (odoberie sa z inventara).
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
     * Pouzitie: vystreli TurdflyProjectile a odoberie item z inventara.
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
        turdfly.setShooter(character);
        level.addProjectile(turdfly);

        // spotrebuj item
        character.getInventory().removeItem(this);
        System.out.println("FriendlyDuck pouzita -> turdfly vystreleny!");
    }

    @Override
    public void update(float deltaTime) {
        animationManager.update(deltaTime);
    }

    @Override
    public String getIconPath() { return "icons/duck.png"; }

    public int getDamage()  { return damage; }
    public AnimationManager getAnimationManager() { return animationManager; }
}
