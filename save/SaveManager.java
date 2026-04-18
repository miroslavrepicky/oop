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
 * Singleton zodpovedný za ukladanie a načítavanie {@link SaveData} na disk.
 *
 * <p>Serializačná stratégia: Java {@link ObjectOutputStream} /
 * {@link ObjectInputStream} so súborom {@value #SAVE_FILE}.
 * {@link SaveData} je čistý DTO (iba primitívy + Stringy) –
 * žiadne LibGDX ani OpenGL závislosti.
 *
 * <p>Ochrana pred nekompatibilnými súbormi: pri načítaní sa porovnáva
 * {@link SaveData#saveVersion} s {@link SaveData#SAVE_VERSION}.
 * Ak sa líšia, súbor sa ignoruje a vráti sa {@code null}.
 *
 * <p>Postup uloženia (save):
 * <ol>
 *   <li>Prečíta aktuálny {@link Inventory} z {@link GameManager}.
 *   <li>Konvertuje každú postavu na {@link SaveData.CharacterData}.
 *   <li>Konvertuje itemy na {@link SaveData.ItemData} (skupinované).
 *   <li>Serializuje {@link SaveData} cez {@link ObjectOutputStream}.
 * </ol>
 *
 * <p>Postup načítania (load):
 * <ol>
 *   <li>Deserializuje {@link SaveData} zo súboru.
 *   <li>Resetuje {@link GameManager}.
 *   <li>Rekonštruuje postavy a itemy do nového {@link Inventory}.
 *   <li>Vráti číslo levelu na ktorý má {@link GameManager} prejsť.
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
     * Uloží aktuálny stav hry do súboru.
     *
     * @param currentLevelNumber číslo levelu kde hráč práve je (1-based)
     * @throws SaveException ak zápis zlyhá
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

    // -------------------------------------------------------------------------
    //  Načítanie
    // -------------------------------------------------------------------------

    /**
     * Načíta uloženú hru a rekonštruuje stav GameManager + Inventory.
     *
     * @return číslo levelu kde hráč bol (na predanie do {@code startLevel()}),
     *         alebo {@code -1} ak súbor neexistuje alebo je nekompatibilný
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

    /** Vráti true ak existuje uložená hra. */
    public boolean hasSave() {
        return new File(SAVE_FILE).exists();
    }

    /** Vymaže uložený súbor (napr. pri New Game). */
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
