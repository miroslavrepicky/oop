package sk.stuba.fiit.render;

import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;
import sk.stuba.fiit.world.MapManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts live model objects into a {@link RenderSnapshot} DTO for the renderer.
 *
 * <p>This is the Controller layer between the game model and the View ({@link GameRenderer}).
 * It is the single place where model classes are translated into render DTOs.
 *
 * <p>Why a separate class instead of inline code in {@code PlayingState}?
 * <ul>
 *   <li>{@code PlayingState} already has enough responsibilities (state machine).</li>
 *   <li>The builder can be tested in isolation.</li>
 *   <li>{@code GameOverDelayState} needs the same logic – both call
 *       {@code SnapshotBuilder.build()} instead of duplicating conversion code.</li>
 * </ul>
 *
 * <p>Rule: this class MAY import model classes.
 * {@link GameRenderer} must NOT import model classes.
 */
public class SnapshotBuilder {

    /**
     * Builds a complete render snapshot from the current level state.
     *
     * @param player        the active player character; may be {@code null}
     * @param level         the current level; may be {@code null}
     * @param debugHitboxes {@code true} if hitbox outlines should be drawn (F1 toggle)
     * @param nearbyItem    {@code true} if the pick-up hint should be shown in the HUD
     * @return an immutable {@link RenderSnapshot} ready for the renderer
     */
    public static RenderSnapshot build(PlayerCharacter player,
                                       Level level,
                                       boolean debugHitboxes,
                                       boolean nearbyItem) {
        EntityRenderData playerData = player != null ? buildPlayer(player) : null;

        List<EntityRenderData>             enemies     = new ArrayList<>();
        List<EntityRenderData>             ducks       = new ArrayList<>();
        List<RenderSnapshot.ItemRenderData> items      = new ArrayList<>();
        List<EntityRenderData>             projectiles = new ArrayList<>();
        RenderSnapshot.MapRenderData       mapData     = null;

        if (level != null) {
            for (EnemyCharacter e : level.getEnemies()) {
                enemies.add(buildEnemy(e));
            }
            for (Duck d : level.getDucks()) {
                if (d.isAlive()) ducks.add(buildDuck(d));
            }
            for (Item item : level.getItems()) {
                items.add(buildItem(item));
            }
            for (Projectile p : level.getProjectiles()) {
                if (p.isActive()) projectiles.add(buildProjectile(p));
            }

            MapManager mm = level.getMapManager();
            if (mm != null) {
                mapData = new RenderSnapshot.MapRenderData(
                    mm::render,
                    mm.getHitboxes()
                );
            }
        }

        return new RenderSnapshot(
            playerData, enemies, ducks, items, projectiles,
            mapData, debugHitboxes, nearbyItem
        );
    }

    // -------------------------------------------------------------------------
    //  Konverzia jednotlivých typov
    // -------------------------------------------------------------------------

    private static EntityRenderData buildPlayer(PlayerCharacter p) {
        AnimationManager am = p.getAnimationManager();
        String currentAnim  = am != null ? am.getCurrentAnimation() : null;
        // Animáciu updateuje GameRenderer – tu len predáme AnimationManager
        return EntityRenderData
            .builder(p.getPosition().getX(), p.getPosition().getY(), am)
            .hitbox(p.getHitbox().width, p.getHitbox().height)
            .flipX(!p.isFacingRight())
            .attacking("attack".equals(currentAnim))
            .build();
    }

    private static EntityRenderData buildEnemy(EnemyCharacter e) {
        AnimationManager am = e.getAnimationManager();
        String currentAnim  = am != null ? am.getCurrentAnimation() : null;
        return EntityRenderData
            .builder(e.getPosition().getX(), e.getPosition().getY(), am)
            .hitbox(e.getHitbox().width, e.getHitbox().height)
            .flipX(!e.isFacingRight())
            .attacking("attack".equals(currentAnim))
            .bars(e.getHp(), e.getMaxHp(), e.getArmor(), e.getMaxArmor())
            .build();
    }

    private static EntityRenderData buildDuck(Duck d) {
        AnimationManager am = d.getAnimationManager();
        return EntityRenderData
            .builder(d.getPosition().getX(), d.getPosition().getY(), am)
            .hitbox(d.getHitbox().width, d.getHitbox().height)
            .flipX(!d.isFacingRight())
            .build();
    }

    private static RenderSnapshot.ItemRenderData buildItem(Item item) {
        return new RenderSnapshot.ItemRenderData(
            item.getPosition().getX(),
            item.getPosition().getY(),
            item.getIconPath()
        );
    }

    private static EntityRenderData buildProjectile(Projectile p) {
        if (!(p instanceof Renderable)) {
            return EntityRenderData
                .builder(p.getPosition().getX(), p.getPosition().getY(), null)
                .renderType(EntityRenderData.RenderType.FIXED_RECT)
                .renderSize(16, 8)
                .build();
        }

        Renderable r = (Renderable) p;
        return EntityRenderData
            .builder(p.getPosition().getX(), p.getPosition().getY(), r.getAnimationManager())
            .renderType(EntityRenderData.RenderType.FIXED_RECT)
            .renderSize(r.getRenderWidth(), r.getRenderHeight())
            .renderOffset(r.getRenderOffsetX(), r.getRenderOffsetY())
            .flipX(r.isFlippedX())
            .tint(p.getTintR(), p.getTintG(), p.getTintB())
            .build();
    }
}
