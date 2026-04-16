package sk.stuba.fiit.core.states;

// ─────────────────────────────────────────────────────────────────────────────
//  Terminálny stav: hra skončila prehrou.
//  GameScreen zistí tento stav cez next() a prepne na GameOverScreen.
// ─────────────────────────────────────────────────────────────────────────────
public class GameOverState implements IGameState {
    /** Flag pre GameScreen – keď vidí tento stav, prepne obrazovku. */
    @Override public void  update(float dt) { /* nič – čakáme na GameScreen */ }
    @Override public void  render(float dt) { /* nič – GameScreen prepne */ }
    @Override public IGameState next()      { return null; }
}
