package sk.stuba.fiit.characters;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.items.EggProjectileSpawner;
import sk.stuba.fiit.items.FriendlyDuck;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.util.Vector2D;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DuckTest extends GdxTest {

    private static final Vector2D ORIGIN = new Vector2D(0, 0);

    /**
     * MockedConstruction initializer that stubs both animation-related queries.
     * Handles Duck's own AnimationManager AND FriendlyDuck's AnimationManager
     * (created inside onKilled()) in a single MockedConstruction scope.
     */
    private MockedConstruction.MockInitializer<AnimationManager> animStub() {
        return (mock, ctx) -> {
            when(mock.getAnimationSize(any())).thenReturn(new Vector2D(32, 32));
            when(mock.getFirstFrameSize(any())).thenReturn(new Vector2D(32, 32));
        };
    }

    // ── Constructor / stats ────────────────────────────────────────────────────

    @Test
    void name_isDuck() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertEquals("Duck", new Duck(ORIGIN).getName());
        }
    }

    @Test
    void hp_is20() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            assertEquals(20, d.getHp());
            assertEquals(20, d.getMaxHp());
        }
    }

    @Test
    void attackPower_is0() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertEquals(0, new Duck(ORIGIN).getAttackPower());
        }
    }

    @Test
    void speed_is1() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertEquals(1.0f, new Duck(ORIGIN).getSpeed(), 0.001f);
        }
    }

    // ── Identity ──────────────────────────────────────────────────────────────

    @Test
    void isAlive_initially() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertTrue(new Duck(ORIGIN).isAlive());
        }
    }

    @Test
    void isAlive_falseAfterLethalDamage() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            d.takeDamage(9999);
            assertFalse(d.isAlive());
        }
    }

    // ── onCollision – no-op ───────────────────────────────────────────────────

    @Test
    void onCollision_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertDoesNotThrow(() -> new Duck(ORIGIN).onCollision("anything"));
        }
    }

    // ── getAnimationManager ───────────────────────────────────────────────────

    @Test
    void getAnimationManager_notNull() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            assertNotNull(new Duck(ORIGIN).getAnimationManager());
        }
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            UpdateContext ctx = new UpdateContext(0.016f, Collections.emptyList(), null, null, null);
            assertDoesNotThrow(() -> d.update(ctx));
        }
    }

    @Test
    void update_multipleFrames_doesNotThrow() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            UpdateContext ctx = new UpdateContext(0.5f, Collections.emptyList(), null, null, null);
            for (int i = 0; i < 20; i++) {
                assertDoesNotThrow(() -> d.update(ctx));
            }
        }
    }

    // ── onKilled ──────────────────────────────────────────────────────────────

    @Test
    void onKilled_returnsNonNullItem() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            Item drop = d.onKilled();
            assertNotNull(drop);
        }
    }

    @Test
    void onKilled_returnsEitherFriendlyDuckOrEggSpawner() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            Item drop = d.onKilled();
            assertTrue(drop instanceof FriendlyDuck || drop instanceof EggProjectileSpawner,
                "Drop must be FriendlyDuck or EggProjectileSpawner");
        }
    }

    @Test
    void onKilled_dropPositionMatchesDuck() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(new Vector2D(100f, 200f));
            Item drop = d.onKilled();
            assertEquals(100f, drop.getPosition().getX(), 0.001f);
            assertEquals(200f, drop.getPosition().getY(), 0.001f);
        }
    }

    @Test
    void onKilled_canBeCalledRepeatedly() {
        // Drop is random – calling multiple times should always return a valid item
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            for (int i = 0; i < 10; i++) {
                Item drop = d.onKilled();
                assertNotNull(drop);
            }
        }
    }

    // ── move ─────────────────────────────────────────────────────────────────

    @Test
    void move_updatesPosition() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            d.move(new Vector2D(10f, 5f));
            assertEquals(10f, d.getPosition().getX(), 0.001f);
            assertEquals(5f,  d.getPosition().getY(), 0.001f);
        }
    }

    @Test
    void move_updatesHitbox() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            d.move(new Vector2D(15f, 0f));
            assertEquals(15f, d.getHitbox().x, 0.001f);
        }
    }

    // ── Damage model ──────────────────────────────────────────────────────────

    @Test
    void takeDamage_reducesHp() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            d.takeDamage(10);
            // Duck has 0 maxArmor, so all damage hits HP
            assertEquals(10, d.getHp());
        }
    }

    @Test
    void restoreStats_setsHp() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Duck d = new Duck(ORIGIN);
            d.takeDamage(15);
            d.restoreStats(20, 0);
            assertEquals(20, d.getHp());
        }
    }
}
