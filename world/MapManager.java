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

public class MapManager {
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private List<Rectangle> hitboxes = new ArrayList<>();;
    private List<Map<String, Object>> entities = new ArrayList<>();
    private static final Logger log = GameLogger.get(MapManager.class);

    public MapManager(String mapPath) {
        try {
            map = new TmxMapLoader().load(mapPath);
            log.info("Map loaded: path={}", mapPath);
        } catch (Exception e) {
            log.error("Failed to load map: path={}", mapPath, e);
            throw new AssetLoadException(mapPath, e);
        }
        renderer = new OrthogonalTiledMapRenderer(map);
        loadEntities();
        loadHitboxes();
    }

    private void loadEntities() {
        if (map.getLayers().get("entities") == null) return;

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

    public void render(com.badlogic.gdx.graphics.OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.render();
    }

    public List<Rectangle> getHitboxes() { return hitboxes; }
    public List<Map<String, Object>> getEntities() { return entities; }

    public void dispose() {
        map.dispose();
        renderer.dispose();
    }
}
