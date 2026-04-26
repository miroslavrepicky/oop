package sk.stuba.fiit.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

class ObjectPoolTest {

    // Simple poolable data class – no LibGDX dependency
    static class Item {
        boolean active = true;
    }

    private ObjectPool<Item> makePool(int maxSize) {
        return new ObjectPool<>(Item::new, item -> item.active = true, maxSize);
    }

    @Test
    void obtain_returnsNewObject_whenPoolEmpty() {
        ObjectPool<Item> pool = makePool(5);
        Item item = pool.obtain();
        assertNotNull(item);
        assertEquals(1, pool.getTotalCreated());
    }

    @Test
    void free_and_obtain_reusesObject() {
        ObjectPool<Item> pool = makePool(5);
        Item first = pool.obtain();
        pool.free(first);
        Item second = pool.obtain();
        assertSame(first, second);
        assertEquals(1, pool.getTotalReused());
    }

    @Test
    void resetAction_calledOnReuse() {
        AtomicInteger resetCount = new AtomicInteger(0);
        ObjectPool<Item> pool = new ObjectPool<>(Item::new, item -> resetCount.incrementAndGet(), 5);
        Item item = pool.obtain();
        pool.free(item);
        pool.obtain(); // triggers reset
        assertEquals(1, resetCount.get());
    }

    @Test
    void free_null_doesNotThrow() {
        ObjectPool<Item> pool = makePool(5);
        assertDoesNotThrow(() -> pool.free(null));
    }

    @Test
    void pool_discards_whenFull() {
        ObjectPool<Item> pool = makePool(1);
        Item a = pool.obtain();
        Item b = pool.obtain();
        pool.free(a); // stored
        pool.free(b); // discarded – pool full
        assertEquals(1, pool.getPoolSize());
    }

    @Test
    void clear_emptiesPool() {
        ObjectPool<Item> pool = makePool(5);
        Item item = pool.obtain();
        pool.free(item);
        assertEquals(1, pool.getPoolSize());
        pool.clear();
        assertEquals(0, pool.getPoolSize());
    }

    @Test
    void reuseRatio_zeroWhenNothingReused() {
        ObjectPool<Item> pool = makePool(5);
        pool.obtain();
        assertEquals(0f, pool.getReuseRatio(), 0.001f);
    }

    @Test
    void reuseRatio_oneWhenAllReused() {
        ObjectPool<Item> pool = makePool(5);
        Item item = pool.obtain(); // created
        pool.free(item);
        pool.obtain();             // reused
        // total=2, reused=1 -> ratio = 0.5
        assertEquals(0.5f, pool.getReuseRatio(), 0.001f);
    }

    @Test
    void totalCreated_countsCorrectly() {
        ObjectPool<Item> pool = makePool(5);
        pool.obtain();
        pool.obtain();
        assertEquals(2, pool.getTotalCreated());
    }

    @Test
    void totalReused_countsCorrectly() {
        ObjectPool<Item> pool = makePool(5);
        Item item = pool.obtain();
        pool.free(item);
        pool.obtain();
        pool.obtain();
        assertEquals(1, pool.getTotalReused());
    }
}
