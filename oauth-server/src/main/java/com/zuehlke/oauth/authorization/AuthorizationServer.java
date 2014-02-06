package com.zuehlke.oauth.authorization;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

/**
 * Manages the list of client credentials. 
 */
@Singleton
public class AuthorizationServer {
    /** Map ClientId -> secret */
    private final Map<String, String> clients = new HashMap<>();
    /** Map bearer token -> authorization */
    private final Map<String, Long> authTokens = new ConcurrentHashMap<>(); 
    
    public AuthorizationServer() {
        /* register dummy users */
        clients.put("abcd", "BLOBBER_CRED");
    }
    
    public AuthorizationResponse isValidClient(String id, String secret) {
        if(!clients.containsKey(id)) return INVALID_CLIENT_ID_RESPONSE;
        if(!clients.get(id).equals(secret)) return INVALID_CREDENTIAL_RESPONSE;
        
        return OK_RESPONSE;
    }
    
    public void registerToken(String token) {
        authTokens.put(token, System.currentTimeMillis());
    }
    
    public boolean isValidToken(String token) {
        return authTokens.containsKey(token);
    }
    
    public interface AuthorizationResponse {
        boolean isValidClientId();
        boolean isValidClientCredentials();
    }
    
    private final AuthorizationResponse OK_RESPONSE = new AuthorizationResponse() {
        @Override
        public boolean isValidClientId() {
            return true;
        }

        @Override
        public boolean isValidClientCredentials() {
            return true;
        }
    };
    
    private final AuthorizationResponse INVALID_CLIENT_ID_RESPONSE = new AuthorizationResponse() {
        @Override
        public boolean isValidClientId() {
            return false;
        }

        @Override
        public boolean isValidClientCredentials() {
            return false;
        }
    };
    
    private final AuthorizationResponse INVALID_CREDENTIAL_RESPONSE = new AuthorizationResponse() {
        @Override
        public boolean isValidClientId() {
            return true;
        }

        @Override
        public boolean isValidClientCredentials() {
            return false;
        }
    };
}
