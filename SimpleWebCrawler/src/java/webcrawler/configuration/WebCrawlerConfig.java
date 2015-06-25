package webcrawler.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"webcrawler"})
@PropertySource("classpath:webcrawler.properties")
public class WebCrawlerConfig extends WebMvcConfigurerAdapter {
  
  protected final Logger logger = LogManager.getLogger(this.getClass());
  
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    logger.entry();
    registry.addResourceHandler("/**").addResourceLocations("/");
    logger.exit();
  }
  
  @Bean
  public ViewResolver viewResolver() {
    logger.entry();
    InternalResourceViewResolver res = new InternalResourceViewResolver();
    res.setViewClass(InternalResourceView.class);
    res.setPrefix("/");
    res.setSuffix(".html");
    return logger.exit(res);
  }
}
