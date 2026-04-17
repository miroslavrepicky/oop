package sk.stuba.fiit.render;

import java.util.List;

/**
 * Nemodifikovateľný dátový objekt popisujúci stav scény v jednom snímku.
 *   - EntityRenderData DTO pre každý vizuálny objekt
 *   - primitívne hodnoty (boolean, float)
 *   - MapRenderData pre mapu (obaluje renderer bez závislosti na MapManager)
 *  GameRenderer teraz importuje iba triedy z balíka sk.stuba.fiit.render.
 *  Nula importov z characters, items, projectiles, world.
 *
 *  Kto zostavuje snapshot: PlayingState (Controller vrstva).
 *  Controller smie poznať model – zostavuje DTO a predá view.
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

    public RenderSnapshot(
        EntityRenderData       player,
        List<EntityRenderData> enemies,
        List<EntityRenderData> ducks,
        List<ItemRenderData>   items,
        List<EntityRenderData> projectiles,
        MapRenderData          map,
        boolean                debugHitboxes,
        boolean                nearbyItemAvailable) {
        this.player              = player;
        this.enemies             = enemies;
        this.ducks               = ducks;
        this.items               = items;
        this.projectiles         = projectiles;
        this.map                 = map;
        this.debugHitboxes       = debugHitboxes;
        this.nearbyItemAvailable = nearbyItemAvailable;
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
