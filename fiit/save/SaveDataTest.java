package sk.stuba.fiit.save;

import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SaveDataTest {

    private SaveData buildMinimal(int level) {
        return new SaveData(level,
            List.of(new SaveData.CharacterData("Knight", 100, 50, true, true, 10f, 20f, true)),
            List.of(new SaveData.ItemData("HealingPotion", 2)),
            List.of(new SaveData.EnemyData("EnemyKnight", 100f, 200f, 80, 10)),
            List.of(new SaveData.GroundItemData("Armour", 50f, 60f)),
            List.of(new SaveData.DuckData(70f, 80f, 20))
        );
    }

    @Test
    void saveVersion_equalsConstant() {
        SaveData data = buildMinimal(1);
        assertEquals(SaveData.SAVE_VERSION, data.saveVersion);
    }

    @Test
    void currentLevel_stored() {
        assertEquals(3, buildMinimal(3).currentLevel);
    }

    @Test
    void savedAt_notNullOrEmpty() {
        assertNotNull(buildMinimal(1).savedAt);
        assertFalse(buildMinimal(1).savedAt.isEmpty());
    }

    @Test
    void characters_stored() {
        SaveData data = buildMinimal(1);
        assertEquals(1, data.characters.size());
        SaveData.CharacterData cd = data.characters.get(0);
        assertEquals("Knight", cd.characterType);
        assertEquals(100, cd.hp);
        assertEquals(50, cd.armor);
        assertTrue(cd.isBase);
        assertTrue(cd.isActive);
        assertEquals(10f, cd.x, 0.001f);
        assertEquals(20f, cd.y, 0.001f);
        assertTrue(cd.facingRight);
    }

    @Test
    void inventoryItems_stored() {
        SaveData.ItemData id = buildMinimal(1).inventoryItems.get(0);
        assertEquals("HealingPotion", id.itemType);
        assertEquals(2, id.count);
    }

    @Test
    void enemies_stored() {
        SaveData.EnemyData ed = buildMinimal(1).enemies.get(0);
        assertEquals("EnemyKnight", ed.type);
        assertEquals(100f, ed.x, 0.001f);
        assertEquals(80,   ed.hp);
        assertEquals(10,   ed.armor);
    }

    @Test
    void groundItems_stored() {
        SaveData.GroundItemData gd = buildMinimal(1).groundItems.get(0);
        assertEquals("Armour", gd.type);
        assertEquals(50f, gd.x, 0.001f);
    }

    @Test
    void ducks_stored() {
        SaveData.DuckData dd = buildMinimal(1).ducks.get(0);
        assertEquals(70f, dd.x, 0.001f);
        assertEquals(20,  dd.hp);
    }

    @Test
    void toString_containsLevel() {
        assertTrue(buildMinimal(2).toString().contains("level=2"));
    }

    @Test
    void characterData_toString_containsType() {
        SaveData.CharacterData cd = new SaveData.CharacterData("Wizzard", 50, 0, false, false, 0, 0, true);
        assertTrue(cd.toString().contains("Wizzard"));
    }

    @Test
    void itemData_toString_containsType() {
        SaveData.ItemData id = new SaveData.ItemData("Armour", 3);
        assertTrue(id.toString().contains("Armour"));
        assertTrue(id.toString().contains("3"));
    }

    @Test
    void enemyData_toString_containsType() {
        SaveData.EnemyData ed = new SaveData.EnemyData("DarkKnight", 0, 0, 100, 30);
        assertTrue(ed.toString().contains("DarkKnight"));
    }

    @Test
    void groundItemData_toString_containsType() {
        SaveData.GroundItemData gd = new SaveData.GroundItemData("HealingPotion", 1f, 2f);
        assertTrue(gd.toString().contains("HealingPotion"));
    }

    @Test
    void duckData_toString_containsHp() {
        SaveData.DuckData dd = new SaveData.DuckData(1f, 2f, 15);
        assertTrue(dd.toString().contains("15"));
    }
}
