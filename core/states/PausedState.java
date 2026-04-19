package sk.stuba.fiit.core.states;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import sk.stuba.fiit.render.GameRenderer;

/**
 * Stav: hra pozastavená (klávesa P).
 * Update sa preskočí; render zobrazí scénu so stmaveným overlayom.
 *
 * Zmena: render() deleguje na resumeState.render() namiesto
 * priameho volania gameRenderer.render() – resumeState (PlayingState)
 * vie zostaviť snapshot, PausedState to nemusí robiť sám.
 */
public class PausedState implements IGameState {

    private final GameRenderer gameRenderer;
    private final IGameState   resumeState;

    public PausedState(GameRenderer gameRenderer, IGameState resumeState) {
        this.gameRenderer = gameRenderer;
        this.resumeState  = resumeState;
    }

    @Override
    public void update(float deltaTime) {
        // Herná logika stojí – len čakáme na unpause
    }

    @Override
    public void render(float deltaTime) {
        // Zakreslíme scénu cez resumeState (ten vie zostaviť snapshot)
        resumeState.render(deltaTime);
        // TODO: nakresliť polopriesvitný overlay a text "PAUSED"
        // (ShapeRenderer.Filled rect so 60 % opacity cez hudCamera)
    }

    @Override
    public IGameState next() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            return resumeState;
        }
        return null;
    }
}
