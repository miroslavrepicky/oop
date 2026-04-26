package sk.stuba.fiit.projectiles;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;

class EggProjectileTest extends GdxTest {

    // Pomocna metoda na vytvorenie kontextu s konkretnym casom (dt)
    private UpdateContext ctx(float dt) {
        // Vychadzam z tvojej struktury UpdateContext z LevelUpdateTest
        return new UpdateContext(dt, null, null, null, null);
    }

    @Test
    void initialProperties_areCorrect() {
        // Try-with-resources blok garantuje, ze mockovanie skonci po otestovani
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class)) {

            EggProjectile egg = new EggProjectile(new Vector2D(100, 100));

            // Overenie pociatocneho stavu
            assertTrue(egg.isActive());
            assertEquals(EggProjectile.EggState.TICKING, egg.getEggState());
            assertFalse(egg.isDamageDealt());

            // Overenie pociatocnych (BOMB) rozmerov pre render
            assertEquals(32f, egg.getRenderWidth());
            assertEquals(32f, egg.getRenderHeight());
            assertEquals(0f, egg.getRenderOffsetX());
        }
    }

    @Test
    void update_transitionsFromTickingToBlasting() {
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class)) {

            EggProjectile egg = new EggProjectile(new Vector2D(0, 0));

            // Simulujeme cas do tesne pred vybuchom (BOMB_DURATION je 2.5)
            egg.update(ctx(2.4f));
            assertEquals(EggProjectile.EggState.TICKING, egg.getEggState(), "Bomba by mala este tikat");

            // Prekrocime hranicu 2.5s
            egg.update(ctx(0.2f)); // Spolu 2.6s

            assertEquals(EggProjectile.EggState.BLASTING, egg.getEggState(), "Bomba mala prejst do stavu BLASTING");

            // Rozmery pre render by sa mali zmenit pre vybuch
            assertEquals(64f, egg.getRenderWidth());
            assertEquals(-16f, egg.getRenderOffsetX(), "Vybuch ma mat posunuty offset pre centrovanie");
        }
    }

    @Test
    void update_deactivatesAfterBlastingDuration() {
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class)) {

            EggProjectile egg = new EggProjectile(new Vector2D(0, 0));

            // 1. Odpálime bombu (prechod do BLASTING)
            egg.update(ctx(2.5f));
            assertEquals(EggProjectile.EggState.BLASTING, egg.getEggState());
            assertTrue(egg.isActive());

            // 2. Necháme dobehnut animáciu vybuchu (BLAST_DURATION je 0.8)
            egg.update(ctx(0.81f));

            // 3. Po skonceni vybuchu sa musi projektil deaktivovat
            assertFalse(egg.isActive(), "Projektil by mal byt po skonceni vybuchu neaktivny");
        }
    }

    @Test
    void damageFlag_canBeMarkedByCollisionManager() {
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class)) {

            EggProjectile egg = new EggProjectile(new Vector2D(0, 0));

            assertFalse(egg.isDamageDealt());

            // Simulujeme prácu CollisionManagera
            egg.markDamageDealt();

            assertTrue(egg.isDamageDealt(), "Priznak musi ostat nastaveny, aby sa zamedzilo dvojitemu damage");
        }
    }

    @Test
    void aoeProperties_areConfiguredCorrectly() {
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class)) {

            EggProjectile egg = new EggProjectile(new Vector2D(0, 0));

            // Konstanty definovane v EggProjectile
            assertEquals(80f, egg.getAoeRadius());
            assertEquals(30, egg.getDamage());
        }
    }
}
