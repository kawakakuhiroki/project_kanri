package jp.co.example.pm.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LoginServletTest {
  @Test
  void doGet_hidesGlobalHeaderAndForwardsToLoginJsp() throws Exception {
    LoginServlet servlet = new LoginServlet();
    RequestCapture requestCapture = new RequestCapture();
    ResponseStub responseStub = new ResponseStub();

    servlet.doGet(requestCapture.proxy(), responseStub.proxy());

    assertEquals("ログイン", requestCapture.attributes.get("pageTitle"));
    assertEquals(Boolean.FALSE, requestCapture.attributes.get("showGlobalHeader"));
    assertEquals("/WEB-INF/jsp/login.jsp", requestCapture.forwardedPath);
    assertSame(requestCapture.proxy(), requestCapture.forwardedRequest);
    assertSame(responseStub.proxy(), requestCapture.forwardedResponse);
  }

  private static final class RequestCapture {
    private final Map<String, Object> attributes = new HashMap<>();
    private String forwardedPath;
    private Object forwardedRequest;
    private Object forwardedResponse;
    private HttpServletRequest proxy;

    private HttpServletRequest proxy() {
      if (proxy == null) {
        proxy =
            (HttpServletRequest)
                Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[] {HttpServletRequest.class},
                    (ignored, method, args) -> {
                      return switch (method.getName()) {
                        case "setAttribute" -> {
                          attributes.put((String) args[0], args[1]);
                          yield null;
                        }
                        case "getAttribute" -> attributes.get((String) args[0]);
                        case "getRequestDispatcher" -> dispatcher((String) args[0]);
                        default -> defaultValue(method.getReturnType());
                      };
                    });
      }
      return proxy;
    }

    private RequestDispatcher dispatcher(String path) {
      return (RequestDispatcher)
          Proxy.newProxyInstance(
              RequestDispatcher.class.getClassLoader(),
              new Class<?>[] {RequestDispatcher.class},
              (ignored, method, args) -> {
                if ("forward".equals(method.getName())) {
                  forwardedPath = path;
                  forwardedRequest = args[0];
                  forwardedResponse = args[1];
                }
                return null;
              });
    }
  }

  private static final class ResponseStub {
    private HttpServletResponse proxy;

    private HttpServletResponse proxy() {
      if (proxy == null) {
        proxy =
            (HttpServletResponse)
                Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[] {HttpServletResponse.class},
                    (ignored, method, args) -> defaultValue(method.getReturnType()));
      }
      return proxy;
    }
  }

  private static Object defaultValue(Class<?> type) {
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
}
