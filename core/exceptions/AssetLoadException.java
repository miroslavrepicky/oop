package sk.stuba.fiit.core.exceptions;

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

    public String getAssetPath() { return assetPath; }
}
