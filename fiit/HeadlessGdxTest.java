package sk.stuba.fiit;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

/**
 * Base class for tests that need a real LibGDX context (atlas loading, Gdx.files, etc.).
 *
 * <p>Requires {@code gdx-backend-headless} on the test classpath.
 * In Gradle add to the test-module dependencies:
 * <pre>
 *   testImplementation "com.badlogic.gdx:gdx-backend-headless:$gdxVersion"
 *   testImplementation "com.badlogic.gdx:gdx-platform:$gdxVersion:natives-desktop"
 * </pre>
 *
 * <p>The working directory when running tests must be the folder that contains
 * the {@code atlas/} directory (typically the {@code assets} folder or wherever
 * the LibGDX desktop module is configured to resolve files).
 */
public abstract class HeadlessGdxTest {

    private static HeadlessApplication application;

    @BeforeAll
    static void initHeadlessGdx() {
        if (application != null) return;

        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = -1; // no render loop needed

        application = new HeadlessApplication(new ApplicationListener() {
            @Override public void create()             {}
            @Override public void resize(int w, int h) {}
            @Override public void render()             {}
            @Override public void pause()              {}
            @Override public void resume()             {}
            @Override public void dispose()            {}
        }, config);

        Gdx.gl20 = Mockito.mock(GL20.class);
        Gdx.gl = Gdx.gl20;
    }
}
