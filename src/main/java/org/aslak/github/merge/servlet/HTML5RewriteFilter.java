package org.aslak.github.merge.servlet;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

@WebFilter(filterName= "HTML5RewriteFilter", urlPatterns = {"/*"})
public class HTML5RewriteFilter implements Filter {

    public static final Pattern PATTERN = Pattern.compile("(^.*/api/|\\.(css|js|png|jpg|html))");
    public static final String APP_INDEX = "/app/index.jsp";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void destroy() {}

    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        request.setAttribute("BASE_ROOT", httpRequest.getContextPath());
        
        if(PATTERN.matcher(httpRequest.getRequestURI()).find()) {
            chain.doFilter(request, response);
        } else {
            request.getRequestDispatcher(APP_INDEX).forward(request, response); 
        }
    }
}
