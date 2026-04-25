package sk.stuba.fiit;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

/**
 * Base class for tests that need LibGDX stubs (Gdx.files, GL20, etc.)
 * without loading native libraries (no gdx64.dll required).
 * Uses Mockito mocks for all Gdx singletons so tests run on any OS/CI.
 */
public abstract class GdxTest {

    private static boolean initialized = false;

    @BeforeAll
    static void initGdx() {
        if (initialized) return;
        initialized = true;

        Gdx.gl   = Mockito.mock(GL20.class);
        Gdx.gl20 = Mockito.mock(GL20.class);

        Application mockApp = Mockito.mock(Application.class);
        // log calls just print nothing
        Mockito.doNothing().when(mockApp).log(Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mockApp).error(Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mockApp).debug(Mockito.anyString(), Mockito.anyString());
        Gdx.app = mockApp;

        // Mock Gdx.files so AtlasCache / AnimationManager don't NPE when
        // tests that don't actually load atlases call code paths touching Gdx.files.
        // Tests that DO load atlases (e.g. AttackExecuteTest) must supply real files
        // or mock the relevant collaborators instead.
        Gdx.files = Mockito.mock(Files.class);
        Gdx.graphics = Mockito.mock(Graphics.class);
    }
}
