package sk.stuba.fiit.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.HeadlessGdxTest;
import sk.stuba.fiit.core.exceptions.AssetLoadException;
import sk.stuba.fiit.core.exceptions.GameStateException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapManagerTest extends HeadlessGdxTest {

    @Test
    void mapManager_loadsEntitiesAndHitboxesCorrectly() {
        // Priprava mapy v pamati
        TiledMap map = new TiledMap();

        // 1. Vrstva pre hitboxy
        MapLayer hitboxLayer = new MapLayer();
        hitboxLayer.setName("hitbox");
        hitboxLayer.getObjects().add(new RectangleMapObject(10, 10, 50, 50));
        map.getLayers().add(hitboxLayer);

        // 2. Vrstva pre entity
        MapLayer entityLayer = new MapLayer();
        entityLayer.setName("entities");
        var playerSpawn = new RectangleMapObject(100, 200, 32, 32);
        playerSpawn.getProperties().put("type", "PLAYER");
        playerSpawn.getProperties().put("x", 100f);
        playerSpawn.getProperties().put("y", 200f);
        entityLayer.getObjects().add(playerSpawn);
        map.getLayers().add(entityLayer);

        // Vykonanie
        MapManager manager = new MapManager(map);

        // Overenie
        assertEquals(1, manager.getHitboxes().size(), "Mal by sa nacitat jeden hitbox");
        assertEquals(50f, manager.getHitboxes().get(0).width);

        assertEquals(1, manager.getEntities().size(), "Mala by sa nacitat jedna entita");
        assertEquals("PLAYER", manager.getEntities().get(0).get("type"));
    }

    @Test
    void mapManager_missingHitboxLayer_throwsGameStateException() {
        TiledMap map = new TiledMap();
        // Pridame len entities vrstvu, hitbox chyba
        MapLayer entityLayer = new MapLayer();
        entityLayer.setName("entities");
        map.getLayers().add(entityLayer);

        // Overenie: MapManager.loadHitboxes vyhadzuje GameStateException
        assertThrows(GameStateException.class, () -> new MapManager(map));
    }

    @Test
    void mapManager_invalidPath_throwsAssetLoadException() {
        // Testujeme ten staticky factory konstruktor
        // Cesta "non_existent.tmx" by mala zlyhat v TmxMapLoader
        AssetLoadException ex = assertThrows(AssetLoadException.class, () -> {
            new MapManager("non_existent.tmx");
        });

        assertTrue(ex.getMessage().contains("non_existent.tmx"));
    }
    @Test
    void mapManager_loadsEntitiesCorrectly() {
        TiledMap map = new TiledMap();

        // Pridame povinnu hitbox vrstvu, aby konstruktor presiel
        MapLayer hitboxLayer = new MapLayer();
        hitboxLayer.setName("hitbox");
        map.getLayers().add(hitboxLayer);

        // Pridame povinnu entities vrstvu
        MapLayer entityLayer = new MapLayer();
        entityLayer.setName("entities");

        // Vytvorime testovaci objekt (napr. spawn bod hraca)
        RectangleMapObject playerSpawn = new RectangleMapObject(100, 200, 32, 32);
        playerSpawn.getProperties().put("type", "PLAYER");
        playerSpawn.getProperties().put("x", 100f);
        playerSpawn.getProperties().put("y", 200f);
        entityLayer.getObjects().add(playerSpawn);

        map.getLayers().add(entityLayer);

        // Vykonanie
        MapManager manager = new MapManager(map);

        // Overenie
        assertEquals(1, manager.getEntities().size());
        Map<String, Object> extractedEntity = manager.getEntities().get(0);
        assertEquals("PLAYER", extractedEntity.get("type"));
        assertEquals(100f, extractedEntity.get("x"));
        assertEquals(200f, extractedEntity.get("y"));
    }

    @Test
    void mapManager_missingEntitiesLayer_throwsGameStateException() {
        TiledMap map = new TiledMap();

        // Pridame hitbox (ok), ale entities nepridame
        MapLayer hitboxLayer = new MapLayer();
        hitboxLayer.setName("hitbox");
        map.getLayers().add(hitboxLayer);

        // Overenie, ze vyhodi GameStateException
        GameStateException ex = assertThrows(GameStateException.class, () -> new MapManager(map));

        // Overime aj spravu, aby sme vedeli, ze to padlo na entities a nie na hitboxe
        assertTrue(ex.getMessage().contains("entities"));
    }
}
