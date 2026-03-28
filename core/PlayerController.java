package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class PlayerController {
    private Inventory inventory;
    private CollisionManager collisionManager;

    public PlayerController(CollisionManager collisionManager) {
        this.inventory = GameManager.getInstance().getInventory();
        this.collisionManager = collisionManager;
    }

    public void update(float deltaTime) {
        PlayerCharacter player = inventory.getActive();
        if (player == null) return;

        Level level = GameManager.getInstance().getCurrentLevel();

        player.applyGravity(deltaTime);

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.move(new Vector2D(-player.getSpeed() * deltaTime * 60, 0));
            player.setFacingRight(false);
            player.setVelocityX(-player.getSpeed());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.move(new Vector2D(player.getSpeed() * deltaTime * 60, 0));
            player.setFacingRight(true);
            player.setVelocityX(player.getSpeed());
        }
        if (!Gdx.input.isKeyPressed(Input.Keys.LEFT) &&
            !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.setVelocityX(0f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            player.jump(300f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            player.performPrimaryAttack();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            player.performSecondaryAttack();
        }

        // zdvihnutie itemu
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && level != null) {
            collisionManager.pickupNearbyItem(player, level);
        }

        // prepínanie postáv
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) inventory.switchCharacter(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) inventory.switchCharacter(2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) inventory.switchCharacter(3);

        // pauza
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            GameManager.getInstance().setGameState(GameState.PAUSED);
        }
    }
}
