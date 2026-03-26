package sk.stuba.fiit.util;

/**
 * Helper class for storing coordinates of objects and characters.
 * <p>
 *  Supports adding, scaling and computing distance to another 2D vector.
 * <p>
 */

public class Vector2D {
    private float x;
    private float y;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D add(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    public Vector2D scale(float factor) {
        return new Vector2D(this.x * factor, this.y * factor);
    }

    public double distanceTo(Vector2D v) {
        float dx = this.x - v.x;
        float dy = this.y - v.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
}
