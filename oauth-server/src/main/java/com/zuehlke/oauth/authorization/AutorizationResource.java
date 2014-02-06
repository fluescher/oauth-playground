package com.zuehlke.oauth.authorization;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.issuer.UUIDValueGenerator;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;

import com.zuehlke.oauth.authorization.AuthorizationServer.AuthorizationResponse;

/**
 * OAuth Authorisation Server. 
 */
@Path("/auth")
public class AutorizationResource {

    private static final int EXPIRE_TIME_IN_SECONDS = 3600;
    @Inject
    private AuthorizationServer authServer;

    public AutorizationResource() {
    }
    
    public AutorizationResource(AuthorizationServer server) {
        this.authServer = server;
    }
    
    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createToken(final @Context HttpServletRequest request) {
        try {
            try {
                final OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
                final OAuthIssuer oauthIssuer = new OAuthIssuerImpl(new UUIDValueGenerator());
                
                final AuthorizationResponse authResponse = authServer.isValidClient(oauthRequest.getClientId(), oauthRequest.getClientSecret());
                
                if(!isClientCredentialGrant(oauthRequest)) {
                    return badRequestResponse();
                }
                
                return doClientCredentialAuth(oauthRequest, oauthIssuer, authResponse);
                
            } catch (OAuthProblemException oAuthProblem) {
                System.out.println(oAuthProblem);
                return createErrorResponse(oAuthProblem);
            } 
        }
        catch (OAuthSystemException oauthException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Path("/tokenInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTokenInformation(final @Context HttpServletRequest request) {
        try {
            final OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request);
            final String token = oauthRequest.getAccessToken();
            
            if(!authServer.isValidToken(token)) {
                OAuthResponse oauthResponse = OAuthRSResponse
                                                .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                                                .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
                                                .setErrorDescription("Invalid token")
                                                .buildJSONMessage();

                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(oauthResponse.getBody())
                        .build();
            }
            
            return Response.ok().entity(authServer.getTokenInformation(token)).build();
        } catch (OAuthProblemException oAuthProblem) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (OAuthSystemException oAuthProblem) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } 
    }

    private Response doClientCredentialAuth(final OAuthTokenRequest authRequest, final OAuthIssuer oauthIssuer, final AuthorizationResponse authResponse) throws OAuthSystemException {
        if(!authResponse.isValidClientId()) {
            return invalidClientResponse();
        }
        
        if(!authResponse.isValidClientCredentials()) {
            return invalidCredentialsResponse();
        }
        
        final String token = oauthIssuer.accessToken();

        authServer.registerToken(token, authRequest.getClientId(), authRequest.getScopes());
        
        OAuthResponse response = OAuthASResponse
                .tokenResponse(HttpServletResponse.SC_OK)
                .setAccessToken(token)
                .setTokenType("Bearer")
                .setExpiresIn(String.valueOf(EXPIRE_TIME_IN_SECONDS))
                .buildJSONMessage();
        
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private boolean isClientCredentialGrant(final OAuthTokenRequest oauthRequest) {
        return GrantType.CLIENT_CREDENTIALS.toString().equals(oauthRequest.getGrantType());
    }

    private Response badRequestResponse() throws OAuthSystemException {
        OAuthResponse response =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE)
                    .setErrorDescription("Only client grant is supported")
                    .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response invalidCredentialsResponse() throws OAuthSystemException {
        OAuthResponse response =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT).setErrorDescription("INVALID CREDENTIALS")
                    .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response invalidClientResponse() throws OAuthSystemException {
        OAuthResponse response =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                    .setErrorDescription("INVALID CLIENT")
                    .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
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
    
}
