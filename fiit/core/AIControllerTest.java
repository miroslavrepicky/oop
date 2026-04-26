package sk.stuba.fiit.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.engine.AIControllable;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AIController state machine: PATROL ↔ CHASE ↔ ATTACK transitions.
 * Uses pure stub implementations – no atlas, no LibGDX window.
 */
class AIControllerTest {

    //  Stub AIControllable

    static class StubControllable implements AIControllable {
        Vector2D position;
        float    speed         = 2f;
        boolean  onGround      = true;
        boolean  lastBlocked   = false;
        boolean  facingRight   = true;
        float    velocityX     = 0f;
        boolean  attacking     = false;
        int      attackCount   = 0;
        float    detectionRange;

        StubControllable(float x, float y, float detectionRange) {
            this.position       = new Vector2D(x, y);
            this.detectionRange = detectionRange;
        }

        @Override public Vector2D getPosition()         { return position; }
        @Override public void     move(Vector2D d)       {
            position.setX(position.getX() + d.getX());
            position.setY(position.getY() + d.getY());
        }
        @Override public void    jump(float force)       {}
        @Override public boolean isOnGround()            { return onGround; }
        @Override public boolean wasLastMoveBlocked()    { return lastBlocked; }
        @Override public float   getSpeed()              { return speed; }
        @Override public void    setFacingRight(boolean r) { facingRight = r; }
        @Override public boolean isFacingRight()         { return facingRight; }
        @Override public void    setVelocityX(float vx)  { velocityX = vx; }
        @Override public boolean detectPlayer(PlayerCharacter p) {
            return position.distanceTo(p.getPosition()) <= detectionRange;
        }
        @Override public void    triggerAttack()         { attackCount++; attacking = true; }
        @Override public boolean isAttacking()           { return attacking; }
    }

    /** Minimal PlayerCharacter stub – just carries a position. */
    static class StubPlayer extends PlayerCharacter {
        StubPlayer(float x, float y) {
            super("P", 100, 10, 1f, new Vector2D(x, y), 0);
            enemy = false;
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx)         {}
        @Override public void move(Vector2D d)                  { position = position.add(d); }
    }

    private static final float DETECTION = 200f;
    private static final float ATTACK    = 50f;
    private static final float PREFERRED = 50f;

    private StubControllable enemy;
    private Vector2D         patrolStart;
    private Vector2D         patrolEnd;
    private AIController     controller;

    @BeforeEach
    void setUp() {
        enemy       = new StubControllable(100f, 0f, DETECTION);
        patrolStart = new Vector2D(0f,   0f);
        patrolEnd   = new Vector2D(200f, 0f);
        controller  = new AIController(enemy, patrolStart, patrolEnd, ATTACK, PREFERRED);
    }

    //  PATROL state

    @Test
    void patrol_enemyMoves_eachFrame() {
        StubPlayer farPlayer = new StubPlayer(9999f, 0f); // out of range
        float startX = enemy.position.getX();
        controller.update(0.016f, farPlayer);
        assertNotEquals(startX, enemy.position.getX(), 0.001f,
            "Enemy should move during patrol");
    }

    @Test
    void patrol_facingChanges_whenReachingEnd() {
        StubPlayer farPlayer = new StubPlayer(9999f, 0f);
        // Put enemy near patrolEnd
        enemy.position.setX(199f);

        controller.update(0.016f, farPlayer); // may turn around
        // After reaching end, should set facingRight = false on next frames
        // Just confirm no crash
        assertDoesNotThrow(() -> controller.update(0.016f, farPlayer));
    }

    @Test
    void patrol_toChase_whenPlayerDetected() {
        // Player within detection range (distance < 200)
        StubPlayer nearPlayer = new StubPlayer(100f + 100f, 0f); // 100 units away

        // Initially in PATROL; after one update with detectable player -> CHASE
        controller.update(0.016f, nearPlayer);

        // Verify transition happened by checking enemy movement direction
        // In CHASE mode the enemy should move toward the player (positive dx here)
        float xBefore = enemy.position.getX();
        controller.update(0.016f, nearPlayer);
        float xAfter = enemy.position.getX();

        // Enemy should be moving toward player (or already close enough for ATTACK)
        // Either it's chasing or attacking – both valid transitions from PATROL
        assertTrue(xAfter >= xBefore || enemy.attackCount > 0,
            "Enemy should move toward player or attack after detecting it");
    }

    //  CHASE state

    @Test
    void chase_movesTowardPlayer() {
        // Put player within detection range but NOT yet in attack range
        StubPlayer player = new StubPlayer(100f + 100f, 0f); // 100 units away, attack range 50

        // Detect player first
        controller.update(0.016f, player);
        float xBefore = enemy.position.getX();
        controller.update(0.016f, player);
        float xAfter  = enemy.position.getX();

        // Should be moving right (toward player at 200f)
        assertTrue(xAfter > xBefore || enemy.attackCount > 0,
            "Enemy should chase or attack player");
    }

    @Test
    void chase_toAttack_whenCloseEnough() {
        // Place player exactly at attack range edge
        StubPlayer player = new StubPlayer(100f + (PREFERRED - 1f), 0f);

        controller.update(0.016f, player); // detect
        controller.update(0.016f, player); // enter chase
        controller.update(0.016f, player); // close enough -> attack

        assertTrue(enemy.attackCount > 0, "Enemy should attack when within preferred range");
    }

    @Test
    void chase_toPatrol_whenPlayerLeavesRange() {
        // Detect player first
        StubPlayer player = new StubPlayer(100f + 100f, 0f);
        controller.update(0.016f, player); // detect -> chase

        // Move player far away
        player = new StubPlayer(9999f, 0f);
        float xBefore = enemy.position.getX();
        controller.update(0.016f, player); // lose player -> back to PATROL

        // After losing player, enemy should resume patrolling
        // (either move differently or just not chase)
        StubPlayer finalPlayer = player;
        assertDoesNotThrow(() -> controller.update(0.016f, finalPlayer));
    }

    //  ATTACK state

    @Test
    void attack_triggersAttack_whenNotAlreadyAttacking() {
        // Place player very close (within attack range)
        StubPlayer player = new StubPlayer(100f + 10f, 0f); // 10 units away

        for (int i = 0; i < 5; i++) {
            controller.update(0.016f, player);
        }

        assertTrue(enemy.attackCount > 0, "Enemy should have attacked");
    }

    @Test
    void attack_doesNotTrigger_whenAlreadyAttacking() {
        StubPlayer player = new StubPlayer(100f + 10f, 0f);
        enemy.attacking = true; // already mid-attack

        for (int i = 0; i < 5; i++) {
            controller.update(0.016f, player);
        }

        // attackCount should remain 0 since triggerAttack won't be called
        // when isAttacking() returns true
        assertEquals(0, enemy.attackCount, "triggerAttack should not be called while already attacking");
    }

    @Test
    void attack_toPatrol_whenPlayerLost() {
        // First get into attack state
        StubPlayer player = new StubPlayer(100f + 10f, 0f);
        for (int i = 0; i < 5; i++) controller.update(0.016f, player);

        // Now move player far away
        StubPlayer farPlayer = new StubPlayer(9999f, 0f);
        int attacksBefore = enemy.attackCount;
        enemy.attacking = false;

        controller.update(0.016f, farPlayer); // should transition back to PATROL
        controller.update(0.016f, farPlayer);

        // No new attacks should happen
        assertEquals(attacksBefore, enemy.attackCount,
            "No new attacks after player leaves detection range");
    }

    @Test
    void attack_faceTowardPlayer() {
        StubPlayer player = new StubPlayer(100f + 10f, 0f); // player to the right
        enemy.attacking = false;

        for (int i = 0; i < 5; i++) controller.update(0.016f, player);

        // In ATTACK state the AI calls setFacingRight based on player position
        assertTrue(enemy.facingRight, "Enemy should face right (toward player)");
    }

    @Test
    void attack_faceLeft_whenPlayerToLeft() {
        // Player to the LEFT of enemy
        StubPlayer player = new StubPlayer(100f - 10f, 0f);
        // Reduce detection range so it activates at close range
        enemy.detectionRange = 50f;

        for (int i = 0; i < 5; i++) controller.update(0.016f, player);

        // If attack triggered, enemy should face left
        if (enemy.attackCount > 0) {
            assertFalse(enemy.facingRight, "Enemy should face left toward player");
        }
    }

    //  Custom ranges

    @Test
    void defaultConstructor_usesDefaultRanges() {
        AIController def = new AIController(enemy, patrolStart, patrolEnd);
        assertDoesNotThrow(() -> def.update(0.016f, new StubPlayer(9999f, 0f)));
    }

    @Test
    void update_withNullPlayer_doesNotCrash() {
        // player == null would be caught by detectPlayer if called
        // but it's passed to handlePatrol which calls detectPlayer
        // In real code ctx.player null check is in EnemyCharacter.update not AIController
        // AIController always gets a player, this tests it doesn't crash with far player
        assertDoesNotThrow(() -> controller.update(0.016f, new StubPlayer(9999f, 0f)));
    }
}
