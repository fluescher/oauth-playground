package com.zuehlke.oauth.resource;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;

import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.zuehlke.oauth.OAuthApplication;
import com.zuehlke.oauth.authorization.AuthorizationServer;
import com.zuehlke.oauth.authorization.RestTest;
import com.zuehlke.oauth.authorization.impl.AuthorizationServerMock;
import com.zuehlke.oauth.resource.auth.OAuthFilter;

@RunWith(Arquillian.class)
@RunAsClient
public class SecuredResourceTest extends RestTest {

    @Deployment(testable=false)
    public static WebArchive createDeployment() {
        File[] deps = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve().withTransitivity().asFile();
        
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage(SecuredResource.class.getPackage())
                .addPackage(OAuthFilter.class.getPackage())
                .addClass(OAuthApplication.class)
                .addClass(AuthorizationServer.class)
                .addClass(AuthorizationServerMock.class)
                .addAsLibraries(deps);
    }
    
    @Test
    public void testAccessWithoutToken_Resteasy() throws Exception {
        ClientRequest request = new ClientRequest(path("options"));
        request.header("Accept", MediaType.APPLICATION_JSON);

        ClientResponse<String> responseObj = request.get(String.class);

        Assert.assertEquals(400, responseObj.getStatus());

        String response = responseObj.getEntity().trim();
        assertTrue(response.contains("Missing authorization header"));
    }
    
    @Test
    public void testAccessWithoutToken_RestAssured() {
        given()
            .request()
                .header("Accept", MediaType.APPLICATION_JSON)
        .then()
            .get(path("options"))
                .then()
                   .statusCode(400);
    }
    
    @Test
    public void testAccessWith_Valid_Token() throws Exception {
        final String token = AuthorizationServerMock.VALID_TOKEN;
        
        given()
        .request()
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
        .then()
            .get(path("options"))
               .then()
               .statusCode(200)
               .body(equalTo("Hi there!\n"));
    }
    
    @Test
    public void testAccessWith_InValid_Token() throws Exception {
        final String token = "ASDF_BLOBBER";
        
        given()
        .request()
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
        .then()
            .get(path("options"))
               .then()
               .statusCode(401)
               .body("error_description", equalTo("Invalid token"));
    }
    
    @Test
    public void testAccess_Not_In_Scope() throws Exception {
        final String token = AuthorizationServerMock.VALID_TOKEN;
        
        given()
        .request()
            .header("Accept", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
        .then()
            .get(path("allOptions"))
               .then()
               .statusCode(401)
               .body("error_description", equalTo("Invalid token"));
    }
}
