package sk.stuba.fiit.core.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import sk.stuba.fiit.core.AppController;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.render.GameRenderer;

/**
 * Pause state: game logic is frozen while the scene remains visible behind a
 * semi-transparent overlay showing the available key bindings.
 *
 * <ul>
 *   <li><b>P</b> – resume the game</li>
 *   <li><b>S</b> – save the game to disk</li>
 *   <li><b>ESC</b> – return to the main menu</li>
 * </ul>
 */
public class PausedState implements IGameState {

    private static final float W = 800f;
    private static final float H = 480f;

    private final GameRenderer   gameRenderer;
    private final IGameState     resumeState;
    private final AppController  app;
    private final GameManager    gameManager;

    private final OrthographicCamera hudCam;
    private final ShapeRenderer      shape;
    private final SpriteBatch        batch;
    private final BitmapFont         font;

    /** Status message displayed to the player after an action (e.g. "Game saved!"). */
    private String statusMsg = "";

    /** When {@code true}, {@link #next()} returns a terminal state that navigates to the main menu. */
    private boolean exitRequested = false;

    /**
     * @param gameRenderer the renderer used to draw the live scene behind the overlay
     * @param resumeState  the state to return to when the player resumes
     * @param app          application controller used for navigation actions
     * @param gameManager  provides the current level number for save operations
     */
    public PausedState(GameRenderer gameRenderer, IGameState resumeState,
                       AppController app, GameManager gameManager) {
        this.gameRenderer = gameRenderer;
        this.resumeState  = resumeState;
        this.app          = app;
        this.gameManager  = gameManager;

        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, W, H);

        shape = new ShapeRenderer();
        batch = new SpriteBatch();
        font  = new BitmapFont();
        font.getData().setScale(1.4f);
    }

    //  IGameState

    /**
     * Processes pause-menu key presses. Game simulation does not advance here.
     *
     * @param deltaTime elapsed time since the last frame, in seconds (unused)
     */
    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            int level = gameManager.getCurrentLevel() != null
                ? gameManager.getCurrentLevel().getLevelNumber() : 1;
            boolean ok = app.saveGame(level);
            statusMsg = ok ? "Hra ulozena!  (Level " + level + ")" : "Ulozenie zlyhalo.";
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            exitRequested = true;
        }
    }

    /**
     * Returns the next state to transition to:
     * <ul>
     *   <li>An {@link ExitToMenuState} if ESC was pressed.</li>
     *   <li>The original {@link #resumeState} if P is pressed (resume).</li>
     *   <li>{@code null} to remain in the paused state.</li>
     * </ul>
     */
    @Override
    public IGameState next() {
        if (exitRequested) {
            // ESC -> TerminalState
            // Vratim TerminalState – GameScreen ho zachyti a zavola execute()
            return new ExitToMenuState();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            dispose();
            return resumeState; // P -> vráť sa do PlayingState
        }
        return null; // zostať v pauze
    }

    /**
     * Renders the live game scene and overlays the pause menu on top of it.
     *
     * @param deltaTime elapsed time since the last frame, in seconds
     */
    @Override
    public void render(float deltaTime) {
        // Render the live scene first
        resumeState.render(deltaTime);

        // Semi-transparent overlay
        shape.setProjectionMatrix(hudCam.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.55f);
        shape.rect(0, 0, W, H);
        shape.end();

        // Pause menu text
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, "PAUZA",       W / 2f - 50,  340f);

        font.setColor(Color.CYAN);
        font.draw(batch, "[P]  Pokracovat",  W / 2f - 120, 285f);

        font.setColor(Color.GREEN);
        font.draw(batch, "[S]  Ulozit hru",  W / 2f - 120, 245f);

        font.setColor(new Color(1f, 0.45f, 0.45f, 1f));
        font.draw(batch, "[ESC]  Hlavne menu", W / 2f - 120, 205f);

        if (!statusMsg.isEmpty()) {
            font.setColor(Color.YELLOW);
            font.draw(batch, statusMsg, W / 2f - 160, 150f);
        }

        batch.end();
    }


    /** Releases LibGDX rendering resources owned by this state. */
    private void dispose() {
        shape.dispose();
        batch.dispose();
        font.dispose();
    }
}
