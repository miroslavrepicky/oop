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
 * Pauza: herná logika stojí, scéna ostáva viditeľná so semi-transparentným
 * overlay a ponukou klávesov.
 *
 * <ul>
 *   <li><b>P</b>     – zrušiť pauzu a pokračovať</li>
 *   <li><b>S</b>     – uložiť hru do súboru</li>
 *   <li><b>ESC</b>   – vrátiť sa do hlavného menu (po uložení ak chce hráč)</li>
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

    /** Správa zobrazená hráčovi po akcii (napr. "Hra uložená!"). */
    private String statusMsg = "";

    /** Ak true, next() vráti TerminalState ktorý spustí hlavné menu. */
    private boolean exitRequested = false;

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

    // ── IGameState ────────────────────────────────────────────────────────────

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

    @Override
    public void render(float deltaTime) {
        // Najprv vykreslíme živú scénu
        resumeState.render(deltaTime);

        // Semi-transparentný overlay
        shape.setProjectionMatrix(hudCam.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.55f);
        shape.rect(0, 0, W, H);
        shape.end();

        // Text
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

    @Override
    public IGameState next() {
        if (exitRequested) {
            // Vrátim TerminalState – GameScreen ho zachytí a zavolá execute()
            return new ExitToMenuState();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            dispose();
            return resumeState;
        }
        return null;
    }

    // ── Pomocné ───────────────────────────────────────────────────────────────

    private void dispose() {
        shape.dispose();
        batch.dispose();
        font.dispose();
    }
}
