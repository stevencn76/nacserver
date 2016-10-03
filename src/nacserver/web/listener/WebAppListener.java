package nacserver.web.listener;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.support.WebApplicationContextUtils;

import nacserver.common.Global;
import nacserver.net.CommandProcessor;
import nacserver.net.TCPNetServerListenerImpl;
import net.ojava.network.tcp.TCPNetServer;
import net.ojava.network.telnet.log.NetLogger;

public class WebAppListener implements ServletContextListener {
	private static final NetLogger LOGGER = NetLogger
			.getLogger(WebAppListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		int port = 0;
		try {
			String sPort = sce.getServletContext().getInitParameter(
					"serverport");
			port = Integer.parseInt(sPort);
		} catch (Exception e) {
			LOGGER.error("Get init parameter for tcp server port error", e);
		}

		if (port <= 0) {
			LOGGER.error(MessageFormat.format("tcp server port invalidate {0}",
					port));
		} else {
			LOGGER.info(MessageFormat.format(
					"ready to start tcp server on port {0}", port));
			TCPNetServer netServer = new TCPNetServer();
			netServer.addListener(new TCPNetServerListenerImpl());
			try {
				netServer.start(port);
				LOGGER.info("start tcp server successfully");

				sce.getServletContext().setAttribute(
						Global.TCP_SERVER_INSTANCE, netServer);
			} catch (IOException e) {
				LOGGER.error("start tcp server error", e);
			}
		}

		CommandProcessor.getInstance().setSpringContext(
				WebApplicationContextUtils.getWebApplicationContext(sce
						.getServletContext()));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		TCPNetServer netServer = (TCPNetServer) sce.getServletContext()
				.getAttribute(Global.TCP_SERVER_INSTANCE);
		if (netServer != null) {
			try {
				netServer.stop();
				LOGGER.info("stop tcp server successfully");
			} catch (IOException e) {
				LOGGER.error("stop tcp server error", e);
			}
		}
	}

}
