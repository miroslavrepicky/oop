package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.projectiles.*;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.world.Level;

public class GameRenderer {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private HUDRenderer hudRenderer;
    private ItemIconRenderer itemIconRenderer;
    private CollisionManager collisionManager;
    private boolean debugHitboxes = false;

    public GameRenderer() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        hudRenderer = new HUDRenderer(batch, collisionManager);
        itemIconRenderer = new ItemIconRenderer();
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

        // 2. ShapeRenderer – fallback pre objekty BEZ animacie
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // nepriatelia bez animacie (obranny fallback)
        for (EnemyCharacter enemy : level.getEnemies()) {
            if (enemy.getAnimationManager() != null) continue;
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(enemy.getPosition().getX(), enemy.getPosition().getY(), 32, 32);
        }


        // fallback pre hraca ak nie je animacia
        if (player != null && player.getAnimationManager() == null) {
            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(player.getPosition().getX(), player.getPosition().getY(), 32, 32);
        }

        shapeRenderer.end();

        // 3. SpriteBatch – vsetky animovane objekty
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // hrac
        if (player != null && player.getAnimationManager() != null) {
            player.updateAnimation(deltaTime);
            player.getAnimationManager().render(batch,
                player.getPosition().getX(),
                player.getPosition().getY(),
                96, 84,
                !player.isFacingRight());
        }

        // nepriatelia – animacia
        for (EnemyCharacter enemy : level.getEnemies()) {
            if (enemy.getAnimationManager() == null) continue;
            enemy.updateAnimation(deltaTime);
            enemy.getAnimationManager().render(batch,
                enemy.getPosition().getX(),
                enemy.getPosition().getY(),
                96, 84,
                !enemy.isFacingRight());
        }

        // kacky – animacia walk/idle, flip podla smeru chodze
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

        // ikony itemov na zemi
        itemIconRenderer.render(batch, level.getItems());

        // projektily s animaciou: EggProjectile a TurdflyProjectile
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;

            if (projectile instanceof MagicSpell) {
                MagicSpell spell = (MagicSpell) projectile;
                if (spell.getAnimationManager() != null) {
                    boolean flipX = spell.getDirection().getX() < 0;
                    spell.getAnimationManager().render(batch,
                        spell.getPosition().getX(),
                        spell.getPosition().getY(),
                        64, 36, flipX);
                    spell.getAnimationManager().update(deltaTime); // ak nevolas inak
                }
            } else if (projectile instanceof Arrow) {
                Arrow arrow = (Arrow) projectile;
                if (arrow.getAnimationManager() != null) {
                    boolean flipX = arrow.getDirection().getX() < 0;
                    arrow.getAnimationManager().render(batch,
                        arrow.getPosition().getX(),
                        arrow.getPosition().getY(),
                        32, 16, flipX);
                }
            } else if (projectile instanceof EggProjectile) {
                EggProjectile egg = (EggProjectile) projectile;
                if (egg.getAnimationManager() != null) {
                    // pocas vybuchu vacsia velkost (AoE vizual)
                    boolean blasting = egg.getEggState() == EggProjectile.EggState.BLASTING;
                    float w = blasting ? 64f : 32f;
                    float h = blasting ? 64f : 32f;
                    float offsetX = blasting ? -16f : 0f; // vycentruj vybuch
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
                    // flip podla smeru letu
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
        if (debugHitboxes) {
            renderHitboxes(level, player);
        }


        // 4. HUD
        hudRenderer.render();
    }

    private void renderHitboxes(Level level, PlayerCharacter player) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // hráč
        if (player != null) {
            shapeRenderer.setColor(Color.GREEN);
            Rectangle hb = player.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        // nepriatelia
        shapeRenderer.setColor(Color.RED);
        for (EnemyCharacter enemy : level.getEnemies()) {
            Rectangle hb = enemy.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        // itemy
        shapeRenderer.setColor(Color.YELLOW);
        for (Item item : level.getItems()) {
            Rectangle hb = item.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        // projektily
        shapeRenderer.setColor(Color.CYAN);
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            Rectangle hb = projectile.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        // kačky
        shapeRenderer.setColor(Color.ORANGE);
        for (Duck duck : level.getDucks()) {
            if (!duck.isAlive()) continue;
            Rectangle hb = duck.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        shapeRenderer.end();
    }

    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    public void setCollisionManager(CollisionManager cm) {
        this.collisionManager = cm;
        this.hudRenderer = new HUDRenderer(batch, cm);
    }

    public void toggleDebugHitboxes() {
        debugHitboxes = !debugHitboxes;
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        hudRenderer.dispose();
        itemIconRenderer.dispose();
    }
}
