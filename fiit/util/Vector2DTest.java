package sk.stuba.fiit.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector2DTest {

    @Test
    void constructor_setsComponents() {
        Vector2D v = new Vector2D(3f, 4f);
        assertEquals(3f, v.getX(), 0.001f);
        assertEquals(4f, v.getY(), 0.001f);
    }

    @Test
    void add_returnsCorrectSum() {
        Vector2D result = new Vector2D(1f, 2f).add(new Vector2D(3f, 4f));
        assertEquals(4f, result.getX(), 0.001f);
        assertEquals(6f, result.getY(), 0.001f);
    }

    @Test
    void add_doesNotMutateOriginal() {
        Vector2D a = new Vector2D(1f, 2f);
        a.add(new Vector2D(9f, 9f));
        assertEquals(1f, a.getX(), 0.001f);
    }

    @Test
    void scale_multipliesComponents() {
        Vector2D result = new Vector2D(2f, 3f).scale(2f);
        assertEquals(4f, result.getX(), 0.001f);
        assertEquals(6f, result.getY(), 0.001f);
    }

    @Test
    void scale_byZero_returnsZeroVector() {
        Vector2D result = new Vector2D(5f, 7f).scale(0f);
        assertEquals(0f, result.getX(), 0.001f);
        assertEquals(0f, result.getY(), 0.001f);
    }

    @Test
    void scale_byNegative_flipsSign() {
        Vector2D result = new Vector2D(3f, -4f).scale(-1f);
        assertEquals(-3f, result.getX(), 0.001f);
        assertEquals(4f,  result.getY(), 0.001f);
    }

    @Test
    void distanceTo_samePoint_returnsZero() {
        Vector2D a = new Vector2D(5f, 5f);
        assertEquals(0.0, a.distanceTo(a), 0.001);
    }

    @Test
    void distanceTo_345Triangle() {
        assertEquals(5.0, new Vector2D(0f, 0f).distanceTo(new Vector2D(3f, 4f)), 0.001);
    }

    @Test
    void distanceTo_isSymmetric() {
        Vector2D a = new Vector2D(1f, 2f);
        Vector2D b = new Vector2D(4f, 6f);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0.001);
    }

    @Test
    void setters_changeValues() {
        Vector2D v = new Vector2D(0f, 0f);
        v.setX(10f);
        v.setY(20f);
        assertEquals(10f, v.getX(), 0.001f);
        assertEquals(20f, v.getY(), 0.001f);
    }

    @Test
    void toString_containsXandY() {
        String s = new Vector2D(1f, 2f).toString();
        assertTrue(s.contains("1.0") && s.contains("2.0"));
    }
}
