package sk.stuba.fiit.core;
import com.badlogic.gdx.Game;

public class ShadowQuest extends Game {

    @Override
    public void create() {
        GameManager.getInstance().initGame();
        setScreen(new GameScreen());
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
