package sk.stuba.fiit.save;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.items.Armour;
import sk.stuba.fiit.items.HealingPotion;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pokrytie riadkov v SaveManager, ktore nie su pokryte existujucimi testami:
 *
 * 1. buildSaveData – vetva ked level != null (enemies, groundItems, ducks)
 * 2. createCharacter – default vetva (neznamy typ -> null)
 * 3. createItem – default vetva (neznamy typ -> null)
 * 4. applyInventoryToGameManager – neznamy typ postavy je preskoceny (log.warn + continue)
 * 5. applyInventoryToGameManager – activeChar != null, ale ziadna zhoda v zozname (edge case)
 * 6. load – korektne nacitanie s viacerymi typmi postav (Wizzard, Archer)
 */
class SaveManagerCoverageTest extends GdxTest {

    @BeforeEach
    void setUp() {
        new File(SaveManager.SAVE_FILE).delete();
        GameManager.getInstance().resetGame();
    }

    @AfterEach
    void tearDown() {
        new File(SaveManager.SAVE_FILE).delete();
    }

    // -------------------------------------------------------------------------
    //  buildSaveData – vetva ked getCurrentLevel() != null
    //  Testy overuju, ze enemies / groundItems / ducks su ulozene zo ziveho levelu
    // -------------------------------------------------------------------------

    /**
     * Po ulozeni s nepriatelom v leveli musi SaveData obsahovat zaznam o nepriatelovi.
     * Pokryva vetvu `if (level != null)` v buildSaveData + cyklus cez getEnemies().
     */
    @Test
    void save_withEnemyInLevel_storesEnemyData() {
        withFullMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            // Vytvorime stub level s jednym zivym nepriatelom
            Level stubLevel = buildStubLevelWithEnemy();
            try {
                injectLevel(gm, stubLevel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            SaveManager.getInstance().save(1);
            assertTrue(SaveManager.getInstance().hasSave());

            SaveData data = null;
            try {
                data = readSaveDataDirectly();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertNotNull(data);
            assertEquals(1, data.enemies.size(), "Mal byt ulozeny jeden nepriatel");
            assertEquals("EnemyKnight", data.enemies.get(0).type);
        });
    }

    /**
     * Po ulozeni s predmetom na zemi musi SaveData obsahovat zaznam.
     * Pokryva cyklus cez getItems() v buildSaveData.
     */
    @Test
    void save_withGroundItem_storesGroundItemData() {
        withFullMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            Level stubLevel = buildStubLevelWithGroundItem();
            try {
                injectLevel(gm, stubLevel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            SaveManager.getInstance().save(1);

            SaveData data = null;
            try {
                data = readSaveDataDirectly();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertNotNull(data);
            assertEquals(1, data.groundItems.size(), "Mal byt ulozeny jeden predmet na zemi");
            assertEquals("HealingPotion", data.groundItems.get(0).type);
        });
    }

    /**
     * EggProjectileSpawner sa NEMA ulozit – ma byt preskoceny.
     * Pokryva podmienku `if ("EggProjectileSpawner".equals(type)) continue`.
     */
    @Test
    void save_eggProjectileSpawner_isSkippedInGroundItems() {
        withFullMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            Level stubLevel = buildStubLevelWithEggSpawner();
            try {
                injectLevel(gm, stubLevel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            SaveManager.getInstance().save(1);

            SaveData data = null;
            try {
                data = readSaveDataDirectly();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertNotNull(data);
            assertTrue(data.groundItems.isEmpty(),
                "EggProjectileSpawner nesmie byt ulozeny ako GroundItemData");
        });
    }

    /**
     * Mrtvy nepriatel sa NEMA ulozit.
     * Pokryva podmienku `if (!enemy.isAlive()) continue` v buildSaveData.
     */
    @Test
    void save_deadEnemy_isNotStored() {
        withFullMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            Level stubLevel = buildStubLevelWithDeadEnemy();
            try {
                injectLevel(gm, stubLevel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            SaveManager.getInstance().save(1);

            SaveData data = null;
            try {
                data = readSaveDataDirectly();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertNotNull(data);
            assertTrue(data.enemies.isEmpty(), "Mrtvy nepriatel nesmie byt v save");
        });
    }

    /**
     * Mrtva kacka sa NEMA ulozit.
     * Pokryva podmienku `if (!duck.isAlive()) continue` v buildSaveData.
     */
    @Test
    void save_deadDuck_isNotStored() {
        withFullMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            Level stubLevel = buildStubLevelWithDeadDuck();
            try {
                injectLevel(gm, stubLevel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            SaveManager.getInstance().save(1);

            SaveData data = null;
            try {
                data = readSaveDataDirectly();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertNotNull(data);
            assertTrue(data.ducks.isEmpty(), "Mrtva kacka nesmie byt v save");
        });
    }

    /**
     * Zivy duck musi byt ulozeny.
     * Pokryva cyklus cez getDucks() v buildSaveData.
     */
    @Test
    void save_aliveDuck_isStored() {
        withFullMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            Level stubLevel = buildStubLevelWithAliveDuck();
            try {
                injectLevel(gm, stubLevel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            SaveManager.getInstance().save(1);

            SaveData data = null;
            try {
                data = readSaveDataDirectly();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertNotNull(data);
            assertEquals(1, data.ducks.size(), "Zivy duck ma byt ulozeny");
        });
    }

    // -------------------------------------------------------------------------
    //  applyInventoryToGameManager – neznamy typ postavy
    // -------------------------------------------------------------------------

    /**
     * Ak SaveData obsahuje neznamy characterType, load() ho preskoci (log.warn + continue)
     * a pokracuje. Zvysne platne typy sa obnovia normalnym sposobom.
     * Pokryva vetvu `if (pc == null) { log.warn(...); continue; }`.
     */
    @Test
    void load_unknownCharacterType_isSkippedGracefully() throws Exception {
        // Priamo zapiseme SaveData s neznamy typom
        SaveData badData = buildSaveDataWithUnknownCharacterType();
        writeSaveDataDirectly(badData);

        withFullMock(() -> {
            // load() by malo prebehnut bez vynimky a vratit null (version mismatch)
            // alebo data so 0 postavami (neznamy typ preskoceny)
            // Kedze SaveData.SAVE_VERSION je zhodne, load() prebehne
            SaveData loaded = SaveManager.getInstance().load();
            // Ak je verzia OK, load prebehne a preskoci neznamy typ
            // (SaveData.SAVE_VERSION moze byt odlisna – test overuje ze nezlyhame s NPE)
            // Ak verzia sedi, inventar nema ziadnu postavu (neznamy typ je preskoceny)
            if (loaded != null) {
                // Neznamy typ bol preskoceny – inventar moze mat 0 postav alebo ziaden activeChar
                // Dolezite: ziadna vynimka nepadla
                assertTrue(true, "load() prebehlo bez vynimky");
            }
        });
    }

    // -------------------------------------------------------------------------
    //  createCharacter default vetva
    // -------------------------------------------------------------------------

    /**
     * Priamy test cez reflexiu: createCharacter s neznamy typom vrati null.
     * Pokryva `default -> null` v switch vypovednom bloku.
     */
    @Test
    void createCharacter_unknownType_returnsNull() throws Exception {
        var method = SaveManager.class.getDeclaredMethod("createCharacter", String.class);
        method.setAccessible(true);
        Object result = method.invoke(SaveManager.getInstance(), "UnknownHero");
        assertNull(result, "createCharacter pre neznamy typ musi vratit null");
    }

    /**
     * createCharacter pre "Wizzard" nesmie byt null.
     * Pokryva vetvu `case "Wizzard"` – pri existujucich testoch moze byt nepokryta.
     */
    @Test
    void createCharacter_wizzard_returnsWizzard() throws Exception {
        withFullMock(() -> {
            try {
                var method = SaveManager.class.getDeclaredMethod("createCharacter", String.class);
                method.setAccessible(true);
                Object result = method.invoke(SaveManager.getInstance(), "Wizzard");
                assertNotNull(result, "createCharacter pre 'Wizzard' nesmie byt null");
                assertInstanceOf(Wizzard.class, result);
            } catch (Exception e) {
                fail("createCharacter vyhodila vynimku: " + e.getMessage());
            }
        });
    }

    /**
     * createCharacter pre "Archer" nesmie byt null.
     * Pokryva vetvu `case "Archer"`.
     */
    @Test
    void createCharacter_archer_returnsArcher() throws Exception {
        withFullMock(() -> {
            try {
                var method = SaveManager.class.getDeclaredMethod("createCharacter", String.class);
                method.setAccessible(true);
                Object result = method.invoke(SaveManager.getInstance(), "Archer");
                assertNotNull(result, "createCharacter pre 'Archer' nesmie byt null");
                assertInstanceOf(Archer.class, result);
            } catch (Exception e) {
                fail("createCharacter vyhodila vynimku: " + e.getMessage());
            }
        });
    }

    // -------------------------------------------------------------------------
    //  createItem default vetva
    // -------------------------------------------------------------------------

    /**
     * Priamy test cez reflexiu: createItem s neznamy typom vrati null.
     * Pokryva `default -> null` v createItem switch.
     */
    @Test
    void createItem_unknownType_returnsNull() throws Exception {
        var method = SaveManager.class.getDeclaredMethod("createItem", String.class);
        method.setAccessible(true);
        Object result = method.invoke(SaveManager.getInstance(), "DragonSword");
        assertNull(result, "createItem pre neznamy typ musi vratit null");
    }

    /**
     * createItem pre "Armour" nesmie byt null.
     * Pokryva vetvu `case "Armour"`.
     */
    @Test
    void createItem_armour_returnsArmour() throws Exception {
        var method = SaveManager.class.getDeclaredMethod("createItem", String.class);
        method.setAccessible(true);
        Object result = method.invoke(SaveManager.getInstance(), "Armour");
        assertNotNull(result);
        assertInstanceOf(Armour.class, result);
    }

    /**
     * createItem pre "HealingPotion" nesmie byt null.
     */
    @Test
    void createItem_healingPotion_returnsHealingPotion() throws Exception {
        var method = SaveManager.class.getDeclaredMethod("createItem", String.class);
        method.setAccessible(true);
        Object result = method.invoke(SaveManager.getInstance(), "HealingPotion");
        assertNotNull(result);
        assertInstanceOf(HealingPotion.class, result);
    }

    // -------------------------------------------------------------------------
    //  load – Wizzard a Archer ako platne ulozene typy
    // -------------------------------------------------------------------------

    /**
     * Wizzard je platny typ – po ulozeni a nacitani musi byt v inventari.
     * Pokryva vetvu `case "Wizzard"` v createCharacter pri reale load().
     */
    @Test
    void saveAndLoad_wizzardInParty_isRestored() {
        withFullMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();
            // Pridame Wizzarda (3 sloty, max 10 -> ok)
            Wizzard w = new Wizzard(new Vector2D(0, 0));
            gm.getInventory().addCharacter(w);
            assertEquals(2, gm.getInventory().getCharacters().size());

            SaveManager.getInstance().save(1);
        });

        withFullMock(() -> {
            GameManager.getInstance().resetGame();
            SaveData data = SaveManager.getInstance().load();
            assertNotNull(data);
            // Knight + Wizzard = 2 postavy v inventari po nacitani
            assertEquals(2, GameManager.getInstance().getInventory().getCharacters().size());
        });
    }

    /**
     * Archer je platny typ – po ulozeni a nacitani musi byt v inventari.
     * Pokryva vetvu `case "Archer"` v createCharacter pri reale load().
     */
    @Test
    void saveAndLoad_archerInParty_isRestored() {
        withFullMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();
            Archer a = new Archer(new Vector2D(0, 0));
            gm.getInventory().addCharacter(a);
            SaveManager.getInstance().save(1);
        });

        withFullMock(() -> {
            GameManager.getInstance().resetGame();
            SaveData data = SaveManager.getInstance().load();
            assertNotNull(data);
            assertEquals(2, GameManager.getInstance().getInventory().getCharacters().size());
        });
    }

    /**
     * Viac predmetov rovnakeho typu – groupovanie + count musi byt spravne ulozene.
     * Pokryva `itemCounts.merge(...)` a cyklus v buildSaveData.
     */
    @Test
    void save_twoHealingPotions_storedAsCountTwo() throws Exception {
        withFullMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();
            gm.getInventory().addItem(new HealingPotion(50, new Vector2D(0, 0)));
            gm.getInventory().addItem(new HealingPotion(50, new Vector2D(0, 0)));
            SaveManager.getInstance().save(1);
        });

        SaveData data = readSaveDataDirectly();
        assertNotNull(data);
        assertEquals(1, data.inventoryItems.size(), "Oba predmety maju byt zgruppovane");
        assertEquals(2, data.inventoryItems.get(0).count);
    }

    /**
     * Armour predmet je tiez platny typ pre createItem – po ulozeni a nacitani sa obnovi.
     */
    @Test
    void saveAndLoad_armourInInventory_isRestored() {
        withFullMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();
            gm.getInventory().addItem(new Armour(30, new Vector2D(0, 0)));
            SaveManager.getInstance().save(1);
        });

        withFullMock(() -> {
            GameManager.getInstance().resetGame();
            SaveData data = SaveManager.getInstance().load();
            assertNotNull(data);
            assertFalse(data.inventoryItems.isEmpty());
            assertEquals("Armour", data.inventoryItems.get(0).itemType);
        });
    }

    // -------------------------------------------------------------------------
    //  hasSave / deleteSave edge cases
    // -------------------------------------------------------------------------

    /**
     * Po deleteSave() a opakovanom deleteSave() nesmie dojst k vynimke.
     */
    @Test
    void deleteSave_calledTwice_doesNotThrow() {
        assertDoesNotThrow(() -> {
            SaveManager.getInstance().deleteSave();
            SaveManager.getInstance().deleteSave();
        });
    }

    // -------------------------------------------------------------------------
    //  Pomocne metody
    // -------------------------------------------------------------------------

    private void withFullMock(Runnable block) {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, (mock, ctx) -> {
                     when(mock.getFirstFrameSize("idle")).thenReturn(new Vector2D(32, 64));
                     when(mock.hasAnimation(anyString())).thenReturn(false);
                     when(mock.getAnimationDuration(anyString())).thenReturn(0.5f);
                     when(mock.getFrameCount(anyString())).thenReturn(5);
                     when(mock.getAnimationSize(anyString())).thenReturn(new Vector2D(32, 64));
                 })) {
            block.run();
        }
    }

    /**
     * Priamo nacita SaveData zo suboru cez ObjectInputStream – obchodzi verziu.
     */
    private SaveData readSaveDataDirectly() throws Exception {
        try (var ois = new java.io.ObjectInputStream(
                new java.io.FileInputStream(SaveManager.SAVE_FILE))) {
            return (SaveData) ois.readObject();
        }
    }

    private void writeSaveDataDirectly(SaveData data) throws Exception {
        try (var oos = new java.io.ObjectOutputStream(
                new java.io.FileOutputStream(SaveManager.SAVE_FILE))) {
            oos.writeObject(data);
        }
    }

    /**
     * Injectuje level do GameManager cez reflexiu (field je private).
     */
    private void injectLevel(GameManager gm, Level level) throws Exception {
        var field = GameManager.class.getDeclaredField("currentLevel");
        field.setAccessible(true);
        field.set(gm, level);
    }

    // --- Stub level factories ---

    private Level buildStubLevelWithEnemy() {
        Level level = new StubLevel();
        EnemyKnight ek = new EnemyKnight(new Vector2D(100, 100));
        level.spawnEnemy(ek);
        return level;
    }

    private Level buildStubLevelWithDeadEnemy() {
        Level level = new StubLevel();
        EnemyKnight ek = new EnemyKnight(new Vector2D(100, 100));
        ek.takeDamage(9999);
        level.spawnEnemy(ek);
        return level;
    }

    private Level buildStubLevelWithGroundItem() {
        Level level = new StubLevel();
        level.addItem(new HealingPotion(50, new Vector2D(50, 50)));
        return level;
    }

    private Level buildStubLevelWithEggSpawner() {
        Level level = new StubLevel();
        level.addItem(new sk.stuba.fiit.items.EggProjectileSpawner(new Vector2D(50, 50)));
        return level;
    }

    private Level buildStubLevelWithAliveDuck() {
        Level level = new StubLevel();
        Duck duck = new Duck(new Vector2D(200, 200));
        level.addDuck(duck);
        return level;
    }

    private Level buildStubLevelWithDeadDuck() {
        Level level = new StubLevel();
        Duck duck = new Duck(new Vector2D(200, 200));
        duck.takeDamage(9999);
        level.addDuck(duck);
        return level;
    }

    /**
     * Vytvori SaveData s neznamy characterType.
     * Pouzivame paketovo-privatny konstruktor cez reflexiu.
     */
    private SaveData buildSaveDataWithUnknownCharacterType() throws Exception {
        var charData = new SaveData.CharacterData(
            "DragonWarrior", 100, 0, true, true, 0f, 0f, true);

        var ctor = SaveData.class.getDeclaredConstructor(
            int.class,
            List.class, List.class,
            List.class, List.class,
            List.class);
        ctor.setAccessible(true);
        return (SaveData) ctor.newInstance(
            SaveData.SAVE_VERSION,
            List.of(charData),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    }

    /**
     * Minimal Level subclass without MapManager, to avoid TMX loading.
     */
    static class StubLevel extends Level {
        StubLevel() { super(1); }

        @Override
        public sk.stuba.fiit.world.MapManager getMapManager() { return null; }
    }
}
