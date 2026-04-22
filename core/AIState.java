package sk.stuba.fiit.core;

/**
 * Represents the possible behavioural states of an AI-controlled enemy.
 *
 * <p>State transitions are managed by {@link AIController}:
 * <ul>
 *   <li>{@link #PATROL} – enemy walks back and forth along a defined route.</li>
 *   <li>{@link #CHASE}  – enemy detected the player and is moving towards them.</li>
 *   <li>{@link #ATTACK} – enemy is within attack range and is executing an attack.</li>
 * </ul>
 */
public enum AIState {
    /** Enemy patrols between two waypoints. */
    PATROL,
    /** Enemy is actively chasing the player. */
    CHASE,
    /** Enemy is within attack range and performing an attack. */
    ATTACK
}
