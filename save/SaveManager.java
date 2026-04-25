package sk.stuba.fiit.save;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.exceptions.SaveException;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.items.Armour;
import sk.stuba.fiit.items.HealingPotion;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton responsible for saving and loading {@link SaveData} to and from disk.
 *
 * <h2>Serialisation strategy</h2>
 * <p>Java {@link ObjectOutputStream}/{@link ObjectInputStream} writing to
 * {@value #SAVE_FILE}. {@link SaveData} is a pure DTO (only primitives and Strings)
 * with no LibGDX or OpenGL dependencies, so it serialises cleanly without a
 * running engine context.
 *
 * <h2>Compatibility guard</h2>
 * <p>On load, {@link SaveData#saveVersion} is compared to
 * {@link SaveData#SAVE_VERSION}. A version mismatch causes the file to be
 * silently ignored and {@code null} returned to the caller, which then falls
 * back to starting a new game.
 *
 * <h2>Save procedure</h2>
 * <ol>
 *   <li>Reads the current {@link Inventory} from {@link GameManager}.</li>
 *   <li>Converts each character to {@link SaveData.CharacterData}.</li>
 *   <li>Groups items by class and converts them to {@link SaveData.ItemData}.</li>
 *   <li>Snapshots living enemies, ground items, and ducks from the current level.</li>
 *   <li>Serialises the complete {@link SaveData} object to disk.</li>
 * </ol>
 *
 * <h2>Load procedure</h2>
 * <ol>
 *   <li>Deserialises {@link SaveData} from disk.</li>
 *   <li>Validates the version number; discards mismatches.</li>
 *   <li>Calls {@link GameManager#resetGame()} to start from a clean state.</li>
 *   <li>Reconstructs characters and items into a fresh {@link Inventory}.</li>
 *   <li>Returns the {@link SaveData} object to the caller
 *       ({@link GameManager#startLevelFromSave(SaveData)}) which restores the level.</li>
 * </ol>
 */
public final class SaveManager {

    private static SaveManager instance;

    /** Path to the save file, relative to the working directory. */
    public static final String SAVE_FILE = "shadowquest.save";

    private static final Logger log = GameLogger.get(SaveManager.class);

    private SaveManager() {}

    /**
     * Returns the single shared instance, creating it on the first call.
     *
     * @return the singleton {@code SaveManager}
     */
    public static SaveManager getInstance() {
        if (instance == null) instance = new SaveManager();
        return instance;
    }

    // -------------------------------------------------------------------------
    //  Save
    // -------------------------------------------------------------------------

    /**
     * Saves the current game state to {@value #SAVE_FILE}.
     *
     * @param currentLevelNumber the 1-based number of the level currently active
     * @throws SaveException if the file cannot be written
     */
    public void save(int currentLevelNumber) {
        SaveData data = buildSaveData(currentLevelNumber);
        writeToDisk(data);
        log.info("Game saved: level={}, characters={}, items={}, file={}",
            currentLevelNumber,
            data.characters.size(),
            data.inventoryItems.size(),
            SAVE_FILE);
    }

    /**
     * Builds a {@link SaveData} snapshot from the current runtime state.
     *
     * @param levelNumber 1-based number of the active level
     * @return a fully populated {@link SaveData} instance
     */
    private SaveData buildSaveData(int levelNumber) {
        Inventory inv          = GameManager.getInstance().getInventory();
        PlayerCharacter active = inv.getActive();

        // Party members
        List<SaveData.CharacterData> chars = new ArrayList<>();
        for (PlayerCharacter pc : inv.getCharacters()) {
            chars.add(new SaveData.CharacterData(
                pc.getClass().getSimpleName(),
                pc.getHp(), pc.getArmor(),
                inv.isBaseCharacter(pc),
                pc == active,
                pc.getPosition().getX(), pc.getPosition().getY(),
                pc.isFacingRight()
            ));
        }

        // Inventory items (grouped by type)
        Map<String, Integer> itemCounts = new LinkedHashMap<>();
        for (Item item : inv.getItems()) {
            itemCounts.merge(item.getClass().getSimpleName(), 1, Integer::sum);
        }
        List<SaveData.ItemData> itemData = new ArrayList<>();
        for (Map.Entry<String, Integer> e : itemCounts.entrySet()) {
            itemData.add(new SaveData.ItemData(e.getKey(), e.getValue()));
        }

        // Living enemies in the level
        List<SaveData.EnemyData> enemyData = new ArrayList<>();
        Level level = GameManager.getInstance().getCurrentLevel();
        if (level != null) {
            for (EnemyCharacter enemy : level.getEnemies()) {
                if (!enemy.isAlive()) continue;
                enemyData.add(new SaveData.EnemyData(
                    enemy.getClass().getSimpleName(),
                    enemy.getPosition().getX(), enemy.getPosition().getY(),
                    enemy.getHp(), enemy.getArmor()
                ));
            }
        }

        // Items on the ground
        List<SaveData.GroundItemData> groundItems = new ArrayList<>();
        if (level != null) {
            for (Item item : level.getItems()) {
                String type = item.getClass().getSimpleName();
                if ("EggProjectileSpawner".equals(type)) continue; // not restorable
                groundItems.add(new SaveData.GroundItemData(
                    type,
                    item.getPosition().getX(), item.getPosition().getY()
                ));
            }
        }

        // Living ducks
        List<SaveData.DuckData> duckData = new ArrayList<>();
        if (level != null) {
            for (Duck duck : level.getDucks()) {
                if (!duck.isAlive()) continue;
                duckData.add(new SaveData.DuckData(
                    duck.getPosition().getX(),
                    duck.getPosition().getY(),
                    duck.getHp()
                ));
            }
        }

        return new SaveData(levelNumber, chars, itemData, enemyData, groundItems, duckData);
    }

    /**
     * Writes the given {@link SaveData} to {@value #SAVE_FILE} using Java serialisation.
     *
     * @param data the snapshot to persist
     * @throws SaveException if the write fails
     */
    private void writeToDisk(SaveData data) {
        File file = new File(SAVE_FILE);
        try (ObjectOutputStream oos =
                 new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        } catch (IOException e) {
            log.error("Failed to write save file: path={}", file.getAbsolutePath(), e);
            throw new SaveException("Game save failed: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    //  Load
    // -------------------------------------------------------------------------

    /**
     * Loads and validates the save file from {@value #SAVE_FILE}.
     *
     * <p>After a successful load the inventory is restored immediately
     * (characters and items). The level itself is restored later by the caller
     * via {@link GameManager#startLevelFromSave(SaveData)}.
     *
     * @return the loaded {@link SaveData}, or {@code null} if no valid save file
     *         exists or the version is incompatible
     */
    public SaveData load() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            log.info("No save file found: path={}", file.getAbsolutePath());
            return null;
        }

        SaveData data = readFromDisk(file);
        if (data == null) return null;

        if (data.saveVersion != SaveData.SAVE_VERSION) {
            log.warn("Save version mismatch: fileVersion={}, expected={} – ignoring",
                data.saveVersion, SaveData.SAVE_VERSION);
            return null;
        }

        applyInventoryToGameManager(data);

        log.info("Game loaded: level={}, chars={}, enemies={}, groundItems={}, savedAt={}",
            data.currentLevel, data.characters.size(),
            data.enemies.size(), data.groundItems.size(), data.savedAt);

        return data;
    }

    /**
     * Resets {@link GameManager} and reconstructs the inventory from the saved data.
     * Characters are recreated via factory methods and have their stats restored.
     * Items are recreated and added to the inventory in the saved order.
     *
     * @param data the validated save data to apply
     */
    private void applyInventoryToGameManager(SaveData data) {
        GameManager gm = GameManager.getInstance();
        gm.resetGame();
        Inventory inv = gm.getInventory();

        PlayerCharacter activeChar = null;
        for (SaveData.CharacterData cd : data.characters) {
            PlayerCharacter pc = createCharacter(cd.characterType);
            if (pc == null) {
                log.warn("Unknown character type in save – skipped: type={}", cd.characterType);
                continue;
            }
            pc.restoreStats(cd.hp, cd.armor);
            inv.addCharacter(pc);
            if (cd.isActive) activeChar = pc;
        }

        if (activeChar != null) {
            for (int i = 0; i < inv.getCharacters().size(); i++) {
                if (inv.getCharacters().get(i) == activeChar) {
                    inv.switchCharacter(i + 1);
                    break;
                }
            }
        }

        for (SaveData.ItemData id : data.inventoryItems) {
            for (int i = 0; i < id.count; i++) {
                Item item = createItem(id.itemType);
                if (item != null) inv.addItem(item);
            }
        }
    }

    /**
     * Reads and deserialises a {@link SaveData} object from the given file.
     *
     * @param file the save file to read
     * @return the deserialised {@link SaveData}, or {@code null} on any error
     */
    private SaveData readFromDisk(File file) {
        try (ObjectInputStream ois =
                 new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (!(obj instanceof SaveData)) {
                log.error("Save file contains unexpected object type: file={}", file.getName());
                return null;
            }
            return (SaveData) obj;
        } catch (IOException | ClassNotFoundException e) {
            log.error("Failed to read save file: path={}", file.getAbsolutePath(), e);
            return null;
        }
    }

    // -------------------------------------------------------------------------
    //  Factory methods – String class name → concrete class
    // -------------------------------------------------------------------------

    /**
     * Creates a new character instance from the class name stored in
     * {@link SaveData.CharacterData#characterType}.
     *
     * <p>The position is set to {@code (0, 0)} during reconstruction; the correct
     * spawn position is applied later by {@code Level.loadFromSave()} from the
     * saved {@link SaveData.CharacterData} coordinates.
     *
     * @param type simple class name, e.g. {@code "Knight"}
     * @return a freshly constructed character, or {@code null} if the type is unknown
     */
    private PlayerCharacter createCharacter(String type) {
        return switch (type) {
            case "Knight"  -> new Knight(new Vector2D(0, 0));
            case "Wizzard" -> new Wizzard(new Vector2D(0, 0));
            case "Archer"  -> new Archer(new Vector2D(0, 0));
            default        -> null;
        };
    }

    /**
     * Creates a new item instance from the class name stored in
     * {@link SaveData.ItemData#itemType}.
     *
     * @param type simple class name, e.g. {@code "HealingPotion"}
     * @return a freshly constructed item, or {@code null} if the type is unknown
     */
    private Item createItem(String type) {
        return switch (type) {
            case "HealingPotion" -> new HealingPotion(50, new Vector2D(0, 0));
            case "Armour"        -> new Armour(30, new Vector2D(0, 0));
            default              -> null;
        };
    }

    // -------------------------------------------------------------------------
    //  Public utility methods
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if a save file currently exists on disk.
     *
     * @return {@code true} when {@value #SAVE_FILE} exists
     */
    public boolean hasSave() {
        return new File(SAVE_FILE).exists();
    }

    /**
     * Deletes the save file from disk.
     * Called when the player starts a new game to prevent a stale "Continue" option.
     */
    public void deleteSave() {
        File file = new File(SAVE_FILE);
        if (file.delete()) {
            log.info("Save file deleted: path={}", file.getAbsolutePath());
        } else {
            log.warn("Could not delete save file (may not exist): path={}", file.getAbsolutePath());
        }
    }
}
