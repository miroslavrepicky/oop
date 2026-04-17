package sk.stuba.fiit.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.items.Item;

import java.util.List;

/**
 * Kreslí HUD (HP, zbroj, inventár, pick-up hint).
 *
 * Zmena oproti pôvodnému kódu:
 *  - Závislosť na {@code CollisionManager} odstránená.
 *    Namiesto toho {@code render()} dostáva jednoduchý boolean
 *    {@code nearbyItemAvailable}. Kto volá render, ten vie či je
 *    item v blízkosti – HUDRenderer to nepotrebuje zisťovať sám.
 *  - Inventory sa stále číta z GameManager, čo je akceptovateľné
 *    pre high-level renderer (alternatíva: predať Inventory ako parameter).
 */
public class HUDRenderer {

    private final SpriteBatch        batch;
    private final BitmapFont         font;
    private final ShapeRenderer      shapeRenderer;
    private final OrthographicCamera hudCamera;

    private static final int   SLOT_COUNT    = 10;
    private static final float SLOT_SIZE     = 40f;
    private static final float SLOT_PAD      = 4f;
    private static final float SLOT_Y        = 430f;
    private static final float TOTAL_WIDTH   = SLOT_COUNT * (SLOT_SIZE + SLOT_PAD) - SLOT_PAD;
    private static final float START_X       = (800f - TOTAL_WIDTH) / 2f;

    public HUDRenderer(SpriteBatch batch) {
        this.batch        = batch;
        this.font         = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        this.hudCamera    = new OrthographicCamera();
        hudCamera.setToOrtho(false, 800, 480);
    }

    /**
     * @param nearbyItemAvailable true = v blízkosti je item → zobraz "[E] PICK-UP ITEM"
     */
    public void render(boolean nearbyItemAvailable) {
        Inventory inventory = GameManager.getInstance().getInventory();
        PlayerCharacter active = inventory.getActive();
        if (active == null) return;

        drawSlotFrames(inventory);
        drawContent(inventory, active, nearbyItemAvailable);
    }

    // -------------------------------------------------------------------------
    //  Súkromné – rámčeky slotov
    // -------------------------------------------------------------------------

    private void drawSlotFrames(Inventory inventory) {
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        int selectedSlot = inventory.getSelectedSlot();
        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = START_X + i * (SLOT_SIZE + SLOT_PAD);
            shapeRenderer.setColor(i == selectedSlot ? Color.YELLOW : Color.WHITE);
            shapeRenderer.rect(x, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
        }
        shapeRenderer.end();
    }

    // -------------------------------------------------------------------------
    //  Súkromné – ikony, texty
    // -------------------------------------------------------------------------

    private void drawContent(Inventory inventory, PlayerCharacter active,
                             boolean nearbyItemAvailable) {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        drawItemIcons(inventory.getItems());
        drawSlotNumbers();
        drawCharacterList(inventory, active);
        drawSlotInfo(inventory);

        if (nearbyItemAvailable) {
            font.setColor(Color.CYAN);
            font.draw(batch, "[E] PICK-UP ITEM", 300, 60);
        }

        batch.end();
    }

    private void drawItemIcons(List<Item> items) {
        for (int i = 0; i < items.size() && i < SLOT_COUNT; i++) {
            float x = START_X + i * (SLOT_SIZE + SLOT_PAD);
            String iconPath = items.get(i).getIconPath();
            if (iconPath != null && !iconPath.isEmpty()) {
                Texture tex = new Texture(iconPath);
                batch.draw(tex, x + 4, SLOT_Y + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);
            }
        }
    }

    private void drawSlotNumbers() {
        font.setColor(Color.GRAY);
        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = START_X + i * (SLOT_SIZE + SLOT_PAD);
            font.draw(batch, String.valueOf(i + 1), x + 2, SLOT_Y + SLOT_SIZE - 2);
        }
    }

    private void drawCharacterList(Inventory inventory, PlayerCharacter active) {
        font.setColor(Color.WHITE);
        font.draw(batch, "Active: " + active.getName(), 10, 470);

        int y = 420;
        for (int i = 0; i < inventory.getCharacters().size(); i++) {
            PlayerCharacter c = inventory.getCharacters().get(i);
            String hpText = (i + 1) + ". " + c.getName()
                + "  HP: "  + c.getHp()    + "/" + c.getMaxHp()
                + "  ARM: " + c.getArmor() + "/" + c.getMaxArmor();
            font.setColor(c == active ? Color.GREEN : Color.WHITE);
            font.draw(batch, hpText, 10, y);
            y -= 20;
        }
    }

    private void drawSlotInfo(Inventory inventory) {
        font.setColor(Color.YELLOW);
        font.draw(batch,
            "Sloty: " + inventory.getUsedSlots() + "/" + inventory.getTotalSlots(),
            10, 300);
    }

    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}
