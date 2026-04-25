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
        // Príprava mapy v pamäti
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
        assertEquals(1, manager.getHitboxes().size(), "Mal by sa načítať jeden hitbox");
        assertEquals(50f, manager.getHitboxes().get(0).width);

        assertEquals(1, manager.getEntities().size(), "Mala by sa načítať jedna entita");
        assertEquals("PLAYER", manager.getEntities().get(0).get("type"));
    }

    @Test
    void mapManager_missingHitboxLayer_throwsGameStateException() {
        TiledMap map = new TiledMap();
        // Pridáme len entities vrstvu, hitbox chýba
        MapLayer entityLayer = new MapLayer();
        entityLayer.setName("entities");
        map.getLayers().add(entityLayer);

        // Overenie: MapManager.loadHitboxes vyhadzuje GameStateException
        assertThrows(GameStateException.class, () -> new MapManager(map));
    }

    @Test
    void mapManager_invalidPath_throwsAssetLoadException() {
        // Testujeme ten statický factory konštruktor
        // Cesta "non_existent.tmx" by mala zlyhať v TmxMapLoader
        AssetLoadException ex = assertThrows(AssetLoadException.class, () -> {
            new MapManager("non_existent.tmx");
        });

        assertTrue(ex.getMessage().contains("non_existent.tmx"));
    }
    @Test
    void mapManager_loadsEntitiesCorrectly() {
        TiledMap map = new TiledMap();

        // Pridáme povinnú hitbox vrstvu, aby konštruktor prešiel
        MapLayer hitboxLayer = new MapLayer();
        hitboxLayer.setName("hitbox");
        map.getLayers().add(hitboxLayer);

        // Pridáme povinnú entities vrstvu
        MapLayer entityLayer = new MapLayer();
        entityLayer.setName("entities");

        // Vytvoríme testovací objekt (napr. spawn bod hráča)
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

        // Pridáme hitbox (ok), ale entities nepridáme
        MapLayer hitboxLayer = new MapLayer();
        hitboxLayer.setName("hitbox");
        map.getLayers().add(hitboxLayer);

        // Overenie, že vyhodí GameStateException
        GameStateException ex = assertThrows(GameStateException.class, () -> new MapManager(map));

        // Overíme aj správu, aby sme vedeli, že to padlo na entities a nie na hitboxe
        assertTrue(ex.getMessage().contains("entities"));
    }
}
