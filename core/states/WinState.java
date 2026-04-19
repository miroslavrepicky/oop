package sk.stuba.fiit.core.states;

// ─────────────────────────────────────────────────────────────────────────────
//  Terminálny stav: hráč vyhral hru.
// ─────────────────────────────────────────────────────────────────────────────
public class WinState implements IGameState {
    @Override public void  update(float dt) { }
    @Override public void  render(float dt) { }
    @Override public IGameState next()      { return null; }
}

