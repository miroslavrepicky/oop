package sk.stuba.fiit.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import sk.stuba.fiit.core.AnimationManager;

/**
 * Kreslí animované sprite-y na obrazovku.
 *
 * Zodpovednosť tejto triedy je výlučne VIEW:
 *  - zoberie aktuálny frame z {@link AnimationManager}
 *  - nakreslí ho na správnu pozíciu cez {@link SpriteBatch}
 *
 * Trieda neobsahuje žiadnu herná logiku ani stav animácie –
 * to je výlučne zodpovednosť {@link AnimationManager}.
 *
 * Použitie:
 * <pre>
 *   // v GameRenderer (jeden zdieľaný renderer pre všetky objekty):
 *   AnimationRenderer.render(batch, enemy.getAnimationManager(), x, y, w, h, flipX);
 *   AnimationRenderer.renderActualSize(batch, player.getAnimationManager(), x, y, hitboxW, flipX);
 * </pre>
 *
 * Všetky metódy sú statické – renderer nemá žiadny vlastný stav,
 * čo zabraňuje neúmyselnému zdieľaniu stavu medzi objektmi.
 */
public final class AnimationRenderer {

    private AnimationRenderer() {
        // utility trieda – žiadne inštancie
    }

    // -------------------------------------------------------------------------
    //  Render do pevne zadaného obdĺžnika
    // -------------------------------------------------------------------------

    /**
     * Nakreslí aktuálny frame animácie do pevne zadaného obdĺžnika (x, y, width, height).
     * Frame sa roztiahne/zmenší tak, aby obdĺžnik vyplnil.
     *
     * Vhodné pre projektily a objekty s konštantnou vizuálnou veľkosťou.
     *
     * @param batch  aktívny SpriteBatch (musí byť medzi begin()/end())
     * @param am     AnimationManager objektu
     * @param x      ľavý okraj v herných súradniciach
     * @param y      spodný okraj v herných súradniciach
     * @param width  šírka výsledného sprite-u
     * @param height výška výsledného sprite-u
     * @param flipX  true = zrkadlovo otočiť horizontálne (postava ide doľava)
     */
    public static void render(SpriteBatch batch, AnimationManager am,
                              float x, float y, float width, float height,
                              boolean flipX) {
        if (am == null) return;
        TextureAtlas.AtlasRegion frame = am.getCurrentFrame();
        if (frame == null) return;

        batch.draw(
            frame,
            flipX ? x + width : x, y,
            flipX ? -width : width,
            height
        );
    }

    // -------------------------------------------------------------------------
    //  Render v skutočnej veľkosti framu, ukotvený na spodný stred hitboxu
    // -------------------------------------------------------------------------

    /**
     * Nakreslí aktuálny frame v SKUTOČNEJ veľkosti tohto konkrétneho framu
     * (packedWidth / packedHeight), ukotvený na spodný stred hitboxu.
     *
     * Každý frame má svoju prirodzenú veľkosť → animácia sa neroztahuje
     * do pevného obdĺžnika. Vlniaci sa plášť, pohyb hore-dole atď. vyzerajú
     * správne, pretože sprite jednoducho „vyčnieva" mimo hitbox podľa potreby.
     *
     * @param batch   aktívny SpriteBatch
     * @param am      AnimationManager objektu
     * @param x       ľavý okraj hitboxu vo svete
     * @param y       spodný okraj hitboxu vo svete
     * @param hitboxW šírka hitboxu – použitá na horizontálne centrovanie sprite-u
     * @param flipX   true = zrkadlovo otočiť horizontálne
     */
    public static void renderActualSize(SpriteBatch batch, AnimationManager am,
                                        float x, float y, float hitboxW,
                                        boolean flipX) {
        renderActualSize(batch, am, x, y, hitboxW, flipX, false);
    }

    /**
     * Rozšírená verzia s možnosťou otočenia strany kotvenia.
     *
     * @param anchorOpposite ak {@code true}, sprite sa ukotvuje na opačnú stranu hitboxu.
     *                       Používa sa pri útočných animáciách kde sprite „vyčnieva" dopredu.
     */
    public static void renderActualSize(SpriteBatch batch, AnimationManager am,
                                        float x, float y, float hitboxW,
                                        boolean flipX, boolean anchorOpposite) {
        if (am == null) return;
        TextureAtlas.AtlasRegion frame = am.getCurrentFrame();
        if (frame == null) return;

        float frameW = frame.packedWidth;
        float frameH = frame.packedHeight;

        float drawX;
        if (!flipX) {
            drawX = anchorOpposite
                ? x
                : x + hitboxW - frameW;
        } else {
            drawX = anchorOpposite
                ? x + hitboxW - frameW
                : x;
        }

        batch.draw(
            frame,
            flipX ? drawX + frameW : drawX, y,
            flipX ? -frameW : frameW,
            frameH
        );
    }
}
