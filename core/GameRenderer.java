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
import sk.stuba.fiit.util.Vector2D;
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

        for (EnemyCharacter enemy : level.getEnemies()) {
            if (enemy.getAnimationManager() != null) continue;
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(enemy.getPosition().getX(), enemy.getPosition().getY(), 32, 32);
        }

        if (player != null && player.getAnimationManager() == null) {
            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(player.getPosition().getX(), player.getPosition().getY(), 32, 32);
        }

        shapeRenderer.end();

        // 3. SpriteBatch – vsetky animovane objekty
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // hrac – skutočná veľkosť každého framu, centrovaná na hitbox
        if (player != null && player.getAnimationManager() != null) {
            player.updateAnimation(deltaTime);
            player.getAnimationManager().renderActualSize(batch,
                player.getPosition().getX(),
                player.getPosition().getY(),
                player.getHitbox().width,
                !player.isFacingRight());
        }

        // nepriatelia – skutočná veľkosť každého framu, centrovaná na hitbox
        for (EnemyCharacter enemy : level.getEnemies()) {
            AnimationManager am = enemy.getAnimationManager();
            if (am == null) continue;
            enemy.updateAnimation(deltaTime);
            am.renderActualSize(batch,
                enemy.getPosition().getX(),
                enemy.getPosition().getY(),
                enemy.getHitbox().width,
                !enemy.isFacingRight());
        }

        // kacky – skutočná veľkosť, centrovaná na hitbox
        for (Duck duck : level.getDucks()) {
            if (!duck.isAlive()) continue;
            AnimationManager am = duck.getAnimationManager();
            if (am == null) continue;
            am.renderActualSize(batch,
                duck.getPosition().getX(),
                duck.getPosition().getY(),
                duck.getHitbox().width,
                !duck.isFacingRight());
        }

        // ikony itemov na zemi
        itemIconRenderer.render(batch, level.getItems());

        // projektily – pevná veľkosť cez render() (tu rozťahovanie nevadí)
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;

            if (projectile instanceof MagicSpell) {
                MagicSpell spell = (MagicSpell) projectile;
                AnimationManager am = spell.getAnimationManager();
                if (am != null) {
                    boolean flipX = spell.getDirection().getX() < 0;
                    am.render(batch,
                        spell.getPosition().getX(),
                        spell.getPosition().getY(),
                        64, 36, flipX);
                    am.update(deltaTime);
                }

            } else if (projectile instanceof Arrow) {
                Arrow arrow = (Arrow) projectile;
                AnimationManager am = arrow.getAnimationManager();
                if (am != null) {
                    boolean flipX = arrow.getDirection().getX() < 0;
                    am.render(batch,
                        arrow.getPosition().getX(),
                        arrow.getPosition().getY(),
                        32, 16, flipX);
                }

            } else if (projectile instanceof EggProjectile) {
                EggProjectile egg = (EggProjectile) projectile;
                AnimationManager am = egg.getAnimationManager();
                if (am != null) {
                    boolean blasting = egg.getEggState() == EggProjectile.EggState.BLASTING;
                    float w = blasting ? 64f : 32f;
                    float h = blasting ? 64f : 32f;
                    float offsetX = blasting ? -16f : 0f;
                    float offsetY = blasting ? -16f : 0f;
                    am.render(batch,
                        egg.getPosition().getX() + offsetX,
                        egg.getPosition().getY() + offsetY,
                        w, h, false);
                }

            } else if (projectile instanceof TurdflyProjectile) {
                TurdflyProjectile turdfly = (TurdflyProjectile) projectile;
                AnimationManager am = turdfly.getAnimationManager();
                if (am != null) {
                    boolean flyingLeft = turdfly.getDirection().getX() < 0;
                    am.render(batch,
                        turdfly.getPosition().getX(),
                        turdfly.getPosition().getY(),
                        46, 33, flyingLeft);
                }
            }
        }

        batch.end();

        if (player != null) {
            renderPlayerIndicator(player);
        }

        if (debugHitboxes) {
            renderHitboxes(level, player);
        }

        // 4. HUD
        hudRenderer.render();
    }

    /**
     * Kresli maly obrateny trojuholnik nad hlavou aktivneho hraca.
     * Trojuholnik je vykresleny vo world suradniciach (camera.combined),
     * takze sa pohybuje spolu s postavou.
     */
    private void renderPlayerIndicator(PlayerCharacter player) {
        // Vyska trojuholnika nad hitboxom
        float hitboxTop = player.getPosition().getY() + player.getHitbox().height;
        float margin    = 6f;   // medzera medzi hlavou a trojuholnikom
        float triH      = 10f;  // vyska trojuholnika
        float triW      = 12f;  // sirka zakladne
        float cx        = player.getPosition().getX() + player.getHitbox().width / 2f;
        float top       = hitboxTop + margin + triH;  // zakladna (hore)
        float tip       = hitboxTop + margin;         // spicka (dole)

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.CYAN);
        // LibGDX triangle: v1 (lavý roh zakladne), v2 (pravy roh zakladne), v3 (spicka)
        shapeRenderer.triangle(
            cx - triW / 2f, top,   // lavy roh zakladne
            cx + triW / 2f, top,   // pravy roh zakladne
            cx,             tip    // spicka dole
        );
        shapeRenderer.end();
    }

    private void renderHitboxes(Level level, PlayerCharacter player) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        if (player != null) {
            shapeRenderer.setColor(Color.GREEN);
            Rectangle hb = player.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        shapeRenderer.setColor(Color.RED);
        for (EnemyCharacter enemy : level.getEnemies()) {
            Rectangle hb = enemy.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        shapeRenderer.setColor(Color.YELLOW);
        for (Item item : level.getItems()) {
            Rectangle hb = item.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        shapeRenderer.setColor(Color.CYAN);
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;
            Rectangle hb = projectile.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

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
