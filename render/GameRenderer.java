package sk.stuba.fiit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.core.AnimationManager;

/**
 * Renders the complete game scene from a {@link RenderSnapshot} DTO.
 *
 * <p>Key architectural constraints after the clean-MVC refactor:
 * <ol>
 *   <li>Zero imports from model packages ({@code characters}, {@code items},
 *       {@code projectiles}, {@code world}, {@code inventory}).</li>
 *   <li>Map rendered via {@code MapRenderData.renderCallback} – a lambda supplied
 *       by the controller. The renderer has no knowledge of {@code MapManager}.</li>
 *   <li>Items rendered via {@code ItemRenderData.iconPath} (a String primitive).</li>
 *   <li>Projectiles rendered via {@code EntityRenderData} ({@code FIXED_RECT} type)
 *       – no {@code instanceof} checks, no {@code Renderable} imports.</li>
 *   <li>HP/Armor bars driven by {@code int} primitives in {@code EntityRenderData}.</li>
 * </ol>
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
        camera        = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        hudRenderer   = new HUDRenderer(batch);
        itemIconRenderer = new ItemIconRenderer();
    }

    // -------------------------------------------------------------------------
    //  Hlavný vstupný bod
    // -------------------------------------------------------------------------

    public void render(RenderSnapshot snapshot, float deltaTime) {
        clearScreen();

        if (snapshot.player != null) {
            camera.position.x = snapshot.player.x;
            camera.position.y = snapshot.player.y;
            camera.update();
        }

        renderMap(snapshot);
        renderFallbackShapes(snapshot);
        renderSprites(snapshot, deltaTime);
        renderEnemyBars(snapshot);

        if (snapshot.player != null) {
            renderPlayerIndicator(snapshot.player);
        }

        if (snapshot.debugHitboxes) {
            renderHitboxes(snapshot);
        }

        hudRenderer.render(snapshot.nearbyItemAvailable);
    }

    // -------------------------------------------------------------------------
    //  Čiastkové kroky
    // -------------------------------------------------------------------------

    private void clearScreen() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void renderMap(RenderSnapshot snapshot) {
        if (snapshot.map != null) {
            snapshot.map.renderCallback.render(camera);
        }
    }

    /** Fallback pre objekty bez animácie (AnimationManager == null). */
    private void renderFallbackShapes(RenderSnapshot snapshot) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (EntityRenderData e : snapshot.enemies) {
            if (e.animationManager != null) continue;
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(e.x, e.y, 32, 32);
        }

        EntityRenderData p = snapshot.player;
        if (p != null && p.animationManager == null) {
            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(p.x, p.y, 32, 32);
        }

        shapeRenderer.end();
    }

    private void renderSprites(RenderSnapshot snapshot, float deltaTime) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (snapshot.player != null) {
            renderEntity(snapshot.player, deltaTime, true);
        }

        for (EntityRenderData e : snapshot.enemies) {
            renderEntity(e, deltaTime, false);
        }

        for (EntityRenderData d : snapshot.ducks) {
            renderEntityActualSize(d.animationManager, d.x, d.y, d.hitboxWidth, d.flipX, false);
        }

        itemIconRenderer.renderDTOs(batch, snapshot.items);

        for (EntityRenderData proj : snapshot.projectiles) {
            renderProjectile(proj);
        }

        batch.end();
    }

    private void renderEntity(EntityRenderData data, float deltaTime, boolean isPlayer) {
        AnimationManager am = data.animationManager;
        if (am == null) return;
        boolean anchorOpposite = "attack".equals(am.getCurrentAnimation());
        renderEntityActualSize(am, data.x, data.y, data.hitboxWidth, data.flipX, anchorOpposite);
    }

    private void renderEntityActualSize(AnimationManager am, float x, float y,
                                        float hitboxW, boolean flipX, boolean anchorOpposite) {
        if (am == null) return;
        AnimationRenderer.renderActualSize(batch, am, x, y, hitboxW, flipX, anchorOpposite);
    }

    private void renderProjectile(EntityRenderData proj) {
        if (proj.animationManager == null) return;

        batch.setColor(proj.tintR, proj.tintG, proj.tintB, 1f); // nové
        AnimationRenderer.render(
            batch, proj.animationManager,
            proj.x + proj.renderOffsetX,
            proj.y + proj.renderOffsetY,
            proj.renderWidth,
            proj.renderHeight,
            proj.flipX
        );
        batch.setColor(1f, 1f, 1f, 1f); // reset aby ostatné sprite-y neboli ovplyvnené
    }

    // -------------------------------------------------------------------------
    //  HP / Armor bary nepriateľov
    // -------------------------------------------------------------------------

    private void renderEnemyBars(RenderSnapshot snapshot) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (EntityRenderData e : snapshot.enemies) {
            if (e.maxHp <= 0) continue;

            float top  = e.y + e.hitboxHeight + BAR_GAP;
            float barX = e.x + (e.hitboxWidth - BAR_WIDTH) / 2f;

            float hpRatio = (float) e.hp / e.maxHp;
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.85f);
            shapeRenderer.rect(barX, top, BAR_WIDTH, BAR_H_HP);

            if      (hpRatio > 0.5f)  shapeRenderer.setColor(0.25f, 0.75f, 0.15f, 1f);
            else if (hpRatio > 0.25f) shapeRenderer.setColor(0.95f, 0.65f, 0.05f, 1f);
            else                      shapeRenderer.setColor(0.90f, 0.15f, 0.10f, 1f);
            shapeRenderer.rect(barX, top, BAR_WIDTH * hpRatio, BAR_H_HP);

            if (e.maxArmor > 0) {
                float armRatio = (float) e.armor / e.maxArmor;
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

    private void renderPlayerIndicator(EntityRenderData player) {
        float hitboxTop = player.y + player.hitboxHeight;
        float margin = 6f;
        float triH   = 10f;
        float triW   = 12f;
        float cx     = player.x + player.hitboxWidth / 2f;
        float top    = hitboxTop + margin + triH;
        float tip    = hitboxTop + margin;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.triangle(cx - triW / 2f, top, cx + triW / 2f, top, cx, tip);
        shapeRenderer.end();
    }

    // -------------------------------------------------------------------------
    //  Debug hitboxy
    // -------------------------------------------------------------------------

    private void renderHitboxes(RenderSnapshot snapshot) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        EntityRenderData p = snapshot.player;
        if (p != null) {
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(p.x, p.y, p.hitboxWidth, p.hitboxHeight);
        }

        shapeRenderer.setColor(Color.RED);
        for (EntityRenderData e : snapshot.enemies) {
            shapeRenderer.rect(e.x, e.y, e.hitboxWidth, e.hitboxHeight);
        }

        shapeRenderer.setColor(Color.YELLOW);
        for (RenderSnapshot.ItemRenderData item : snapshot.items) {
            shapeRenderer.rect(item.x, item.y, 32, 32);
        }

        shapeRenderer.setColor(Color.CYAN);
        for (EntityRenderData proj : snapshot.projectiles) {
            shapeRenderer.rect(proj.x, proj.y, proj.renderWidth, proj.renderHeight);
        }

        shapeRenderer.setColor(Color.ORANGE);
        for (EntityRenderData d : snapshot.ducks) {
            shapeRenderer.rect(d.x, d.y, d.hitboxWidth, d.hitboxHeight);
        }

        if (snapshot.map != null) {
            shapeRenderer.setColor(Color.WHITE);
            for (Rectangle wall : snapshot.map.hitboxes) {
                shapeRenderer.rect(wall.x, wall.y, wall.width, wall.height);
            }
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
