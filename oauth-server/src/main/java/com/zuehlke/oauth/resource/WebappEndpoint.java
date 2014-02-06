package com.zuehlke.oauth.resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@ApplicationPath("/webapp")
@Path("/")
public class WebappEndpoint extends Application {

    private static final String AUTH_ENDPOINT = "http://localhost:8080/oauth-server/api/auth/token";
    private static final String CLIENT_ID = "abcd";
    private static final String CLIENT_SECRET = "BLOBBER_CRED";
    
    @POST
    @Path("/obtainToken")
    @Produces(MediaType.TEXT_PLAIN)
    public Response obtainToken() {
        try {
            URL url = new URL(AUTH_ENDPOINT + "?grant_type=client_credentials&client_id="+CLIENT_ID+"&client_secret="+CLIENT_SECRET); 
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
            connection.setDoOutput(false); 
            connection.setInstanceFollowRedirects(false); 
            connection.setRequestMethod("POST"); 
            connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED); 
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();
            
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String responseToken = reader.readLine();
                return Response.ok().entity(responseToken).build();
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
