package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
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
        float moveX = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX = -player.getSpeed() * deltaTime * 60;
            player.setFacingRight(false);
            player.setVelocityX(-player.getSpeed());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX = player.getSpeed() * deltaTime * 60;
            player.setFacingRight(true);
            player.setVelocityX(player.getSpeed());
        }
        if (!Gdx.input.isKeyPressed(Input.Keys.LEFT) &&
            !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.setVelocityX(0f);
        }

        // Horizontalna kolizia so stenami
        if (moveX != 0f && level != null && level.getMapManager() != null) {
            float newX = player.getPosition().getX() + moveX;
            Rectangle testBox = new Rectangle(
                newX,
                player.getPosition().getY(),
                player.getHitbox().width,
                player.getHitbox().height
            );
            boolean blockedX = false;
            for (Rectangle wall : level.getMapManager().getHitboxes()) {
                if (testBox.overlaps(wall)) {
                    // skontroluj ci je to skutocne horizontalna bariera
                    float overlapX = Math.min(testBox.x + testBox.width, wall.x + wall.width)
                        - Math.max(testBox.x, wall.x);
                    float overlapY = Math.min(testBox.y + testBox.height, wall.y + wall.height)
                        - Math.max(testBox.y, wall.y);
                    if (overlapX < overlapY) { // X je mensi prienik -> bocna stena
                        blockedX = true;
                        break;
                    }
                }
            }
            if (!blockedX) {
                player.move(new Vector2D(moveX, 0));
            }
        } else if (moveX != 0f) {
            player.move(new Vector2D(moveX, 0));
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            inventory.useSelected(player);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            inventory.selectPrevious();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            inventory.selectNext();
        }

        // prepinanie postav
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) inventory.switchCharacter(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) inventory.switchCharacter(2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) inventory.switchCharacter(3);

        // pauza
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            GameManager.getInstance().setGameState(GameState.PAUSED);
        }
    }
}
