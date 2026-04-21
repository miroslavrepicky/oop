package sk.stuba.fiit.core.states;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import sk.stuba.fiit.render.GameRenderer;

/**
 * Game state entered when the player presses the {@code P} key.
 *
 * <p>While paused, the game logic (physics, AI, projectiles) is frozen –
 * {@link #update(float)} is intentionally a no-op. Rendering is delegated to
 * the resume state ({@link PlayingState}) so that the last live scene snapshot
 * remains visible on screen.
 *
 * <p>Pressing {@code P} again returns to the resume state via {@link #next()}.
 *
 * <h2>TODO</h2>
 * <ul>
 *   <li>Draw a semi-transparent dark overlay with a centred "PAUSED" label using
 *       {@code ShapeRenderer} (Filled rect at ~60 % opacity) and {@code BitmapFont}
 *       rendered through a dedicated HUD camera.</li>
 * </ul>
 */
public class PausedState implements IGameState {

    private final GameRenderer gameRenderer;

    /** The state to return to when the player unpauses. */
    private final IGameState   resumeState;

    /**
     * @param gameRenderer the active game renderer (kept for future overlay drawing)
     * @param resumeState  the state to resume when the player presses P again
     */
    public PausedState(GameRenderer gameRenderer, IGameState resumeState) {
        this.gameRenderer = gameRenderer;
        this.resumeState  = resumeState;
    }

    /**
     * No-op while paused – game logic is frozen.
     */
    @Override
    public void update(float deltaTime) {
        // Herná logika stojí – len čakáme na unpause
    }

    /**
     * Delegates scene rendering to the resume state so the live frame remains visible.
     * A pause overlay should be drawn on top here (see class-level TODO).
     */
    @Override
    public void render(float deltaTime) {
        // Zakreslíme scénu cez resumeState (ten vie zostaviť snapshot)
        resumeState.render(deltaTime);
        // TODO: nakresliť polopriesvitný overlay a text "PAUSED"
        // (ShapeRenderer.Filled rect so 60 % opacity cez hudCamera)
    }

    /**
     * Returns the resume state when P is pressed; otherwise returns {@code null}.
     */
    @Override
    public IGameState next() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            return resumeState;
        }
        return null;
    }
}
