package sk.stuba.fiit.render;

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
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.projectiles.*;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.world.Level;

/**
 * Riadi vykresľovanie celej hernej scény.
 *
 * Zmeny po refaktore AnimationManager/AnimationRenderer:
 *  - Všetky volania {@code am.render()} a {@code am.renderActualSize()}
 *    nahradené statickými volaniami {@link AnimationRenderer}.
 *  - {@code AnimationManager} sa používa výlučne na správu stavu animácií
 *    (play, update, getCurrentAnimation) – žiadne vykresľovanie.
 */
public class GameRenderer {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private HUDRenderer hudRenderer;
    private ItemIconRenderer itemIconRenderer;
    private CollisionManager collisionManager;
    private boolean debugHitboxes = false;

    private static final float BAR_WIDTH   = 40f;
    private static final float BAR_H_HP    = 5f;
    private static final float BAR_H_ARM   = 3f;
    private static final float BAR_GAP     = 8f;
    private static final float BAR_SPACING = 2f;

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

        // 2. ShapeRenderer – fallback pre objekty BEZ animácie
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

        // 3. SpriteBatch – všetky animované objekty
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // --- Hráč ---
        if (player != null) {
            AnimationManager pam = player.getAnimationManager();
            if (pam != null) {
                player.updateAnimation(deltaTime);
                boolean isAttackAnim = "attack".equals(pam.getCurrentAnimation());
                AnimationRenderer.renderActualSize(
                    batch, pam,
                    player.getPosition().getX(),
                    player.getPosition().getY(),
                    player.getHitbox().width,
                    !player.isFacingRight(),
                    isAttackAnim
                );
            }
        }

        // --- Nepriatelia ---
        for (EnemyCharacter enemy : level.getEnemies()) {
            AnimationManager am = enemy.getAnimationManager();
            if (am == null) continue;
            enemy.updateAnimation(deltaTime);
            boolean isAttackAnim = "attack".equals(am.getCurrentAnimation());
            AnimationRenderer.renderActualSize(
                batch, am,
                enemy.getPosition().getX(),
                enemy.getPosition().getY(),
                enemy.getHitbox().width,
                !enemy.isFacingRight(),
                isAttackAnim
            );
        }

        // --- Kačky ---
        for (Duck duck : level.getDucks()) {
            if (!duck.isAlive()) continue;
            AnimationManager am = duck.getAnimationManager();
            if (am == null) continue;
            AnimationRenderer.renderActualSize(
                batch, am,
                duck.getPosition().getX(),
                duck.getPosition().getY(),
                duck.getHitbox().width,
                !duck.isFacingRight()
            );
        }

        // --- Ikony itemov na zemi ---
        itemIconRenderer.render(batch, level.getItems());

        // --- Projektily ---
        for (Projectile projectile : level.getProjectiles()) {
            if (!projectile.isActive()) continue;

            if (projectile instanceof MagicSpell) {
                MagicSpell spell = (MagicSpell) projectile;
                AnimationManager am = spell.getAnimationManager();
                boolean flipX = spell.getDirection().getX() < 0;
                AnimationRenderer.render(
                    batch, am,
                    spell.getPosition().getX(),
                    spell.getPosition().getY(),
                    64, 36, flipX
                );

            } else if (projectile instanceof Arrow) {
                Arrow arrow = (Arrow) projectile;
                AnimationManager am = arrow.getAnimationManager();
                boolean flipX = arrow.getDirection().getX() < 0;
                AnimationRenderer.render(
                    batch, am,
                    arrow.getPosition().getX(),
                    arrow.getPosition().getY(),
                    32, 16, flipX
                );

            } else if (projectile instanceof EggProjectile) {
                EggProjectile egg = (EggProjectile) projectile;
                AnimationManager am = egg.getAnimationManager();
                boolean blasting = egg.getEggState() == EggProjectile.EggState.BLASTING;
                float w       = blasting ? 64f : 32f;
                float h       = blasting ? 64f : 32f;
                float offsetX = blasting ? -16f : 0f;
                float offsetY = blasting ? -16f : 0f;
                AnimationRenderer.render(
                    batch, am,
                    egg.getPosition().getX() + offsetX,
                    egg.getPosition().getY() + offsetY,
                    w, h, false
                );

            } else if (projectile instanceof TurdflyProjectile) {
                TurdflyProjectile turdfly = (TurdflyProjectile) projectile;
                AnimationManager am = turdfly.getAnimationManager();
                boolean flyingLeft = turdfly.getDirection().getX() < 0;
                AnimationRenderer.render(
                    batch, am,
                    turdfly.getPosition().getX(),
                    turdfly.getPosition().getY(),
                    46, 33, flyingLeft
                );
            }
        }

        batch.end();

        renderEnemyBars(level);

        if (player != null) {
            renderPlayerIndicator(player);
        }

        if (debugHitboxes) {
            renderHitboxes(level, player);
        }

        // 4. HUD
        hudRenderer.render();
    }

    // -------------------------------------------------------------------------
    //  HP / Armor bary nepriateľov
    // -------------------------------------------------------------------------

    private void renderEnemyBars(Level level) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;

            float ex  = enemy.getPosition().getX();
            float ey  = enemy.getPosition().getY();
            float ew  = enemy.getHitbox().width;
            float top = ey + enemy.getHitbox().height + BAR_GAP;
            float barX = ex + (ew - BAR_WIDTH) / 2f;

            // HP bar
            float hpRatio = (float) enemy.getHp() / enemy.getMaxHp();
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.85f);
            shapeRenderer.rect(barX, top, BAR_WIDTH, BAR_H_HP);

            if (hpRatio > 0.5f)      shapeRenderer.setColor(0.25f, 0.75f, 0.15f, 1f);
            else if (hpRatio > 0.25f) shapeRenderer.setColor(0.95f, 0.65f, 0.05f, 1f);
            else                      shapeRenderer.setColor(0.90f, 0.15f, 0.10f, 1f);
            shapeRenderer.rect(barX, top, BAR_WIDTH * hpRatio, BAR_H_HP);

            // Armor bar
            if (enemy.getMaxArmor() > 0) {
                float armRatio = (float) enemy.getArmor() / enemy.getMaxArmor();
                float armY = top + BAR_H_HP + BAR_SPACING;
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.85f);
                shapeRenderer.rect(barX, armY, BAR_WIDTH, BAR_H_ARM);
                shapeRenderer.setColor(0.2f, 0.55f, 0.9f, 1f);
                shapeRenderer.rect(barX, armY, BAR_WIDTH * armRatio, BAR_H_ARM);
            }
        }

        shapeRenderer.end();
    }

    // -------------------------------------------------------------------------
    //  Indikátor aktívneho hráča
    // -------------------------------------------------------------------------

    private void renderPlayerIndicator(PlayerCharacter player) {
        float hitboxTop = player.getPosition().getY() + player.getHitbox().height;
        float margin = 6f;
        float triH   = 10f;
        float triW   = 12f;
        float cx     = player.getPosition().getX() + player.getHitbox().width / 2f;
        float top    = hitboxTop + margin + triH;
        float tip    = hitboxTop + margin;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.triangle(
            cx - triW / 2f, top,
            cx + triW / 2f, top,
            cx,             tip
        );
        shapeRenderer.end();
    }

    // -------------------------------------------------------------------------
    //  Debug hitboxy
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    //  Verejné API
    // -------------------------------------------------------------------------

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
