package com.zuehlke.oauth.authorization.impl;

import java.util.Arrays;
import java.util.Set;

import javax.inject.Singleton;

import com.zuehlke.oauth.authorization.AuthorizationServer;

@Singleton
public class AuthorizationServerMock implements AuthorizationServer {

    public static final String VALID_TOKEN = "1234";
    public static final String[] VALID_SCOPES = {"options"};
    
    @Override
    public AuthorizationResponse isValidClient(String id, String secret) {
        return null;
    }

    @Override
    public Token getTokenInformation(String tokenId) {
        return null;
    }

    @Override
    public void registerToken(String token, String clientId, Set<String> scopes) {
    }

    @Override
    public boolean isValidToken(String tokenId) {
        return VALID_TOKEN.equals(tokenId);
    }

    @Override
    public boolean isValidToken(String tokenId, String[] scopes) {
        return isValidToken(tokenId) && Arrays.asList(VALID_SCOPES).containsAll(Arrays.asList(scopes));
    }

}
