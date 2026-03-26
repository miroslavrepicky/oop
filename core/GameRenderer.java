package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.projectiles.Arrow;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

public class GameRenderer {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private HUDRenderer hudRenderer;

    public GameRenderer() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        hudRenderer = new HUDRenderer(batch);
    }

    public void render(float deltaTime) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        GameManager gm = GameManager.getInstance();
        Level level = gm.getCurrentLevel();
        if (level == null) return;

        PlayerCharacter player = gm.getInventory().getActive();
        if (player != null) {
            camera.position.x = player.getPosition().getX();
            camera.position.y = player.getPosition().getY();
            camera.update();
        }

        // 1. Mapa
        if (level.getMapManager() != null) {
            level.getMapManager().render(camera);
        }

        // 2. ShapeRenderer – nepriatelia, kačky, projektily
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (EnemyCharacter enemy : level.getEnemies()) {
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(enemy.getPosition().getX(), enemy.getPosition().getY(), 32, 32);
        }

        for (Duck duck : level.getDucks()) {
            shapeRenderer.setColor(1, 1, 0, 1);
            shapeRenderer.rect(duck.getPosition().getX(), duck.getPosition().getY(), 32, 32);
        }

        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            if (projectile instanceof Arrow) {
                shapeRenderer.setColor(0.8f, 0.6f, 0.2f, 1);
            } else {
                shapeRenderer.setColor(0.5f, 0, 1, 1);
            }
            shapeRenderer.rect(projectile.getPosition().getX(),
                projectile.getPosition().getY(), 16, 8);
        }

        // fallback pre hráča ak nie je animácia
        if (player != null && player.getAnimationManager() == null) {
            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(player.getPosition().getX(), player.getPosition().getY(), 32, 32);
        }

        shapeRenderer.end();

        // 3. SpriteBatch – sprites a animácie
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (player != null && player.getAnimationManager() != null) {
            player.updateAnimation(deltaTime);
            player.getAnimationManager().render(batch,
                player.getPosition().getX(),
                player.getPosition().getY(),
                96, 84,
                !player.isFacingRight());
        }

        batch.end();

        // 4. HUD
        hudRenderer.render();
    }

    private void renderCharacter(float x, float y, int type) {
        // placeholder – nahradíš sprite-mi
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        switch (type) {
            case 0: shapeRenderer.setColor(0, 1, 0, 1); break; // hráč – zelený
            case 1: shapeRenderer.setColor(1, 0, 0, 1); break; // nepriateľ – červený
            case 2: shapeRenderer.setColor(1, 1, 0, 1); break; // kačka – žltá
        }
        shapeRenderer.rect(x, y, 32, 32);
        shapeRenderer.end();
    }

    private void renderProjectile(Projectile projectile) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (projectile instanceof Arrow) {
            shapeRenderer.setColor(0.8f, 0.6f, 0.2f, 1); // hnedá
        } else {
            shapeRenderer.setColor(0.5f, 0, 1, 1); // fialová
        }
        shapeRenderer.rect(projectile.getPosition().getX(),
            projectile.getPosition().getY(), 16, 8);
        shapeRenderer.end();
    }

    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        hudRenderer.dispose();
    }
}
