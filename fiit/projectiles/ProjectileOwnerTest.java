package sk.stuba.fiit.projectiles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectileOwnerTest {

    @Test
    void enumHasPlayerAndEnemy() {
        ProjectileOwner[] values = ProjectileOwner.values();
        assertEquals(2, values.length);
        assertEquals(ProjectileOwner.PLAYER, ProjectileOwner.valueOf("PLAYER"));
        assertEquals(ProjectileOwner.ENEMY,  ProjectileOwner.valueOf("ENEMY"));
    }
}
