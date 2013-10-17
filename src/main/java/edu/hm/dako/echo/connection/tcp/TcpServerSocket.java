package edu.hm.dako.echo.connection.tcp;

import edu.hm.dako.echo.connection.Connection;
import edu.hm.dako.echo.connection.ServerSocket;

import java.io.IOException;

public class TcpServerSocket implements ServerSocket {

    private static java.net.ServerSocket serverSocket;

    public TcpServerSocket(int port) throws IOException {
        serverSocket = new java.net.ServerSocket(port);
    }

    @Override
    public Connection accept() throws IOException {
        return new TcpConnection(serverSocket.accept());
    }

    @Override
    public void close() throws IOException {
        serverSocket.close(); 
    }

    @Override
    public boolean isClosed() {
        return serverSocket.isClosed();
    }
}
