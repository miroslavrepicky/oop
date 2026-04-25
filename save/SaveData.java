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
 *   ├── saveVersion      – guards against incompatible old files
 *   ├── savedAt          – ISO timestamp for the UI
 *   ├── currentLevel     – 1-based level number where the player saved
 *   ├── characters[]     – party snapshot (type, HP, armor, position, facing…)
 *   ├── inventoryItems[] – item snapshot (type + count, grouped by class)
 *   ├── enemies[]        – live enemies in the level (type, position, HP, armor)
 *   ├── groundItems[]    – items lying on the ground (type, position)
 *   └── ducks[]          – live ducks in the level (position, HP)
 * </pre>
 */
public final class SaveData implements Serializable {

    /** Zvýšiť pri každej zmene štruktúry ktorá je nekompatibilná so starými súbormi. */
    public static final int SAVE_VERSION = 3;

    private static final long serialVersionUID = 3L;

    // -------------------------------------------------------------------------
    //  Polia uloženého stavu
    // -------------------------------------------------------------------------

    /** Verzia formátu – porovnaná pri načítaní so {@link #SAVE_VERSION}. */
    public final int    saveVersion;

    /** ISO dátum a čas uloženia, napr. "2025-04-19 14:32:01". */
    public final String savedAt;

    /** Číslo levelu, ktorý bol aktívny pri uložení (1-based). */
    public final int    currentLevel;

    /** Stav každej postavy v inventári. */
    public final List<CharacterData>  characters;

    /** Itemy v inventári (skupinové – jeden záznam na typ + počet). */
    public final List<ItemData>       inventoryItems;

    /** Živí nepriatelia v leveli pri uložení. */
    public final List<EnemyData>      enemies;

    /** Predmety ležiace na zemi pri uložení. */
    public final List<GroundItemData> groundItems;

    /** Živé kačky v leveli pri uložení. */
    public final List<DuckData>       ducks;


    SaveData(int currentLevel,
                    List<CharacterData> characters,
                    List<ItemData> inventoryItems,
                    List<EnemyData> enemies,
                    List<GroundItemData> groundItems,
                    List<DuckData> ducks) {
        this.saveVersion    = SAVE_VERSION;
        this.savedAt        = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.currentLevel   = currentLevel;
        this.characters     = new ArrayList<>(characters);
        this.inventoryItems = new ArrayList<>(inventoryItems);
        this.enemies        = new ArrayList<>(enemies);
        this.groundItems    = new ArrayList<>(groundItems);
        this.ducks          = new ArrayList<>(ducks);
    }

    // =========================================================================
    //  Vnorené DTO pre postavu
    // =========================================================================

    /**
     * Serializovateľný snapshot jednej postavy v inventári.
     */
    public static final class CharacterData implements Serializable {
        private static final long serialVersionUID = 2L;

        /** Trieda postavy, napr. "Knight", "Wizzard", "Archer". */
        public final String  characterType;
        /** Aktuálne HP pri uložení. */
        public final int     hp;
        /** Aktuálny armor pri uložení. */
        public final int     armor;
        /** True = táto postava je base (Knight) a nemôže byť odstránená. */
        public final boolean isBase;
        /** True = táto postava bola aktívna pri uložení. */
        public final boolean isActive;
        /** Pozícia vo svete – relevantná najmä pre aktívnu postavu. */
        public final float   x, y;
        public final boolean facingRight;

        public CharacterData(String characterType, int hp, int armor,
                             boolean isBase, boolean isActive,
                             float x, float y, boolean facingRight) {
            this.characterType = characterType;
            this.hp            = hp;
            this.armor         = armor;
            this.isBase        = isBase;
            this.isActive      = isActive;
            this.x             = x;
            this.y             = y;
            this.facingRight   = facingRight;
        }

        @Override
        public String toString() {
            return "CharacterData{type=" + characterType
                + ", hp=" + hp + ", armor=" + armor
                + ", isBase=" + isBase + ", isActive=" + isActive
                + ", x=" + x + ", y=" + y
                + ", facingRight=" + facingRight + "}";
        }
    }

    // =========================================================================
    //  Vnorené DTO pre inventárový item
    // =========================================================================

    /**
     * Serializovateľný snapshot jedného druhu itemu v inventári.
     * Itemy rovnakého druhu sa skupinujú (count).
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

    // =========================================================================
    //  Vnorené DTO pre nepriateľa v leveli
    // =========================================================================

    /** Snapshot jedného živého nepriateľa v leveli. */
    public static final class EnemyData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Jednoduchý názov triedy, napr. "EnemyKnight", "DarkKnight". */
        public final String type;
        public final float  x, y;
        public final int    hp, armor;

        public EnemyData(String type, float x, float y, int hp, int armor) {
            this.type  = type;
            this.x     = x;
            this.y     = y;
            this.hp    = hp;
            this.armor = armor;
        }

        @Override
        public String toString() {
            return "EnemyData{type=" + type
                + ", x=" + x + ", y=" + y
                + ", hp=" + hp + ", armor=" + armor + "}";
        }
    }

    // =========================================================================
    //  Vnorené DTO pre predmet na zemi
    // =========================================================================

    /** Snapshot jedného predmetu ležiaceho na zemi v leveli. */
    public static final class GroundItemData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Jednoduchý názov triedy, napr. "HealingPotion", "Armour". */
        public final String type;
        public final float  x, y;

        public GroundItemData(String type, float x, float y) {
            this.type = type;
            this.x    = x;
            this.y    = y;
        }

        @Override
        public String toString() {
            return "GroundItemData{type=" + type + ", x=" + x + ", y=" + y + "}";
        }
    }

    // =========================================================================
    //  Vnorené DTO pre kačku
    // =========================================================================

    /** Snapshot jednej živej kačky v leveli. */
    public static final class DuckData implements Serializable {
        private static final long serialVersionUID = 1L;

        public final float x, y;
        /** Aktuálne HP kačky pri uložení (zvyčajne plné, ale môže byť poškodená AOE). */
        public final int   hp;

        public DuckData(float x, float y, int hp) {
            this.x  = x;
            this.y  = y;
            this.hp = hp;
        }

        @Override
        public String toString() {
            return "DuckData{x=" + x + ", y=" + y + ", hp=" + hp + "}";
        }
    }

    // -------------------------------------------------------------------------
    //  toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "SaveData{version=" + saveVersion
            + ", savedAt='" + savedAt + "'"
            + ", level=" + currentLevel
            + ", chars=" + characters.size()
            + ", inventoryItems=" + inventoryItems.size()
            + ", enemies=" + enemies.size()
            + ", groundItems=" + groundItems.size()
            + ", ducks=" + ducks.size() + "}";
    }
}
