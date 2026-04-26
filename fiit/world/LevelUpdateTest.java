package sk.stuba.fiit.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.items.Armour;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.*;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Level update logic.
 * Does NOT test Duck, EggProjectile, or any class that loads texture atlases,
 * because atlas files are not available in the unit-test classpath.
 */
class LevelUpdateTest {

    //  Stubs

    static class StubPlayer extends PlayerCharacter {
        StubPlayer() { super("P", 100, 10, 1f, new Vector2D(0, 0), 0); enemy = false; }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
    }

    static class StubEnemy extends EnemyCharacter {
        boolean deathDone = false;
        StubEnemy() { super("E", 100, 10, 1f, new Vector2D(0, 0), 100f, 200f); }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public boolean isDeathAnimationDone() { return deathDone; }
    }

    static class StubProjectile extends Projectile {
        StubProjectile() { super(10, 1f, new Vector2D(0, 0), new Vector2D(1, 0)); }
        @Override public void update(UpdateContext ctx) {}
    }

    static class TestLevel extends Level {
        TestLevel() { super(1); }
        @Override public MapManager getMapManager() { return null; }
    }

    private TestLevel level;

    private UpdateContext ctx(float dt) {
        return new UpdateContext(dt, null, level, new StubPlayer(), null);
    }

    @BeforeEach
    void setUp() { level = new TestLevel(); }

    //  Projectiles

    @Test
    void addProjectile_appearsInList() {
        StubProjectile p = new StubProjectile();
        level.addProjectile(p);
        assertTrue(level.getProjectiles().contains(p));
    }

    @Test
    void inactiveProjectile_removedOnUpdate() {
        StubProjectile p = new StubProjectile();
        level.addProjectile(p);
        p.setActive(false);
        level.update(ctx(0.016f));
        assertFalse(level.getProjectiles().contains(p));
    }

    @Test
    void activeProjectile_remainsInList() {
        StubProjectile p = new StubProjectile();
        level.addProjectile(p);
        level.update(ctx(0.016f));
        assertTrue(level.getProjectiles().contains(p));
    }

    @Test
    void multipleProjectiles_inactiveRemovedActiveKept() {
        StubProjectile active   = new StubProjectile();
        StubProjectile inactive = new StubProjectile();
        level.addProjectile(active);
        level.addProjectile(inactive);
        inactive.setActive(false);
        level.update(ctx(0.016f));
        assertTrue(level.getProjectiles().contains(active));
        assertFalse(level.getProjectiles().contains(inactive));
    }

    //  Enemies

    @Test
    void deadEnemy_animDone_removedOnUpdate() {
        StubEnemy e = new StubEnemy();
        e.takeDamage(999); e.deathDone = true;
        level.spawnEnemy(e);
        level.update(ctx(0.016f));
        assertFalse(level.getEnemies().contains(e));
    }

    @Test
    void deadEnemy_animNotDone_remainsInList() {
        StubEnemy e = new StubEnemy();
        e.takeDamage(999); e.deathDone = false;
        level.spawnEnemy(e);
        level.update(ctx(0.016f));
        assertTrue(level.getEnemies().contains(e));
    }

    @Test
    void allEnemiesDead_levelCompleted() {
        StubEnemy e = new StubEnemy();
        e.takeDamage(999);
        level.spawnEnemy(e);
        level.update(ctx(0.016f));
        assertTrue(level.isCompleted());
    }

    @Test
    void noEnemies_notCompleted() {
        level.update(ctx(0.016f));
        assertFalse(level.isCompleted());
    }

    @Test
    void aliveEnemy_notCompleted() {
        level.spawnEnemy(new StubEnemy());
        level.update(ctx(0.016f));
        assertFalse(level.isCompleted());
    }

    @Test
    void multipleEnemies_oneAlive_notCompleted() {
        StubEnemy dead  = new StubEnemy(); dead.takeDamage(999); dead.deathDone = true;
        StubEnemy alive = new StubEnemy();
        level.spawnEnemy(dead);
        level.spawnEnemy(alive);
        level.update(ctx(0.016f));
        assertFalse(level.isCompleted());
    }

    @Test
    void completedFlag_staysTrue_afterSecondUpdate() {
        StubEnemy e = new StubEnemy();
        e.takeDamage(999);
        level.spawnEnemy(e);
        level.update(ctx(0.016f));
        level.update(ctx(0.016f));
        assertTrue(level.isCompleted());
    }

    //  Items

    @Test
    void addItem_appearsInList() {
        Item item = new Armour(10, new Vector2D(0, 0));
        level.addItem(item);
        assertTrue(level.getItems().contains(item));
    }

    @Test
    void regularItem_remainsAfterUpdate() {
        Item item = new Armour(10, new Vector2D(0, 0));
        level.addItem(item);
        level.update(ctx(0.016f));
        assertTrue(level.getItems().contains(item));
    }

    //  Level metadata

    @Test
    void getLevelNumber_returnsConstructorValue() {
        assertEquals(1, level.getLevelNumber());
    }

    @Test
    void isCompleted_falseInitially() {
        assertFalse(level.isCompleted());
    }

    @Test
    void getEnemies_emptyInitially() {
        assertTrue(level.getEnemies().isEmpty());
    }

    @Test
    void getItems_emptyInitially() {
        assertTrue(level.getItems().isEmpty());
    }

    @Test
    void getProjectiles_emptyInitially() {
        assertTrue(level.getProjectiles().isEmpty());
    }

    @Test
    void getDucks_emptyInitially() {
        assertTrue(level.getDucks().isEmpty());
    }

    @Test
    void spawnEnemy_appearsInList() {
        StubEnemy e = new StubEnemy();
        level.spawnEnemy(e);
        assertTrue(level.getEnemies().contains(e));
    }
}
