package sk.stuba.fiit.core;
import com.badlogic.gdx.Game;
import sk.stuba.fiit.ui.MainMenuScreen;

public class ShadowQuest extends Game {

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        AtlasCache.getInstance().dispose();
        super.dispose();
    }
}
