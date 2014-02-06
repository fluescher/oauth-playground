
package com.zuehlke.oauth.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.zuehlke.oauth.resource.auth.OAuthRequired;

/** Example resource class hosted at the URI path "/"
 */
@Path("/")
public class SecuredResource {
    
    @GET 
    @Produces("text/plain")
    @Path("/options")
    @OAuthRequired
    public String getOptions() {
        return "Hi there!\n";
    }
    
    @GET 
    @Produces("text/plain")
    @Path("/allOptions")
    @OAuthRequired
    public String getAllOptions() {
        return "Hi there!\n";
    }
    
}
