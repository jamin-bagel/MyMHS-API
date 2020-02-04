package me.jaminbagel.mymhs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Ben on 1/20/20 @ 5:30 PM
 */
public class Main implements ServletContextListener {

  public static final Logger logger = LogManager.getLogger("GLOBAL");

  // Startup listener
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    logger.info("Finished initializing");
  }
}
