package sk.stuba.fiit.core.states;

/**
 * Contract for each node in the game's State-pattern machine.
 *
 * <p>Motivation: the logic for PLAYING, PAUSED, and GAME_OVER_DELAY was previously
 * spread across {@code if/switch} blocks inside {@code GameScreen.render()} and
 * {@code GameManager.update()}. Each state now encapsulates its own update and
 * render logic and declares its successor.
 *
 * <h2>Frame loop</h2>
 * <pre>
 *   GameScreen holds currentState : IGameState
 *   each frame:
 *     currentState.update(dt)
 *     currentState.render(dt)
 *     if currentState.next() != null -> handleTransition(next)
 * </pre>
 */
public interface IGameState {

    /**
     * Advances game logic for one frame (movement, AI, collision detection, …).
     *
     * @param deltaTime elapsed time since the last frame, in seconds
     */
    void update(float deltaTime);

    /**
     * Performs state-specific rendering (overlays, tints, etc.).
     * Base scene rendering is handled by {@code GameRenderer}; states may augment it.
     *
     * @param deltaTime elapsed time since the last frame, in seconds
     */
    void render(float deltaTime);

    /**
     * Returns the next state to transition to, or {@code null} to remain in the
     * current state. Checked by {@code GameScreen} after every {@code update()}.
     *
     * @return the successor {@link IGameState}, or {@code null}
     */
    IGameState next();
}
