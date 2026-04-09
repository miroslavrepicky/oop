package sk.stuba.fiit.world;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.maps.MapProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MapManager {
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private List<Rectangle> hitboxes;
    private List<Map<String, Object>> entities = new ArrayList<>();

    public MapManager(String mapPath) {
        map = new TmxMapLoader().load(mapPath);
        renderer = new OrthogonalTiledMapRenderer(map);
        hitboxes = new ArrayList<>();
        entities = new ArrayList<>();
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
