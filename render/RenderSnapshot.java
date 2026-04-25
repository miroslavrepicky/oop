package sk.stuba.fiit.render;

import java.util.List;

/**
 * Immutable data transfer object describing the complete visual state of the
 * game scene for a single frame.
 *
 * <h2>MVC role</h2>
 * <p>After the clean-MVC refactor, {@link GameRenderer} imports <em>only</em> classes
 * from the {@code render} package. No model classes ({@code characters},
 * {@code items}, {@code projectiles}, {@code world}) are referenced by the renderer.
 * This snapshot is the boundary between the Controller and the View.
 *
 * <p>Who builds the snapshot: {@link SnapshotBuilder} (Controller layer).
 * The controller may import model classes; the View ({@code GameRenderer}) must not.
 *
 * <h2>Contents</h2>
 * <ul>
 *   <li>{@link #player} – active player render data; {@code null} if the party is defeated.</li>
 *   <li>{@link #enemies} – all enemies currently in the level (alive and in death animation).</li>
 *   <li>{@link #ducks}   – all living ducks.</li>
 *   <li>{@link #items}   – items lying on the ground (position + icon path).</li>
 *   <li>{@link #projectiles} – all active projectiles.</li>
 *   <li>{@link #map}     – map render callback and hitbox list; {@code null} if no map is loaded.</li>
 *   <li>{@link #debugHitboxes} – whether the debug hitbox overlay is enabled (F1 toggle).</li>
 *   <li>{@link #nearbyItemAvailable} – whether the "[E] PICK-UP" hint should be shown.</li>
 *   <li>{@link #hud}     – complete HUD data snapshot.</li>
 * </ul>
 */
public final class RenderSnapshot {

    /** Active player; {@code null} if the party is defeated. */
    public final EntityRenderData       player;

    /** All enemies – alive and playing their death animation. */
    public final List<EntityRenderData> enemies;

    /** Living ducks. */
    public final List<EntityRenderData> ducks;

    /** Items on the ground – position and icon path. */
    public final List<ItemRenderData>   items;

    /** Active projectiles. */
    public final List<EntityRenderData> projectiles;

    /** Map render data; {@code null} if no map is loaded. */
    public final MapRenderData          map;

    /** {@code true} = draw debug hitbox outlines (F1 toggle). */
    public final boolean debugHitboxes;

    /** {@code true} = a nearby item is within pick-up range; show the "[E] PICK-UP" hint. */
    public final boolean nearbyItemAvailable;

    /** HUD snapshot containing character stats and inventory slot data. */
    public final HUDSnapshot            hud;

    public RenderSnapshot(
        EntityRenderData       player,
        List<EntityRenderData> enemies,
        List<EntityRenderData> ducks,
        List<ItemRenderData>   items,
        List<EntityRenderData> projectiles,
        MapRenderData          map,
        boolean                debugHitboxes,
        boolean                nearbyItemAvailable,
        HUDSnapshot            hud) {
        this.player              = player;
        this.enemies             = enemies;
        this.ducks               = ducks;
        this.items               = items;
        this.projectiles         = projectiles;
        this.map                 = map;
        this.debugHitboxes       = debugHitboxes;
        this.nearbyItemAvailable = nearbyItemAvailable;
        this.hud                 = hud;
    }

    // =========================================================================
    //  Nested DTO – HUD
    // =========================================================================

    /**
     * Snapshot of all data required to render the HUD for one frame.
     * Built by {@link SnapshotBuilder} and consumed by {@link HUDRenderer}.
     */
    public static final class HUDSnapshot {

        /** Status data for each party member, in party order. */
        public final List<CharacterHUDData> characters;

        /** Zero-based index of the currently selected inventory slot. */
        public final int                    selectedSlot;

        /** Descriptor for each item currently in the inventory, in slot order. */
        public final List<ItemSlotData>     itemSlots;

        /** Number of inventory slots currently in use. */
        public final int                    usedSlots;

        /** Total number of inventory slots available. */
        public final int                    totalSlots;

        /** {@code true} when a nearby item is within pick-up range. */
        public final boolean                nearbyItemAvailable;

        public HUDSnapshot(List<CharacterHUDData> characters,
                           int                    selectedSlot,
                           List<ItemSlotData>     itemSlots,
                           int                    usedSlots,
                           int                    totalSlots,
                           boolean                nearbyItemAvailable) {
            this.characters          = characters;
            this.selectedSlot        = selectedSlot;
            this.itemSlots           = itemSlots;
            this.usedSlots           = usedSlots;
            this.totalSlots          = totalSlots;
            this.nearbyItemAvailable = nearbyItemAvailable;
        }

        // -----------------------------------------------------------------------
        //  Nested DTO – per-character HUD row
        // -----------------------------------------------------------------------

        /**
         * HUD data for a single party member.
         *
         * <p>Resource fields ({@code mana}, {@code maxMana}, {@code arrows},
         * {@code maxArrows}) use {@code -1} to signal "not applicable".
         * {@link HUDRenderer} skips the corresponding bar or counter when it
         * encounters a {@code -1} value, so no conditional logic on concrete
         * character types is needed in the renderer.
         */
        public static final class CharacterHUDData {
            /** Display name shown in the character list. */
            public final String  name;
            public final int     hp;
            public final int     maxHp;
            public final int     armor;
            public final int     maxArmor;
            /** {@code true} when this is the currently controlled character. */
            public final boolean isActive;

            /**
             * Current mana, or {@code -1} if this character has no mana system.
             * A value of {@code -1} tells {@link HUDRenderer} to skip the mana bar.
             */
            public final int mana;

            /**
             * Maximum mana, or {@code -1} if this character has no mana system.
             */
            public final int maxMana;

            /**
             * Remaining arrows, or {@code -1} if this character has no arrow limit.
             * A value of {@code -1} tells {@link HUDRenderer} to skip the arrow counter.
             */
            public final int arrows;

            /**
             * Maximum arrow capacity, or {@code -1} if this character has no arrow limit.
             */
            public final int maxArrows;

            /**
             * @param name      display name
             * @param hp        current HP
             * @param maxHp     maximum HP
             * @param armor     current armour
             * @param maxArmor  maximum armour
             * @param isActive  {@code true} when this is the currently controlled character
             * @param mana      current mana, or {@code -1} if not applicable
             * @param maxMana   maximum mana, or {@code -1} if not applicable
             * @param arrows    remaining arrows, or {@code -1} if not applicable
             * @param maxArrows maximum arrows, or {@code -1} if not applicable
             */
            public CharacterHUDData(String  name,
                                    int     hp,
                                    int     maxHp,
                                    int     armor,
                                    int     maxArmor,
                                    boolean isActive,
                                    int     mana,
                                    int     maxMana,
                                    int     arrows,
                                    int     maxArrows) {
                this.name      = name;
                this.hp        = hp;
                this.maxHp     = maxHp;
                this.armor     = armor;
                this.maxArmor  = maxArmor;
                this.isActive  = isActive;
                this.mana      = mana;
                this.maxMana   = maxMana;
                this.arrows    = arrows;
                this.maxArrows = maxArrows;
            }
        }

        // -----------------------------------------------------------------------
        //  Nested DTO – inventory slot
        // -----------------------------------------------------------------------

        /**
         * Render descriptor for a single occupied inventory slot.
         */
        public static final class ItemSlotData {
            /** Relative path to the item icon texture, e.g. {@code "icons/potion.png"}. */
            public final String iconPath;
            /** Number of inventory slots this item occupies. */
            public final int    slotsRequired;

            public ItemSlotData(String iconPath, int slotsRequired) {
                this.iconPath      = iconPath;
                this.slotsRequired = slotsRequired;
            }
        }
    }

    // =========================================================================
    //  Nested DTO – ground item
    // =========================================================================

    /**
     * Render descriptor for an item lying on the ground.
     * Contains only a world position and an icon path – no model class reference.
     */
    public static final class ItemRenderData {
        /** World X position of the item hitbox. */
        public final float  x;
        /** World Y position of the item hitbox. */
        public final float  y;
        /** Relative path to the item icon texture; {@code null} = no icon, skip rendering. */
        public final String iconPath;

        public ItemRenderData(float x, float y, String iconPath) {
            this.x = x; this.y = y; this.iconPath = iconPath;
        }
    }

    // =========================================================================
    //  Nested DTO – map
    // =========================================================================

    /**
     * Wraps the map renderer behind a functional callback so the View never
     * imports {@code MapManager} directly.
     *
     * <p>The render lambda is created in {@link SnapshotBuilder} as
     * {@code mapManager::render} and is invoked by {@link GameRenderer}
     * with the active camera. Debug hitboxes are provided as a separate
     * rectangle list so {@code GameRenderer} can draw them with its
     * {@link com.badlogic.gdx.graphics.glutils.ShapeRenderer}.
     */
    public static final class MapRenderData {
        /** Calls {@code mapRenderer.render(camera)} when invoked by the View. */
        public final MapRenderCallback renderCallback;

        /** Wall/platform rectangles used for debug hitbox drawing. */
        public final List<com.badlogic.gdx.math.Rectangle> hitboxes;

        public MapRenderData(MapRenderCallback renderCallback,
                             List<com.badlogic.gdx.math.Rectangle> hitboxes) {
            this.renderCallback = renderCallback;
            this.hitboxes       = hitboxes;
        }

        /**
         * Functional interface for the map rendering callback.
         * Implemented as {@code mapManager::render} in {@link SnapshotBuilder}.
         */
        @FunctionalInterface
        public interface MapRenderCallback {
            /**
             * Renders the tile map using the provided camera.
             *
             * @param camera the active orthographic camera
             */
            void render(com.badlogic.gdx.graphics.OrthographicCamera camera);
        }
    }
}
