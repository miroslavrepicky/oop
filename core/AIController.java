package sk.stuba.fiit.core;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.engine.AIControllable;
import sk.stuba.fiit.util.Vector2D;

/**
 * Controls AI behaviour (PATROL → CHASE → ATTACK) for any character
 * implementing {@link AIControllable}.
 *
 * <p>The controller has no knowledge of {@code EnemyCharacter}; all
 * communication is through the interface. This allows new enemy types
 * or even friendly NPCs to use the same AI without modifying this class.
 *
 * <p>State machine:
 * <ul>
 *   <li>{@code PATROL} – walks between {@code patrolStart} and {@code patrolEnd}.
 *       Detects wall blocks and attempts a jump after a threshold; reverses direction
 *       if still blocked.</li>
 *   <li>{@code CHASE}  – moves toward the player until within {@code preferredRange}
 *       or until the player leaves {@code detectionRange}.</li>
 *   <li>{@code ATTACK} – faces the player and calls {@code performAttack()} each
 *       time the enemy is not already attacking.</li>
 * </ul>
 */
public class AIController {
    private final AIControllable enemy;
    private AIState state;
    private Vector2D patrolStart;
    private Vector2D patrolEnd;
    private boolean patrollingRight;

    /** Vzdialenosť, od ktorej enemy začne útočiť. */
    private final float attackRange;
    /**
     * Ideálna bojová vzdialenosť – ranged enemy sa snaží udržať túto
     * vzdialenosť a nepribližovať sa ďalej. Pre melee == attackRange.
     */
    private final float preferredRange;

    private static final float DEFAULT_ATTACK_RANGE    = 80f;
    private static final float DEFAULT_PREFERRED_RANGE = 80f;
    private static final int   BLOCKED_THRESHOLD       = 4;

    private int blockedFrames = 0;

    private static final Logger log = GameLogger.get(AIController.class);

    // -------------------------------------------------------------------------
    //  Konštruktory
    // -------------------------------------------------------------------------

    public AIController(AIControllable enemy,
                        Vector2D patrolStart, Vector2D patrolEnd) {
        this(enemy, patrolStart, patrolEnd,
            DEFAULT_ATTACK_RANGE, DEFAULT_PREFERRED_RANGE);
    }

    /**
     * @param enemy          the AI-controlled entity
     * @param patrolStart    left boundary of the patrol route
     * @param patrolEnd      right boundary of the patrol route
     * @param attackRange    distance at which the enemy begins attacking
     * @param preferredRange ideal combat distance (ranged enemies stay at this distance)
     */
    public AIController(AIControllable enemy,
                        Vector2D patrolStart, Vector2D patrolEnd,
                        float attackRange, float preferredRange) {
        this.enemy          = enemy;
        this.patrolStart    = patrolStart;
        this.patrolEnd      = patrolEnd;
        this.state          = AIState.PATROL;
        this.patrollingRight = true;
        this.attackRange    = attackRange;
        this.preferredRange = preferredRange;
    }

    /**
     * Updates the AI state machine for one frame.
     *
     * @param deltaTime time elapsed since the last frame in seconds
     * @param player    the active player character used for detection and targeting
     */
    public void update(float deltaTime, PlayerCharacter player) {
        switch (state) {
            case PATROL: handlePatrol(deltaTime, player); break;
            case CHASE:  handleChase(deltaTime, player);  break;
            case ATTACK: handleAttack(player); break;
        }
    }

    // -------------------------------------------------------------------------
    //  Stavy
    // -------------------------------------------------------------------------

    private void handlePatrol(float deltaTime, PlayerCharacter player) {
        float speed     = enemy.getSpeed() * deltaTime * 60;
        float tolerance = speed + 1f;
        Vector2D pos    = enemy.getPosition();

        float dx = patrollingRight ? speed : -speed;
        enemy.move(new Vector2D(dx, 0));
        enemy.setFacingRight(patrollingRight);

        if (enemy.wasLastMoveBlocked()) {
            enemy.setVelocityX(0);
            blockedFrames++;
        } else {
            enemy.setVelocityX(dx);
            blockedFrames = 0;
        }

        if (blockedFrames == 5 && enemy.isOnGround()) {
            enemy.jump(50f);
        }

        if (blockedFrames >= BLOCKED_THRESHOLD) {
            patrollingRight = !patrollingRight;
            blockedFrames   = 0;
            float range     = 200f;
            patrolStart.setX(pos.getX() - (patrollingRight ? 0 : range));
            patrolEnd.setX(  pos.getX() + (patrollingRight ? range : 0));
        }

        if (!enemy.wasLastMoveBlocked()) {
            if (patrollingRight && pos.getX() >= patrolEnd.getX() - tolerance) {
                patrollingRight = false;
            } else if (!patrollingRight && pos.getX() <= patrolStart.getX() + tolerance) {
                patrollingRight = true;
            }
        }

        if (enemy.detectPlayer(player)) transitionTo(AIState.CHASE);
    }

    private void handleChase(float deltaTime, PlayerCharacter player) {
        if (!enemy.detectPlayer(player)) {
            resetPatrolAroundCurrent();
            transitionTo(AIState.PATROL);
            return;
        }

        Vector2D enemyPos  = enemy.getPosition();
        Vector2D playerPos = player.getPosition();
        double   dist      = enemyPos.distanceTo(playerPos);
        float    speed     = enemy.getSpeed() * deltaTime * 60;

        if (dist <= preferredRange) {
            enemy.setVelocityX(0);
            transitionTo(AIState.ATTACK);
            return;
        }

        float dx = playerPos.getX() > enemyPos.getX() ? speed : -speed;
        enemy.move(new Vector2D(dx, 0));
        enemy.setVelocityX(dx);
        enemy.setFacingRight(dx > 0);

        if (enemy.wasLastMoveBlocked()) {
            enemy.setVelocityX(0);
            if (dist <= attackRange) transitionTo(AIState.ATTACK);
        }
    }

    private void handleAttack(PlayerCharacter player) {
        if (!enemy.detectPlayer(player)) {
            resetPatrolAroundCurrent();
            transitionTo(AIState.PATROL);
            return;
        }

        Vector2D enemyPos  = enemy.getPosition();
        Vector2D playerPos = player.getPosition();
        double   dist      = enemyPos.distanceTo(playerPos);

        enemy.setFacingRight(playerPos.getX() > enemyPos.getX());
        enemy.setVelocityX(0);
        if (!enemy.isAttacking()) {
            enemy.triggerAttack();
        }

        if (dist > attackRange) transitionTo(AIState.CHASE);
        if (!enemy.detectPlayer(player)) transitionTo(AIState.PATROL);
    }

    // -------------------------------------------------------------------------
    //  Pomocné metódy
    // -------------------------------------------------------------------------

    private void transitionTo(AIState newState) {
        if (log.isDebugEnabled()) {
            log.debug("AI state transition: enemy={}, from={}, to={}",
                enemy.getPosition().toString(), state, newState);
        }
        state = newState;
    }

    private void resetPatrolAroundCurrent() {
        float x   = enemy.getPosition().getX();
        float y   = enemy.getPosition().getY();
        patrolStart = new Vector2D(x - 100, y);
        patrolEnd   = new Vector2D(x + 100, y);
    }
}
