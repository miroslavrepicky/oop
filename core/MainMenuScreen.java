package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class MainMenuScreen implements Screen {

    private static final float W = 800f;
    private static final float H = 480f;

    private final ShadowQuest game;
    private final OrthographicCamera cam;
    private final SpriteBatch batch;
    private final ShapeRenderer shape;
    private final BitmapFont font;
    private final Rectangle btnNewGame;
    private final Rectangle btnExit;

    public MainMenuScreen(ShadowQuest game) {
        this.game = game;

        cam = new OrthographicCamera();
        cam.setToOrtho(false, W, H);

        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        font  = new BitmapFont();
        font.getData().setScale(1.5f);

        // stred obrazovky - tlacidla vycentrovane
        btnNewGame = new Rectangle(W / 2 - 100, 240, 200, 44);
        btnExit    = new Rectangle(W / 2 - 100, 175, 200, 44);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.08f, 1f);
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
        drawButtonShape(btnNewGame, mx, my);
        drawButtonShape(btnExit, mx, my);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.DARK_GRAY);
        shape.rect(btnNewGame.x, btnNewGame.y, btnNewGame.width, btnNewGame.height);
        shape.rect(btnExit.x,    btnExit.y,    btnExit.width,    btnExit.height);
        shape.end();
    }

    private void drawButtonShape(Rectangle btn, float mx, float my) {
        boolean hover = btn.contains(mx, my);
        shape.setColor(hover ? 0.25f : 0.15f, hover ? 0.45f : 0.28f, hover ? 0.25f : 0.15f, 1f);
        shape.rect(btn.x, btn.y, btn.width, btn.height);
    }

    private void drawText(float mx, float my) {
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, "SHADOW QUEST", W / 2 - 90, 380f);

        drawButtonLabel(btnNewGame, "New Game", mx, my);
        drawButtonLabel(btnExit,    "Exit",     mx, my);

        batch.end();
    }

    private void drawButtonLabel(Rectangle btn, String label, float mx, float my) {
        boolean hover = btn.contains(mx, my);
        font.setColor(hover ? Color.WHITE : Color.LIGHT_GRAY);
        font.draw(batch, label, btn.x + 10, btn.y + btn.height - 10);
    }

    private void handleClick(float mx, float my) {
        if (btnNewGame.contains(mx, my)) {
            GameManager.getInstance().resetGame();
            GameManager.getInstance().initGame();
            game.setScreen(new InventoryScreen(game, 1));
        }
        if (btnExit.contains(mx, my)) {
            Gdx.app.exit();
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
