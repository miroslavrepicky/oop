package sk.stuba.fiit.core;

import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;

public class AIController {
    private EnemyCharacter enemy;
    private AIState state;
    private Vector2D patrolStart;
    private Vector2D patrolEnd;
    private boolean patrollingRight;
    private static final float ATTACK_RANGE = 50f;

    public AIController(EnemyCharacter enemy, Vector2D patrolStart, Vector2D patrolEnd) {
        this.enemy = enemy;
        this.patrolStart = patrolStart;
        this.patrolEnd = patrolEnd;
        this.state = AIState.PATROL;
        this.patrollingRight = true;
    }

    public void update(float deltaTime, PlayerCharacter player) {
        switch (state) {
            case PATROL: handlePatrol(deltaTime, player); break;
            case CHASE:  handleChase(deltaTime, player);  break;
            case ATTACK: handleAttack(deltaTime, player); break;
        }
    }

    private void handlePatrol(float deltaTime, PlayerCharacter player) {
        float speed = enemy.getSpeed() * deltaTime * 60;
        Vector2D pos = enemy.getPosition();
        float tolerance = speed + 1f;

        if (patrollingRight) {
            enemy.move(new Vector2D(speed, 0));
            enemy.setVelocityX(speed);
            enemy.setFacingRight(true);
            if (pos.getX() >= patrolEnd.getX() - tolerance) {
                patrollingRight = false;
            }
        } else {
            enemy.move(new Vector2D(-speed, 0));
            enemy.setVelocityX(-speed);
            enemy.setFacingRight(false);
            if (pos.getX() <= patrolStart.getX() + tolerance) {
                patrollingRight = true;
            }
        }

        if (enemy.detectPlayer(player)) {
            state = AIState.CHASE;
        }
    }

    private void handleChase(float deltaTime, PlayerCharacter player) {
        Vector2D enemyPos = enemy.getPosition();
        Vector2D playerPos = player.getPosition();

        float speed = enemy.getSpeed() * deltaTime * 60;
        float dx = playerPos.getX() > enemyPos.getX() ? speed : -speed;

        enemy.move(new Vector2D(dx, 0));
        enemy.setVelocityX(dx);
        enemy.setFacingRight(dx > 0);

        if (enemyPos.distanceTo(playerPos) <= ATTACK_RANGE) {
            state = AIState.ATTACK;
        }
        if (!enemy.detectPlayer(player)) {
            float currentX = enemy.getPosition().getX();
            patrolStart = new Vector2D(currentX - 100, enemy.getPosition().getY());
            patrolEnd   = new Vector2D(currentX + 100, enemy.getPosition().getY());
            state = AIState.PATROL;
        }
    }

    private void handleAttack(float deltaTime, PlayerCharacter player) {
        Vector2D enemyPos = enemy.getPosition();
        Vector2D playerPos = player.getPosition();

        enemy.setVelocityX(0); // stojí počas útoku

        // damage + cooldown sa riešia v EnemyCharacter.performAttack(player)
        enemy.performAttack(player);

        if (enemyPos.distanceTo(playerPos) > ATTACK_RANGE) {
            state = AIState.CHASE;
        }
        if (!enemy.detectPlayer(player)) {
            state = AIState.PATROL;
        }
    }

    public AIState getState() { return state; }
}
