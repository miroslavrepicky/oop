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
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.ShadowQuest;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.items.Armour;
import sk.stuba.fiit.items.HealingPotion;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.save.SaveManager;
import sk.stuba.fiit.util.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class InventoryScreen implements Screen {

    // rozlisenie "virtualneho" platna - musi sediet s OrthographicCamera
    private static final float W   = 800f;
    private static final float H   = 480f;
    private static final float PAD = 20f;

    // vyska jedneho riadku a rozmer tlacidiel
    private static final float ROW_H  = 40f;
    private static final float BTN_W  = 90f;
    private static final float BTN_H  = 28f;
    private static final float MID    = W / 2f; // 400 - stred obrazovky

    private final ShadowQuest game;
    private final int levelToStart;

    private final OrthographicCamera cam;
    private final SpriteBatch        batch;
    private final ShapeRenderer      shape;
    private final BitmapFont         font;

    private final Rectangle btnStart;
    private final Rectangle btnSave;

    private final List<PlayerCharacter> availableChars = new ArrayList<>();
    private final List<ItemOffer> offers = new ArrayList<>();

    // tlacidla - vzdy sa prepocitaju cez rebuildButtons()
    private final List<Rectangle> addCharBtns = new ArrayList<>();
    private final List<Rectangle> remCharBtns = new ArrayList<>();
    private final List<Rectangle> addItemBtns = new ArrayList<>();
    private final List<Rectangle> remItemBtns = new ArrayList<>();

    private String feedback = "";

    private static class ItemOffer {
        final String label;
        final int    slotCost;
        final java.util.function.Supplier<Item> factory; // vyraba novu instanciu
        int count; // kolkokrat ho hrac "objednal" (len pre info, nie limit)

        ItemOffer(String label, int slotCost, java.util.function.Supplier<Item> factory) {
            this.label    = label;
            this.slotCost = slotCost;
            this.factory  = factory;
        }
    }

    public InventoryScreen(ShadowQuest game, int levelToStart) {
        this.game         = game;
        this.levelToStart = levelToStart;

        cam = new OrthographicCamera();
        cam.setToOrtho(false, W, H);   // (0,0) = lavy dolny roh

        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        font  = new BitmapFont();
        font.getData().setScale(1.1f);

        // Start tlacidlo - vpravo dole
        btnStart = new Rectangle(W - PAD - 180, PAD, 180, 36);
        btnSave  = new Rectangle(W - PAD - 180, PAD + 46,     180, 36);

        offers.add(new ItemOffer("HealingPotion", 2,
            () -> new HealingPotion(50, new Vector2D(0, 0))));
        offers.add(new ItemOffer("Armour", 1,
            () -> new Armour(30, new Vector2D(0, 0))));
        buildAvailable();
        rebuildButtons();
    }

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


    // -------------------------------------------------------------------------
    //  Prepocitaj hitboxy tlacidiel podla aktualneho stavu inventara
    //  POZOR: y rastie NAHOR - prvy riadok je hore (vysoke y), kazdy dalsi nizsie
    // -------------------------------------------------------------------------
    private void rebuildButtons() {
        addCharBtns.clear();
        remCharBtns.clear();
        addItemBtns.clear();
        remItemBtns.clear();

        // --- lavy panel: dostupne postavy, zacinaju na y=390 ---
        float y = 390f;
        for (int i = 0; i < availableChars.size(); i++, y -= ROW_H) {
            // tlacidlo je napravo od textu postavy
            addCharBtns.add(new Rectangle(PAD + 220, y - BTN_H + 4, BTN_W, BTN_H));
        }

        // --- lavy panel: dostupne itemy, zacinaju na y=230 ---
        y = 230f;
        for (int i = 0; i < offers.size(); i++, y -= ROW_H) {
            addItemBtns.add(new Rectangle(PAD + 220, y - BTN_H + 4, BTN_W, BTN_H));
        }

        Inventory inv = GameManager.getInstance().getInventory();

        // ---pravy panel: postavy v inventari---
        y = 390f;
        for (int i = 0; i < inv.getCharacters().size(); i++, y -= ROW_H) {
            remCharBtns.add(new Rectangle(MID + 320, y - BTN_H + 4, BTN_W, BTN_H));
        }

        // --- pravy panel: itemy v inventari ---
        y = 230f;
        java.util.LinkedHashMap<Class<?>, List<Item>> grouped = new java.util.LinkedHashMap<>();
        for (Item item : inv.getItems()) {
            grouped.computeIfAbsent(item.getClass(), k -> new ArrayList<>()).add(item);
        }
        for (java.util.Map.Entry<Class<?>, List<Item>> entry : grouped.entrySet()) {
            remItemBtns.add(new Rectangle(MID + 320, y - BTN_H + 4, BTN_W, BTN_H));
            y -= ROW_H;
        }
    }

    // -------------------------------------------------------------------------
    //  render
    // -------------------------------------------------------------------------
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // mys - prevedieme do virtualnych suradnic (y otocime)
        float mx = Gdx.input.getX() * (W / Gdx.graphics.getWidth());
        float my = H - Gdx.input.getY() * (H / Gdx.graphics.getHeight());
        boolean click = Gdx.input.justTouched();

        if (click) handleClick(mx, my);

        shape.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);

        drawBackground(mx, my);
        drawText(mx, my);
    }

    // -------------------------------------------------------------------------
    //  Kreslenie ShapeRenderer - pozadia tlacidiel a deliace ciary
    // -------------------------------------------------------------------------
    private void drawBackground(float mx, float my) {
        shape.begin(ShapeRenderer.ShapeType.Filled);

        // deliaca ciara v strede
        shape.setColor(0.2f, 0.2f, 0.2f, 1f);
        shape.rect(MID - 1, 0, 2, H);

        // deliaca ciara medzi postavami a itemami (oba panely)
        shape.rect(PAD, 255f, MID - PAD * 2, 1f);
        shape.rect(MID + PAD, 255f, MID - PAD * 2, 1f);

        // nakresli vsetky tlacidla, ako filled rect + outline
        drawAllButtons(mx, my);

        shape.end();
    }

    private void drawAllButtons(float mx, float my) {
        for (Rectangle btn : addCharBtns) drawButtonShape(btn, mx, my, false);
        for (Rectangle btn : addItemBtns) drawButtonShape(btn, mx, my, false);
        for (Rectangle btn : remCharBtns) drawButtonShape(btn, mx, my, true);
        for (Rectangle btn : remItemBtns) drawButtonShape(btn, mx, my, true);
        drawButtonShape(btnStart, mx, my, false);
        drawButtonShape(btnSave,  mx, my, false);

        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.DARK_GRAY);
        for (Rectangle btn : addCharBtns) shape.rect(btn.x, btn.y, btn.width, btn.height);
        for (Rectangle btn : addItemBtns) shape.rect(btn.x, btn.y, btn.width, btn.height);
        for (Rectangle btn : remCharBtns) shape.rect(btn.x, btn.y, btn.width, btn.height);
        for (Rectangle btn : remItemBtns) shape.rect(btn.x, btn.y, btn.width, btn.height);
        shape.setColor(Color.GOLD);
        shape.rect(btnStart.x, btnStart.y, btnStart.width, btnStart.height);
        shape.setColor(new Color(0.4f, 0.8f, 1f, 1f)); // cyan pre Save
        shape.rect(btnSave.x, btnSave.y, btnSave.width, btnSave.height);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
    }

    /** Vyplni pozadie tlacidla-hover efekt. */
    private void drawButtonShape(Rectangle btn, float mx, float my, boolean isDanger) {
        boolean hover = btn.contains(mx, my);
        if (isDanger) {
            shape.setColor(hover ? 0.6f : 0.35f, 0.05f, 0.05f, 1f);
        } else {
            shape.setColor(hover ? 0.25f : 0.15f, hover ? 0.45f : 0.28f, hover ? 0.25f : 0.15f, 1f);
        }
        shape.rect(btn.x, btn.y, btn.width, btn.height);
    }

    // -------------------------------------------------------------------------
    //  Kreslenie textu cez SpriteBatch
    //  PRAVIDLO: font.draw(batch, text, x, y) - y je BASELINE (kresli od y nadol)
    //            teda y = btn.y + btn.height - 6 umiestnuje text vnutri tlacidla
    // -------------------------------------------------------------------------
    private void drawText(float mx, float my) {
        Inventory inv = GameManager.getInstance().getInventory();

        batch.begin();

        // --- Hlavicky ---
        font.setColor(Color.WHITE);
        font.draw(batch, "INVENTAR  -  Level " + levelToStart, PAD, H - PAD);

        font.setColor(new Color(0.4f, 0.8f, 1f, 1f));
        font.draw(batch, "Dostupne postavy",     PAD,       430f);
        font.draw(batch, "Dostupne itemy",        PAD,       265f);
        font.draw(batch, "Inventar - postavy",   MID + PAD, 430f);
        font.draw(batch, "Inventar - itemy",      MID + PAD, 265f);

        // --- Dostupne postavy (lavy panel, y=390 nadol) ---
        float y = 390f;
        for (int i = 0; i < availableChars.size(); i++, y -= ROW_H) {
            PlayerCharacter c = availableChars.get(i);
            font.setColor(Color.WHITE);
            font.draw(batch, c.getName() + "  (3 sloty)", PAD, y);
            // text tlacidla-vycentrovany vertikalne v btn
            Rectangle btn = addCharBtns.get(i);
            font.setColor(Color.GREEN);
            font.draw(batch, "+ Pridaj", btn.x + 6, btn.y + btn.height - 6);
        }

        // --- Dostupne itemy (lavy panel, y=230 nadol) ---
        y = 230f;
        for (int i = 0; i < offers.size(); i++, y -= ROW_H) {
            ItemOffer offer = offers.get(i);
            font.setColor(Color.WHITE);
            font.draw(batch,
                offer.label + "  (" + offer.slotCost + " slot)",
                PAD, y);
            Rectangle btn = addItemBtns.get(i);
            font.setColor(Color.GREEN);
            font.draw(batch, "+ Pridaj", btn.x + 6, btn.y + btn.height - 6);
        }

        // --- Inventar - postavy (pravy panel, y=390 nadol) ---
        y = 390f;
        List<PlayerCharacter> chars = inv.getCharacters();
        for (int i = 0; i < chars.size(); i++, y -= ROW_H) {
            PlayerCharacter c = chars.get(i);
            boolean isBase = inv.isBaseCharacter(c);
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

        // --- Inventar - itemy (pravy panel, y=230 nadol) ---
        y = 230f;
        List<Item> items = inv.getItems();

// zoskup podla triedy
        java.util.LinkedHashMap<Class<?>, List<Item>> grouped = new java.util.LinkedHashMap<>();
        for (Item item : items) {
            grouped.computeIfAbsent(item.getClass(), k -> new ArrayList<>()).add(item);
        }

        int btnIndex = 0;
        for (java.util.Map.Entry<Class<?>, List<Item>> entry : grouped.entrySet()) {
            List<Item> group = entry.getValue();
            Item sample = group.getFirst();
            int count = group.size();

            font.setColor(Color.WHITE);
            font.draw(batch,
                sample.getClass().getSimpleName()
                    + "  (" + sample.getSlotsRequired() + " slot)"
                    + "   x" + count,   // pocet tu
                MID + PAD, y);

            if (btnIndex < remItemBtns.size()) {
                Rectangle btn = remItemBtns.get(btnIndex);
                font.setColor(new Color(1f, 0.4f, 0.4f, 1f));
                font.draw(batch, "- Odober", btn.x + 6, btn.y + btn.height - 6);
            }
            btnIndex++;
            y -= ROW_H;
        }

        // --- Sloty + feedback ---
        font.setColor(Color.YELLOW);
        font.draw(batch,
            "Sloty: " + inv.getUsedSlots() + "/" + inv.getTotalSlots()
                + "   volne: " + inv.getFreeSlots(),
            PAD, 80f);

        font.setColor(new Color(1f, 0.6f, 0.1f, 1f));
        font.draw(batch, feedback, PAD, 55f);

        // --- Start tlacidlo ---
        font.setColor(Color.GOLD);
        font.draw(batch, "Start  Level " + levelToStart,
            btnStart.x + 10, btnStart.y + btnStart.height - 6);

        boolean saveHover = btnSave.contains(
            Gdx.input.getX() * (W / Gdx.graphics.getWidth()),
            H - Gdx.input.getY() * (H / Gdx.graphics.getHeight())
        );
        font.setColor(saveHover ? Color.WHITE : new Color(0.4f, 0.8f, 1f, 1f));
        font.draw(batch, "Ulozit hru",
            btnSave.x + 10, btnSave.y + btnSave.height - 6);

        batch.end();
    }

    // -------------------------------------------------------------------------
    //  Logika kliknutia
    // -------------------------------------------------------------------------
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
                ItemOffer offer = offers.get(i);
                Item newItem = offer.factory.get(); // nova instancia zakazdym
                if (inv.addItem(newItem)) {
                    feedback = offer.label + " pridany.";
                } else {
                    feedback = "Nedostatok slotov!";
                }
                rebuildButtons();
                return;
            }
        }

        for (int i = 0; i < remItemBtns.size(); i++) {
            if (remItemBtns.get(i).contains(mx, my)) {
                // zober skupiny v rovnakom poradi ako rebuildButtons
                java.util.LinkedHashMap<Class<?>, List<Item>> grouped = new java.util.LinkedHashMap<>();
                for (Item item : inv.getItems()) {
                    grouped.computeIfAbsent(item.getClass(), k -> new ArrayList<>()).add(item);
                }
                List<Item> group = new ArrayList<>(grouped.values()).get(i);
                Item toRemove = group.getLast(); // posledna instancia
                inv.removeItem(toRemove);
                feedback = toRemove.getClass().getSimpleName() + " odstraneny.";
                rebuildButtons();
                return;
            }
        }

        // --- Uložiť hru ---
        if (btnSave.contains(mx, my)) {
            try {
                SaveManager.getInstance().save(levelToStart);
                feedback = "Hra ulozena!  (Level " + levelToStart + ")";
            } catch (SaveManager.SaveException e) {
                feedback = "Ulozenie zlyhalo: " + e.getMessage();
            }
            return;
        }

        if (btnStart.contains(mx, my)) {
            GameManager.getInstance().startLevel(levelToStart);
            game.setScreen(new GameScreen(game));
        }
    }

    @Override public void resize(int w, int h) {
        cam.setToOrtho(false, W, H);
    }
    @Override public void show()   {}
    @Override public void hide()   {}
    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();
    }
}
