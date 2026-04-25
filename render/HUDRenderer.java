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
 * Kresli HUD (HP, zbroj, mana, sipy, inventár, pick-up hint).
 *
 * Po refaktore: ziadna závislost na GameManager, Inventory, PlayerCharacter
 * ani Item. Vsetky dáta pridu cez {@link RenderSnapshot.HUDSnapshot} DTO,
 * ktory zostavi {@link SnapshotBuilder}.
 *
 * <h2>Mana a sipy</h2>
 * <p>Kazdy {@link RenderSnapshot.HUDSnapshot.CharacterHUDData} nesie
 * {@code mana}/{@code maxMana} a {@code arrows}/{@code maxArrows}.
 * Hodnota {@code -1} znamená „neaplikovatelne" – bar sa nevykresli.
 * Tym sa renderer nemusi pytat na konkretny typ postavy.
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

    //  Resource bar constants (mana / arrows)
    /** Width of the mana/arrow bar drawn next to the character row. */
    private static final float RES_BAR_W  = 60f;
    private static final float RES_BAR_H  = 6f;
    /** Horizontal gap between the text line and the bar. */
    private static final float RES_BAR_GAP = 6f;
    /** Vertical offset of the bar center relative to the text baseline. */
    private static final float RES_BAR_Y_OFFSET = -4f;

    private static final Color MANA_COLOR   = new Color(0.25f, 0.55f, 1.00f, 1f);
    private static final Color ARROW_COLOR  = new Color(0.85f, 0.65f, 0.10f, 1f);
    private static final Color BAR_BG_COLOR = new Color(0.15f, 0.15f, 0.15f, 0.85f);

    public HUDRenderer(SpriteBatch batch) {
        this.batch        = batch;
        this.font         = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        this.hudCamera    = new OrthographicCamera();
        hudCamera.setToOrtho(false, 800, 480);
    }

    /**
     * Vykresli cely HUD z predpripraveneho snapshotu.
     * ziadne volania na GameManager ani Inventory.
     *
     * @param hud snapshot zostaveny cez SnapshotBuilder; moze byt null
     */
    public void render(RenderSnapshot.HUDSnapshot hud) {
        if (hud == null || hud.characters.isEmpty()) return;

        drawSlotFrames(hud.selectedSlot);
        drawContent(hud);
    }

    // -------------------------------------------------------------------------
    //  Sukromne – rámceky slotov
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
    //  Sukromne – ikony, texty
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

        for (RenderSnapshot.HUDSnapshot.CharacterHUDData c : characters) {
            if (c.isActive) {
                font.setColor(Color.WHITE);
                font.draw(batch, "Active: " + c.name, 10, 470);
                break;
            }
        }

        float y = 420f;
        for (int i = 0; i < characters.size(); i++) {
            RenderSnapshot.HUDSnapshot.CharacterHUDData c = characters.get(i);

            StringBuilder sb = new StringBuilder();
            sb.append(i + 1).append(". ").append(c.name)
                .append("  HP: ").append(c.hp).append("/").append(c.maxHp)
                .append("  ARM: ").append(c.armor).append("/").append(c.maxArmor);

            // Doplnok za HP/ARM – mana alebo sipy (ciselne)
            if (c.mana >= 0 && c.maxMana > 0) {
                sb.append("  MANA: ").append(c.mana).append("/").append(c.maxMana);
            } else if (c.arrows >= 0 && c.maxArrows > 0) {
                sb.append("  SIPY: ").append(c.arrows).append("/").append(c.maxArrows);
            }

            font.setColor(c.isActive ? Color.GREEN : Color.WHITE);
            font.draw(batch, sb.toString(), 10, y);
            y -= 20f;
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
