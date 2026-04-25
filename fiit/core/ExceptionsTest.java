package sk.stuba.fiit.core;


import org.junit.jupiter.api.Test;
import sk.stuba.fiit.core.exceptions.*;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    // ── ShadowQuestException ──────────────────────────────────────────────────

    @Test
    void shadowQuestException_message() {
        ShadowQuestException ex = new ShadowQuestException("oops");
        assertEquals("oops", ex.getMessage());
    }

    @Test
    void shadowQuestException_cause() {
        RuntimeException cause = new RuntimeException("root");
        ShadowQuestException ex = new ShadowQuestException("wrap", cause);
        assertSame(cause, ex.getCause());
    }

    // ── AssetLoadException ────────────────────────────────────────────────────

    @Test
    void assetLoadException_storesPath() {
        AssetLoadException ex = new AssetLoadException("atlas/knight.atlas");
        assertEquals("atlas/knight.atlas", ex.getAssetPath());
    }

    @Test
    void assetLoadException_messageContainsPath() {
        AssetLoadException ex = new AssetLoadException("some/path");
        assertTrue(ex.getMessage().contains("some/path"));
    }

    @Test
    void assetLoadException_withCause() {
        Throwable cause = new RuntimeException("io");
        AssetLoadException ex = new AssetLoadException("path", cause);
        assertSame(cause, ex.getCause());
        assertEquals("path", ex.getAssetPath());
    }

    // ── GameStateException ────────────────────────────────────────────────────

    @Test
    void gameStateException_storesContext() {
        GameStateException ex = new GameStateException("bad state", "GameManager.start");
        assertEquals("GameManager.start", ex.getContext());
    }

    @Test
    void gameStateException_messageAndContext() {
        GameStateException ex = new GameStateException("msg", "ctx");
        assertEquals("msg", ex.getMessage());
        assertEquals("ctx", ex.getContext());
    }

    @Test
    void gameStateException_withCause() {
        RuntimeException cause = new RuntimeException();
        GameStateException ex = new GameStateException("msg", "ctx", cause);
        assertSame(cause, ex.getCause());
        assertEquals("ctx", ex.getContext());
    }

    // ── InventoryException ────────────────────────────────────────────────────

    @Test
    void inventoryException_slots() {
        InventoryException ex = new InventoryException("full", 8, 10);
        assertEquals(8, ex.getUsedSlots());
        assertEquals(10, ex.getTotalSlots());
        assertEquals(2, ex.getFreeSlots());
    }

    @Test
    void inventoryException_messageStored() {
        InventoryException ex = new InventoryException("inventory full", 10, 10);
        assertEquals("inventory full", ex.getMessage());
    }

    // ── InvalidAttackException ────────────────────────────────────────────────

    @Test
    void invalidAttackException_storesFields() {
        InvalidAttackException ex = new InvalidAttackException("Knight", "null wrapped");
        assertEquals("Knight", ex.getAttackerName());
        assertEquals("null wrapped", ex.getReason());
    }

    @Test
    void invalidAttackException_messageContainsFields() {
        InvalidAttackException ex = new InvalidAttackException("Wizzard", "bad range");
        assertTrue(ex.getMessage().contains("Wizzard"));
        assertTrue(ex.getMessage().contains("bad range"));
    }

    // ── SaveException ─────────────────────────────────────────────────────────

    @Test
    void saveException_withCause() {
        RuntimeException cause = new RuntimeException("io error");
        SaveException ex = new SaveException("save failed", cause);
        assertEquals("save failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
