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
 * <h2>Architectural constraints (clean MVC)</h2>
 * <ol>
 *   <li>Zero imports from model packages ({@code characters}, {@code items},
 *       {@code projectiles}, {@code world}, {@code inventory}).</li>
 *   <li>Map rendered via {@code MapRenderData.renderCallback} – a lambda supplied
 *       by the controller. The renderer has no knowledge of {@code MapManager}.</li>
 *   <li>Items rendered via {@code ItemRenderData.iconPath} (a plain String).</li>
 *   <li>Projectiles rendered via {@code EntityRenderData} with type
 *       {@code FIXED_RECT} – no {@code instanceof} checks, no {@code Renderable} imports.</li>
 *   <li>HP/Armour bars driven by {@code int} primitives in {@code EntityRenderData}.</li>
 * </ol>
 *
 * <h2>Render order per frame</h2>
 * <ol>
 *   <li>Clear screen.</li>
 *   <li>Render tiled map.</li>
 *   <li>Render fallback shapes for entities without an animation manager.</li>
 *   <li>Render animated sprites (player, enemies, ducks, items, projectiles).</li>
 *   <li>Render HP/armour bars above enemies.</li>
 *   <li>Render the cyan triangle indicator above the active player.</li>
 *   <li>Optionally render debug hitbox outlines (toggled by {@link #toggleDebugHitboxes()}).</li>
 *   <li>Render the HUD overlay.</li>
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

    /**
     * Creates all rendering resources. Must be called on the LibGDX render thread
     * after the OpenGL context is ready.
     */
    public GameRenderer() {
        camera        = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        hudRenderer   = new HUDRenderer(batch);
        itemIconRenderer = new ItemIconRenderer();
    }

    // -------------------------------------------------------------------------
    //  Main entry point
    // -------------------------------------------------------------------------

    /**
     * Renders one complete frame from the provided snapshot.
     *
     * <p>The camera is centred on the player's position (if available) before
     * any rendering begins. All sub-renderers share the same {@code camera.combined}
     * projection matrix.
     *
     * @param snapshot immutable DTO describing the scene; must not be {@code null}
     * @param deltaTime time elapsed since the last frame in seconds; forwarded to
     *                  sub-renderers that drive their own animation timers
     */
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

        hudRenderer.render(snapshot.hud);
    }

    // -------------------------------------------------------------------------
    //  Render steps
    // -------------------------------------------------------------------------

    /**
     * Clears the colour buffer with a dark background colour.
     */
    private void clearScreen() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Delegates map rendering to the callback stored in the snapshot.
     * Has no effect when no map is loaded ({@code snapshot.map == null}).
     *
     * @param snapshot the current render snapshot
     */
    private void renderMap(RenderSnapshot snapshot) {
        if (snapshot.map != null) {
            snapshot.map.renderCallback.render(camera);
        }
    }

    /**
     * Draws plain coloured rectangles for entities that have no
     * {@link AnimationManager} (e.g. placeholder enemies during development).
     * Enemies without an animation manager are drawn in red; the player in green.
     *
     * @param snapshot the current render snapshot
     */
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

    /**
     * Draws all animated entities: player, enemies, ducks, ground items,
     * and projectiles. Uses the world-space camera projection.
     *
     * @param snapshot  the current render snapshot
     * @param deltaTime time elapsed since the last frame in seconds
     */
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

    /**
     * Draws a single character entity at its actual (native) frame size.
     * During attack animations the sprite is anchored to the opposite edge of
     * the hitbox so the weapon extends outward correctly.
     *
     * @param data      render descriptor for this entity
     * @param deltaTime unused here; present for future per-entity updates
     * @param isPlayer  reserved for player-specific rendering adjustments
     */
    private void renderEntity(EntityRenderData data, float deltaTime, boolean isPlayer) {
        AnimationManager am = data.animationManager;
        if (am == null) return;
        boolean anchorOpposite = "attack".equals(am.getCurrentAnimation());
        renderEntityActualSize(am, data.x, data.y, data.hitboxWidth, data.flipX, anchorOpposite);
    }

    /**
     * Delegates to {@link AnimationRenderer#renderActualSize} with the supplied parameters.
     *
     * @param am       animation manager providing the current frame
     * @param x        hitbox left edge in world coordinates
     * @param y        hitbox bottom edge in world coordinates
     * @param hitboxW  hitbox width used to compute sprite alignment
     * @param flipX    {@code true} to mirror the sprite horizontally
     * @param anchorOpposite {@code true} to anchor the sprite on the side opposite
     *                       to the character's facing direction (used for attacks)
     */
    private void renderEntityActualSize(AnimationManager am, float x, float y,
                                        float hitboxW, boolean flipX, boolean anchorOpposite) {
        if (am == null) return;
        AnimationRenderer.renderActualSize(batch, am, x, y, hitboxW, flipX, anchorOpposite);
    }

    /**
     * Draws a single projectile, applying its RGB tint before drawing and
     * resetting the batch colour to white afterwards so subsequent sprites
     * are unaffected.
     *
     * @param proj render descriptor for this projectile
     */
    private void renderProjectile(EntityRenderData proj) {
        if (proj.animationManager == null) return;

        batch.setColor(proj.tintR, proj.tintG, proj.tintB, 1f);
        AnimationRenderer.render(
            batch, proj.animationManager,
            proj.x + proj.renderOffsetX,
            proj.y + proj.renderOffsetY,
            proj.renderWidth,
            proj.renderHeight,
            proj.flipX
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // -------------------------------------------------------------------------
    //  HP / Armour bars for enemies
    // -------------------------------------------------------------------------

    /**
     * Draws HP and (optionally) armour bars above each enemy.
     * The HP bar colour transitions from green (above 50 %) to orange (25–50 %)
     * to red (below 25 %). Enemies with {@code maxHp == 0} are skipped.
     *
     * @param snapshot the current render snapshot
     */
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
    //  Active player indicator
    // -------------------------------------------------------------------------

    /**
     * Draws a small downward-pointing cyan triangle above the active player to
     * make them easy to identify at a glance.
     *
     * @param player render data for the currently controlled character
     */
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
    //  Debug hitboxes (F1 toggle)
    // -------------------------------------------------------------------------

    /**
     * Draws wireframe outlines for all entity and map hitboxes.
     * Colour coding: green = player, red = enemies, yellow = items,
     * cyan = projectiles, orange = ducks, white = map walls.
     *
     * @param snapshot the current render snapshot
     */
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
    //  Public API
    // -------------------------------------------------------------------------

    /**
     * Updates the camera viewport to match the new window dimensions.
     * Called by {@code GameScreen.resize()}.
     *
     * @param width  new window width in pixels
     * @param height new window height in pixels
     */
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    /**
     * Toggles the debug hitbox overlay on or off.
     * Bound to the {@code F1} key in {@code GameScreen}.
     */
    public void toggleDebugHitboxes() {
        debugHitboxes = !debugHitboxes;
    }

    /**
     * Returns {@code true} when debug hitbox outlines are currently visible.
     * Forwarded to {@link sk.stuba.fiit.render.SnapshotBuilder} so it can
     * include the flag in the snapshot.
     *
     * @return {@code true} if hitboxes are being drawn
     */
    public boolean isDebugHitboxes() {
        return debugHitboxes;
    }

    /**
     * Releases all LibGDX rendering resources owned by this renderer.
     * Must be called from {@code GameScreen.dispose()}.
     */
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        hudRenderer.dispose();
        itemIconRenderer.dispose();
    }
}
