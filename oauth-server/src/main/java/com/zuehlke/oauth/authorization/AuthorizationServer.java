package com.zuehlke.oauth.authorization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

/**
 * Manages the list of client credentials. 
 */
@Singleton
public class AuthorizationServer {
    private final static long TOKEN_EXPIRATION_TIME_MS = 60 * 1000;
    
    /** Map ClientId -> secret */
    private final Map<String, String> clients = new HashMap<>();
    /** Map bearer token -> authorization */
    private final Map<String, Token> authTokens = new ConcurrentHashMap<>(); 
    
    public AuthorizationServer() {
        /* register dummy users */
        clients.put("abcd", "BLOBBER_CRED");
    }
    
    public AuthorizationResponse isValidClient(String id, String secret) {
        if(!clients.containsKey(id)) return INVALID_CLIENT_ID_RESPONSE;
        if(!clients.get(id).equals(secret)) return INVALID_CREDENTIAL_RESPONSE;
        
        return OK_RESPONSE;
    }
    
    public Token getTokenInformation(String tokenId) {
        return authTokens.get(tokenId);
    }
    
    public void registerToken(String token, String clientId, Set<String> scopes) {
        authTokens.put(token, new Token(clientId, scopes, System.currentTimeMillis()));
    }
    
    public boolean isValidToken(String tokenId) {
        if(!authTokens.containsKey(tokenId))
            return false;
        
        Token token = authTokens.get(tokenId);
        if(System.currentTimeMillis() - token.getIssueTime() > TOKEN_EXPIRATION_TIME_MS) {
            removeToken(tokenId);
            return false;
        }
        
        return true;
    }
    
    private void removeToken(String tokenId) {
        authTokens.remove(tokenId);
    }

    public static class Token {
        private final String clientId;
        private final Set<String> scopes;
        private final long issueTime;
        
        public Token(String id, Set<String> scopes, long time) {
            this.clientId = id;
            this.scopes = Collections.unmodifiableSet(scopes);
            this.issueTime = time;
        }
        
        public String getClientId() {
            return clientId;
        }
        
        public Set<String> getScope() {
            return scopes;
        }

        public long getIssueTime() {
            return issueTime;
        }
        
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
