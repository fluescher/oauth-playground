package com.zuehlke.oauth.authorization;

import java.util.Collections;
import java.util.Set;

public interface AuthorizationServer {

    public abstract AuthorizationResponse isValidClient(String id, String secret);

    public abstract Token getTokenInformation(String tokenId);

    public abstract void registerToken(String token, String clientId, Set<String> scopes);

    public abstract boolean isValidToken(String tokenId);
    
    public abstract boolean isValidToken(String tokenId, String[] scopes);
    
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
}