package sk.stuba.fiit.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.inventory.Inventory;

public class HUDRenderer {
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera hudCamera;

    public HUDRenderer(SpriteBatch batch) {
        this.batch = batch;
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        this.hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, 800, 480);
    }

    public void render() {
        Inventory inventory = GameManager.getInstance().getInventory();
        PlayerCharacter active = inventory.getActive();
        if (active == null) return;

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        if (GameManager.getInstance().getGameState() == GameState.WIN) {
            font.setColor(Color.GOLD);
            font.draw(batch, "VYHRAL SI!", 320, 240);
        }

        // meno aktívnej postavy
        font.setColor(Color.WHITE);
        font.draw(batch, "Aktívna: " + active.getName(), 10, 470);

        // HP všetkých postáv v inventári
        int y = 450;
        for (int i = 0; i < inventory.getCharacters().size(); i++) {
            PlayerCharacter c = inventory.getCharacters().get(i);
            String hpText = (i + 1) + ". " + c.getName() +
                "  HP: " + c.getHp() + "/" + c.getMaxHp();
            font.setColor(c == active ? Color.GREEN : Color.WHITE);
            font.draw(batch, hpText, 10, y);
            y -= 20;
        }

        // sloty inventára
        font.setColor(Color.YELLOW);
        font.draw(batch, "Sloty: " + inventory.getUsedSlots() +
            "/" + inventory.getTotalSlots(), 10, y - 10);

        if (GameManager.getInstance().getGameState() == GameState.GAME_OVER) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER – reštart...", 300, 240);
        }

        batch.end();

    }

    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}
