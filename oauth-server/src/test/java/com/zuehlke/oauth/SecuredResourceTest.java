package com.zuehlke.oauth;

import java.net.URL;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@RunWith(Arquillian.class)
@RunAsClient
public class SecuredResourceTest {

    private static final String RESOURCE_PREFIX = OAuthApplication.class.getAnnotation(ApplicationPath.class).value().substring(1);
    
    @Deployment(testable=false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage(SecuredResource.class.getPackage())
                .addClass(OAuthApplication.class);
    }
    
    @ArquillianResource URL deploymentUrl;
    
    @Test
    public void testAccessWithoutToken_Resteasy() throws Exception {
        ClientRequest request = new ClientRequest(deploymentUrl.toString() + RESOURCE_PREFIX);
        request.header("Accept", MediaType.TEXT_PLAIN);

        ClientResponse<String> responseObj = request.get(String.class);

        Assert.assertEquals(200, responseObj.getStatus());

        String response = responseObj.getEntity().trim();
        Assert.assertEquals("Hi there!", response);
    }
    
    @Test
    public void testAccessWithoutToken_RestAssured() {
       get(path(""))
           .then()
               .statusCode(200)
               .body(equalTo("Hi there!\n"));
    }
    
    private String path(String path) {
        return deploymentUrl.toString() + RESOURCE_PREFIX + path;
    }
}
