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

    public PlayerController() {
        this.inventory = GameManager.getInstance().getInventory();
    }

    private void performPlayerAttack(PlayerCharacter player) {
        Level level = GameManager.getInstance().getCurrentLevel();
        if (level == null) return;

        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;
            if (player.getHitbox().overlaps(enemy.getHitbox())) {
                enemy.takeDamage(player.getAttackPower());
                System.out.println("Nepriateľ HP: " + enemy.getHp());
            }
        }
    }

    public void update(float deltaTime) {

        PlayerCharacter player = inventory.getActive();
        if (player == null) return;

        player.applyGravity(deltaTime);
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.move(new Vector2D(-player.getSpeed() * deltaTime * 60, 0));
            player.setFacingRight(false);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.move(new Vector2D(player.getSpeed() * deltaTime * 60, 0));
            player.setFacingRight(true);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            player.jump(300f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            player.performAttack();
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
