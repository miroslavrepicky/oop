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
 * Renders the heads-up display: HP/armour bars, mana/arrow counters,
 * inventory slot frames, item icons, and the item pick-up hint.
 *
 * <h2>Data contract</h2>
 * <p>All data arrives via {@link RenderSnapshot.HUDSnapshot}, built by
 * {@link SnapshotBuilder}. This renderer has no dependency on
 * {@code GameManager}, {@code Inventory}, {@code PlayerCharacter}, or
 * any {@code Item} class – it operates exclusively on primitive values and
 * strings extracted by the controller layer.
 *
 * <h2>Mana and arrow bars</h2>
 * <p>Each {@link RenderSnapshot.HUDSnapshot.CharacterHUDData} carries
 * {@code mana}/{@code maxMana} and {@code arrows}/{@code maxArrows}.
 * A value of {@code -1} means "not applicable" – the corresponding bar
 * or counter is simply not drawn. This avoids any need for the renderer
 * to inspect the concrete character type.
 *
 * <h2>Coordinate system</h2>
 * <p>The HUD uses a fixed virtual resolution of 800 × 480 units mapped through
 * a dedicated {@link OrthographicCamera} that is independent of the game-world
 * camera, so HUD elements remain at stable screen positions regardless of where
 * the camera is looking in the world.
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

    /** Width of the mana/arrow resource bar drawn beside the character row. */
    private static final float RES_BAR_W      = 60f;
    private static final float RES_BAR_H      = 6f;
    private static final float RES_BAR_GAP    = 6f;
    private static final float RES_BAR_Y_OFFSET = -4f;

    private static final Color MANA_COLOR   = new Color(0.25f, 0.55f, 1.00f, 1f);
    private static final Color ARROW_COLOR  = new Color(0.85f, 0.65f, 0.10f, 1f);
    private static final Color BAR_BG_COLOR = new Color(0.15f, 0.15f, 0.15f, 0.85f);

    /**
     * @param batch the shared {@link SpriteBatch} used for text and icon rendering;
     *              must be the same instance as the one used by {@link GameRenderer}
     */
    public HUDRenderer(SpriteBatch batch) {
        this.batch        = batch;
        this.font         = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        this.hudCamera    = new OrthographicCamera();
        hudCamera.setToOrtho(false, 800, 480);
    }

    /**
     * Renders the complete HUD from the provided snapshot.
     * Has no effect when {@code hud} is {@code null} or the character list is empty.
     *
     * @param hud snapshot built by {@link SnapshotBuilder}; may be {@code null}
     */
    public void render(RenderSnapshot.HUDSnapshot hud) {
        if (hud == null || hud.characters.isEmpty()) return;

        drawSlotFrames(hud.selectedSlot);
        drawContent(hud);
    }

    // -------------------------------------------------------------------------
    //  Slot frame outlines
    // -------------------------------------------------------------------------

    /**
     * Draws the 10 inventory slot outlines along the top of the screen.
     * The currently selected slot is highlighted in yellow; others are white.
     *
     * @param selectedSlot zero-based index of the active inventory slot
     */
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
    //  Icons and text
    // -------------------------------------------------------------------------

    /**
     * Draws all HUD text and icon content inside a single {@code SpriteBatch} block:
     * item slot icons, slot number labels, the character status list,
     * the slot usage counter, and the item pick-up hint.
     *
     * @param hud the snapshot containing all data to display
     */
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

    /**
     * Draws item icons inside the inventory slot frames.
     * Icons are loaded from disk on first use and cached for subsequent frames.
     * Slots beyond the visible 10 are ignored.
     *
     * @param slots list of slot descriptors, one per inventory item
     */
    private void drawItemIcons(List<RenderSnapshot.HUDSnapshot.ItemSlotData> slots) {
        for (int i = 0; i < slots.size() && i < SLOT_COUNT; i++) {
            String iconPath = slots.get(i).iconPath;
            if (iconPath == null || iconPath.isEmpty()) continue;

            float x = START_X + i * (SLOT_SIZE + SLOT_PAD);
            Texture tex = iconCache.computeIfAbsent(iconPath, Texture::new);
            batch.draw(tex, x + 4, SLOT_Y + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);
        }
    }

    /**
     * Draws the slot index number (1–10) in the top-left corner of each slot frame.
     */
    private void drawSlotNumbers() {
        font.setColor(Color.GRAY);
        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = START_X + i * (SLOT_SIZE + SLOT_PAD);
            font.draw(batch, String.valueOf(i + 1), x + 2, SLOT_Y + SLOT_SIZE - 2);
        }
    }

    /**
     * Draws the character status list on the left side of the screen.
     * Each row shows the character's name, HP, armour, and (if applicable)
     * mana or arrow count as plain text. The currently active character is
     * also shown separately at the top.
     *
     * @param characters list of character HUD descriptors, one per party member
     */
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

            if (c.mana >= 0 && c.maxMana > 0) {
                sb.append("  MANA: ").append(c.mana).append("/").append(c.maxMana);
            } else if (c.arrows >= 0 && c.maxArrows > 0) {
                sb.append("  ARROWS: ").append(c.arrows).append("/").append(c.maxArrows);
            }

            font.setColor(c.isActive ? Color.GREEN : Color.WHITE);
            font.draw(batch, sb.toString(), 10, y);
            y -= 20f;
        }
    }

    /**
     * Draws the used/total slot counter below the character list.
     *
     * @param usedSlots  number of inventory slots currently occupied
     * @param totalSlots total number of slots available in the inventory
     */
    private void drawSlotInfo(int usedSlots, int totalSlots) {
        font.setColor(Color.YELLOW);
        font.draw(batch, "Slots: " + usedSlots + "/" + totalSlots, 10, 300);
    }

    /**
     * Releases all LibGDX resources owned by this renderer: font, shape renderer,
     * and all cached icon textures. Must be called when the renderer is no longer needed.
     */
    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
        iconCache.values().forEach(Texture::dispose);
    }
}
