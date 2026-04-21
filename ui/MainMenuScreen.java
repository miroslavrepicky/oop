package sk.stuba.fiit.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;
import sk.stuba.fiit.core.AppController;
import sk.stuba.fiit.save.SaveManager;

/**
 * Main menu screen shown on application startup and after returning from
 * in-game screens.
 *
 * <h2>MVC placement</h2>
 * <p>This class is a <b>View + input handler</b>. It renders buttons and
 * reacts to mouse clicks, but contains no business logic. All game-state
 * mutations and navigation are delegated to the {@link AppController}.
 *
 * <p>The only read access to game state is {@link SaveManager#hasSave()},
 * which is a pure query with no side-effects and is needed to decide whether
 * the Continue button should be enabled.
 *
 * <h2>Actions</h2>
 * <ul>
 *   <li><b>New Game</b> – calls {@link AppController#startNewGame()}.</li>
 *   <li><b>Continue</b> – active only when a save exists; calls
 *       {@link AppController#continueGame()}.</li>
 *   <li><b>Exit</b> – calls {@link AppController#exitApp()}.</li>
 * </ul>
 *
 * <p>Uses a fixed virtual resolution of {@value #W}×{@value #H} pixels mapped
 * through an {@link OrthographicCamera} so button hit-testing is
 * resolution-independent.
 */
public class MainMenuScreen implements Screen {

    private static final float W = 800f;
    private static final float H = 480f;

    private final AppController      app;
    private final OrthographicCamera cam;
    private final SpriteBatch        batch;
    private final ShapeRenderer      shape;
    private final BitmapFont         font;

    private final Rectangle btnNewGame;
    private final Rectangle btnContinue;
    private final Rectangle btnExit;

    /**
     * {@code true} when a save file was detected at screen construction time.
     * Determines whether the Continue button is enabled.
     */
    private final boolean hasSave;

    /**
     * @param app the application controller used for navigation and business logic;
     *            must not be {@code null}
     */
    public MainMenuScreen(AppController app) {
        this.app = app;

        cam = new OrthographicCamera();
        cam.setToOrtho(false, W, H);

        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        font  = new BitmapFont();
        font.getData().setScale(1.5f);

        btnNewGame  = new Rectangle(W / 2 - 100, 275, 200, 44);
        btnContinue = new Rectangle(W / 2 - 100, 215, 200, 44);
        btnExit     = new Rectangle(W / 2 - 100, 155, 200, 44);

        hasSave = SaveManager.getInstance().hasSave();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float   mx    = Gdx.input.getX() * (W / Gdx.graphics.getWidth());
        float   my    = H - Gdx.input.getY() * (H / Gdx.graphics.getHeight());
        boolean click = Gdx.input.justTouched();

        if (click) handleClick(mx, my);

        shape.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);

        drawBackground(mx, my);
        drawText(mx, my);
    }

    // -------------------------------------------------------------------------
    //  Drawing
    // -------------------------------------------------------------------------

    /**
     * Draws all button backgrounds and outlines.
     * The Continue button uses a gold border when a save exists, dark-gray otherwise.
     *
     * @param mx virtual mouse X coordinate
     * @param my virtual mouse Y coordinate
     */
    private void drawBackground(float mx, float my) {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawButtonShape(btnNewGame,  mx, my, false);
        drawButtonShape(btnContinue, mx, my, !hasSave);
        drawButtonShape(btnExit,     mx, my, false);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.DARK_GRAY);
        shape.rect(btnNewGame.x, btnNewGame.y, btnNewGame.width, btnNewGame.height);
        shape.rect(btnExit.x,    btnExit.y,    btnExit.width,    btnExit.height);
        shape.setColor(hasSave ? Color.GOLD : Color.DARK_GRAY);
        shape.rect(btnContinue.x, btnContinue.y, btnContinue.width, btnContinue.height);
        shape.end();
    }

    /**
     * Fills a button background with a hover tint, or a flat grey colour when disabled.
     *
     * @param btn      the button rectangle
     * @param mx       virtual mouse X
     * @param my       virtual mouse Y
     * @param disabled when {@code true} the button is rendered as inactive
     */
    private void drawButtonShape(Rectangle btn, float mx, float my, boolean disabled) {
        if (disabled) {
            shape.setColor(0.12f, 0.12f, 0.12f, 1f);
            shape.rect(btn.x, btn.y, btn.width, btn.height);
            return;
        }
        boolean hover = btn.contains(mx, my);
        shape.setColor(hover ? 0.25f : 0.15f, hover ? 0.45f : 0.28f, hover ? 0.25f : 0.15f, 1f);
        shape.rect(btn.x, btn.y, btn.width, btn.height);
    }

    /**
     * Draws the title and all button labels.
     *
     * @param mx virtual mouse X coordinate
     * @param my virtual mouse Y coordinate
     */
    private void drawText(float mx, float my) {
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, "SHADOW QUEST", W / 2 - 90, 400f);

        drawButtonLabel(btnNewGame,  "New Game",                         mx, my, false);
        drawButtonLabel(btnContinue, hasSave ? "Continue" : "Continue  (no save)", mx, my, !hasSave);
        drawButtonLabel(btnExit,     "Exit",                             mx, my, false);

        batch.end();
    }

    /**
     * Draws a button label. Disabled buttons are rendered in dark-gray; active
     * buttons brighten when hovered.
     *
     * @param btn      the button rectangle
     * @param label    text to display
     * @param mx       virtual mouse X
     * @param my       virtual mouse Y
     * @param disabled when {@code true} the label is greyed out
     */
    private void drawButtonLabel(Rectangle btn, String label,
                                 float mx, float my, boolean disabled) {
        if (disabled) {
            font.setColor(Color.DARK_GRAY);
        } else {
            font.setColor(btn.contains(mx, my) ? Color.WHITE : Color.LIGHT_GRAY);
        }
        font.draw(batch, label, btn.x + 10, btn.y + btn.height - 10);
    }

    // -------------------------------------------------------------------------
    //  Input handling – no business logic, only AppController calls
    // -------------------------------------------------------------------------

    /**
     * Routes button clicks to the appropriate {@link AppController} method.
     *
     * <p>No game state is mutated here – all mutations live in
     * {@link sk.stuba.fiit.core.ShadowQuest}.
     *
     * @param mx virtual mouse X coordinate
     * @param my virtual mouse Y coordinate
     */
    private void handleClick(float mx, float my) {
        if (btnNewGame.contains(mx, my)) {
            app.startNewGame();
        }
        if (hasSave && btnContinue.contains(mx, my)) {
            app.continueGame();
        }
        if (btnExit.contains(mx, my)) {
            app.exitApp();
        }
    }

    // -------------------------------------------------------------------------
    //  Screen lifecycle
    // -------------------------------------------------------------------------

    @Override public void resize(int w, int h) { cam.setToOrtho(false, W, H); }
    @Override public void show()    {}
    @Override public void hide()    {}
    @Override public void pause()   {}
    @Override public void resume()  {}

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();
    }
}
