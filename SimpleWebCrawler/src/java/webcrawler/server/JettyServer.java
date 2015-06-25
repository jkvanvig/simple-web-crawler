package webcrawler.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * This class has a main method that starts an embedded Jetty server. To use, simply run it as a
 * java application. After startup, the Swagger documentation should be available at
 * http://localhost:8080/web-crawler
 */
public class JettyServer {
  
  private static Logger logger;
  private static final String WEBAPP_BASE = "src/webapp";
  private static final int DEFAULT_PORT = 8080;
  
  private static final String CONTEXT_PATH = "/simple-web-crawler";
  
  public static void main(String[] args) throws Exception {
    logger = LogManager.getLogger(JettyServer.class);
    logger.entry();
    Server server = new Server(DEFAULT_PORT);
    server.setHandler(new WebAppContext(WEBAPP_BASE, CONTEXT_PATH));
    server.start();
    server.join();
    logger.exit();
  }
}
