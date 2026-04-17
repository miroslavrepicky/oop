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
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.Projectile;

/**
 * Riadi vykresľovanie celej hernej scény.
 *
 * Zmeny po refaktore:
 *
 * 1. ŽIADNY GameManager.getInstance() – renderer dostáva všetky dáta
 *    cez {@link RenderSnapshot}. View nepozná Model priamo.
 *
 * 2. ŽIADNE instanceof na projektily – každý projektil implementuje
 *    {@link Renderable} a sám poskytuje vizuálne parametre. Pridanie
 *    nového projektilu = žiadna zmena v tejto triede.
 *
 * 3. ŽIADNY CollisionManager – HUDRenderer dostáva boolean nearbyItemAvailable
 *    priamo zo snapshotu.
 *
 * 4. toggleDebugHitboxes() zostáva tu – je to vizuálny stav, nie herná logika.
 *    Snapshot nesie výsledný boolean (vypočítaný volajúcim).
 */
public class GameRenderer {

    private final OrthographicCamera camera;
    private final SpriteBatch        batch;
    private final ShapeRenderer      shapeRenderer;
    private final HUDRenderer        hudRenderer;
    private final ItemIconRenderer   itemIconRenderer;

    private boolean debugHitboxes = false;

    private static final float BAR_WIDTH   = 40f;
    private static final float BAR_H_HP    = 5f;
    private static final float BAR_H_ARM   = 3f;
    private static final float BAR_GAP     = 8f;
    private static final float BAR_SPACING = 2f;

    public GameRenderer() {
        camera           = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch            = new SpriteBatch();
        shapeRenderer    = new ShapeRenderer();
        hudRenderer      = new HUDRenderer(batch);
        itemIconRenderer = new ItemIconRenderer();
    }

    // -------------------------------------------------------------------------
    //  Hlavný vstupný bod – dostáva snapshot, nie GameManager
    // -------------------------------------------------------------------------

    /**
     * Nakreslí celú scénu podľa aktuálneho snapshotu.
     *
     * @param snapshot popis scény zostavený volajúcim (PlayingState / GameOverDelayState)
     * @param deltaTime čas od posledného snímka (len pre animácie)
     */
    public void render(RenderSnapshot snapshot, float deltaTime) {
        clearScreen();

        PlayerCharacter player = snapshot.player;
        updateCamera(player);

        renderMap(snapshot);
        renderFallbackShapes(snapshot);
        renderSprites(snapshot, player, deltaTime);
        renderEnemyBars(snapshot);

        if (player != null) {
            renderPlayerIndicator(player);
        }

        if (snapshot.debugHitboxes) {
            renderHitboxes(snapshot, player);
        }

        hudRenderer.render(snapshot.nearbyItemAvailable);
    }

    // -------------------------------------------------------------------------
    //  Čiastkové kroky renderovania
    // -------------------------------------------------------------------------

    private void clearScreen() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void updateCamera(PlayerCharacter player) {
        if (player != null) {
            camera.position.x = player.getPosition().getX();
            camera.position.y = player.getPosition().getY();
            camera.update();
        }
    }

    private void renderMap(RenderSnapshot snapshot) {
        if (snapshot.mapManager != null) {
            snapshot.mapManager.render(camera);
        }
    }

    /** ShapeRenderer – fallback pre objekty bez animácie. */
    private void renderFallbackShapes(RenderSnapshot snapshot) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (EnemyCharacter enemy : snapshot.enemies) {
            if (enemy.getAnimationManager() != null) continue;
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(enemy.getPosition().getX(), enemy.getPosition().getY(), 32, 32);
        }

        PlayerCharacter player = snapshot.player;
        if (player != null && player.getAnimationManager() == null) {
            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(player.getPosition().getX(), player.getPosition().getY(), 32, 32);
        }

        shapeRenderer.end();
    }

    /** SpriteBatch – všetky animované objekty. */
    private void renderSprites(RenderSnapshot snapshot, PlayerCharacter player, float deltaTime) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        renderPlayer(player, deltaTime);
        renderEnemies(snapshot, deltaTime);
        renderDucks(snapshot);
        itemIconRenderer.render(batch, snapshot.items);
        renderProjectiles(snapshot);

        batch.end();
    }

    private void renderPlayer(PlayerCharacter player, float deltaTime) {
        if (player == null) return;
        AnimationManager am = player.getAnimationManager();
        if (am == null) return;

        player.updateAnimation(deltaTime);
        boolean isAttackAnim = "attack".equals(am.getCurrentAnimation());
        AnimationRenderer.renderActualSize(
            batch, am,
            player.getPosition().getX(),
            player.getPosition().getY(),
            player.getHitbox().width,
            !player.isFacingRight(),
            isAttackAnim
        );
    }

    private void renderEnemies(RenderSnapshot snapshot, float deltaTime) {
        for (EnemyCharacter enemy : snapshot.enemies) {
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
    }

    private void renderDucks(RenderSnapshot snapshot) {
        for (Duck duck : snapshot.ducks) {
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
    }

    /**
     * Kreslí projektily cez {@link Renderable} interface.
     * Žiadne instanceof – každý projektil pozná svoje vizuálne parametre.
     */
    private void renderProjectiles(RenderSnapshot snapshot) {
        for (Projectile projectile : snapshot.projectiles) {
            if (!projectile.isActive()) continue;
            if (!(projectile instanceof Renderable)) continue;

            Renderable r = (Renderable) projectile;
            AnimationManager am = r.getAnimationManager();
            if (am == null) continue;

            AnimationRenderer.render(
                batch, am,
                projectile.getPosition().getX() + r.getRenderOffsetX(),
                projectile.getPosition().getY() + r.getRenderOffsetY(),
                r.getRenderWidth(),
                r.getRenderHeight(),
                r.isFlippedX()
            );
        }
    }

    // -------------------------------------------------------------------------
    //  HP / Armor bary nepriateľov
    // -------------------------------------------------------------------------

    private void renderEnemyBars(RenderSnapshot snapshot) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (EnemyCharacter enemy : snapshot.enemies) {
            if (!enemy.isAlive()) continue;

            float ex   = enemy.getPosition().getX();
            float ey   = enemy.getPosition().getY();
            float ew   = enemy.getHitbox().width;
            float top  = ey + enemy.getHitbox().height + BAR_GAP;
            float barX = ex + (ew - BAR_WIDTH) / 2f;

            // HP bar
            float hpRatio = (float) enemy.getHp() / enemy.getMaxHp();
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.85f);
            shapeRenderer.rect(barX, top, BAR_WIDTH, BAR_H_HP);

            if      (hpRatio > 0.5f)  shapeRenderer.setColor(0.25f, 0.75f, 0.15f, 1f);
            else if (hpRatio > 0.25f) shapeRenderer.setColor(0.95f, 0.65f, 0.05f, 1f);
            else                      shapeRenderer.setColor(0.90f, 0.15f, 0.10f, 1f);
            shapeRenderer.rect(barX, top, BAR_WIDTH * hpRatio, BAR_H_HP);

            // Armor bar
            if (enemy.getMaxArmor() > 0) {
                float armRatio = (float) enemy.getArmor() / enemy.getMaxArmor();
                float armY     = top + BAR_H_HP + BAR_SPACING;
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

    private void renderHitboxes(RenderSnapshot snapshot, PlayerCharacter player) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        if (player != null) {
            shapeRenderer.setColor(Color.GREEN);
            Rectangle hb = player.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        shapeRenderer.setColor(Color.RED);
        for (EnemyCharacter enemy : snapshot.enemies) {
            Rectangle hb = enemy.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        shapeRenderer.setColor(Color.YELLOW);
        for (Item item : snapshot.items) {
            Rectangle hb = item.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        shapeRenderer.setColor(Color.CYAN);
        for (Projectile p : snapshot.projectiles) {
            if (!p.isActive()) continue;
            Rectangle hb = p.getHitbox();
            shapeRenderer.rect(hb.x, hb.y, hb.width, hb.height);
        }

        shapeRenderer.setColor(Color.ORANGE);
        for (Duck duck : snapshot.ducks) {
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

    public void toggleDebugHitboxes() {
        debugHitboxes = !debugHitboxes;
    }

    public boolean isDebugHitboxes() {
        return debugHitboxes;
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        hudRenderer.dispose();
        itemIconRenderer.dispose();
    }
}
