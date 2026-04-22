package sk.stuba.fiit.render;

import java.util.List;

/**
 * Immutable data transfer object describing the complete visual state of the
 * game scene for a single frame.
 *
 * <p>After refactoring to clean MVC, {@link GameRenderer} imports ONLY classes
 * from the {@code render} package. No model classes ({@code characters},
 * {@code items}, {@code projectiles}, {@code world}) are referenced by the renderer.
 *
 * <p>Who builds the snapshot: {@link SnapshotBuilder} (Controller layer).
 * The controller may know the model; the View ({@code GameRenderer}) may not.
 */
public final class RenderSnapshot {

    /** Aktívny hráč – null ak party porazená. */
    public final EntityRenderData       player;

    /** Všetci živí a mŕtvi (death anim) nepriatelia. */
    public final List<EntityRenderData> enemies;

    /** Živé kačky. */
    public final List<EntityRenderData> ducks;

    /** Itemy na zemi – pozícia + iconPath. */
    public final List<ItemRenderData>   items;

    /** Aktívne projektily. */
    public final List<EntityRenderData> projectiles;

    /** Mapa – null ak nie je načítaná. */
    public final MapRenderData          map;

    /** true = zobraziť debug hitboxy (F1). */
    public final boolean debugHitboxes;

    /** true = v blízkosti je item → HUD "[E] PICK-UP". */
    public final boolean nearbyItemAvailable;

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

    // -------------------------------------------------------------------------
    //  DTO pre HUD
    // -------------------------------------------------------------------------

    public static final class HUDSnapshot {
        public final List<CharacterHUDData> characters;
        public final int                    selectedSlot;
        public final List<ItemSlotData>     itemSlots;
        public final int                    usedSlots;
        public final int                    totalSlots;
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

        public static final class CharacterHUDData {
            public final String  name;
            public final int     hp;
            public final int     maxHp;
            public final int     armor;
            public final int     maxArmor;
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
             * Maximum arrows, or {@code -1} if this character has no arrow limit.
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
                this.name     = name;
                this.hp       = hp;
                this.maxHp    = maxHp;
                this.armor    = armor;
                this.maxArmor = maxArmor;
                this.isActive = isActive;
                this.mana     = mana;
                this.maxMana  = maxMana;
                this.arrows   = arrows;
                this.maxArrows = maxArrows;
            }
        }

        public static final class ItemSlotData {
            public final String iconPath;
            public final int    slotsRequired;

            public ItemSlotData(String iconPath, int slotsRequired) {
                this.iconPath      = iconPath;
                this.slotsRequired = slotsRequired;
            }
        }
    }


    // -------------------------------------------------------------------------
    //  Vnorené DTO pre itemy (ikonka + pozícia)
    // -------------------------------------------------------------------------

    public static final class ItemRenderData {
        public final float  x, y;
        public final String iconPath;

        public ItemRenderData(float x, float y, String iconPath) {
            this.x = x; this.y = y; this.iconPath = iconPath;
        }
    }

    // -------------------------------------------------------------------------
    //  Vnorené DTO pre mapu
    //  Obaľuje OrthogonalTiledMapRenderer bez toho, aby View vedelo o MapManager.
    //  Render sa deleguje cez lambda / functional interface.
    // -------------------------------------------------------------------------

    public static final class MapRenderData {
        /** Volá mapRenderer.render(camera) – lambda vykonaná v GameRenderer. */
        public final MapRenderCallback renderCallback;
        /** Debug hitboxy mapy – list Rectangle-ov pre ShapeRenderer. */
        public final List<com.badlogic.gdx.math.Rectangle> hitboxes;

        public MapRenderData(MapRenderCallback renderCallback,
                             List<com.badlogic.gdx.math.Rectangle> hitboxes) {
            this.renderCallback = renderCallback;
            this.hitboxes       = hitboxes;
        }

        @FunctionalInterface
        public interface MapRenderCallback {
            void render(com.badlogic.gdx.graphics.OrthographicCamera camera);
        }
    }
}
