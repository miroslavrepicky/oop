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

    /** Vzdialenost, od ktorej enemy zacne utocit. */
    private final float attackRange;
    /**
     * Idealna bojova vzdialenost - ranged enemy sa snazi udrzat tuto
     * vzdialenost od hraca a nebude sa k nemu dalej priblizovat.
     * Pre melee je rovnaka ako attackRange.
     */
    private final float preferredRange;

    // Default hodnoty pre spatnu kompatibilitu (melee)
    private static final float DEFAULT_ATTACK_RANGE   = 80f;
    private static final float DEFAULT_PREFERRED_RANGE = 80f;

    // -------------------------------------------------------------------------
    //  Konstruktory
    // -------------------------------------------------------------------------

    /** Spatne kompatibilny konstruktor. Sprava sa ako povodny melee AI. */
    public AIController(EnemyCharacter enemy,
                        Vector2D patrolStart, Vector2D patrolEnd) {
        this(enemy, patrolStart, patrolEnd,
            DEFAULT_ATTACK_RANGE, DEFAULT_PREFERRED_RANGE);
    }

    /**
     * Plny konstruktor.
     *
     * @param attackRange    maximalna vzdialenost, od ktorej enemy utoci
     * @param preferredRange idealna bojova vzdialenost (ranged: vacsia ako attackRange,
     *                       melee: rovnaka ako attackRange)
     */
    public AIController(EnemyCharacter enemy,
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

    // -------------------------------------------------------------------------
    //  Update
    // -------------------------------------------------------------------------

    public void update(float deltaTime, PlayerCharacter player) {
        switch (state) {
            case PATROL: handlePatrol(deltaTime, player); break;
            case CHASE:  handleChase(deltaTime, player);  break;
            case ATTACK: handleAttack(deltaTime, player); break;
        }
    }

    // -------------------------------------------------------------------------
    //  Stavy
    // -------------------------------------------------------------------------

    private void handlePatrol(float deltaTime, PlayerCharacter player) {
        float speed     = enemy.getSpeed() * deltaTime * 60;
        Vector2D pos    = enemy.getPosition();
        float tolerance = speed + 1f;

        if (patrollingRight) {
            enemy.move(new Vector2D(speed, 0));
            enemy.setVelocityX(speed);
            enemy.setFacingRight(true);
            if (enemy.wasLastMoveBlocked() || pos.getX() >= patrolEnd.getX() - tolerance) {
                patrollingRight = false;
            }
        } else {
            enemy.move(new Vector2D(-speed, 0));
            enemy.setVelocityX(-speed);
            enemy.setFacingRight(false);
            if (enemy.wasLastMoveBlocked() || pos.getX() <= patrolStart.getX() + tolerance) {
                patrollingRight = true;
            }
        }

        if (enemy.detectPlayer(player)) state = AIState.CHASE;
    }

    private void handleChase(float deltaTime, PlayerCharacter player) {
        Vector2D enemyPos  = enemy.getPosition();
        Vector2D playerPos = player.getPosition();
        double dist        = enemyPos.distanceTo(playerPos);
        float speed        = enemy.getSpeed() * deltaTime * 60;

        if (dist <= preferredRange) {
            // Sme dostatocne blizko – stojime a prejdeme do utoku.
            // (Ranged enemy sa tu zastavi, melee tiez – utok nastane hned.)
            enemy.setVelocityX(0);
            state = AIState.ATTACK;
            return;
        }

        // Stale mimo preferredRange – pohybujeme sa smerom k hracovi.
        float dx = playerPos.getX() > enemyPos.getX() ? speed : -speed;
        enemy.move(new Vector2D(dx, 0));
        enemy.setVelocityX(dx);
        enemy.setFacingRight(dx > 0);

        if (enemy.wasLastMoveBlocked()) {
            enemy.setVelocityX(0);
            // ak je hrac v attackRange, rovnou utocit
            if (dist <= attackRange) {
                state = AIState.ATTACK;
            }
            return;
        }

        if (!enemy.detectPlayer(player)) {
            // Hrac sa stratil z dosahu detekcie – obnovime hliadku v okoli.
            float currentX  = enemy.getPosition().getX();
            patrolStart     = new Vector2D(currentX - 100, enemy.getPosition().getY());
            patrolEnd       = new Vector2D(currentX + 100, enemy.getPosition().getY());
            state = AIState.PATROL;
        }
    }

    private void handleAttack(float deltaTime, PlayerCharacter player) {
        Vector2D enemyPos  = enemy.getPosition();
        Vector2D playerPos = player.getPosition();
        double dist        = enemyPos.distanceTo(playerPos);

        // Otocime sa k hracovi aj pocas utoku.
        enemy.setFacingRight(playerPos.getX() > enemyPos.getX());
        enemy.setVelocityX(0);

        enemy.performAttack(player);

        // Hrac je prilis daleko od attackRange – nahaname ho znova.
        if (dist > attackRange) state = AIState.CHASE;

        // Hrac uplne vypadol z detekcie – spat na hliadku.
        if (!enemy.detectPlayer(player)) state = AIState.PATROL;
    }

    public AIState getState() { return state; }
}
