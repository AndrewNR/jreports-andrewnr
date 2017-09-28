package servlets.filters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.andrewnr.oauth.service.ConnectionManager;

public class AuthenticationFilter extends HttpFilter {

    private static final String PAGE_HOME = "/home";
    private static final String PAGE_LOGIN = "/login";
    private static final String PAGE_CALLBACK = "/callback";
    private static final String PAGE_NOT_LOGGED_IN = "/notLoggedIn.jsp";
    public static final String ATTR_PREV_URI = "prevURI";
    
    private static Set<String> excludedPages = new HashSet<String>();
    static {
        excludedPages.add(PAGE_HOME);
        excludedPages.add(PAGE_LOGIN);
        excludedPages.add(PAGE_CALLBACK);
        excludedPages.add(PAGE_NOT_LOGGED_IN);
    }
    
    @Override
    public void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        String requestURI = request.getRequestURI();
        System.out.println("requestURI: " + requestURI);
        if (!isExcludedURI(requestURI) && !ConnectionManager.getConnectionManager().hasBeenAuthorizeded()) {
            System.out.println("Not logged in.");
            request.getSession().setAttribute(ATTR_PREV_URI, requestURI);
            request.getRequestDispatcher(PAGE_NOT_LOGGED_IN).forward(request, response);
            return;
        } else {
            System.out.println("Logged in, removing prevURI from session attribute.");
            request.getSession().removeAttribute(ATTR_PREV_URI);
        }
        chain.doFilter(request, response);
    }

    private static boolean isExcludedURI(String requestURI) {
        if (requestURI != null) {
            for (String excludedPage : excludedPages) {
                if (requestURI.endsWith(excludedPage)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
