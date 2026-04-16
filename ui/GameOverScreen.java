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

public class GameOverScreen implements Screen {

    private static final float W = 800f;
    private static final float H = 480f;

    private final ShadowQuest game;
    private final int failedLevel;
    private final OrthographicCamera cam;
    private final SpriteBatch batch;
    private final ShapeRenderer shape;
    private final BitmapFont font;
    private final Rectangle btnRetry;
    private final Rectangle btnMenu;

    public GameOverScreen(ShadowQuest game, int failedLevel) {
        this.game         = game;
        this.failedLevel  = failedLevel;

        cam = new OrthographicCamera();
        cam.setToOrtho(false, W, H);

        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        font  = new BitmapFont();
        font.getData().setScale(1.5f);

        btnRetry = new Rectangle(W / 2 - 100, 220, 200, 44);
        btnMenu  = new Rectangle(W / 2 - 100, 155, 200, 44);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.05f, 0.05f, 1f);
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

    private void drawBackground(float mx, float my) {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawButtonShape(btnRetry, mx, my, false);
        drawButtonShape(btnMenu,  mx, my, false);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.DARK_GRAY);
        shape.rect(btnRetry.x, btnRetry.y, btnRetry.width, btnRetry.height);
        shape.rect(btnMenu.x,  btnMenu.y,  btnMenu.width,  btnMenu.height);
        shape.end();
    }

    private void drawButtonShape(Rectangle btn, float mx, float my, boolean danger) {
        boolean hover = btn.contains(mx, my);
        shape.setColor(
            hover ? 0.25f : 0.15f,
            hover ? 0.45f : 0.28f,
            hover ? 0.25f : 0.15f,
            1f);
        shape.rect(btn.x, btn.y, btn.width, btn.height);
    }

    private void drawText(float mx, float my) {
        batch.begin();

        font.setColor(Color.RED);
        font.draw(batch, "YOU FAILED", W / 2 - 80, 380f);

        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Level " + failedLevel + " - skus to znova.", W / 2 - 145, 310f);

        drawButtonLabel(btnRetry, "Skus znova", mx, my);
        drawButtonLabel(btnMenu,  "Hlavne menu", mx, my);

        batch.end();
    }

    private void drawButtonLabel(Rectangle btn, String label, float mx, float my) {
        boolean hover = btn.contains(mx, my);
        font.setColor(hover ? Color.WHITE : Color.LIGHT_GRAY);
        font.draw(batch, label, btn.x + 10, btn.y + btn.height - 10);
    }

    private void handleClick(float mx, float my) {
        if (btnRetry.contains(mx, my)) {
            // revive postav - game over ich zabil
            GameManager.getInstance().reviveParty();
            game.setScreen(new InventoryScreen(game, failedLevel));
        }
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
