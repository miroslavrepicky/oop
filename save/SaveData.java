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
 *   ├ saveVersion      – guards against incompatible old files
 *   ├ savedAt          – ISO timestamp for the UI
 *   ├ currentLevel     – 1-based level number where the player saved
 *   ├ characters[]     – party snapshot (type, HP, armor, position, facing…)
 *   ├ inventoryItems[] – item snapshot (type + count, grouped by class)
 *   ├ enemies[]        – live enemies in the level (type, position, HP, armor)
 *   ├ groundItems[]    – items lying on the ground (type, position)
 *   └ ducks[]          – live ducks in the level (position, HP)
 * </pre>
 */
public final class SaveData implements Serializable {

    /** Zvysit pri kazdej zmene struktury ktora je nekompatibilná so starymi subormi. */
    public static final int SAVE_VERSION = 3;

    private static final long serialVersionUID = 3L;

    // -------------------------------------------------------------------------
    //  Polia ulozeneho stavu
    // -------------------------------------------------------------------------

    /** Verzia formátu – porovnaná pri nacitani so {@link #SAVE_VERSION}. */
    public final int    saveVersion;

    /** ISO dátum a cas ulozenia, napr. "2025-04-19 14:32:01". */
    public final String savedAt;

    /** cislo levelu, ktory bol aktivny pri ulozeni (1-based). */
    public final int    currentLevel;

    /** Stav kazdej postavy v inventári. */
    public final List<CharacterData>  characters;

    /** Itemy v inventári (skupinove – jeden záznam na typ + pocet). */
    public final List<ItemData>       inventoryItems;

    /** zivi nepriatelia v leveli pri ulozeni. */
    public final List<EnemyData>      enemies;

    /** Predmety leziace na zemi pri ulozeni. */
    public final List<GroundItemData> groundItems;

    /** zive kacky v leveli pri ulozeni. */
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
    //  Vnorene DTO pre postavu
    // =========================================================================

    /**
     * Serializovatelny snapshot jednej postavy v inventári.
     */
    public static final class CharacterData implements Serializable {
        private static final long serialVersionUID = 2L;

        /** Trieda postavy, napr. "Knight", "Wizzard", "Archer". */
        public final String  characterType;
        /** Aktuálne HP pri ulozeni. */
        public final int     hp;
        /** Aktuálny armor pri ulozeni. */
        public final int     armor;
        /** True = táto postava je base (Knight) a nemoze byt odstránená. */
        public final boolean isBase;
        /** True = táto postava bola aktivna pri ulozeni. */
        public final boolean isActive;
        /** Pozicia vo svete – relevantná najma pre aktivnu postavu. */
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
    //  Vnorene DTO pre inventárovy item
    // =========================================================================

    /**
     * Serializovatelny snapshot jedneho druhu itemu v inventári.
     * Itemy rovnakeho druhu sa skupinuju (count).
     */
    public static final class ItemData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Trieda itemu, napr. "HealingPotion", "Armour". */
        public final String itemType;
        /** Pocet itemov tohto druhu. */
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
    //  Vnorene DTO pre nepriatela v leveli
    // =========================================================================

    /** Snapshot jedneho ziveho nepriatela v leveli. */
    public static final class EnemyData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Jednoduchy názov triedy, napr. "EnemyKnight", "DarkKnight". */
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
    //  Vnorene DTO pre predmet na zemi
    // =========================================================================

    /** Snapshot jedneho predmetu leziaceho na zemi v leveli. */
    public static final class GroundItemData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Jednoduchy názov triedy, napr. "HealingPotion", "Armour". */
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
    //  Vnorene DTO pre kacku
    // =========================================================================

    /** Snapshot jednej zivej kacky v leveli. */
    public static final class DuckData implements Serializable {
        private static final long serialVersionUID = 1L;

        public final float x, y;
        /** Aktuálne HP kacky pri ulozeni (zvycajne plne, ale moze byt poskodená AOE). */
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
