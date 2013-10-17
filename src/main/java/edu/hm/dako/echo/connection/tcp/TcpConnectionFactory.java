package edu.hm.dako.echo.connection.tcp;

import edu.hm.dako.echo.connection.Connection;
import edu.hm.dako.echo.connection.ConnectionFactory;

import java.io.IOException;
import java.net.Socket;

public class TcpConnectionFactory implements ConnectionFactory {

	public Connection connectToServer(String remoteServerAddress,
			int serverPort, int localPort) throws IOException {
		TcpConnection connection = null;
		boolean connected = false;
		while (!connected) {
			try {
				connection = new TcpConnection(new Socket(remoteServerAddress, serverPort));
				connected = true;
			} catch (Exception e) {
				// try again
			}
		}
		return connection;
	}

}
