package sk.stuba.fiit.core.states;

/**
 * Terminal state entered when the player completes the final level.
 *
 * <p>Both lifecycle methods are no-ops: {@code GameScreen} detects this state type
 * in {@code handleTransition()} and immediately switches to {@code WinScreen}.
 */
public class WinState implements IGameState {

    /**
     * No-op – the game has been won; no further logic runs in this state.
     */
    @Override public void  update(float dt) { }

    /**
     * No-op – rendering is handled by {@code WinScreen} after the transition.
     */
    @Override public void  render(float dt) { }

    /**
     * Always returns {@code null}; {@code GameScreen} reacts to this state type directly.
     */
    @Override public IGameState next()      { return null; }
}

