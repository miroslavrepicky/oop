package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.characters.Character;
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

/**
 * Tests for CollisionManager.
 * All stubs avoid loading texture atlases (no AnimationManager in constructors).
 * Duck is replaced with StubDuck that bypasses atlas loading.
 */
class CollisionManagerTest {

    //  Stubs

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

    /**
     * Duck stub that bypasses the atlas-loading constructor.
     * We extend Character directly since Duck extends Character.
     */
    static class StubDuck extends Character {
        boolean alive = true;
        int damageTaken = 0;
        Item dropItem;

        StubDuck(float x, float y) {
            super("Duck", 20, 0, 1f, new Vector2D(x, y));
            hitbox.setPosition(x, y);
            hitbox.setSize(32, 32);
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
        @Override public void onCollision(Object o) {}
        @Override public boolean isAlive() { return alive && super.isAlive(); }
        @Override public void takeDamage(int dmg) { damageTaken += dmg; super.takeDamage(dmg); }
        public Item onKilled() { return dropItem; }
    }

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
        List<EnemyCharacter> enemies     = new ArrayList<>();
        List<Character>      ducks       = new ArrayList<>(); // holds StubDuck
        List<Projectile>     projectiles = new ArrayList<>();
        List<Item>           items       = new ArrayList<>();

        FakeLevel() { super(1); }

        @Override public List<EnemyCharacter> getEnemies()    { return enemies; }
        @Override public List<Projectile>     getProjectiles() { return projectiles; }
        @Override public List<Item>           getItems()       { return items; }
        @Override public void addItem(Item i)                  { items.add(i); }
        @Override public sk.stuba.fiit.world.MapManager getMapManager() { return null; }

        // We need to expose StubDucks to CollisionManager.
        // CollisionManager calls level.getDucks() which returns List<Duck>.
        // We can't put StubDuck there since it doesn't extend Duck.
        // Solution: override getDucks() to return empty and test duck logic separately.
        @Override public List<Duck> getDucks() { return new ArrayList<>(); }

        // Expose stub ducks for manual duck-collision tests
        List<StubDuck> stubDucks = new ArrayList<>();
    }

    private CollisionManager cm;
    private StubPlayer player;
    private FakeLevel level;

    @BeforeEach void setUp() {
        cm     = new CollisionManager();
        player = new StubPlayer();
        level  = new FakeLevel();
    }

    //  Null guards

    @Test void update_nullPlayer_doesNotThrow() { assertDoesNotThrow(() -> cm.update(level, null)); }
    @Test void update_nullLevel_doesNotThrow()  { assertDoesNotThrow(() -> cm.update(null, player)); }
    @Test void update_bothNull_doesNotThrow()   { assertDoesNotThrow(() -> cm.update(null, null)); }

    //  Nearby items

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

    //  Pickup

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

    //  Player projectile hits enemy

    @Test void playerProjectile_hitsEnemy_dealsDamage() {
        StubEnemy e = enemy(50, 50);
        level.enemies.add(e);
        level.projectiles.add(proj(25, 50, 50, ProjectileOwner.PLAYER));
        cm.update(level, player);
        assertEquals(25, e.damageTaken);
    }

    @Test void playerProjectile_hitsEnemy_deactivates() {
        level.enemies.add(enemy(50, 50));
        StubProjectile p = proj(10, 50, 50, ProjectileOwner.PLAYER);
        level.projectiles.add(p);
        cm.update(level, player);
        assertFalse(p.isActive());
    }

    @Test void playerProjectile_deadEnemy_noDamage() {
        StubEnemy e = enemy(50, 50); e.alive = false;
        level.enemies.add(e);
        level.projectiles.add(proj(25, 50, 50, ProjectileOwner.PLAYER));
        cm.update(level, player);
        assertEquals(0, e.damageTaken);
    }

    @Test void singleUseProjectile_deactivates_evenWithNoTarget() {
        SingleUseProj p = new SingleUseProj(new Vector2D(999, 999));
        level.projectiles.add(p);
        cm.update(level, player);
        assertFalse(p.isActive());
    }

    @Test void playerProjectile_enemyProjectile_notProcessedAsPlayer() {
        StubEnemy e = enemy(50, 50);
        level.enemies.add(e);
        level.projectiles.add(proj(25, 50, 50, ProjectileOwner.ENEMY));
        cm.update(level, player);
        assertEquals(0, e.damageTaken); // enemy proj doesn't hit enemy
    }

    //  On-hit effects

    @Test void playerProjectile_withDot_appliesDot() {
        StubEnemy e = enemy(50, 50);
        level.enemies.add(e);
        StubProjectile p = proj(10, 50, 50, ProjectileOwner.PLAYER);
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
        StubProjectile p = proj(10, 50, 50, ProjectileOwner.PLAYER);
        p.setSlowEffect(0.3f, 2f);
        level.projectiles.add(p);
        cm.update(level, player);
        assertTrue(e.getSpeed() < originalSpeed);
    }

    //  Enemy projectile hits player

    @Test void enemyProjectile_hitsPlayer_dealsDamage() {
        level.projectiles.add(proj(15, 50, 50, ProjectileOwner.ENEMY));
        cm.update(level, player);
        assertTrue(player.damageTaken > 0);
    }

    @Test void enemyProjectile_missesPlayer_staysActive() {
        StubProjectile p = proj(15, 999, 999, ProjectileOwner.ENEMY);
        level.projectiles.add(p);
        cm.update(level, player);
        assertTrue(p.isActive());
    }

    //  Player vs enemy push

    @Test void playerVsEnemy_overlap_playerPushed() {
        StubEnemy e = enemy(50, 50);
        level.enemies.add(e);
        float startX = player.getPosition().getX();
        cm.update(level, player);
        assertNotEquals(startX, player.getPosition().getX(), 0.001f);
    }

    @Test void playerVsDeadEnemy_noPush() {
        StubEnemy e = enemy(50, 50); e.alive = false;
        level.enemies.add(e);
        float startX = player.getPosition().getX();
        cm.update(level, player);
        assertEquals(startX, player.getPosition().getX(), 0.001f);
    }

    //  Multiple projectiles

    @Test void multiplePlayerProjectiles_allProcessed() {
        StubEnemy e1 = enemy(50, 50);
        StubEnemy e2 = enemy(50, 50);
        level.enemies.add(e1);
        level.enemies.add(e2);
        // One projectile hits first alive enemy
        level.projectiles.add(proj(10, 50, 50, ProjectileOwner.PLAYER));
        cm.update(level, player);
        // At least one enemy took damage
        assertTrue(e1.damageTaken > 0 || e2.damageTaken > 0);
    }

    @Test void inactiveProjectile_notProcessed() {
        StubEnemy e = enemy(50, 50);
        level.enemies.add(e);
        StubProjectile p = proj(99, 50, 50, ProjectileOwner.PLAYER);
        p.setActive(false);
        level.projectiles.add(p);
        cm.update(level, player);
        assertEquals(0, e.damageTaken);
    }

    @Test
    void nearbyItem_isDetected_whenPlayerClose() {
        // Vytvorime item a polozime ho k hracovi (hrac je na 50, 50)
        Item mockItem = Mockito.mock(Item.class);
        Rectangle itemHitbox = new Rectangle(55, 55, 20, 20);
        Mockito.when(mockItem.getHitbox()).thenReturn(itemHitbox);
        level.items.add(mockItem);

        cm.update(level, player);

        assertEquals(mockItem, cm.getNearbyItem(), "Manazer by mal najst predmet v blizkosti hraca.");
    }

    @Test
    void projectile_hitsWall_becomesInactive() {
        // Nastavime stenu v mape
        sk.stuba.fiit.world.MapManager mockMap = Mockito.mock(sk.stuba.fiit.world.MapManager.class);
        Rectangle wall = new Rectangle(200, 200, 100, 100);
        Mockito.when(mockMap.getHitboxes()).thenReturn(List.of(wall));

        // Musime zabezpecit, aby level vracal nas mock mapy
        level = new FakeLevel() {
            @Override public sk.stuba.fiit.world.MapManager getMapManager() { return mockMap; }
        };

        // Projektil letiaci do steny
        StubProjectile p = proj(10, 210, 210, ProjectileOwner.PLAYER);
        level.projectiles.add(p);

        cm.update(level, player);

        assertFalse(p.isActive(), "Projektil by mal po náraze do steny zaniknut.");
    }

    @Test
    void eggProjectile_inBlastingState_dealsDamageOnce() {
        // 1. Priprava nepriatela
        StubEnemy enemy = new StubEnemy(100, 100);
        // Nastavime hitbox manuálne, aby sme mali istotu, ze sa prekryva
        enemy.getHitbox().setPosition(100, 100);
        enemy.getHitbox().setSize(32, 64);
        level.getEnemies().add(enemy);

        // 2. Priprava Mock vajicka
        EggProjectile eggMock = Mockito.mock(EggProjectile.class);
        Vector2D eggPos = new Vector2D(100, 100); // Pozicia vybuchu

        // Definujeme správanie mocku (Stubbing)
        Mockito.when(eggMock.isActive()).thenReturn(true);
        Mockito.when(eggMock.getEggState()).thenReturn(EggProjectile.EggState.BLASTING);
        Mockito.when(eggMock.isDamageDealt()).thenReturn(false);
        Mockito.when(eggMock.getPosition()).thenReturn(eggPos); // FIX: Toto riesi NPE
        Mockito.when(eggMock.getAoeRadius()).thenReturn(100f);
        Mockito.when(eggMock.getDamage()).thenReturn(50);

        // Pridáme vajicko do zoznamu v leveli
        level.getProjectiles().add(eggMock);

        // 3. Vykonanie testovanej logiky
        cm.update(level, player);

        // 4. Overenie vysledkov
        assertTrue(enemy.damageTaken > 0, "Nepriatel mal dostat damage z AOE vybuchu");

        // Overime, ze CollisionManager zavolal metodu na oznacenie, ze damage bol udeleny
        Mockito.verify(eggMock).markDamageDealt();
    }

    //  Helpers

    private StubEnemy enemy(float x, float y) {
        StubEnemy e = new StubEnemy(x, y);
        e.getHitbox().setPosition(x, y);
        e.getHitbox().setSize(32, 64);
        return e;
    }

    private StubProjectile proj(int dmg, float x, float y, ProjectileOwner owner) {
        return new StubProjectile(dmg, new Vector2D(x, y), new Vector2D(1, 0), owner);
    }
}
