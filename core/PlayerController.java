package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.util.Collections;
import java.util.List;

/**
 * Processes player input and applies it to the active character each frame.
 *
 * <p>Input mappings:
 * <ul>
 *   <li>LEFT / RIGHT – horizontal movement</li>
 *   <li>UP – jump</li>
 *   <li>SPACE – primary attack</li>
 *   <li>V – secondary attack</li>
 *   <li>E – pick up nearby item</li>
 *   <li>Q – use selected inventory item</li>
 *   <li>A / D – cycle inventory slot selection</li>
 *   <li>1 / 2 / 3 – switch active party character</li>
 * </ul>
 *
 * <p>Platform data is retrieved once per frame from the map and passed directly
 * to gravity and collision methods, avoiding calls to {@code GameManager} for
 * physics data.
 */
public class PlayerController {
    private final Inventory inventory;
    private final CollisionManager collisionManager;

    /**
     * @param collisionManager used to resolve item pickup on the {@code E} key press
     */
    public PlayerController(CollisionManager collisionManager) {
        this.inventory        = GameManager.getInstance().getInventory();
        this.collisionManager = collisionManager;
    }

    /**
     * Updates player input, applies gravity, resolves horizontal collisions,
     * handles jump/attack/item actions, and drives the character animation.
     *
     * @param deltaTime time elapsed since the last frame in seconds
     */
    public void update(float deltaTime) {
        PlayerCharacter player = inventory.getActive();
        if (player == null) return;
        Level level = GameManager.getInstance().getCurrentLevel();

        // Fetch platforms once per frame for gravity and collision
        List<Rectangle> platforms = (level != null && level.getMapManager() != null)
            ? level.getMapManager().getHitboxes()
            : Collections.emptyList();
        if (!player.isAlive()){
            player.applyGravity(deltaTime, platforms);
            UpdateContext ctx = new UpdateContext(deltaTime, platforms, level, player, inventory);
            player.update(ctx);
            player.updateAnimation(ctx);
            return;
        }

        // Apply gravity using the pre-fetched platform list
        player.applyGravity(deltaTime, platforms);
        player.tickEffects(deltaTime);

        // Horizontal movement
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

        // Horizontal wall collision
        if (moveX != 0f && !platforms.isEmpty()) {
            float newX = player.getPosition().getX() + moveX;
            Rectangle testBox = new Rectangle(
                newX,
                player.getPosition().getY(),
                player.getHitbox().width,
                player.getHitbox().height
            );
            boolean blockedX = false;
            for (Rectangle wall : platforms) {
                if (testBox.overlaps(wall)) {
                    float overlapX = Math.min(testBox.x + testBox.width,  wall.x + wall.width)
                        - Math.max(testBox.x, wall.x);
                    float overlapY = Math.min(testBox.y + testBox.height, wall.y + wall.height)
                        - Math.max(testBox.y, wall.y);
                    if (overlapX < overlapY) {
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

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            player.jump(300f);
        }

        // Attack
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            player.performPrimaryAttack(level);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            player.performSecondaryAttack(level);
        }

        // Item pickup
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && level != null) {
            collisionManager.pickupNearbyItem(player, level, inventory);
        }

        // Use selected item – level is passed directly, not fetched via GameManager
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            inventory.useSelected(player, level);
        }

        // Slot selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) inventory.selectPrevious();
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) inventory.selectNext();

        // Character switching
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) inventory.switchCharacter(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) inventory.switchCharacter(2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) inventory.switchCharacter(3);

        UpdateContext ctx = new UpdateContext(deltaTime, platforms, level, player, inventory);
        player.update(ctx);
        player.updateAnimation(ctx);
    }
}
