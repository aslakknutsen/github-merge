package org.aslak.github.merge;

public class Config {

    private static final String MERGER_TEMP_STORAGE = "MERGER_TEMP_STORAGE";

    private static final String GITHUB_CLIENT_SECRET = "GITHUB_CLIENT_SECRET";
    private static final String GITHUB_CLIENT_ID = "GITHUB_CLIENT_ID";
    private static final String MERGER_CALLBACK_URL = "MERGER_CALLBACK_URL";

    public static String tempStorage() {
        return env(MERGER_TEMP_STORAGE, "/tmp/mergerer/");
    }

    public static String githubClientId() {
        return requireEnv(GITHUB_CLIENT_ID);
    }

    public static String githubClientSecret() {
        return requireEnv(GITHUB_CLIENT_SECRET);
    }

    public static String baseCallBackUrl() {
        return requireEnv(MERGER_CALLBACK_URL);
    }

    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if(value == null) {
            throw new IllegalStateException(key + " env variable is missing");
        }
        return value;
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }
}
