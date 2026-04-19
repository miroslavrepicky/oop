package sk.stuba.fiit.core.states;

/**
 * Contract for each game state in the State pattern.
 *
 * <p>Motivation: the logic for PLAYING, PAUSED, GAME_OVER_DELAY was spread
 * across {@code if/switch} blocks inside {@code GameScreen.render()} and
 * {@code GameManager.update()}. Each state now encapsulates its own logic
 * and knows which state to transition to next.
 *
 * <p>Flow:
 * <pre>
 *   GameScreen holds currentState : IGameState
 *   each frame: currentState.update(dt) → currentState.render(dt)
 *   if currentState.next() != null → transition to new state
 * </pre>
 */
public interface IGameState {

    /**
     * Game logic for this state (movement, AI, collisions...).
     *
     * @param deltaTime time elapsed since the last frame in seconds
     */
    void update(float deltaTime);

    /**
     * State-specific rendering (e.g. overlay, fade, pause tint...).
     * Base level rendering is handled by {@code GameRenderer}; the state
     * may augment it (pause = darkening, game-over = red tint...).
     *
     * @param deltaTime time elapsed since the last frame in seconds
     */
    void render(float deltaTime);

    /**
     * Returns the next state to transition to, or {@code null} to remain in
     * the current state. Checked by {@code GameScreen} after every {@code update()}.
     *
     * @return the next {@link IGameState}, or {@code null}
     */
    IGameState next();
}
