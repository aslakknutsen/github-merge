package org.aslak.github.merge;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aslak.github.merge.model.CurrentUser;

@WebFilter(filterName = "GithubAuthFilter")
public class GithubAuthFilter implements Filter {

    private static String AUTH_URL = "https://github.com/login/oauth/authorize";
    private static String AUTH_ACCESS_URL = "https://github.com/login/oauth/access_token";

    private String AUTH_CLIENT_ID = System.getenv("GITHUB_CLIENT_ID");
    private String AUTH_CLIENT_SECRET = System.getenv("GITHUB_CLIENT_SECRET");
    private String AUTH_SCOPE = "public_repo";

    @Inject
    private CurrentUser user;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if(AUTH_CLIENT_ID == null || AUTH_CLIENT_ID.isEmpty()) {
            throw new IllegalStateException("GITHUB_CLIENT_ID env variable is missing");
        }
        if(AUTH_CLIENT_SECRET == null || AUTH_CLIENT_SECRET.isEmpty()) {
            throw new IllegalStateException("GITHUB_CLIENT_SECRET env variable is missing");
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        if(!user.isAuthorized()) {
            if(isAuthenticationResponse(request)) {
                getAccessToken(request, response);
            } else {
                doAuthorizeRequest(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private void getAccessToken(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append(AUTH_ACCESS_URL).append("?");
        sb.append("client_id=").append(AUTH_CLIENT_ID).append("&");
        sb.append("client_secret=").append(AUTH_CLIENT_SECRET).append("&");
        sb.append("redirect_url=").append(request.getRequestURL()).append("&");
        sb.append("code=").append(user.getCode());

        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection url = (HttpURLConnection)new URL(sb.toString()).openConnection();
            url.setRequestMethod("POST");
            url.setRequestProperty("Accept", "application/json");
            String json = IOUtil.asString(url.getInputStream());

            user.setAccessToken(extractAccessToken(json));
            user.setScopes(extractScope(json));
            response.sendRedirect(user.getOriginalRequest());

         } catch(Exception e) {
             throw new RuntimeException("Could not fetch AccessToken", e);
         }
    }

    private boolean isAuthenticationResponse(HttpServletRequest request) {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        if(code != null && state != null) {
            if(state.equals(user.getState())) {
                user.setCode(code);
                return true;
            }
        }
        return false;
    }

    private void doAuthorizeRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(AUTH_URL).append("?");
        sb.append("client_id=").append(AUTH_CLIENT_ID).append("&");
        sb.append("redirect_uri=").append(request.getRequestURL()).append("&");
        sb.append("scope=").append(AUTH_SCOPE).append("&");
        sb.append("state=").append(user.getState());

        user.setOriginalRequest(request.getRequestURL().toString());

        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.setHeader("Location", sb.toString());
        response.sendRedirect(sb.toString());
    }

    @Override
    public void destroy() {
    }

    public static String extractAccessToken(String json) {
        Pattern pattern = Pattern.compile("\"access_token\":\"([a-z0-9]+)\"");
        Matcher m = pattern.matcher(json);
        if(m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static String[] extractScope(String json) {
        Pattern pattern = Pattern.compile("\"scope\":\"([a-z0-9,_:]+)\"");
        Matcher m = pattern.matcher(json);
        if(m.find()) {
            return m.group(1).split(",");
        }
        return new String[0];
    }
}
