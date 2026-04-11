package sk.stuba.fiit.core;
import com.badlogic.gdx.Game;

public class ShadowQuest extends Game {

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
