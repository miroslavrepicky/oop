package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.util.Collections;
import java.util.List;

/**
 * Spracúva vstup od hráča a aplikuje ho na aktívnu postavu.
 *
 * Zmeny oproti pôvodnému kódu:
 *  - {@code applyGravity()} dostáva platformy priamo z mapy
 *  - {@code inventory.useSelected()} dostáva Level ako parameter
 *  - Žiadne volanie {@code GameManager} okrem získania inventory/level
 *    na začiatku update() – to je akceptovateľné (controller je high-level)
 */
public class PlayerController {
    private final Inventory inventory;
    private final CollisionManager collisionManager;

    public PlayerController(CollisionManager collisionManager) {
        this.inventory        = GameManager.getInstance().getInventory();
        this.collisionManager = collisionManager;
    }

    public void update(float deltaTime) {
        PlayerCharacter player = inventory.getActive();
        if (player == null) return;

        Level level = GameManager.getInstance().getCurrentLevel();

        // --- Platformy pre gravitáciu – vypočítané raz ---
        List<Rectangle> platforms = (level != null && level.getMapManager() != null)
            ? level.getMapManager().getHitboxes()
            : Collections.emptyList();

        // --- Gravitácia – platformy predané priamo ---
        player.applyGravity(deltaTime, platforms);

        // --- Horizontálny pohyb ---
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

        // --- Horizontálna kolízia so stenami ---
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

        // --- Skok ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            player.jump(300f);
        }

        // --- Útok ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            player.performPrimaryAttack();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            player.performSecondaryAttack();
        }

        // --- Zdvihnutie itemu ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && level != null) {
            collisionManager.pickupNearbyItem(player, level);
        }

        // --- Použitie itemu – Level predaný do useSelected ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            inventory.useSelected(player, level);
        }

        // --- Výber slotu ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) inventory.selectPrevious();
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) inventory.selectNext();

        // --- Prepínanie postáv ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) inventory.switchCharacter(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) inventory.switchCharacter(2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) inventory.switchCharacter(3);

        player.updateAnimation(deltaTime);
    }
}
