package sk.stuba.fiit.save;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.Archer;
import sk.stuba.fiit.characters.Knight;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.characters.Wizzard;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.exceptions.ShadowQuestException;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.items.Armour;
import sk.stuba.fiit.items.HealingPotion;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.util.Vector2D;

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
 * <p>Serialisation strategy: Java {@link ObjectOutputStream}/{@link ObjectInputStream}
 * writing to {@value #SAVE_FILE}. {@link SaveData} is a pure DTO (only primitives
 * and Strings) – no LibGDX or OpenGL dependencies.
 *
 * <p>Compatibility guard: on load, {@link SaveData#saveVersion} is compared to
 * {@link SaveData#SAVE_VERSION}. A mismatch causes the file to be ignored and
 * {@code -1} to be returned.
 *
 * <p>Save procedure:
 * <ol>
 *   <li>Reads the current {@link Inventory} from {@link GameManager}.</li>
 *   <li>Converts each character to {@link SaveData.CharacterData}.</li>
 *   <li>Groups items by class and converts them to {@link SaveData.ItemData}.</li>
 *   <li>Serialises the {@link SaveData} object.</li>
 * </ol>
 *
 * <p>Load procedure:
 * <ol>
 *   <li>Deserialises {@link SaveData}.</li>
 *   <li>Resets {@link GameManager}.</li>
 *   <li>Reconstructs characters and items into a new {@link Inventory}.</li>
 *   <li>Returns the saved level number for {@link GameManager} to start.</li>
 * </ol>
 */
public final class SaveManager {

    private static SaveManager instance;

    /** Cesta k súboru uloženej hry – relatívna k working directory. */
    public static final String SAVE_FILE = "shadowquest.save";

    private static final Logger log = GameLogger.get(SaveManager.class);

    private SaveManager() {}

    public static SaveManager getInstance() {
        if (instance == null) instance = new SaveManager();
        return instance;
    }

    // -------------------------------------------------------------------------
    //  Uloženie
    // -------------------------------------------------------------------------

    /**
     * Saves the current game state to disk.
     *
     * @param currentLevelNumber the level currently active (1-based)
     * @throws SaveException if the write fails
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

    private SaveData buildSaveData(int levelNumber) {
        Inventory inv = GameManager.getInstance().getInventory();

        // --- Konverzia postáv ---
        List<SaveData.CharacterData> chars = new ArrayList<>();
        PlayerCharacter activeChar = inv.getActive();

        for (PlayerCharacter pc : inv.getCharacters()) {
            chars.add(new SaveData.CharacterData(
                pc.getClass().getSimpleName(),
                pc.getHp(),
                pc.getArmor(),
                inv.isBaseCharacter(pc),
                pc == activeChar
            ));
            log.debug("Saving character: type={}, hp={}, armor={}, isBase={}",
                pc.getClass().getSimpleName(), pc.getHp(), pc.getArmor(),
                inv.isBaseCharacter(pc));
        }

        // --- Konverzia itemov (skupinované podľa triedy) ---
        Map<String, Integer> itemCounts = new LinkedHashMap<>();
        for (Item item : inv.getItems()) {
            String key = item.getClass().getSimpleName();
            itemCounts.merge(key, 1, Integer::sum);
        }

        List<SaveData.ItemData> itemData = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            itemData.add(new SaveData.ItemData(entry.getKey(), entry.getValue()));
            log.debug("Saving item: type={}, count={}", entry.getKey(), entry.getValue());
        }

        return new SaveData(levelNumber, chars, itemData);
    }

    private void writeToDisk(SaveData data) {
        File file = new File(SAVE_FILE);
        try (ObjectOutputStream oos =
                 new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        } catch (IOException e) {
            log.error("Failed to write save file: path={}", file.getAbsolutePath(), e);
            throw new SaveException("Uloženie hry zlyhalo: " + e.getMessage(), e);
        }
    }

    /**
     * Loads a saved game and reconstructs {@code GameManager} + {@code Inventory} state.
     *
     * @return the saved level number to pass to {@code startLevel()},
     *         or {@code -1} if no save file exists or the version is incompatible
     */
    public int load() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            log.info("No save file found: path={}", file.getAbsolutePath());
            return -1;
        }

        SaveData data = readFromDisk(file);
        if (data == null) return -1;

        if (data.saveVersion != SaveData.SAVE_VERSION) {
            log.warn("Save file version mismatch: fileVersion={}, expectedVersion={} – ignoring",
                data.saveVersion, SaveData.SAVE_VERSION);
            return -1;
        }

        applyToGameManager(data);

        log.info("Game loaded: level={}, characters={}, items={}, savedAt={}",
            data.currentLevel,
            data.characters.size(),
            data.inventoryItems.size(),
            data.savedAt);

        return data.currentLevel;
    }

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

    private void applyToGameManager(SaveData data) {
        // Resetujeme hru a zostavíme nový Inventory zo SaveData
        GameManager gm = GameManager.getInstance();
        gm.resetGame();

        Inventory inv = gm.getInventory();

        // --- Rekonštrukcia postáv ---
        PlayerCharacter activeChar = null;
        for (SaveData.CharacterData cd : data.characters) {
            PlayerCharacter pc = createCharacter(cd.characterType);
            if (pc == null) {
                log.warn("Unknown character type in save – skipped: type={}", cd.characterType);
                continue;
            }
            // Obnovíme HP a armor zo save
            pc.revive();                          // nastaví maxHp
            pc.takeDamage(pc.getMaxHp() - cd.hp); // zredukujeme na uložené HP
            // Armor: rozdiel oproti max
            int armorDiff = pc.getMaxArmor() - cd.armor;
            if (armorDiff > 0) pc.takeDamage(armorDiff); // jednoduché – armor sa spotrebuje

            inv.addCharacter(pc);

            if (cd.isActive) activeChar = pc;

            log.debug("Restored character: type={}, hp={}, armor={}",
                cd.characterType, cd.hp, cd.armor);
        }

        // Aktivujeme správnu postavu
        if (activeChar != null) {
            for (int i = 0; i < inv.getCharacters().size(); i++) {
                if (inv.getCharacters().get(i) == activeChar) {
                    inv.switchCharacter(i + 1);
                    break;
                }
            }
        }

        // --- Rekonštrukcia itemov ---
        for (SaveData.ItemData id : data.inventoryItems) {
            for (int i = 0; i < id.count; i++) {
                Item item = createItem(id.itemType);
                if (item == null) {
                    log.warn("Unknown item type in save – skipped: type={}", id.itemType);
                    continue;
                }
                inv.addItem(item);
                log.debug("Restored item: type={}", id.itemType);
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Factory metódy – mapovanie String → konkrétna trieda
    // -------------------------------------------------------------------------

    /**
     * Vytvorí postavu podľa názvu triedy uloženého v {@link SaveData.CharacterData#characterType}.
     *
     * <p>Pozícia je pri rekonštrukcii (0,0) – Level.load() ju prepíše
     * z Tiled mapy keď sa level spustí.
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
     * Vytvorí item podľa názvu triedy uloženého v {@link SaveData.ItemData#itemType}.
     */
    private Item createItem(String type) {
        return switch (type) {
            case "HealingPotion" -> new HealingPotion(50, new Vector2D(0, 0));
            case "Armour"        -> new Armour(30, new Vector2D(0, 0));
            default              -> null;
        };
    }

    // -------------------------------------------------------------------------
    //  Pomocné verejné metódy
    // -------------------------------------------------------------------------

    /** @return {@code true} if a save file exists on disk */
    public boolean hasSave() {
        return new File(SAVE_FILE).exists();
    }

    /** Deletes the save file from the disk (e.g., when starting a new game). */
    public void deleteSave() {
        File file = new File(SAVE_FILE);
        if (file.delete()) {
            log.info("Save file deleted: path={}", file.getAbsolutePath());
        } else {
            log.warn("Could not delete save file (may not exist): path={}", file.getAbsolutePath());
        }
    }

    // =========================================================================
    //  Vnorená výnimka
    // =========================================================================

    /**
     * Výnimka pri zlyhaní uloženia hry.
     * Rozlišuje chyby ukladania od ostatných herných chýb.
     */
    public static final class SaveException extends ShadowQuestException {
        public SaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
