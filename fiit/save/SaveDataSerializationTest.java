package sk.stuba.fiit.save;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SaveDataSerializationTest {

    private SaveData buildSample() {
        return new SaveData(1,
            List.of(new SaveData.CharacterData("Knight", 100, 50, true, true, 0, 0, true)),
            List.of(new SaveData.ItemData("HealingPotion", 1)),
            List.of(new SaveData.EnemyData("EnemyKnight", 10, 20, 80, 5)),
            List.of(new SaveData.GroundItemData("Armour", 30, 40)),
            List.of(new SaveData.DuckData(5, 6, 20))
        );
    }

    @Test
    void saveData_isSerializable() throws Exception {
        SaveData original = buildSample();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        SaveData loaded;
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            loaded = (SaveData) ois.readObject();
        }
        assertEquals(original.currentLevel, loaded.currentLevel);
        assertEquals(original.saveVersion, loaded.saveVersion);
        assertEquals(original.characters.size(), loaded.characters.size());
        assertEquals(original.inventoryItems.size(), loaded.inventoryItems.size());
        assertEquals(original.enemies.size(), loaded.enemies.size());
        assertEquals(original.groundItems.size(), loaded.groundItems.size());
        assertEquals(original.ducks.size(), loaded.ducks.size());
    }

    @Test
    void characterData_fieldsPreservedAfterSerialization() throws Exception {
        SaveData.CharacterData cd = new SaveData.CharacterData("Wizzard", 55, 10, false, true, 1.5f, 2.5f, false);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(cd); }
        SaveData.CharacterData loaded;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            loaded = (SaveData.CharacterData) ois.readObject();
        }
        assertEquals("Wizzard", loaded.characterType);
        assertEquals(55, loaded.hp);
        assertEquals(10, loaded.armor);
        assertFalse(loaded.isBase);
        assertTrue(loaded.isActive);
        assertEquals(1.5f, loaded.x, 0.001f);
        assertFalse(loaded.facingRight);
    }

    @Test
    void saveVersion_constant_isPositive() {
        assertTrue(SaveData.SAVE_VERSION > 0);
    }

    @Test
    void lists_areDefensiveCopies_notSameReference() {
        List<SaveData.CharacterData> chars = new java.util.ArrayList<>();
        chars.add(new SaveData.CharacterData("Knight", 100, 0, true, true, 0, 0, true));
        SaveData data = new SaveData(1, chars, List.of(), List.of(), List.of(), List.of());
        chars.clear(); // mutate original
        assertEquals(1, data.characters.size()); // DTO should be unaffected
    }
}
