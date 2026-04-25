package sk.stuba.fiit.physics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.items.Armour;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.*;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollisionManagerTest extends GdxTest {

    // ── Stubs ─────────────────────────────────────────────────────────────────

    static class StubPlayer extends PlayerCharacter {
        int damageTaken = 0;
        StubPlayer() {
            super("Player", 100, 10, 1f, new Vector2D(50, 50), 0);
            enemy = false;
            hitbox.setPosition(50, 50); hitbox.setSize(32, 64);
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
        @Override public void takeDamage(int dmg) { damageTaken += dmg; super.takeDamage(dmg); }
    }

    static class StubEnemy extends EnemyCharacter {
        int damageTaken = 0;
        boolean alive = true;
        StubEnemy(float x, float y) {
            super("Enemy", 100, 10, 1f, new Vector2D(x, y), 100f, 200f);
            this.enemy = true;
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public boolean isAlive() { return alive && super.isAlive(); }
        @Override public void takeDamage(int dmg) { damageTaken += dmg; super.takeDamage(dmg); }
    }

    /** Minimal projectile – no AnimationManager */
    static class StubProjectile extends Projectile {
        StubProjectile(int dmg, Vector2D pos, Vector2D dir, ProjectileOwner owner) {
            super(dmg, 1f, pos, dir);
            setOwner(owner);
            hitbox.setSize(20, 20);
            hitbox.setPosition(pos.getX(), pos.getY());
        }
        @Override public void update(UpdateContext ctx) { move(); }
    }

    static class SingleUseProj extends StubProjectile {
        SingleUseProj(Vector2D pos) { super(10, pos, new Vector2D(1,0), ProjectileOwner.PLAYER); }
        @Override public boolean isSingleUse() { return true; }
    }

    static class FakeLevel extends Level {
        List<EnemyCharacter> enemies    = new ArrayList<>();
        List<Duck>           ducks      = new ArrayList<>();
        List<Projectile>     projectiles= new ArrayList<>();
        List<Item>           items      = new ArrayList<>();
        FakeLevel() { super(1); }
        @Override public List<EnemyCharacter> getEnemies()    { return enemies; }
        @Override public List<Duck>           getDucks()       { return ducks; }
        @Override public List<Projectile>     getProjectiles() { return projectiles; }
        @Override public List<Item>           getItems()       { return items; }
        @Override public void addItem(Item i)                  { items.add(i); }
        @Override public sk.stuba.fiit.world.MapManager getMapManager() { return null; }
    }

    private CollisionManager cm;
    private StubPlayer player;
    private FakeLevel level;

    @BeforeEach void setUp() {
        cm = new CollisionManager();
        player = new StubPlayer();
        level  = new FakeLevel();
    }

    // ── null guards ───────────────────────────────────────────────────────────

    @Test void update_nullPlayer_doesNotThrow() { assertDoesNotThrow(() -> cm.update(level, null)); }
    @Test void update_nullLevel_doesNotThrow()  { assertDoesNotThrow(() -> cm.update(null, player)); }
    @Test void update_bothNull_doesNotThrow()   { assertDoesNotThrow(() -> cm.update(null, null)); }

    // ── nearby items ──────────────────────────────────────────────────────────

    @Test void getNearbyItem_nullWhenNoItems() {
        cm.update(level, player);
        assertNull(cm.getNearbyItem());
    }

    @Test void getNearbyItem_returnsItemWhenOverlapping() {
        Item item = new Armour(10, new Vector2D(50, 50));
        level.items.add(item);
        cm.update(level, player);
        assertSame(item, cm.getNearbyItem());
    }

    @Test void getNearbyItem_nullWhenFarAway() {
        level.items.add(new Armour(10, new Vector2D(999, 999)));
        cm.update(level, player);
        assertNull(cm.getNearbyItem());
    }

    // ── pickup ────────────────────────────────────────────────────────────────

    @Test void pickupNearbyItem_noNearby_doesNotThrow() {
        assertDoesNotThrow(() -> cm.pickupNearbyItem(player, level, new Inventory(10)));
    }

    @Test void pickupNearbyItem_addsToInventory() {
        level.items.add(new Armour(10, new Vector2D(50, 50)));
        Inventory inv = new Inventory(10);
        cm.update(level, player);
        cm.pickupNearbyItem(player, level, inv);
        assertEquals(1, inv.getItems().size());
        assertTrue(level.items.isEmpty());
    }

    @Test void pickupNearbyItem_fullInventory_itemStaysOnGround() {
        level.items.add(new Armour(10, new Vector2D(50, 50)));
        Inventory inv = new Inventory(0);
        cm.update(level, player);
        cm.pickupNearbyItem(player, level, inv);
        assertFalse(level.items.isEmpty());
    }

    // ── player projectile hits enemy ──────────────────────────────────────────

    @Test void playerProjectile_hitsEnemy_dealsDamage() {
        StubEnemy enemy = enemy(50, 50);
        level.enemies.add(enemy);
        level.projectiles.add(new StubProjectile(25, new Vector2D(50,50), new Vector2D(1,0), ProjectileOwner.PLAYER));
        cm.update(level, player);
        assertEquals(25, enemy.damageTaken);
    }

    @Test void playerProjectile_hitsEnemy_deactivates() {
        level.enemies.add(enemy(50, 50));
        StubProjectile p = new StubProjectile(10, new Vector2D(50,50), new Vector2D(1,0), ProjectileOwner.PLAYER);
        level.projectiles.add(p);
        cm.update(level, player);
        assertFalse(p.isActive());
    }

    @Test void playerProjectile_deadEnemy_noDamage() {
        StubEnemy e = enemy(50, 50); e.alive = false;
        level.enemies.add(e);
        level.projectiles.add(new StubProjectile(25, new Vector2D(50,50), new Vector2D(1,0), ProjectileOwner.PLAYER));
        cm.update(level, player);
        assertEquals(0, e.damageTaken);
    }

    @Test void singleUseProjectile_deactivates_evenWithNoTarget() {
        SingleUseProj p = new SingleUseProj(new Vector2D(999, 999));
        level.projectiles.add(p);
        cm.update(level, player);
        assertFalse(p.isActive());
    }

    // ── player projectile hits duck (needs LibGDX for Duck constructor) ───────

    @Test void playerProjectile_hitsDuck_killsDuck() {
        Duck duck = new Duck(new Vector2D(50, 50));
        duck.getHitbox().setPosition(50, 50); duck.getHitbox().setSize(32, 32);
        level.ducks.add(duck);
        StubProjectile p = new StubProjectile(100, new Vector2D(50,50), new Vector2D(1,0), ProjectileOwner.PLAYER);
        level.projectiles.add(p);
        cm.update(level, player);
        assertFalse(duck.isAlive());
        assertFalse(p.isActive());
    }

    @Test void playerProjectile_hitsDuck_dropsItem() {
        Duck duck = new Duck(new Vector2D(50, 50));
        duck.getHitbox().setPosition(50, 50); duck.getHitbox().setSize(32, 32);
        level.ducks.add(duck);
        level.projectiles.add(new StubProjectile(100, new Vector2D(50,50), new Vector2D(1,0), ProjectileOwner.PLAYER));
        cm.update(level, player);
        assertEquals(1, level.items.size());
    }

    // ── enemy projectile hits player ──────────────────────────────────────────

    @Test void enemyProjectile_hitsPlayer_dealsDamage() {
        level.projectiles.add(new StubProjectile(15, new Vector2D(50,50), new Vector2D(-1,0), ProjectileOwner.ENEMY));
        cm.update(level, player);
        assertTrue(player.damageTaken > 0);
    }

    @Test void enemyProjectile_missesPlayer_staysActive() {
        StubProjectile p = new StubProjectile(15, new Vector2D(999,999), new Vector2D(-1,0), ProjectileOwner.ENEMY);
        level.projectiles.add(p);
        cm.update(level, player);
        assertTrue(p.isActive());
    }

    // ── on-hit effects ────────────────────────────────────────────────────────

    @Test void playerProjectile_withDot_appliesDot() {
        StubEnemy e = enemy(50, 50);
        level.enemies.add(e);
        StubProjectile p = new StubProjectile(10, new Vector2D(50,50), new Vector2D(1,0), ProjectileOwner.PLAYER);
        p.setDotEffect(20, 3f);
        level.projectiles.add(p);
        cm.update(level, player);
        int hpAfter = e.getHp();
        e.tickEffects(1f);
        assertTrue(e.getHp() < hpAfter);
    }

    @Test void playerProjectile_withSlow_appliesSlow() {
        StubEnemy e = enemy(50, 50);
        level.enemies.add(e);
        float originalSpeed = e.getSpeed();
        StubProjectile p = new StubProjectile(10, new Vector2D(50,50), new Vector2D(1,0), ProjectileOwner.PLAYER);
        p.setSlowEffect(0.3f, 2f);
        level.projectiles.add(p);
        cm.update(level, player);
        assertTrue(e.getSpeed() < originalSpeed);
    }

    // ── push ─────────────────────────────────────────────────────────────────

    @Test void playerVsEnemy_overlap_playerPushed() {
        StubEnemy e = enemy(50, 50);
        level.enemies.add(e);
        float startX = player.getPosition().getX();
        cm.update(level, player);
        assertNotEquals(startX, player.getPosition().getX(), 0.001f);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private StubEnemy enemy(float x, float y) {
        StubEnemy e = new StubEnemy(x, y);
        e.getHitbox().setPosition(x, y); e.getHitbox().setSize(32, 64);
        return e;
    }
}
