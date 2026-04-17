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
 * Zostavuje RenderSnapshot zo živých model objektov.
 *
 * Toto je CONTROLLER trieda – jediné miesto kde sa model
 * konvertuje na DTO pre view. Patrí do Controller vrstvy
 * (mohla by byť v balíku sk.stuba.fiit.core alebo sk.stuba.fiit.render).
 *
 * Prečo samostatná trieda a nie kód priamo v PlayingState?
 *  - PlayingState má dosť zodpovedností (stavový automat)
 *  - SnapshotBuilder sa dá testovať izolovane
 *  - GameOverDelayState potrebuje rovnakú logiku →
 *    namiesto duplikácie obaja zavolajú SnapshotBuilder.build()
 *
 * Pravidlo: táto trieda SMIE importovať model triedy.
 * GameRenderer už NIE.
 */
public class SnapshotBuilder {

    /**
     * Zostaví RenderSnapshot z aktuálneho stavu levelu.
     *
     * @param player          aktívna hráčska postava (môže byť null)
     * @param level           aktuálny level (môže byť null)
     * @param debugHitboxes   F1 flag
     * @param nearbyItem      true = hint "[E] PICK-UP"
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
            // Fallback: projektil bez animácie – vrátime prázdny DTO
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
            .build();
    }
}
