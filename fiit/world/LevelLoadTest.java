package sk.stuba.fiit.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.items.Armour;
import sk.stuba.fiit.items.HealingPotion;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class LevelLoadTest extends GdxTest {

    @Test
    void load_correctlySpawnsEntitiesAndSetsPlayerPos() {
        // Definujeme správanie pre každý vytvorený AnimationManager
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class,
            (mock, context) -> {
                // Keď sa nepriateľ pýta na veľkosť animácie, vrátime mu dummy vektor namiesto null
                when(mock.getFirstFrameSize("idle")).thenReturn(new Vector2D(32, 32));
            })) {

            TiledMap tMap = new TiledMap();
            tMap.getLayers().add(new MapLayer() {{ setName("hitbox"); }});
            MapLayer entityLayer = new MapLayer() {{ setName("entities"); }};
            entityLayer.getObjects().add(createEntityObj("player", 500, 600));
            entityLayer.getObjects().add(createEntityObj("enemy_knight", 100, 100));
            tMap.getLayers().add(entityLayer);

            MapManager mm = new MapManager(tMap);
            Level level = new Level(1);
            LevelUpdateTest.StubPlayer player = new LevelUpdateTest.StubPlayer();

            level.load(mm, player);

            assertEquals(500f, player.getPosition().getX());
            assertEquals(1, level.getEnemies().size());
        }
    }

    @Test
    void load_coversAllSwitchCases() {
        // Rovnaká oprava aj tu - (mock, context) lambda nastaví defaultné návratové hodnoty
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class,
            (mock, context) -> {
                when(mock.getFirstFrameSize("idle")).thenReturn(new Vector2D(32, 32));
            })) {

            TiledMap tMap = new TiledMap();
            tMap.getLayers().add(new MapLayer() {{ setName("hitbox"); }});
            MapLayer entityLayer = new MapLayer() {{ setName("entities"); }};

            entityLayer.getObjects().add(createEntityObj("enemy_knight", 10, 10));
            entityLayer.getObjects().add(createEntityObj("enemy_archer", 20, 20));
            entityLayer.getObjects().add(createEntityObj("enemy_wizzard", 30, 30));
            entityLayer.getObjects().add(createEntityObj("dark_knight", 40, 40));
            entityLayer.getObjects().add(createEntityObj("duck", 50, 50));
            entityLayer.getObjects().add(createEntityObj("healing_potion", 60, 60));
            entityLayer.getObjects().add(createEntityObj("armour", 70, 70));

            tMap.getLayers().add(entityLayer);
            MapManager mm = new MapManager(tMap);
            Level level = new Level(1);
            LevelUpdateTest.StubPlayer player = new LevelUpdateTest.StubPlayer();

            level.load(mm, player);

            assertEquals(4, level.getEnemies().size());
            assertEquals(1, level.getDucks().size());
            assertEquals(2, level.getItems().size());
        }
    }

    private RectangleMapObject createEntityObj(String type, float x, float y) {
        RectangleMapObject obj = new RectangleMapObject(x, y, 32, 32);
        obj.getProperties().put("type", type);
        obj.getProperties().put("x", x);
        obj.getProperties().put("y", y);
        return obj;
    }
}
