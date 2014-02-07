package com.zuehlke.oauth.resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/obtainToken")
public class WebappEndpoint extends HttpServlet {

    private static final long serialVersionUID = -4057891155116975629L;
    private static final String HOST = "localhost";
    private static final String AUTH_ENDPOINT = "/oauth-server/api/auth/token";
    private static final String CLIENT_ID = "abcd";
    private static final String CLIENT_SECRET = "BLOBBER_CRED";

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            final String host = request.getScheme()+":// "+HOST+" :" + request.getServerPort();
            System.out.println(host);
            final String scopes = request.getParameter("scope").replace(" ", "%20");
            
            URL url = new URL(host+ AUTH_ENDPOINT + "?grant_type=client_credentials"
                                            + "&client_id="+CLIENT_ID+""
                                            + "&client_secret="+CLIENT_SECRET
                                            + "&scope="+scopes); 
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
            connection.setDoOutput(false); 
            connection.setInstanceFollowRedirects(false); 
            connection.setRequestMethod("POST"); 
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();
            
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String responseToken = reader.readLine();
                response.setStatus(200);
                response.getWriter().print(responseToken);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus(500);
        }
    }
}
