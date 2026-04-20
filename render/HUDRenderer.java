package sk.stuba.fiit.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kreslí HUD (HP, zbroj, inventár, pick-up hint).
 *
 * Po refaktore: žiadna závislosť na GameManager, Inventory, PlayerCharacter
 * ani Item. Všetky dáta prídu cez {@link RenderSnapshot.HUDSnapshot} DTO,
 * ktorý zostaví {@link SnapshotBuilder}.
 */
public class HUDRenderer {

    private final SpriteBatch        batch;
    private final BitmapFont         font;
    private final ShapeRenderer      shapeRenderer;
    private final OrthographicCamera hudCamera;
    private final Map<String, Texture> iconCache = new HashMap<>();

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
     * Vykreslí celý HUD z predpripraveného snapshotu.
     * Žiadne volania na GameManager ani Inventory.
     *
     * @param hud snapshot zostavený cez SnapshotBuilder; môže byť null
     */
    public void render(RenderSnapshot.HUDSnapshot hud) {
        if (hud == null || hud.characters.isEmpty()) return;

        drawSlotFrames(hud.selectedSlot);
        drawContent(hud);
    }

    // -------------------------------------------------------------------------
    //  Súkromné – rámčeky slotov
    // -------------------------------------------------------------------------

    private void drawSlotFrames(int selectedSlot) {
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

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

    private void drawContent(RenderSnapshot.HUDSnapshot hud) {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        drawItemIcons(hud.itemSlots);
        drawSlotNumbers();
        drawCharacterList(hud.characters);
        drawSlotInfo(hud.usedSlots, hud.totalSlots);

        if (hud.nearbyItemAvailable) {
            font.setColor(Color.CYAN);
            font.draw(batch, "[E] PICK-UP ITEM", 300, 60);
        }

        batch.end();
    }

    private void drawItemIcons(List<RenderSnapshot.HUDSnapshot.ItemSlotData> slots) {
        for (int i = 0; i < slots.size() && i < SLOT_COUNT; i++) {
            String iconPath = slots.get(i).iconPath;
            if (iconPath == null || iconPath.isEmpty()) continue;

            float x = START_X + i * (SLOT_SIZE + SLOT_PAD);
            Texture tex = iconCache.computeIfAbsent(iconPath, Texture::new);
            batch.draw(tex, x + 4, SLOT_Y + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);
        }
    }

    private void drawSlotNumbers() {
        font.setColor(Color.GRAY);
        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = START_X + i * (SLOT_SIZE + SLOT_PAD);
            font.draw(batch, String.valueOf(i + 1), x + 2, SLOT_Y + SLOT_SIZE - 2);
        }
    }

    private void drawCharacterList(
        List<RenderSnapshot.HUDSnapshot.CharacterHUDData> characters) {

        // Nájdeme aktívnu postavu pre riadok "Active: ..."
        for (RenderSnapshot.HUDSnapshot.CharacterHUDData c : characters) {
            if (c.isActive) {
                font.setColor(Color.WHITE);
                font.draw(batch, "Active: " + c.name, 10, 470);
                break;
            }
        }

        int y = 420;
        for (int i = 0; i < characters.size(); i++) {
            RenderSnapshot.HUDSnapshot.CharacterHUDData c = characters.get(i);
            String hpText = (i + 1) + ". " + c.name
                + "  HP: "  + c.hp    + "/" + c.maxHp
                + "  ARM: " + c.armor + "/" + c.maxArmor;
            font.setColor(c.isActive ? Color.GREEN : Color.WHITE);
            font.draw(batch, hpText, 10, y);
            y -= 20;
        }
    }

    private void drawSlotInfo(int usedSlots, int totalSlots) {
        font.setColor(Color.YELLOW);
        font.draw(batch, "Sloty: " + usedSlots + "/" + totalSlots, 10, 300);
    }

    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
        iconCache.values().forEach(Texture::dispose);
    }
}
