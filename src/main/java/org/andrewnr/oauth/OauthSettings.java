package org.andrewnr.oauth;

import java.util.HashMap;
import java.util.Map;

/**
 * Class containing static members/settings used by the application
 * 
 * @author Jeff Douglas
 */

public class OauthSettings {
    
    // Environment config keys
    public static final String ENV_HOST = "env.HOST";
    public static final String ENV_OAUTH_URL_CALLBACK = "env.OAUTH_URL_CALLBACK";
    public static final String ENV_OAUTH_CONSUMER_KEY = "env.OAUTH_CONSUMER_KEY";
    public static final String ENV_OAUTH_CONSUMER_SECRET = "env.OAUTH_CONSUMER_SECRET";

    // URLs used by the application - change to test.salesforce.com for sandboxes
    public static String HOST = "https://login.salesforce.com";
    // OAuth callback URL
    public static String URL_CALLBACK;
    // Consumer settings copied from Salesforce.com Remote Access application
    public static String CONSUMER_KEY;
    public static String CONSUMER_SECRET;

    // URLs used by the application
    public static String URL_API_LOGIN = HOST + "/services/OAuth/c/17.0";
    public static String URL_AUTHORIZATION = HOST + "/setup/secur/RemoteAccessAuthorizationPage.apexp";
    public static String URL_AUTH_ENDPOINT = HOST + "/services/Soap/u/17.0";
    public static String URL_REQUEST_TOKEN = HOST + "/_nc_external/system/security/oauth/RequestTokenHandler";
    public static String URL_ACCESS_TOKEN = HOST + "/_nc_external/system/security/oauth/AccessTokenHandler";

    
    static {
        initConfig();
    }
    
    public static void initConfig() {
        Map<String, String> configValues = new HashMap<String, String>();
        configValues.put(ENV_HOST, "https://login.salesforce.com");
        configValues.put(ENV_OAUTH_URL_CALLBACK, "https://andrewnr-heroku-oauth.herokuapp.com/callback");
        configValues.put(ENV_OAUTH_CONSUMER_KEY, "3MVG9A2kN3Bn17hszKpKA8xWHsPmGWw6gSsTwftwvAZUgrU8XqN3LjRdPwrAM5D2Mep_C9Q4LqH8Ux5BXpz3T");
        configValues.put(ENV_OAUTH_CONSUMER_SECRET, "8542237700479489045");
        
        for (String key : configValues.keySet()) {
            String envValue = System.getenv(key);
            if (envValue != null) {
                configValues.put(key, envValue);
            }
        }
        
        HOST = configValues.get(ENV_HOST);
        URL_CALLBACK = configValues.get(ENV_OAUTH_URL_CALLBACK);
        CONSUMER_KEY = configValues.get(ENV_OAUTH_CONSUMER_KEY);
        CONSUMER_SECRET = configValues.get(ENV_OAUTH_CONSUMER_SECRET);
    }
}
