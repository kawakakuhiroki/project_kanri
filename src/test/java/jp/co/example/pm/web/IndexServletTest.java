package jp.co.example.pm.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;

class IndexServletTest {
  @Test
  void doGet_redirectsRootRequestToProjects() throws Exception {
    IndexServlet servlet = new IndexServlet();
    ResponseCapture responseCapture = new ResponseCapture();
    HttpServletRequest request = request("/project-kanri/", "/project-kanri", null);

    servlet.doGet(request, responseCapture.proxy());

    assertEquals("/project-kanri/projects", responseCapture.redirectLocation);
    assertNull(responseCapture.errorCode);
  }

  @Test
  void doGet_forwardsStaticAssetRequestToDefaultServlet() throws Exception {
    IndexServlet servlet = new IndexServlet();
    ForwardCapture forwardCapture = new ForwardCapture();
    ServletContext servletContext = servletContext(forwardCapture.dispatcher());
    HttpServletRequest request =
        request("/project-kanri/static/app.css", "/project-kanri", servletContext);
    ResponseCapture responseCapture = new ResponseCapture();

    servlet.doGet(request, responseCapture.proxy());

    assertTrue(forwardCapture.forwarded);
    assertSame(request, forwardCapture.forwardedRequest);
    assertSame(responseCapture.proxy(), forwardCapture.forwardedResponse);
    assertNull(responseCapture.redirectLocation);
    assertNull(responseCapture.errorCode);
  }

  private HttpServletRequest request(
      String requestUri, String contextPath, ServletContext servletContext) {
    return (HttpServletRequest)
        Proxy.newProxyInstance(
            HttpServletRequest.class.getClassLoader(),
            new Class<?>[] {HttpServletRequest.class},
            (proxy, method, args) -> {
              return switch (method.getName()) {
                case "getRequestURI" -> requestUri;
                case "getContextPath" -> contextPath;
                case "getServletContext" -> servletContext;
                default -> defaultValue(method.getReturnType());
              };
            });
  }

  private ServletContext servletContext(RequestDispatcher dispatcher) {
    return (ServletContext)
        Proxy.newProxyInstance(
            ServletContext.class.getClassLoader(),
            new Class<?>[] {ServletContext.class},
            (proxy, method, args) -> {
              if ("getNamedDispatcher".equals(method.getName())
                  && args != null
                  && args.length == 1
                  && "default".equals(args[0])) {
                return dispatcher;
              }
              return defaultValue(method.getReturnType());
            });
  }

  private Object defaultValue(Class<?> type) {
    if (!type.isPrimitive()) {
      return null;
    }
    if (boolean.class.equals(type)) {
      return false;
    }
    if (char.class.equals(type)) {
      return '\0';
    }
    return 0;
  }

  private static final class ResponseCapture {
    private String redirectLocation;
    private Integer errorCode;
    private HttpServletResponse proxy;

    private HttpServletResponse proxy() {
      if (proxy == null) {
        proxy =
            (HttpServletResponse)
                Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[] {HttpServletResponse.class},
                    (ignored, method, args) -> {
                      if ("sendRedirect".equals(method.getName())) {
                        redirectLocation = (String) args[0];
                        return null;
                      }
                      if ("sendError".equals(method.getName())) {
                        errorCode = (Integer) args[0];
                        return null;
                      }
                      return null;
                    });
      }
      return proxy;
    }
  }

  private static final class ForwardCapture {
    private boolean forwarded;
    private Object forwardedRequest;
    private Object forwardedResponse;

    private RequestDispatcher dispatcher() {
      return (RequestDispatcher)
          Proxy.newProxyInstance(
              RequestDispatcher.class.getClassLoader(),
              new Class<?>[] {RequestDispatcher.class},
              (proxy, method, args) -> {
                if ("forward".equals(method.getName())) {
                  forwarded = true;
                  forwardedRequest = args[0];
                  forwardedResponse = args[1];
                }
                return null;
              });
    }
  }
}
