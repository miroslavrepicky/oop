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
import sk.stuba.fiit.projectiles.EggProjectile;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.projectiles.TurdflyProjectile;
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

        // 2. ShapeRenderer – fallback pre objekty bez animácie (nepriatelia)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (EnemyCharacter enemy : level.getEnemies()) {
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(enemy.getPosition().getX(), enemy.getPosition().getY(), 32, 32);
        }

        // fallback pre projektily bez animácie (Arrow)
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            if (projectile instanceof Arrow) {
                shapeRenderer.setColor(0.8f, 0.6f, 0.2f, 1);
                shapeRenderer.rect(projectile.getPosition().getX(),
                    projectile.getPosition().getY(), 16, 8);
            }
        }

        // fallback pre hráča ak nie je animácia
        if (player != null && player.getAnimationManager() == null) {
            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(player.getPosition().getX(), player.getPosition().getY(), 32, 32);
        }

        shapeRenderer.end();

        // 3. SpriteBatch – všetky animované objekty
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // hráč
        if (player != null && player.getAnimationManager() != null) {
            player.updateAnimation(deltaTime);
            player.getAnimationManager().render(batch,
                player.getPosition().getX(),
                player.getPosition().getY(),
                96, 84,
                !player.isFacingRight());
        }

        // kačky – animácia walk/idle, flip podľa smeru chôdze
        for (Duck duck : level.getDucks()) {
            if (!duck.isAlive()) continue;
            if (duck.getAnimationManager() != null) {
                duck.getAnimationManager().render(batch,
                    duck.getPosition().getX(),
                    duck.getPosition().getY(),
                    32, 32,
                    !duck.isFacingRight());
            }
        }

        // projektily s animáciou: EggProjectile a TurdflyProjectile
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;

            if (projectile instanceof EggProjectile) {
                EggProjectile egg = (EggProjectile) projectile;
                if (egg.getAnimationManager() != null) {
                    // počas výbuchu väčšia veľkosť (AoE vizuál)
                    boolean blasting = egg.getEggState() == EggProjectile.EggState.BLASTING;
                    float w = blasting ? 64f : 32f;
                    float h = blasting ? 64f : 32f;
                    float offsetX = blasting ? -16f : 0f; // vycentruj výbuch
                    float offsetY = blasting ? -16f : 0f;
                    egg.getAnimationManager().render(batch,
                        egg.getPosition().getX() + offsetX,
                        egg.getPosition().getY() + offsetY,
                        w, h,
                        false);
                }

            } else if (projectile instanceof TurdflyProjectile) {
                TurdflyProjectile turdfly = (TurdflyProjectile) projectile;
                if (turdfly.getAnimationManager() != null) {
                    // flip podľa smeru letu
                    boolean flyingLeft = turdfly.getPosition().getX() < 0 ||
                        turdfly.getDirection().getX() < 0;
                    turdfly.getAnimationManager().render(batch,
                        turdfly.getPosition().getX(),
                        turdfly.getPosition().getY(),
                        46, 33,
                        flyingLeft);
                }
            }
        }

        batch.end();

        // 4. HUD
        hudRenderer.render();
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
