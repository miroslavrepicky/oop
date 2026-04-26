package sk.stuba.fiit.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.exceptions.GameStateException;
import sk.stuba.fiit.save.SaveData;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;
import sk.stuba.fiit.world.MapManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pokryva riadky v GameManager.startLevelFromSave(), ktore nie su testovane
 * existujucimi testami v GameManagerTest.
 *
 * startLevelFromSave:
 *  - throws GameStateException ked nie je aktivna postava
 *  - resetuje stav vsetkych postav (resetState sa vola na kazdom characterovi)
 *  - nastavuje currentLevel
 *  - deleguje na Level.loadFromSave()
 *
 * Testy v tejto triede mockuju AnimationManager a MapManager, aby sa vyhli
 * skutocnemu nacitaniu TMX suborov a texture atlasov.
 */
class GameManagerStartLevelFromSaveTest extends GdxTest {

    @BeforeEach
    void reset() {
        GameManager.getInstance().resetGame();
    }

    // -------------------------------------------------------------------------
    //  Validacia – chybajuca aktivna postava
    // -------------------------------------------------------------------------

    /**
     * startLevelFromSave hodi GameStateException ked inventar nema aktivnu postavu.
     * Pokryva vetvu `if (inventory.getActive() == null)` v startLevelFromSave.
     */
    @Test
    void startLevelFromSave_throwsGameStateException_whenNoActivePlayer() {
        SaveData data = buildMinimalSaveData(1);
        // Po resete nie je ziadna postava -> getActive() == null
        assertThrows(GameStateException.class,
            () -> GameManager.getInstance().startLevelFromSave(data));
    }

    // -------------------------------------------------------------------------
    //  Normalne spustenie z ulozeneho stavu
    // -------------------------------------------------------------------------

    /**
     * Po startLevelFromSave() musi getCurrentLevel() obsahovat Level s rovnakym cislom.
     * Pokryva: this.currentLevel = new Level(...) + loadFromSave(...).
     */
    @Test
    void startLevelFromSave_setsCurrentLevel() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame(); // prida Knighta -> getActive() != null

            SaveData data = buildSaveDataWithKnight(2);
            gm.startLevelFromSave(data);

            assertNotNull(gm.getCurrentLevel());
            assertEquals(2, gm.getCurrentLevel().getLevelNumber());
        });
    }

    /**
     * startLevelFromSave() vola resetState() na kazdej postave pred nacitanim.
     * Overime to tak, ze postava bola "pri utoku" (isAttacking via reflection)
     * a po startLevelFromSave() stav je vycisteny.
     *
     * Pokryva: `for (PlayerCharacter pc : ...) { pc.resetState(); }`.
     */
    @Test
    void startLevelFromSave_resetsCharacterState() throws Exception {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            // Simulujeme "isAttacking" stav na aktivnom charaktere
            var player = gm.getInventory().getActive();
            setIsAttacking(player, true);
            assertTrue(isAttacking(player), "Pred volanim: hrac ma byt v stave utoku");

            SaveData data = buildSaveDataWithKnight(1);
            gm.startLevelFromSave(data);

            // resetState() sa vola v startLevelFromSave -> isAttacking == false
            assertFalse(isAttacking(player), "Po startLevelFromSave: isAttacking musi byt false");
        });
    }

    /**
     * Enemies zo SaveData su obnovene v leveli po startLevelFromSave().
     * Pokryva vetvu kde loadFromSave pracuje s EnemyData.
     */
    @Test
    void startLevelFromSave_restoresEnemiesFromSave() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            SaveData data = buildSaveDataWithKnightAndEnemy(1, "EnemyKnight");
            gm.startLevelFromSave(data);

            Level level = gm.getCurrentLevel();
            assertNotNull(level);
            assertEquals(1, level.getEnemies().size(), "Nepriatel ma byt obnoveny zo save");
        });
    }

    /**
     * EnemyArcher zo SaveData je obnoveny.
     * Pokryva vetvu `case "EnemyArcher"` v Level.createEnemyFromSave().
     */
    @Test
    void startLevelFromSave_restoresEnemyArcher() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            SaveData data = buildSaveDataWithKnightAndEnemy(1, "EnemyArcher");
            gm.startLevelFromSave(data);

            assertEquals(1, gm.getCurrentLevel().getEnemies().size());
        });
    }

    /**
     * EnemyWizzard zo SaveData je obnoveny.
     * Pokryva vetvu `case "EnemyWizzard"` v Level.createEnemyFromSave().
     */
    @Test
    void startLevelFromSave_restoresEnemyWizzard() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            SaveData data = buildSaveDataWithKnightAndEnemy(1, "EnemyWizzard");
            gm.startLevelFromSave(data);

            assertEquals(1, gm.getCurrentLevel().getEnemies().size());
        });
    }

    /**
     * DarkKnight zo SaveData je obnoveny.
     * Pokryva vetvu `case "DarkKnight"` v Level.createEnemyFromSave().
     */
    @Test
    void startLevelFromSave_restoresDarkKnight() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            SaveData data = buildSaveDataWithKnightAndEnemy(1, "DarkKnight");
            gm.startLevelFromSave(data);

            assertEquals(1, gm.getCurrentLevel().getEnemies().size());
        });
    }

    /**
     * Neznamy typ nepriatel a v SaveData je preskoceny bez vynimky.
     * Pokryva vetvu `default: log.warn(...); return null` v createEnemyFromSave.
     */
    @Test
    void startLevelFromSave_unknownEnemyType_isSkipped() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            SaveData data = buildSaveDataWithKnightAndEnemy(1, "AlienInvader");
            // Nesmie hodit vynimku – neznamy typ je preskoceny
            assertDoesNotThrow(() -> gm.startLevelFromSave(data));
            assertEquals(0, gm.getCurrentLevel().getEnemies().size());
        });
    }

    /**
     * GroundItems sa obnovia zo SaveData.
     * Pokryva cyklus cez groundItems + createGroundItemFromSave "HealingPotion".
     */
    @Test
    void startLevelFromSave_restoresGroundItems() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            SaveData data = buildSaveDataWithGroundItem(1, "HealingPotion");
            gm.startLevelFromSave(data);

            assertEquals(1, gm.getCurrentLevel().getItems().size());
        });
    }

    /**
     * Armour ako GroundItem sa tiez obnovi.
     * Pokryva vetvu `case "Armour"` v createGroundItemFromSave.
     */
    @Test
    void startLevelFromSave_restoresArmourGroundItem() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            SaveData data = buildSaveDataWithGroundItem(1, "Armour");
            gm.startLevelFromSave(data);

            assertEquals(1, gm.getCurrentLevel().getItems().size());
        });
    }

    /**
     * Neznamy GroundItem typ je preskoceny.
     * Pokryva `default: log.warn; return null` v createGroundItemFromSave.
     */
    @Test
    void startLevelFromSave_unknownGroundItemType_isSkipped() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            SaveData data = buildSaveDataWithGroundItem(1, "MysticOrb");
            assertDoesNotThrow(() -> gm.startLevelFromSave(data));
            assertEquals(0, gm.getCurrentLevel().getItems().size());
        });
    }

    /**
     * Ducks zo SaveData su obnovene.
     * Pokryva cyklus cez ducks + new Duck + restoreStats v loadFromSave.
     */
    @Test
    void startLevelFromSave_restoresDucks() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            SaveData data = buildSaveDataWithDuck(1, 15);
            gm.startLevelFromSave(data);

            assertEquals(1, gm.getCurrentLevel().getDucks().size());
        });
    }

    /**
     * Pozicia aktivnej postavy sa obnovi zo SaveData.
     * Pokryva vetvu kde `cd.isActive == true` v loadFromSave a nastavuje pozicii.
     */
    @Test
    void startLevelFromSave_restoresPlayerPosition() {
        withAnimAndMapMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            SaveData data = buildSaveDataWithKnightAtPosition(1, 123f, 456f);
            gm.startLevelFromSave(data);

            var pos = gm.getInventory().getActive().getPosition();
            assertEquals(123f, pos.getX(), 0.01f);
            assertEquals(456f, pos.getY(), 0.01f);
        });
    }

    // -------------------------------------------------------------------------
    //  Pomocne metody
    // -------------------------------------------------------------------------

    /**
     * Spusti blok s mocknutym AnimationManager a MapManager.
     * MapManager je mocknuty, aby sa vyhlo nacitaniu realneho TMX suboru.
     */
    private void withAnimAndMapMock(Runnable block) {
        try (MockedConstruction<AnimationManager> animMock =
                 mockConstruction(AnimationManager.class, (m, ctx) -> {
                     when(m.getFirstFrameSize("idle")).thenReturn(new Vector2D(32, 64));
                     when(m.hasAnimation(anyString())).thenReturn(false);
                     when(m.getAnimationDuration(anyString())).thenReturn(0.5f);
                     when(m.getFrameCount(anyString())).thenReturn(5);
                     when(m.getAnimationSize(anyString())).thenReturn(new Vector2D(32, 64));
                 });
             MockedConstruction<MapManager> mapMock =
                 mockConstruction(MapManager.class, (m, ctx) -> {
                     when(m.getHitboxes()).thenReturn(java.util.Collections.emptyList());
                     when(m.getEntities()).thenReturn(java.util.Collections.emptyList());
                 })) {
            block.run();
        }
    }

    // --- SaveData factory helpers ---

    private SaveData buildMinimalSaveData(int level) {
        return buildSaveData(level,
            List.of(new SaveData.CharacterData("Knight", 100, 0, true, true, 0f, 0f, true)),
            List.of(), List.of(), List.of(), List.of());
    }

    private SaveData buildSaveDataWithKnight(int level) {
        return buildMinimalSaveData(level);
    }

    private SaveData buildSaveDataWithKnightAtPosition(int level, float x, float y) {
        return buildSaveData(level,
            List.of(new SaveData.CharacterData("Knight", 100, 0, true, true, x, y, true)),
            List.of(), List.of(), List.of(), List.of());
    }

    private SaveData buildSaveDataWithKnightAndEnemy(int level, String enemyType) {
        return buildSaveData(level,
            List.of(new SaveData.CharacterData("Knight", 100, 0, true, true, 0f, 0f, true)),
            List.of(),
            List.of(new SaveData.EnemyData(enemyType, 100f, 100f, 80, 5)),
            List.of(), List.of());
    }

    private SaveData buildSaveDataWithGroundItem(int level, String itemType) {
        return buildSaveData(level,
            List.of(new SaveData.CharacterData("Knight", 100, 0, true, true, 0f, 0f, true)),
            List.of(), List.of(),
            List.of(new SaveData.GroundItemData(itemType, 50f, 50f)),
            List.of());
    }

    private SaveData buildSaveDataWithDuck(int level, int duckHp) {
        return buildSaveData(level,
            List.of(new SaveData.CharacterData("Knight", 100, 0, true, true, 0f, 0f, true)),
            List.of(), List.of(), List.of(),
            List.of(new SaveData.DuckData(200f, 200f, duckHp)));
    }

    /**
     * Vytvori SaveData cez paketovo-privatny konstruktor cez reflexiu.
     */
    private SaveData buildSaveData(int level,
                                   List<SaveData.CharacterData> chars,
                                   List<SaveData.ItemData> items,
                                   List<SaveData.EnemyData> enemies,
                                   List<SaveData.GroundItemData> groundItems,
                                   List<SaveData.DuckData> ducks) {
        try {
            var ctor = SaveData.class.getDeclaredConstructor(
                int.class,
                List.class, List.class,
                List.class, List.class,
                List.class);
            ctor.setAccessible(true);
            return (SaveData) ctor.newInstance(level, chars, items, enemies, groundItems, ducks);
        } catch (Exception e) {
            throw new RuntimeException("Nepodarilo sa vytvorit SaveData: " + e.getMessage(), e);
        }
    }

    // --- Reflection helpers pre isAttacking field na PlayerCharacter ---

    private void setIsAttacking(Object character, boolean value) {
        try {
            var f = character.getClass().getSuperclass().getDeclaredField("isAttacking");
            f.setAccessible(true);
            f.setBoolean(character, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isAttacking(Object character) {
        try {
            var f = character.getClass().getSuperclass().getDeclaredField("isAttacking");
            f.setAccessible(true);
            return f.getBoolean(character);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
