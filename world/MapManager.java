package sk.stuba.fiit.world;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.maps.MapProperties;
import org.slf4j.Logger;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.exceptions.AssetLoadException;
import sk.stuba.fiit.core.exceptions.GameStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/**
 * Loads and manages a Tiled map ({@code .tmx}) for a single level.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Loads the map via {@link TmxMapLoader} and wraps it in an
 *       {@link OrthogonalTiledMapRenderer}.</li>
 *   <li>Extracts collision rectangles from the {@code "hitbox"} layer.</li>
 *   <li>Extracts entity spawn data from the {@code "entities"} layer.</li>
 * </ul>
 *
 * <p>The {@code "hitbox"} layer is mandatory; a missing layer throws
 * {@link GameStateException} during construction.
 */
public class MapManager {
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private List<Rectangle> hitboxes = new ArrayList<>();;
    private List<Map<String, Object>> entities = new ArrayList<>();
    private static final Logger log = GameLogger.get(MapManager.class);

    /**
     * @param mapPath relative path to the {@code .tmx} file
     * @throws AssetLoadException  if the map file cannot be loaded
     * @throws GameStateException  if the required {@code "hitbox"} layer is absent
     */
    public MapManager(String mapPath) {
        this(loadMapSafe(mapPath));
    }

    public MapManager(TiledMap map) {
        this.map = map;
        //this.renderer = new OrthogonalTiledMapRenderer(map);
        this.entities = new ArrayList<>();
        this.hitboxes = new ArrayList<>();
        loadEntities();
        loadHitboxes();
    }

    private static TiledMap loadMapSafe(String path) {
        try {
            return new TmxMapLoader().load(path);
        } catch (Exception e) {
            throw new AssetLoadException(path, e);
        }
    }

    private void loadEntities() {
        if (map.getLayers().get("entities") == null) {
            log.error("Map is missing required layer: layer=entities");
            throw new GameStateException(
                "Missing 'entities' layer",
                "MapManager.loadEntities");
        }

        for (MapObject object : map.getLayers().get("entities").getObjects()) {
            MapProperties props = object.getProperties();
            Map<String, Object> entity = new HashMap<>();
            entity.put("type", props.get("type", String.class));
            entity.put("x", props.get("x", Float.class));
            entity.put("y", props.get("y", Float.class));
            entities.add(entity);
        }
    }

    private void loadHitboxes() {
        if (map.getLayers().get("hitbox") == null) {
            log.error("Map is missing required layer: layer=hitbox");
            throw new GameStateException(
                "Missing 'hitbox' layer",
                "MapManager.loadHitboxes");
        }
        for (MapObject object : map.getLayers().get("hitbox").getObjects()) {
            if (object instanceof RectangleMapObject) {
                hitboxes.add(((RectangleMapObject) object).getRectangle());
            }
        }
    }

    /**
     * Renders the map using the given camera's view matrix.
     * Should be called once per frame inside the render pass.
     *
     * @param camera the active orthographic camera
     */
    public void render(com.badlogic.gdx.graphics.OrthographicCamera camera) {
        if (renderer == null) {
            renderer = new OrthogonalTiledMapRenderer(map);
        }
        renderer.setView(camera);
        renderer.render();
    }

    public List<Rectangle> getHitboxes() { return hitboxes; }
    public List<Map<String, Object>> getEntities() { return entities; }

    public void dispose() {
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
    }
}
