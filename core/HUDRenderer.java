package sk.stuba.fiit.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.items.Item;

import java.util.List;

public class HUDRenderer {
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera hudCamera;
    private CollisionManager collisionManager;

    private static final int SLOT_COUNT    = 10;
    private static final float SLOT_SIZE   = 40f;
    private static final float SLOT_PAD    = 4f;
    private static final float SLOT_Y      = 430f; // vrch obrazovky (480 - 40 - 10)
    private static final float TOTAL_WIDTH = SLOT_COUNT * (SLOT_SIZE + SLOT_PAD) - SLOT_PAD;
    private static final float START_X     = (800f - TOTAL_WIDTH) / 2f;

    public HUDRenderer(SpriteBatch batch, CollisionManager collisionManager) {
        this.batch            = batch;
        this.collisionManager = collisionManager;
        this.font             = new BitmapFont();
        this.shapeRenderer    = new ShapeRenderer();
        this.hudCamera        = new OrthographicCamera();
        hudCamera.setToOrtho(false, 800, 480);
    }

    public void render() {
        Inventory inventory = GameManager.getInstance().getInventory();
        PlayerCharacter active = inventory.getActive();
        if (active == null) return;

        // --- ShapeRenderer: ramceky inventara ---
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        List<Item> items = inventory.getItems();
        int selectedSlot = inventory.getSelectedSlot();

        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = START_X + i * (SLOT_SIZE + SLOT_PAD);

            // zvyraznenie vybraneho slotu
            if (i == selectedSlot) {
                shapeRenderer.setColor(Color.YELLOW);
            } else {
                shapeRenderer.setColor(Color.WHITE);
            }
            shapeRenderer.rect(x, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
        }
        shapeRenderer.end();

        // --- SpriteBatch: ikony itemov + texty ---
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // ikony itemov v slotoch
        for (int i = 0; i < items.size() && i < SLOT_COUNT; i++) {
            float x = START_X + i * (SLOT_SIZE + SLOT_PAD);
            Item item = items.get(i);
            String iconPath = item.getIconPath();
            if (iconPath != null && !iconPath.isEmpty()) {
                Texture tex = new Texture(iconPath);
                batch.draw(tex, x + 4, SLOT_Y + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);
            }
        }

        // cisla slotov
        font.setColor(Color.GRAY);
        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = START_X + i * (SLOT_SIZE + SLOT_PAD);
            font.draw(batch, String.valueOf(i + 1), x + 2, SLOT_Y + SLOT_SIZE - 2);
        }

        // HP + armor
        font.setColor(Color.WHITE);
        font.draw(batch, "Active: " + active.getName(), 10, 470);

        int y = 420;
        for (int i = 0; i < inventory.getCharacters().size(); i++) {
            PlayerCharacter c = inventory.getCharacters().get(i);
            String hpText = (i + 1) + ". " + c.getName()
                + "  HP: " + c.getHp() + "/" + c.getMaxHp()
                + "  ARM: " + c.getArmor() + "/" + c.getMaxArmor();
            font.setColor(c == active ? Color.GREEN : Color.WHITE);
            font.draw(batch, hpText, 10, y);
            y -= 20;
        }

        font.setColor(Color.YELLOW);
        font.draw(batch, "Sloty: " + inventory.getUsedSlots()
            + "/" + inventory.getTotalSlots(), 10, y - 10);

        if (collisionManager != null && collisionManager.getNearbyItem() != null) {
            font.setColor(Color.CYAN);
            font.draw(batch, "[E] PICK-UP ITEM", 300, 60);
        }

        if (GameManager.getInstance().getGameState() == GameState.WIN) {
            font.setColor(Color.GOLD);
            font.draw(batch, "YOU HAVE WON!", 320, 240);
        }
        if (GameManager.getInstance().getGameState() == GameState.GAME_OVER) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER – restart...", 300, 240);
        }

        batch.end();
    }

    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}
