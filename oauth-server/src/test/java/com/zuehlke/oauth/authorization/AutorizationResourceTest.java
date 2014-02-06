package com.zuehlke.oauth.authorization;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.http.ContentType;
import com.zuehlke.oauth.OAuthApplication;

@RunWith(Arquillian.class)
@RunAsClient
public class AutorizationResourceTest {

    private static final String RESOURCE_PREFIX = OAuthApplication.class.getAnnotation(ApplicationPath.class).value().substring(1);
    
    @Deployment(testable=false)
    public static WebArchive createDeployment() {
        File[] deps = Maven.resolver()
                    .loadPomFromFile("pom.xml")
                    .importRuntimeDependencies()
                    .resolve().withTransitivity().asFile();
        
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage(AutorizationResource.class.getPackage())
                .addClass(OAuthApplication.class)
                .addAsLibraries(deps);
    }
    
    @ArquillianResource URL deploymentUrl;
    
    @Test
    public void testGetToken_ClientCredentialGrant_Valid_Credentials() {
        given()
            .param("grant_type", "client_credentials")
            .param("client_id", "abcd")
            .param("client_secret", "BLOBBER_CRED")
        .then()
        .post(path("auth/token"))
            .then()
               .statusCode(200)
               .contentType(ContentType.JSON)
               .body("expires_in", equalTo(3600));
    }
    
    @Test
    public void testGetToken_ClientCredentialGrant_In_Valid_Credentials() {
        given()
            .param("grant_type", "client_credentials")
            .param("client_id", "abcd")
            .param("client_secret", "BLOBBER_CRED_INVAL")
        .then()
        .post(path("auth/token"))
            .then()
               .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
               .contentType(ContentType.JSON)
               .body("error", equalTo("unauthorized_client"))
               .body("error_description", equalTo("INVALID CREDENTIALS"));
    }
    
    @Test
    public void testGetToken_ClientCredentialGrant_In_Valid_ClientId() {
        given()
            .param("grant_type", "client_credentials")
            .param("client_id", "abcde")
            .param("client_secret", "BLOBBER_CRED")
        .then()
        .post(path("auth/token"))
            .then()
               .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
               .contentType(ContentType.JSON)
               .body("error", equalTo("invalid_client"))
               .body("error_description", equalTo("INVALID CLIENT"));
    }
    
    @Test
    public void testGetToken_ClientCredentialGrant_In_Valid_Grant() {
        given()
            .param("grant_type", "authorization_code")
            .param("redirect_uri", "http://www.zuehlke.com")
            .param("code", "BLOBB")
            .param("client_id", "abcde")
            .param("client_secret", "BLOBBER_CRED")
        .then()
        .post(path("auth/token"))
            .then()
               .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
               .contentType(ContentType.JSON)
               .body("error", equalTo("unsupported_grant_type"))
               .body("error_description", equalTo("Only client grant is supported"));
    }
    
    @Test
    public void testGetToken_ClientCredentialGrant_In_Valid_Request() {
        given()
            .param("grant_type", "authorization_code")
            .param("code", "BLOBB")
            .param("client_id", "abcde")
            .param("client_secret", "BLOBBER_CRED")
        .then()
        .post(path("auth/token"))
            .then()
               .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
               .contentType(ContentType.JSON)
               .body("error", equalTo("invalid_request"));
    }
    
    @Test
    public void test_OLTU_Client_Valid() throws Exception {
        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(path("auth/token"))
                .setGrantType(GrantType.CLIENT_CREDENTIALS)
                .setClientId("abcd")
                .setClientSecret("BLOBBER_CRED")
                .buildBodyMessage();
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        
        OAuthJSONAccessTokenResponse response = oAuthClient.accessToken(request);

        assertNotNull(response.getAccessToken());
    }
    
    @Test(expected = OAuthProblemException.class)
    public void test_OLTU_Client_In_Valid() throws Exception {
        OAuthClientRequest request = OAuthClientRequest.tokenLocation(path("auth/token"))
                .setGrantType(GrantType.CLIENT_CREDENTIALS).setClientId("wrong_client_id")
                .setClientSecret("clearly wrong").buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        oAuthClient.accessToken(request);
    }
    
    private String path(String path) {
        return deploymentUrl.toString() + RESOURCE_PREFIX + path;
    }
}
