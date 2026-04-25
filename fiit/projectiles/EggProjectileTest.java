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

    // Pomocná metóda na vytvorenie kontextu s konkrétnym časom (dt)
    private UpdateContext ctx(float dt) {
        // Vychádzam z tvojej štruktúry UpdateContext z LevelUpdateTest
        return new UpdateContext(dt, null, null, null, null);
    }

    @Test
    void initialProperties_areCorrect() {
        // Try-with-resources blok garantuje, že mockovanie skončí po otestovaní
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class)) {

            EggProjectile egg = new EggProjectile(new Vector2D(100, 100));

            // Overenie počiatočného stavu
            assertTrue(egg.isActive());
            assertEquals(EggProjectile.EggState.TICKING, egg.getEggState());
            assertFalse(egg.isDamageDealt());

            // Overenie počiatočných (BOMB) rozmerov pre render
            assertEquals(32f, egg.getRenderWidth());
            assertEquals(32f, egg.getRenderHeight());
            assertEquals(0f, egg.getRenderOffsetX());
        }
    }

    @Test
    void update_transitionsFromTickingToBlasting() {
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class)) {

            EggProjectile egg = new EggProjectile(new Vector2D(0, 0));

            // Simulujeme čas do tesne pred výbuchom (BOMB_DURATION je 2.5)
            egg.update(ctx(2.4f));
            assertEquals(EggProjectile.EggState.TICKING, egg.getEggState(), "Bomba by mala ešte tikať");

            // Prekročíme hranicu 2.5s
            egg.update(ctx(0.2f)); // Spolu 2.6s

            assertEquals(EggProjectile.EggState.BLASTING, egg.getEggState(), "Bomba mala prejsť do stavu BLASTING");

            // Rozmery pre render by sa mali zmeniť pre výbuch
            assertEquals(64f, egg.getRenderWidth());
            assertEquals(-16f, egg.getRenderOffsetX(), "Výbuch má mať posunutý offset pre centrovanie");
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

            // 2. Necháme dobehnúť animáciu výbuchu (BLAST_DURATION je 0.8)
            egg.update(ctx(0.81f));

            // 3. Po skončení výbuchu sa musí projektil deaktivovať
            assertFalse(egg.isActive(), "Projektil by mal byť po skončení výbuchu neaktívny");
        }
    }

    @Test
    void damageFlag_canBeMarkedByCollisionManager() {
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class)) {

            EggProjectile egg = new EggProjectile(new Vector2D(0, 0));

            assertFalse(egg.isDamageDealt());

            // Simulujeme prácu CollisionManagera
            egg.markDamageDealt();

            assertTrue(egg.isDamageDealt(), "Príznak musí ostať nastavený, aby sa zamedzilo dvojitému damage");
        }
    }

    @Test
    void aoeProperties_areConfiguredCorrectly() {
        try (MockedConstruction<AnimationManager> mocked = mockConstruction(AnimationManager.class)) {

            EggProjectile egg = new EggProjectile(new Vector2D(0, 0));

            // Konštanty definované v EggProjectile
            assertEquals(80f, egg.getAoeRadius());
            assertEquals(30, egg.getDamage());
        }
    }
}
