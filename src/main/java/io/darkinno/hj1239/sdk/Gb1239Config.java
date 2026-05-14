package io.darkinno.hj1239.sdk;

/**
 * Configuration for the GB1239 SDK.
 */
public class Gb1239Config {

    private boolean strictMode = true;
    private boolean enableValidation = true;

    public Gb1239Config() {
    }

    Gb1239Config(Gb1239Config other) {
        this.strictMode = other.strictMode;
        this.enableValidation = other.enableValidation;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public boolean isEnableValidation() {
        return enableValidation;
    }

    public void setEnableValidation(boolean enableValidation) {
        this.enableValidation = enableValidation;
    }
}
