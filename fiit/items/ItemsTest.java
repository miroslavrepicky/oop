package sk.stuba.fiit.items;

import org.junit.jupiter.api.Test;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import static org.junit.jupiter.api.Assertions.*;

class ItemsTest {

    //  Stub player bez LibGDX

    static class StubPlayer extends PlayerCharacter {
        private int hp;
        private final int maxHp;
        private int armor;
        private final int maxArmor;

        StubPlayer(int hp, int maxHp, int armor, int maxArmor) {
            super("SP", hp, 10, 1f, new Vector2D(0,0), maxArmor);
            this.hp = hp; this.maxHp = maxHp;
            this.armor = armor; this.maxArmor = maxArmor;
        }

        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }

        @Override public int getHp()       { return hp; }
        @Override public int getMaxHp()    { return maxHp; }
        @Override public int getArmor()    { return armor; }
        @Override public int getMaxArmor() { return maxArmor; }

        @Override public void takeDamage(int dmg) {
            if (dmg < 0) hp = Math.min(maxHp, hp + (-dmg));
            else         hp = Math.max(0, hp - dmg);
        }
        @Override public void addArmor(int amount) {
            armor = Math.min(maxArmor, armor + amount);
        }
    }

    //  HealingPotion

    @Test void healingPotion_healsCharacter() {
        StubPlayer p = new StubPlayer(50, 100, 0, 0);
        Inventory inv = inventoryWith(new HealingPotion(30, new Vector2D(0,0)));
        inv.getItems().get(0).use(p, null, inv);
        assertEquals(80, p.getHp());
    }

    @Test void healingPotion_removesFromInventory() {
        StubPlayer p = new StubPlayer(50, 100, 0, 0);
        HealingPotion potion = new HealingPotion(30, new Vector2D(0,0));
        Inventory inv = inventoryWith(potion);
        potion.use(p, null, inv);
        assertTrue(inv.getItems().isEmpty());
    }

    @Test void healingPotion_fullHp_doesNotHeal() {
        StubPlayer p = new StubPlayer(100, 100, 0, 0);
        HealingPotion potion = new HealingPotion(30, new Vector2D(0,0));
        Inventory inv = inventoryWith(potion);
        potion.use(p, null, inv);
        assertEquals(1, inv.getItems().size()); // not removed
        assertEquals(100, p.getHp());
    }

    @Test void healingPotion_doesNotExceedMaxHp() {
        StubPlayer p = new StubPlayer(90, 100, 0, 0);
        HealingPotion potion = new HealingPotion(50, new Vector2D(0,0));
        Inventory inv = inventoryWith(potion);
        potion.use(p, null, inv);
        assertEquals(100, p.getHp());
    }

    @Test void healingPotion_costs2Slots() {
        assertEquals(2, new HealingPotion(30, new Vector2D(0,0)).getSlotsRequired());
    }

    @Test void healingPotion_iconPath_notNull() {
        assertNotNull(new HealingPotion(30, new Vector2D(0,0)).getIconPath());
    }

    @Test void healingPotion_hitbox_atPosition() {
        HealingPotion p = new HealingPotion(30, new Vector2D(10f, 20f));
        assertEquals(10f, p.getHitbox().x, 0.001f);
        assertEquals(20f, p.getHitbox().y, 0.001f);
    }

    @Test void healingPotion_getPosition() {
        HealingPotion p = new HealingPotion(30, new Vector2D(5f, 7f));
        assertEquals(5f, p.getPosition().getX(), 0.001f);
    }

    @Test void healingPotion_onPickup_addsToInventory() {
        StubPlayer p = new StubPlayer(100, 100, 0, 0);
        Inventory inv = new Inventory(10);
        assertTrue(new HealingPotion(30, new Vector2D(0,0)).onPickup(p, inv));
        assertEquals(1, inv.getItems().size());
    }

    @Test void healingPotion_onPickup_failsWhenFull() {
        StubPlayer p = new StubPlayer(100, 100, 0, 0);
        assertFalse(new HealingPotion(30, new Vector2D(0,0)).onPickup(p, new Inventory(0)));
    }

    //  Armour

    @Test void armour_increasesArmor() {
        StubPlayer p = new StubPlayer(100, 100, 0, 100);
        Armour armour = new Armour(30, new Vector2D(0,0));
        Inventory inv = inventoryWith(armour);
        armour.use(p, null, inv);
        assertEquals(30, p.getArmor());
    }

    @Test void armour_removesFromInventory() {
        StubPlayer p = new StubPlayer(100, 100, 0, 100);
        Armour armour = new Armour(30, new Vector2D(0,0));
        Inventory inv = inventoryWith(armour);
        armour.use(p, null, inv);
        assertTrue(inv.getItems().isEmpty());
    }

    @Test void armour_costs1Slot() {
        assertEquals(1, new Armour(30, new Vector2D(0,0)).getSlotsRequired());
    }

    @Test void armour_iconPath_notNull() {
        assertNotNull(new Armour(30, new Vector2D(0,0)).getIconPath());
    }

    @Test void armour_onPickup_addsToInventory() {
        StubPlayer p = new StubPlayer(100, 100, 0, 100);
        Inventory inv = new Inventory(10);
        assertTrue(new Armour(30, new Vector2D(0,0)).onPickup(p, inv));
    }

    @Test void armour_onPickup_failsWhenFull() {
        StubPlayer p = new StubPlayer(100, 100, 0, 100);
        assertFalse(new Armour(30, new Vector2D(0,0)).onPickup(p, new Inventory(0)));
    }

    //  EggProjectileSpawner

    @Test void eggSpawner_onPickup_returnsFalse() {
        StubPlayer p = new StubPlayer(100, 100, 0, 0);
        Inventory inv = new Inventory(10);
        assertFalse(new EggProjectileSpawner(new Vector2D(0,0)).onPickup(p, inv));
    }

    @Test void eggSpawner_costs0Slots() {
        assertEquals(0, new EggProjectileSpawner(new Vector2D(0,0)).getSlotsRequired());
    }

    @Test void eggSpawner_iconPath_null() {
        assertNull(new EggProjectileSpawner(new Vector2D(0,0)).getIconPath());
    }

    //  helper

    private Inventory inventoryWith(Item item) {
        Inventory inv = new Inventory(10);
        inv.addItem(item);
        return inv;
    }
}
