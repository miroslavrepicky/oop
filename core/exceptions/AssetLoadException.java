package sk.stuba.fiit.core.exceptions;


/**
 * Thrown when a required asset (texture atlas, map file, animation) cannot be loaded.
 */
public class AssetLoadException extends ShadowQuestException {
    private final String assetPath;

    public AssetLoadException(String assetPath) {
        super("Failed to load asset: " + assetPath);
        this.assetPath = assetPath;
    }

    public AssetLoadException(String assetPath, Throwable cause) {
        super("Failed to load asset: " + assetPath, cause);
        this.assetPath = assetPath;
    }
    /** @return the path of the asset that could not be loaded */
    public String getAssetPath() { return assetPath; }
}
