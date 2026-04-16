package sk.stuba.fiit.core;

// ─────────────────────────────────────────────────────────────────────────────
//  Terminálny stav: hra skončila prehrou.
//  GameScreen zistí tento stav cez next() a prepne na GameOverScreen.
// ─────────────────────────────────────────────────────────────────────────────
class GameOverState implements IGameState {
    /** Flag pre GameScreen – keď vidí tento stav, prepne obrazovku. */
    @Override public void  update(float dt) { /* nič – čakáme na GameScreen */ }
    @Override public void  render(float dt) { /* nič – GameScreen prepne */ }
    @Override public IGameState next()      { return null; }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Terminálny stav: level dokončený.
//  GameScreen prepne na InventoryScreen s číslom ďalšieho levelu.
// ─────────────────────────────────────────────────────────────────────────────
class LevelCompleteState implements IGameState {
    private final GameManager gameManager;
    LevelCompleteState(GameManager gm) { this.gameManager = gm; }

    @Override public void  update(float dt) { }
    @Override public void  render(float dt) { }
    @Override public IGameState next()      { return null; }

    public int nextLevelNumber() {
        return gameManager.getCurrentLevel().getLevelNumber() + 1;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Terminálny stav: hráč vyhral hru.
// ─────────────────────────────────────────────────────────────────────────────
class WinState implements IGameState {
    @Override public void  update(float dt) { }
    @Override public void  render(float dt) { }
    @Override public IGameState next()      { return null; }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Stav: hra pozastavená (klávesa P).
//  Update sa preskočí; render zobrazí scénu so stmaveným overlayom.
// ─────────────────────────────────────────────────────────────────────────────
class PausedState implements IGameState {

    private final GameRenderer gameRenderer;
    private IGameState resumeState;  // stav, do ktorého sa vrátime po unpause

    PausedState(GameRenderer gameRenderer, IGameState resumeState) {
        this.gameRenderer = gameRenderer;
        this.resumeState  = resumeState;
    }

    @Override
    public void update(float deltaTime) {
        // Herná logika stojí – len kontrolujeme vstup na unpause
        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.P)) {
            // signál pre next() – vrátime resumeState
        }
    }

    @Override
    public void render(float deltaTime) {
        gameRenderer.render(deltaTime);
        // TODO: nakresliť polopriesvitný overlay a text "PAUSED"
        // (ShapeRenderer.Filled rect so 60 % opacity)
    }

    /**
     * Vracia resumeState iba ak hráč stlačil P.
     * Ak nie, vracia null (zostávame v pauze).
     */
    @Override
    public IGameState next() {
        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.P)) {
            return resumeState;
        }
        return null;
    }
}
