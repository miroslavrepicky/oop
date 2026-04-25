package sk.stuba.fiit;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

/**
 * Base class for tests that need LibGDX (Gdx.files, AtlasCache, AnimationManager...).
 * Uses the headless backend – no window is created.
 */
public abstract class GdxTest {

    private static HeadlessApplication app;

    @BeforeAll
    static void initGdx() {
        if (app == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            app = new HeadlessApplication(new ApplicationAdapter() {}, config);
            Gdx.gl   = Mockito.mock(GL20.class);
            Gdx.gl20 = Mockito.mock(GL20.class);
        }
    }
}
