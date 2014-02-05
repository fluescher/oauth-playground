
package com.zuehlke.oauth;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/** Example resource class hosted at the URI path "/myresource"
 */
@Path("/myresource")
public class MyResource {
    
    @GET 
    @Produces("text/plain")
    public String getIt() {
        return "Hi there!";
    }
}
