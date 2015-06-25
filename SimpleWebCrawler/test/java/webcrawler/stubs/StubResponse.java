package webcrawler.stubs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

public abstract class StubResponse implements Response {
  
  public static Response basicSuccess(String baseUrl) {
    Response stubResponse = new StubResponse() {
      @Override
      public URL url() {
        try {
          return new URL(baseUrl);
        } catch (MalformedURLException e) {
          throw new RuntimeException(e);
        }
      }
      
      @Override
      public int statusCode() {
        return 200;
      }
    };
    return stubResponse;
  }
  
  public static Response basicFailure(String baseUrl) {
    Response stubResponse = new StubResponse() {
      @Override
      public URL url() {
        try {
          return new URL(baseUrl);
        } catch (MalformedURLException e) {
          throw new RuntimeException(e);
        }
      }
      
      @Override
      public int statusCode() {
        return 404;
      }
    };
    return stubResponse;
  }
  
  @Override
  public URL url() {
    return null;
  }
  
  @Override
  public Response url(URL url) {
    return null;
  }
  
  @Override
  public Method method() {
    return null;
  }
  
  @Override
  public Response method(Method method) {
    return null;
  }
  
  @Override
  public String header(String name) {
    return null;
  }
  
  @Override
  public Response header(String name, String value) {
    return null;
  }
  
  @Override
  public boolean hasHeader(String name) {
    return false;
  }
  
  @Override
  public boolean hasHeaderWithValue(String name, String value) {
    return false;
  }
  
  @Override
  public Response removeHeader(String name) {
    return null;
  }
  
  @Override
  public Map<String, String> headers() {
    return null;
  }
  
  @Override
  public String cookie(String name) {
    return null;
  }
  
  @Override
  public Response cookie(String name, String value) {
    return null;
  }
  
  @Override
  public boolean hasCookie(String name) {
    return false;
  }
  
  @Override
  public Response removeCookie(String name) {
    return null;
  }
  
  @Override
  public Map<String, String> cookies() {
    return null;
  }
  
  @Override
  public int statusCode() {
    return 0;
  }
  
  @Override
  public String statusMessage() {
    return null;
  }
  
  @Override
  public String charset() {
    return null;
  }
  
  @Override
  public String contentType() {
    return null;
  }
  
  @Override
  public Document parse() throws IOException {
    return null;
  }
  
  @Override
  public String body() {
    return null;
  }
  
  @Override
  public byte[] bodyAsBytes() {
    return null;
  }
}
