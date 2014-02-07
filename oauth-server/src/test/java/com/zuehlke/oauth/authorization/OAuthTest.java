package com.zuehlke.oauth.authorization;

import java.net.URL;

import javax.ws.rs.ApplicationPath;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.jboss.arquillian.test.api.ArquillianResource;

import com.zuehlke.oauth.OAuthApplication;


public class OAuthTest {
    protected static final String RESOURCE_PREFIX = OAuthApplication.class.getAnnotation(ApplicationPath.class).value().substring(1);
    
    @ArquillianResource URL deploymentUrl;
    
    protected String getAccessToken() throws OAuthSystemException, OAuthProblemException {
        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(path("auth/token"))
                .setGrantType(GrantType.CLIENT_CREDENTIALS)
                .setClientId("abcd")
                .setClientSecret("BLOBBER_CRED")
                .buildBodyMessage();
        OAuthClient client = new OAuthClient(new URLConnectionClient());
        OAuthJSONAccessTokenResponse response = client.accessToken(request);
        String token = response.getAccessToken();
        return token;
    }
    
    protected String path(String path) {
        return deploymentUrl.toString() + RESOURCE_PREFIX +"/"+ path;
    }
}
