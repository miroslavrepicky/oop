package sk.stuba.fiit.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.ShadowQuest;


/**
 * Screen displayed when the player successfully completes all levels.
 *
 * <p>Renders a congratulatory message and a single "Main Menu" button that
 * resets the game state via {@link GameManager#resetGame()} before navigating
 * back to {@link MainMenuScreen}.
 *
 * <p>Uses a fixed virtual resolution of {@value #W}×{@value #H} pixels mapped
 * through an {@link OrthographicCamera}, so button hit-testing is resolution-independent.
 */
public class WinScreen implements Screen {

    private static final float W = 800f;
    private static final float H = 480f;

    private final ShadowQuest game;
    private final OrthographicCamera cam;
    private final SpriteBatch batch;
    private final ShapeRenderer shape;
    private final BitmapFont font;

    /** Hit-area for the "Main Menu" button. */
    private final Rectangle btnMenu;

    public WinScreen(ShadowQuest game) {
        this.game = game;

        cam = new OrthographicCamera();
        cam.setToOrtho(false, W, H);

        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        font  = new BitmapFont();
        font.getData().setScale(1.5f);

        btnMenu = new Rectangle(W / 2 - 100, 175, 200, 44);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.1f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float mx = Gdx.input.getX() * (W / Gdx.graphics.getWidth());
        float my = H - Gdx.input.getY() * (H / Gdx.graphics.getHeight());
        boolean click = Gdx.input.justTouched();

        if (click) handleClick(mx, my);

        shape.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);

        drawBackground(mx, my);
        drawText(mx, my);
    }

    /**
     * Draws the button background with a hover highlight.
     *
     * @param mx virtual mouse X coordinate
     * @param my virtual mouse Y coordinate
     */
    private void drawBackground(float mx, float my) {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        boolean hover = btnMenu.contains(mx, my);
        shape.setColor(hover ? 0.25f : 0.15f, hover ? 0.45f : 0.28f, hover ? 0.25f : 0.15f, 1f);
        shape.rect(btnMenu.x, btnMenu.y, btnMenu.width, btnMenu.height);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.GOLD);
        shape.rect(btnMenu.x, btnMenu.y, btnMenu.width, btnMenu.height);
        shape.end();
    }

    /**
     * Draws the win message and the button label.
     *
     * @param mx virtual mouse X coordinate
     * @param my virtual mouse Y coordinate
     */
    private void drawText(float mx, float my) {
        batch.begin();

        font.setColor(Color.GOLD);
        font.draw(batch, "VYHRAL SI!", W / 2 - 75, 370f);

        font.setColor(Color.WHITE);
        font.draw(batch, "Gratulujeme, svet je zachraneny.", W / 2 - 185, 310f);

        boolean hover = btnMenu.contains(mx, my);
        font.setColor(hover ? Color.WHITE : Color.LIGHT_GRAY);
        font.draw(batch, "Hlavne menu", btnMenu.x + 10, btnMenu.y + btnMenu.height - 10);

        batch.end();
    }

    /**
     * Handles a mouse click: navigates to the main menu if the button was clicked.
     *
     * @param mx virtual mouse X coordinate
     * @param my virtual mouse Y coordinate
     */
    private void handleClick(float mx, float my) {
        if (btnMenu.contains(mx, my)) {
            GameManager.getInstance().resetGame();
            game.setScreen(new MainMenuScreen(game));
        }
    }

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
