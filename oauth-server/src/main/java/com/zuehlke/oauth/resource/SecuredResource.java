
package com.zuehlke.oauth.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/** Example resource class hosted at the URI path "/"
 */
@Path("/")
public class SecuredResource {
    
    @GET 
    @Produces("text/plain")
    @Path("/options")
    public String getOptions() {
        return "Hi there!\n";
    }
    
    @GET 
    @Produces("text/plain")
    @Path("/allOptions")
    public String getAllOptions() {
        return "Hi there!\n";
    }
    
}
