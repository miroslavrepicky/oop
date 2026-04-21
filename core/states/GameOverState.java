package sk.stuba.fiit.core.states;

// ─────────────────────────────────────────────────────────────────────────────
//  Terminálny stav: hra skončila prehrou.
//  GameScreen zistí tento stav cez next() a prepne na GameOverScreen.
// ─────────────────────────────────────────────────────────────────────────────
public class GameOverState implements IGameState {

    /**
     * No-op – the game has ended; no further logic is executed in this state.
     */
    @Override public void  update(float dt) {
        /* terminal – GameScreen switches screen */
    }

    /**
     * No-op – rendering is handled by {@code GameOverScreen} after the transition.
     */
    @Override public void  render(float dt) {
        /* terminal – GameScreen switches screen */
    }

    /**
     * Always returns {@code null}; {@code GameScreen} reacts to this state type directly.
     */
    @Override public IGameState next()      { return null; }
}
