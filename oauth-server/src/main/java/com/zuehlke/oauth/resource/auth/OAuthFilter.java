package com.zuehlke.oauth.resource.auth;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;

import com.zuehlke.oauth.authorization.AuthorizationServer;

@Provider
@OAuthRequired
public class OAuthFilter implements ContainerRequestFilter {

    @Inject
    private AuthorizationServer authServer;
    private HttpServletRequest request;
    
    public void setAuthorizationServer(AuthorizationServer server) {
        authServer = server;
    }
    
    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        try {
            try {
                final OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(getRequest());
                final String token = oauthRequest.getAccessToken();
                
                if(!authServer.isValidToken(token)) {
                    context.abortWith(createInvalidTokenResponse());
                }
            } catch(OAuthProblemException problemEx) {
                context.abortWith(createErrorResponse(problemEx));
            }
        } catch (OAuthSystemException oAuthProblem) {
            context.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }
    }

    public HttpServletRequest getRequest() {
        return request;
    }
    
    @Context
    public void setRequest(HttpServletRequest req) {
        this.request = req;
    }
    
    private Response createErrorResponse(OAuthProblemException ex) throws OAuthSystemException {
        final OAuthResponse response = OAuthASResponse
                                        .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                        .error(ex)
                                        .buildJSONMessage();
        return Response
                .status(response.getResponseStatus())
                .entity(response.getBody())
                .build();      
    }
    
    private Response createInvalidTokenResponse() throws OAuthSystemException {
        OAuthResponse oauthResponse = OAuthRSResponse
                .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
                .setErrorDescription("Invalid token")
                .buildJSONMessage();
        return Response.status(oauthResponse.getResponseStatus()).entity(oauthResponse.getBody()).build();
    }
}
