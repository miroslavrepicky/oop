package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.core.engine.Physicable;
import sk.stuba.fiit.util.Vector2D;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlyingGravityTest {

    static class StubBody implements Physicable {
        Vector2D pos;
        float vy;
        boolean onGround;
        Rectangle hitbox;

        StubBody(float x, float y, float vy) {
            this.pos = new Vector2D(x, y);
            this.vy = vy;
            this.hitbox = new Rectangle(x, y, 32, 32);
        }

        @Override public Vector2D  getPosition()           { return pos; }
        @Override public void      setPosition(Vector2D p) { this.pos = p; }
        @Override public float     getVelocityY()          { return vy; }
        @Override public void      setVelocityY(float v)   { this.vy = v; }
        @Override public Rectangle getHitbox()             { return hitbox; }
        @Override public boolean   isOnGround()            { return onGround; }
        @Override public void      setOnGround(boolean b)  { this.onGround = b; }
        @Override public void      updateHitbox()          { hitbox.setPosition(pos.getX(), pos.getY()); }
    }

    @Test
    void apply_changesVelocityY() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(0, 100, 0);
        g.apply(body, 0.1f, Collections.emptyList());
        // velocity must change (positive or negative random increment)
        assertNotEquals(0f, body.vy, 1f); // allows tiny float rounding
    }

    @Test
    void apply_nullPlatforms_doesNotThrow() {
        FlyingGravity g = new FlyingGravity();
        assertDoesNotThrow(() -> g.apply(new StubBody(0, 50, 0), 0.016f, null));
    }

    @Test
    void apply_velocityClampedBelowMax() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(0, 500, 200f); // above MAX_DIFFERENCE=150
        g.apply(body, 0.1f, Collections.emptyList());
        assertTrue(body.vy <= 150f);
    }

    @Test
    void apply_velocityClampedAboveMin() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(0, 500, -200f); // below -MAX_DIFFERENCE
        g.apply(body, 0.1f, Collections.emptyList());
        assertTrue(body.vy >= -150f);
    }

    @Test
    void apply_withPlatformBelow_setsOnGround() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(0, 100, -50f); // moving down
        Rectangle platform = new Rectangle(0, 90, 100, 20); // platform at y=90-110
        g.apply(body, 0.1f, java.util.List.of(platform));
        // after collision body should be on ground
        assertTrue(body.onGround);
        assertEquals(0f, body.vy, 0.001f);
    }

    @Test
    void apply_updatesHitboxPosition() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(10, 200, 0);
        g.apply(body, 0.1f, Collections.emptyList());
        assertEquals(body.pos.getY(), body.hitbox.y, 0.5f);
    }

    @Test
    void hitCeiling_zerosVelocityAndStopsAscent() {
        FlyingGravity g = new FlyingGravity();
        // Telo letiace nahor (vy = 100)
        StubBody body = new StubBody(0, 185, 100f);
        // Platforma nad nim (strop) na y=200
        Rectangle ceiling = new Rectangle(-10, 200, 100, 10);

        g.apply(body, 0.1f, List.of(ceiling));

        // Mal by sa zastavit o spodok stropu (200 - vyska hitboxu 32)
        assertEquals(0f, body.vy, 0.001f, "Vertikalna rychlost pri naraze do stropu musi byt 0.");
        assertEquals(200 - 32, body.pos.getY(), 0.001f, "Pozicia by mala byt zarovnana pod strop.");
        assertFalse(body.onGround, "Pri naraze do stropu nesmie byt onGround true.");
    }

    @Test
    void randomVelocity_staysWithinBounds() {
        FlyingGravity g = new FlyingGravity();
        StubBody body = new StubBody(0, 500, 0f);

        // Simulujeme viacero krokov, aby sme preverili nahodny pohyb
        for (int i = 0; i < 100; i++) {
            g.apply(body, 1.0f, Collections.emptyList());
            assertTrue(body.vy >= -150f && body.vy <= 150f,
                "Rychlost " + body.vy + " prekrocila limity MAX_DIFFERENCE.");
        }
    }

    @Test
    void horizontalOverlap_isIgnoredByVerticalLogic() {
        FlyingGravity g = new FlyingGravity();
        // Telo je vedla platformy, ale v kode je podmienka overlapY <= overlapX
        // Chceme dosiahnut stav, kedy overlapX je mensi, takze kod s vertikalnou korekciou neprebehne
        StubBody body = new StubBody(95, 100, -10f); // Hitbox x=95 az 127
        Rectangle sideWall = new Rectangle(120, 90, 50, 50); // Prekrytie na X je male (7px)

        float initialY = body.pos.getY();
        g.apply(body, 0.1f, List.of(sideWall));

        // Pretoze overlapX (7) < overlapY (velke), vetva pre vertikalnu korekciu sa preskoci
        assertNotEquals(initialY, body.pos.getY(), "Pozicia Y by sa mala pohnut, kedze bocna kolizia nie je v FlyingGravity implementovana.");
    }

    @Test
    void hitCeiling_stopsMovementAndVelocity() {
        FlyingGravity g = new FlyingGravity();
        // Telo leti nahor (vy = 100)
        StubBody body = new StubBody(0, 190, 100f);
        // Strop je na y=200, hrubka 10 (spodok je na 200)
        Rectangle ceiling = new Rectangle(-10, 200, 100, 10);

        g.apply(body, 0.1f, List.of(ceiling));

        // Po naraze do stropu (vy > 0) by mal byt posunuty pod neho
        assertEquals(0f, body.vy, 0.001f, "Rychlost nahor by sa mala vynulovat.");
        assertEquals(200 - body.getHitbox().height, body.pos.getY(), 0.001f);
    }

    @Test
    void horizontalCollision_isIgnored() {
        FlyingGravity g = new FlyingGravity();
        // Telo je vedla steny, nie pod/nad nou
        StubBody body = new StubBody(100, 100, 0f);
        // Stena z boku (velky overlap na Y, maly na X)
        Rectangle wall = new Rectangle(130, 80, 20, 100);

        float startY = body.pos.getY();
        g.apply(body, 0.1f, List.of(wall));

        // FlyingGravity riesi len vertikalne kolizie (overlapY <= overlapX)
        // Ak je to bocny náraz, Y pozicia by sa nemala korigovat
        assertFalse(body.onGround);
    }

    @Test
    void velocityLimits_areEnforced() {
        FlyingGravity g = new FlyingGravity();
        // Extremne vysoká rychlost
        StubBody body = new StubBody(0, 500, 1000f);

        g.apply(body, 0.1f, Collections.emptyList());

        // Mala by byt orezaná (clamp) na MAX_DIFFERENCE (150)
        assertTrue(body.vy <= 150f, "Rychlost musi byt orezaná na 150.");
    }

}
