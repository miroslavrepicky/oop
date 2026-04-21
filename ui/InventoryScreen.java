package sk.stuba.fiit.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.Archer;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.characters.Wizzard;
import sk.stuba.fiit.core.AppController;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.items.Armour;
import sk.stuba.fiit.items.HealingPotion;
import sk.stuba.fiit.items.Item;

import sk.stuba.fiit.util.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Pre-level inventory management screen.
 *
 * <h2>MVC placement</h2>
 * <p>This class is a <b>View + input handler</b>. It reads the shared
 * {@link Inventory} from {@link GameManager} to display available characters
 * and items, and lets the player add or remove them before starting a level.
 *
 * <p>Reading the inventory for display purposes is acceptable View behaviour.
 * The only write operations performed here are on the {@link Inventory} object
 * itself (add/remove character, add/remove item), which is part of the model –
 * these are inventory <em>management</em> actions, not game-flow actions, so
 * they belong here rather than in {@link AppController}.
 *
 * <p>Navigation and level-start business logic are fully delegated to the
 * {@link AppController}. The screen never calls
 * {@link GameManager#startLevel(int)}.
 */
public class InventoryScreen implements Screen {

    // ── Layout constants ──────────────────────────────────────────────────────

    private static final float W     = 800f;
    private static final float H     = 480f;
    private static final float PAD   = 20f;
    private static final float ROW_H = 40f;
    private static final float BTN_W = 90f;
    private static final float BTN_H = 28f;

    /** Horizontal centre of the virtual canvas. */
    private static final float MID = W / 2f;

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final AppController app;
    private final int           levelToStart;

    // ── LibGDX rendering objects ──────────────────────────────────────────────

    private final OrthographicCamera cam;
    private final SpriteBatch        batch;
    private final ShapeRenderer      shape;
    private final BitmapFont         font;

    // ── Fixed buttons ─────────────────────────────────────────────────────────

    private final Rectangle btnStart;
    private final Rectangle btnSave;

    // ── Data ──────────────────────────────────────────────────────────────────

    /**
     * Characters that exist in the game's character pool but are not yet in
     * the inventory. Rebuilds whenever the player adds or removes a character.
     */
    private final List<PlayerCharacter> availableChars = new ArrayList<>();

    /**
     * Item templates that can be added to the inventory.
     * Each offer carries a factory lambda so every "add" creates a fresh instance.
     */
    private final List<ItemOffer> offers = new ArrayList<>();

    // ── Dynamic button hit-boxes (rebuilt after every inventory change) ────────

    private final List<Rectangle> addCharBtns = new ArrayList<>();
    private final List<Rectangle> remCharBtns = new ArrayList<>();
    private final List<Rectangle> addItemBtns = new ArrayList<>();
    private final List<Rectangle> remItemBtns = new ArrayList<>();

    /** One-line status message shown at the bottom of the screen. */
    private String feedback = "";

    // ── Inner helper ──────────────────────────────────────────────────────────

    /**
     * Descriptor for a single item type available for purchase in this screen.
     */
    private static class ItemOffer {
        final String           label;
        final int              slotCost;
        final Supplier<Item>   factory;

        ItemOffer(String label, int slotCost, Supplier<Item> factory) {
            this.label    = label;
            this.slotCost = slotCost;
            this.factory  = factory;
        }
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param app          the application controller used for navigation;
     *                     must not be {@code null}
     * @param levelToStart the 1-based level that will be started when the
     *                     player presses "Start"
     */
    public InventoryScreen(AppController app, int levelToStart) {
        this.app          = app;
        this.levelToStart = levelToStart;

        cam = new OrthographicCamera();
        cam.setToOrtho(false, W, H);

        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        font  = new BitmapFont();
        font.getData().setScale(1.1f);

        btnStart = new Rectangle(W - PAD - 180, PAD,      180, 36);
        btnSave  = new Rectangle(W - PAD - 180, PAD + 46, 180, 36);

        offers.add(new ItemOffer("HealingPotion", 2,
            () -> new HealingPotion(50, new Vector2D(0, 0))));
        offers.add(new ItemOffer("Armour", 1,
            () -> new Armour(30, new Vector2D(0, 0))));

        buildAvailable();
        rebuildButtons();
    }

    // ── Data helpers ──────────────────────────────────────────────────────────

    /**
     * Populates {@link #availableChars} with characters that are in the global
     * character pool but not yet in the current inventory.
     */
    private void buildAvailable() {
        Inventory inv = GameManager.getInstance().getInventory();
        availableChars.clear();

        List<PlayerCharacter> pool = List.of(
            new Wizzard(new Vector2D(0, 0)),
            new Archer(new Vector2D(0, 0))
        );

        for (PlayerCharacter c : pool) {
            boolean alreadyIn = inv.getCharacters().stream()
                .anyMatch(ic -> ic.getClass() == c.getClass());
            if (!alreadyIn) availableChars.add(c);
        }
    }

    /**
     * Recomputes all dynamic button hit-boxes based on the current inventory
     * and available character/item lists. Must be called after every inventory
     * change.
     *
     * <p>Coordinate note: {@code y} increases upward in LibGDX; the first row
     * starts at a high {@code y} value and each subsequent row decreases it.
     */
    private void rebuildButtons() {
        addCharBtns.clear();
        remCharBtns.clear();
        addItemBtns.clear();
        remItemBtns.clear();

        float y = 390f;
        for (int i = 0; i < availableChars.size(); i++, y -= ROW_H) {
            addCharBtns.add(new Rectangle(PAD + 220, y - BTN_H + 4, BTN_W, BTN_H));
        }

        y = 230f;
        for (int i = 0; i < offers.size(); i++, y -= ROW_H) {
            addItemBtns.add(new Rectangle(PAD + 220, y - BTN_H + 4, BTN_W, BTN_H));
        }

        Inventory inv = GameManager.getInstance().getInventory();

        y = 390f;
        for (int i = 0; i < inv.getCharacters().size(); i++, y -= ROW_H) {
            remCharBtns.add(new Rectangle(MID + 320, y - BTN_H + 4, BTN_W, BTN_H));
        }

        y = 230f;
        java.util.LinkedHashMap<Class<?>, List<Item>> grouped = groupItems(inv);
        for (int i = 0; i < grouped.size(); i++, y -= ROW_H) {
            remItemBtns.add(new Rectangle(MID + 320, y - BTN_H + 4, BTN_W, BTN_H));
        }
    }

    /**
     * Groups the inventory items by class, preserving insertion order.
     *
     * @param inv the inventory to group
     * @return a {@link java.util.LinkedHashMap} from item class to matching instances
     */
    private java.util.LinkedHashMap<Class<?>, List<Item>> groupItems(Inventory inv) {
        java.util.LinkedHashMap<Class<?>, List<Item>> grouped = new java.util.LinkedHashMap<>();
        for (Item item : inv.getItems()) {
            grouped.computeIfAbsent(item.getClass(), k -> new ArrayList<>()).add(item);
        }
        return grouped;
    }

    // ── LibGDX Screen ─────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float   mx    = Gdx.input.getX() * (W / Gdx.graphics.getWidth());
        float   my    = H - Gdx.input.getY() * (H / Gdx.graphics.getHeight());
        boolean click = Gdx.input.justTouched();

        if (click) handleClick(mx, my);

        shape.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);

        drawBackground(mx, my);
        drawText(mx, my);
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    /**
     * Draws all button backgrounds and the two panel dividers using
     * {@link ShapeRenderer}.
     *
     * @param mx virtual mouse X coordinate
     * @param my virtual mouse Y coordinate
     */
    private void drawBackground(float mx, float my) {
        shape.begin(ShapeRenderer.ShapeType.Filled);

        shape.setColor(0.2f, 0.2f, 0.2f, 1f);
        shape.rect(MID - 1, 0, 2, H);
        shape.rect(PAD,       255f, MID - PAD * 2, 1f);
        shape.rect(MID + PAD, 255f, MID - PAD * 2, 1f);

        drawAllButtonShapes(mx, my);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.DARK_GRAY);
        for (Rectangle btn : addCharBtns) shape.rect(btn.x, btn.y, btn.width, btn.height);
        for (Rectangle btn : addItemBtns) shape.rect(btn.x, btn.y, btn.width, btn.height);
        for (Rectangle btn : remCharBtns) shape.rect(btn.x, btn.y, btn.width, btn.height);
        for (Rectangle btn : remItemBtns) shape.rect(btn.x, btn.y, btn.width, btn.height);
        shape.setColor(Color.GOLD);
        shape.rect(btnStart.x, btnStart.y, btnStart.width, btnStart.height);
        shape.setColor(new Color(0.4f, 0.8f, 1f, 1f));
        shape.rect(btnSave.x,  btnSave.y,  btnSave.width,  btnSave.height);
        shape.end();
    }

    private void drawAllButtonShapes(float mx, float my) {
        for (Rectangle btn : addCharBtns) drawButtonShape(btn, mx, my, false);
        for (Rectangle btn : addItemBtns) drawButtonShape(btn, mx, my, false);
        for (Rectangle btn : remCharBtns) drawButtonShape(btn, mx, my, true);
        for (Rectangle btn : remItemBtns) drawButtonShape(btn, mx, my, true);
        drawButtonShape(btnStart, mx, my, false);
        drawButtonShape(btnSave,  mx, my, false);
    }

    /**
     * Fills a single button with a hover-sensitive background colour.
     *
     * @param btn      the button rectangle
     * @param mx       virtual mouse X
     * @param my       virtual mouse Y
     * @param isDanger {@code true} for destructive (remove) actions; renders red
     */
    private void drawButtonShape(Rectangle btn, float mx, float my, boolean isDanger) {
        boolean hover = btn.contains(mx, my);
        if (isDanger) {
            shape.setColor(hover ? 0.6f : 0.35f, 0.05f, 0.05f, 1f);
        } else {
            shape.setColor(hover ? 0.25f : 0.15f, hover ? 0.45f : 0.28f, hover ? 0.25f : 0.15f, 1f);
        }
        shape.rect(btn.x, btn.y, btn.width, btn.height);
    }

    /**
     * Draws all text elements: headers, character/item rows, slot counter,
     * feedback message, and button labels.
     *
     * <p>LibGDX {@code BitmapFont.draw()} places the baseline at the given
     * {@code y}; text renders downward from there.
     *
     * @param mx virtual mouse X coordinate
     * @param my virtual mouse Y coordinate
     */
    private void drawText(float mx, float my) {
        Inventory inv = GameManager.getInstance().getInventory();
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, "INVENTAR  -  Level " + levelToStart, PAD, H - PAD);

        font.setColor(new Color(0.4f, 0.8f, 1f, 1f));
        font.draw(batch, "Dostupne postavy",   PAD,       430f);
        font.draw(batch, "Dostupne itemy",      PAD,       265f);
        font.draw(batch, "Inventar - postavy", MID + PAD, 430f);
        font.draw(batch, "Inventar - itemy",    MID + PAD, 265f);

        drawAvailableChars();
        drawAvailableItems();
        drawInventoryChars(inv);
        drawInventoryItems(inv);

        font.setColor(Color.YELLOW);
        font.draw(batch,
            "Sloty: " + inv.getUsedSlots() + "/" + inv.getTotalSlots()
                + "   volne: " + inv.getFreeSlots(),
            PAD, 80f);

        font.setColor(new Color(1f, 0.6f, 0.1f, 1f));
        font.draw(batch, feedback, PAD, 55f);

        font.setColor(Color.GOLD);
        font.draw(batch, "Start  Level " + levelToStart,
            btnStart.x + 10, btnStart.y + btnStart.height - 6);

        boolean saveHover = btnSave.contains(mx, my);
        font.setColor(saveHover ? Color.WHITE : new Color(0.4f, 0.8f, 1f, 1f));
        font.draw(batch, "Ulozit hru", btnSave.x + 10, btnSave.y + btnSave.height - 6);

        batch.end();
    }

    private void drawAvailableChars() {
        float y = 390f;
        for (int i = 0; i < availableChars.size(); i++, y -= ROW_H) {
            font.setColor(Color.WHITE);
            font.draw(batch, availableChars.get(i).getName() + "  (3 sloty)", PAD, y);
            Rectangle btn = addCharBtns.get(i);
            font.setColor(Color.GREEN);
            font.draw(batch, "+ Pridaj", btn.x + 6, btn.y + btn.height - 6);
        }
    }

    private void drawAvailableItems() {
        float y = 230f;
        for (int i = 0; i < offers.size(); i++, y -= ROW_H) {
            ItemOffer offer = offers.get(i);
            font.setColor(Color.WHITE);
            font.draw(batch, offer.label + "  (" + offer.slotCost + " slot)", PAD, y);
            Rectangle btn = addItemBtns.get(i);
            font.setColor(Color.GREEN);
            font.draw(batch, "+ Pridaj", btn.x + 6, btn.y + btn.height - 6);
        }
    }

    private void drawInventoryChars(Inventory inv) {
        float y = 390f;
        List<PlayerCharacter> chars = inv.getCharacters();
        for (int i = 0; i < chars.size(); i++, y -= ROW_H) {
            PlayerCharacter c      = chars.get(i);
            boolean         isBase = inv.isBaseCharacter(c);
            font.setColor(isBase ? Color.GOLD : Color.WHITE);
            font.draw(batch,
                c.getName()
                    + (isBase ? "  [zakladna]" : "  (3 sloty)")
                    + "  HP:" + c.getHp(),
                MID + PAD, y);
            if (!isBase) {
                Rectangle btn = remCharBtns.get(i);
                font.setColor(new Color(1f, 0.4f, 0.4f, 1f));
                font.draw(batch, "- Odober", btn.x + 6, btn.y + btn.height - 6);
            }
        }
    }

    private void drawInventoryItems(Inventory inv) {
        float y = 230f;
        java.util.LinkedHashMap<Class<?>, List<Item>> grouped = groupItems(inv);
        int btnIndex = 0;
        for (java.util.Map.Entry<Class<?>, List<Item>> entry : grouped.entrySet()) {
            List<Item> group  = entry.getValue();
            Item       sample = group.getFirst();
            font.setColor(Color.WHITE);
            font.draw(batch,
                sample.getClass().getSimpleName()
                    + "  (" + sample.getSlotsRequired() + " slot)"
                    + "   x" + group.size(),
                MID + PAD, y);
            if (btnIndex < remItemBtns.size()) {
                Rectangle btn = remItemBtns.get(btnIndex);
                font.setColor(new Color(1f, 0.4f, 0.4f, 1f));
                font.draw(batch, "- Odober", btn.x + 6, btn.y + btn.height - 6);
            }
            btnIndex++;
            y -= ROW_H;
        }
    }

    // ── Input handling ────────────────────────────────────────────────────────

    /**
     * Dispatches a mouse click to the appropriate handler.
     *
     * <p>Inventory mutations (add/remove character, add/remove item) are
     * performed directly on the {@link Inventory} model – they are inventory
     * management operations, not game-flow operations.

     *
     * @param mx virtual mouse X coordinate
     * @param my virtual mouse Y coordinate
     */
    private void handleClick(float mx, float my) {
        Inventory inv = GameManager.getInstance().getInventory();

        for (int i = 0; i < addCharBtns.size(); i++) {
            if (addCharBtns.get(i).contains(mx, my)) {
                PlayerCharacter c = availableChars.get(i);
                if (inv.addCharacter(c)) {
                    availableChars.remove(i);
                    feedback = c.getName() + " pridany do inventara.";
                } else {
                    feedback = "Nedostatok slotov!";
                }
                rebuildButtons();
                return;
            }
        }

        for (int i = 0; i < remCharBtns.size(); i++) {
            if (remCharBtns.get(i).contains(mx, my)) {
                PlayerCharacter c = inv.getCharacters().get(i);
                if (inv.removeCharacter(c)) {
                    availableChars.add(c);
                    feedback = c.getName() + " odstraneny.";
                } else {
                    feedback = c.getName() + " je zakladna postava - neda sa odstranit.";
                }
                rebuildButtons();
                return;
            }
        }

        for (int i = 0; i < addItemBtns.size(); i++) {
            if (addItemBtns.get(i).contains(mx, my)) {
                ItemOffer offer   = offers.get(i);
                Item      newItem = offer.factory.get();
                feedback = inv.addItem(newItem)
                    ? offer.label + " pridany."
                    : "Nedostatok slotov!";
                rebuildButtons();
                return;
            }
        }

        for (int i = 0; i < remItemBtns.size(); i++) {
            if (remItemBtns.get(i).contains(mx, my)) {
                java.util.LinkedHashMap<Class<?>, List<Item>> grouped = groupItems(inv);
                List<Item> group    = new ArrayList<>(grouped.values()).get(i);
                Item       toRemove = group.getLast();
                inv.removeItem(toRemove);
                feedback = toRemove.getClass().getSimpleName() + " odstraneny.";
                rebuildButtons();
                return;
            }
        }

        if (btnSave.contains(mx, my)) {
            feedback = app.saveGame(levelToStart)
                ? "Hra ulozena!  (Level " + levelToStart + ")"
                : "Ulozenie zlyhalo.";
            return;
        }

        if (btnStart.contains(mx, my)) {
            // Business logic (startLevel) runs inside AppController, not here.
            app.startGame(levelToStart);
        }
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override public void resize(int w, int h) { cam.setToOrtho(false, W, H); }
    @Override public void show()    {}
    @Override public void hide()    {}
    @Override public void pause()   {}
    @Override public void resume()  {}

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();
    }
}
