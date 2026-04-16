package sk.stuba.fiit.core.states;

import sk.stuba.fiit.render.GameRenderer;

// ─────────────────────────────────────────────────────────────────────────────
//  Stav: hra pozastavená (klávesa P).
//  Update sa preskočí; render zobrazí scénu so stmaveným overlayom.
// ─────────────────────────────────────────────────────────────────────────────
public class PausedState implements IGameState {

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
