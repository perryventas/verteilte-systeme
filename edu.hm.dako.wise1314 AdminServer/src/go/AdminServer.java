package go;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

/**
 * @author Christoph Friegel
 * @version 1.0
 */

public class AdminServer {

	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServerFactory.create(CONSTANTS.WS_AS_HOST);
		server.start();
		// never ending ;)
	}
}
