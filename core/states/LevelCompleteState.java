package sk.stuba.fiit.core.states;

import sk.stuba.fiit.core.GameManager;

// ─────────────────────────────────────────────────────────────────────────────
//  Terminálny stav: level dokončený.
//  GameScreen prepne na InventoryScreen s číslom ďalšieho levelu.
// ─────────────────────────────────────────────────────────────────────────────
public class LevelCompleteState implements IGameState {
    private final GameManager gameManager;
    LevelCompleteState(GameManager gm) { this.gameManager = gm; }

    @Override public void  update(float dt) { }
    @Override public void  render(float dt) { }
    @Override public IGameState next()      { return null; }

    public int nextLevelNumber() {
        return gameManager.getCurrentLevel().getLevelNumber() + 1;
    }
}
