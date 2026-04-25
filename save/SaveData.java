package sk.stuba.fiit.save;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Serialisable data-transfer object that captures the complete state of a saved game.
 *
 * <h2>Why a dedicated DTO</h2>
 * <ul>
 *   <li>Game classes depend on LibGDX objects ({@code TextureAtlas}, {@code Rectangle}, …)
 *       which are not {@link Serializable} and belong only to the runtime.</li>
 *   <li>{@code SaveData} contains only primitives and {@link String}s – it can be saved,
 *       loaded, and validated without a LibGDX context (e.g. in unit tests).</li>
 *   <li>The format version {@link #SAVE_VERSION} protects against loading stale files
 *       written by an older version of the game.</li>
 * </ul>
 *
 * <h2>Structure</h2>
 * <pre>
 *   SaveData
 *   ├ saveVersion      – guards against incompatible older files
 *   ├ savedAt          – ISO timestamp shown in the UI
 *   ├ currentLevel     – 1-based level number at the time of saving
 *   ├ characters[]     – party snapshot (type, HP, armour, position, facing…)
 *   ├ inventoryItems[] – item snapshot (type + count, grouped by class)
 *   ├ enemies[]        – living enemies in the level at save time
 *   ├ groundItems[]    – items lying on the ground at save time
 *   └ ducks[]          – living ducks in the level at save time
 * </pre>
 */
public final class SaveData implements Serializable {

    /**
     * Increment this constant whenever the structure changes in a way that is
     * incompatible with previously written save files. {@link SaveManager#load()}
     * discards files whose {@link #saveVersion} does not match.
     */
    public static final int SAVE_VERSION = 3;

    private static final long serialVersionUID = 3L;

    // -------------------------------------------------------------------------
    //  Saved-state fields
    // -------------------------------------------------------------------------

    /** Format version – compared against {@link #SAVE_VERSION} on load. */
    public final int    saveVersion;

    /** ISO date and time of the save, e.g. {@code "2025-04-19 14:32:01"}. */
    public final String savedAt;

    /** 1-based level number that was active when the game was saved. */
    public final int    currentLevel;

    /** Snapshot of every character in the inventory at save time. */
    public final List<CharacterData>  characters;

    /**
     * Items in the inventory at save time, grouped by class.
     * One entry per distinct item type; the {@code count} field records how many
     * of that type were present.
     */
    public final List<ItemData>       inventoryItems;

    /** Living enemies in the level at save time. */
    public final List<EnemyData>      enemies;

    /** Items lying on the ground at save time. */
    public final List<GroundItemData> groundItems;

    /** Living ducks in the level at save time. */
    public final List<DuckData>       ducks;

    SaveData(int currentLevel,
             List<CharacterData>  characters,
             List<ItemData>       inventoryItems,
             List<EnemyData>      enemies,
             List<GroundItemData> groundItems,
             List<DuckData>       ducks) {
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
    //  Nested DTO – character
    // =========================================================================

    /**
     * Serialisable snapshot of a single party member at save time.
     */
    public static final class CharacterData implements Serializable {
        private static final long serialVersionUID = 2L;

        /** Simple class name of the character, e.g. {@code "Knight"}, {@code "Wizzard"}, {@code "Archer"}. */
        public final String  characterType;
        /** HP at the time of saving. */
        public final int     hp;
        /** Armour value at the time of saving. */
        public final int     armor;
        /** {@code true} if this character is the base character (Knight) and cannot be removed. */
        public final boolean isBase;
        /** {@code true} if this character was the active (player-controlled) character at save time. */
        public final boolean isActive;
        /** World position at save time – most relevant for the active character. */
        public final float   x, y;
        /** Facing direction at save time. */
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
    //  Nested DTO – inventory item
    // =========================================================================

    /**
     * Serialisable snapshot of one item type in the inventory.
     * Items of the same type are grouped into a single entry with a {@code count}.
     */
    public static final class ItemData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Simple class name of the item, e.g. {@code "HealingPotion"}, {@code "Armour"}. */
        public final String itemType;
        /** Number of items of this type in the inventory at save time. */
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
    //  Nested DTO – enemy in the level
    // =========================================================================

    /**
     * Snapshot of a single living enemy in the level at save time.
     */
    public static final class EnemyData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Simple class name, e.g. {@code "EnemyKnight"}, {@code "DarkKnight"}. */
        public final String type;
        /** World position at save time. */
        public final float  x, y;
        /** HP and armour at save time. */
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
    //  Nested DTO – ground item
    // =========================================================================

    /**
     * Snapshot of a single item lying on the ground in the level at save time.
     */
    public static final class GroundItemData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Simple class name, e.g. {@code "HealingPotion"}, {@code "Armour"}. */
        public final String type;
        /** World position at save time. */
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
    //  Nested DTO – duck
    // =========================================================================

    /**
     * Snapshot of a single living duck in the level at save time.
     */
    public static final class DuckData implements Serializable {
        private static final long serialVersionUID = 1L;

        /** World position at save time. */
        public final float x, y;
        /**
         * HP at save time. Normally full, but may be reduced if the duck was
         * caught in an AOE explosion before the game was saved.
         */
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
