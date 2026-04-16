package sk.stuba.fiit.core.states;

import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.render.GameRenderer;

// ─────────────────────────────────────────────────────────────────────────────
//  Terminálny stav: hráč vyhral hru.
// ─────────────────────────────────────────────────────────────────────────────
public class WinState implements IGameState {
    @Override public void  update(float dt) { }
    @Override public void  render(float dt) { }
    @Override public IGameState next()      { return null; }
}

