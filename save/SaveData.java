package sk.stuba.fiit.save;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Serialisable DTO that captures the complete state of a saved game.
 *
 * <p>Why a dedicated DTO instead of serialising game classes directly:
 * <ul>
 *   <li>Game classes depend on LibGDX objects ({@code TextureAtlas}, {@code Rectangle}…)
 *       which are not serialisable and belong only to the runtime.</li>
 *   <li>{@code SaveData} contains only primitives and Strings – it can be saved,
 *       loaded and validated without a LibGDX context (e.g. in unit tests).</li>
 *   <li>The format version {@link #SAVE_VERSION} protects against loading stale files.</li>
 * </ul>
 *
 * <p>Structure:
 * <pre>
 *   SaveData
 *   ├── saveVersion     – guards against incompatible old files
 *   ├── savedAt         – ISO timestamp for the UI
 *   ├── currentLevel    – 1-based level number where the player saved
 *   ├── characters[]    – party snapshot (type, HP, armor, base flag, active flag)
 *   └── inventoryItems[]– item snapshot (type + count, grouped by class)
 * </pre>
 */
public final class SaveData implements Serializable {

    /** Zvýšiť pri každej zmene štruktúry ktorá je nekompatibilná so starými súbormi. */
    public static final int SAVE_VERSION = 1;

    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    //  Polia uloženého stavu
    // -------------------------------------------------------------------------

    /** Verzia formátu – porovnaná pri načítaní so {@link #SAVE_VERSION}. */
    public final int    saveVersion;

    /** ISO dátum a čas uloženia, napr. "2025-04-19 14:32:01". */
    public final String savedAt;

    /** Číslo levelu ktorý bol aktívny pri uložení (1-based). */
    public final int    currentLevel;

    /** Stav každej postavy v inventári. */
    public final List<CharacterData> characters;

    /** Itemy v inventári (skupinové – jeden záznam na typ + počet). */
    public final List<ItemData> inventoryItems;

    // -------------------------------------------------------------------------
    //  Konštruktor – volá iba SaveManager
    // -------------------------------------------------------------------------

    SaveData(int currentLevel,
             List<CharacterData> characters,
             List<ItemData>      inventoryItems) {
        this.saveVersion     = SAVE_VERSION;
        this.savedAt         = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.currentLevel    = currentLevel;
        this.characters      = new ArrayList<>(characters);
        this.inventoryItems  = new ArrayList<>(inventoryItems);
    }

    // =========================================================================
    //  Vnorené DTO pre postavu
    // =========================================================================

    /**
     * Serializovateľný snapshot jednej postavy.
     *
     * <p>Toto je netriviálna entita projektu: obsahuje HP, armor, typ postavy
     * a príznak či je to základná (nezmazateľná) postava. Tieto hodnoty
     * sú pri načítaní rekonštruované do reálnych {@code PlayerCharacter} objektov.
     */
    public static final class CharacterData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Trieda postavy, napr. "Knight", "Wizzard", "Archer". */
        public final String characterType;

        /** Aktuálne HP pri uložení. */
        public final int hp;

        /** Aktuálny armor pri uložení. */
        public final int armor;

        /** True = táto postava je base (Knight) a nemôže byť odstránená. */
        public final boolean isBase;

        /** True = táto postava bola aktívna pri uložení. */
        public final boolean isActive;

        public CharacterData(String characterType, int hp, int armor,
                             boolean isBase, boolean isActive) {
            this.characterType = characterType;
            this.hp            = hp;
            this.armor         = armor;
            this.isBase        = isBase;
            this.isActive      = isActive;
        }

        @Override
        public String toString() {
            return "CharacterData{type=" + characterType
                + ", hp=" + hp + ", armor=" + armor
                + ", isBase=" + isBase + ", isActive=" + isActive + "}";
        }
    }

    // =========================================================================
    //  Vnorené DTO pre item
    // =========================================================================

    /**
     * Serializovateľný snapshot jedného druhu itemu.
     *
     * <p>Itemy rovnakého druhu sa skupinujú (count) – rovnaký prístup
     * ako v {@code InventoryScreen}.
     */
    public static final class ItemData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Trieda itemu, napr. "HealingPotion", "Armour". */
        public final String itemType;

        /** Počet itemov tohto druhu. */
        public final int    count;

        public ItemData(String itemType, int count) {
            this.itemType = itemType;
            this.count    = count;
        }

        @Override
        public String toString() {
            return "ItemData{type=" + itemType + ", count=" + count + "}";
        }
    }

    // -------------------------------------------------------------------------
    //  toString pre debug
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "SaveData{version=" + saveVersion
            + ", savedAt='" + savedAt + "'"
            + ", level=" + currentLevel
            + ", characters=" + characters.size()
            + ", items=" + inventoryItems.size() + "}";
    }
}
