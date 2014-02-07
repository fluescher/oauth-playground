
package com.zuehlke.oauth.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.zuehlke.oauth.resource.auth.OAuthRequired;

/** Example resource class hosted at the URI path "/" */
@Path("/")
public class SecuredResource {
    
    @GET 
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/options")
    @OAuthRequired(scopes={"options"})
    public String getOptions() {
        return "Hi there!\n";
    }
    
    @GET 
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/allOptions")
    @OAuthRequired(scopes={"allOption"})
    public String getAllOptions() {
        return "Hi there all!\n";
    }
    
}
