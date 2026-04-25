package sk.stuba.fiit.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AIStateTest {

    @Test
    void enumValues_present() {
        AIState[] values = AIState.values();
        assertEquals(3, values.length);
    }

    @Test
    void valueOf_patrol() {
        assertEquals(AIState.PATROL, AIState.valueOf("PATROL"));
    }

    @Test
    void valueOf_chase() {
        assertEquals(AIState.CHASE, AIState.valueOf("CHASE"));
    }

    @Test
    void valueOf_attack() {
        assertEquals(AIState.ATTACK, AIState.valueOf("ATTACK"));
    }
}
