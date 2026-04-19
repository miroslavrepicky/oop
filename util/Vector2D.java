package sk.stuba.fiit.util;

/**
 * Immutable-style helper for storing 2D coordinates of objects and characters.
 *
 * <p>Supports vector addition, scalar scaling, and Euclidean distance computation.
 * Used throughout the engine as the primary position and direction type.
 */
public class Vector2D {
    private float x;
    private float y;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns a new vector that is the sum of this vector and {@code v}.
     *
     * @param v the vector to add
     * @return new Vector2D representing the sum
     */

    public Vector2D add(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    /**
     * Returns a new vector scaled by the given factor.
     *
     * @param factor scalar multiplier
     * @return new Vector2D with components multiplied by {@code factor}
     */
    public Vector2D scale(float factor) {
        return new Vector2D(this.x * factor, this.y * factor);
    }

    /**
     * Computes the Euclidean distance from this vector to {@code v}.
     *
     * @param v the target vector
     * @return distance as a double
     */
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
