package edu.hm.dako.echo.server;

import edu.hm.dako.echo.benchmarking.UserInterfaceInputParameters;
import edu.hm.dako.echo.connection.Connection;
import edu.hm.dako.echo.connection.LoggingConnectionDecorator;
import edu.hm.dako.echo.connection.ServerSocket;
import edu.hm.dako.echo.connection.tcp.TcpServerSocket;


import java.util.concurrent.Executors;

/**
 * Uebernimmt die Konfiguration und Erzeugung bestimmter Server-Typen.
 * Siehe {@link edu.hm.dako.echo.benchmarking.UserInterfaceInputParameters.ImplementationType}
 * Dies beinhaltet Art des Servers und Konfiguration dessen Thread-Pool.
 */
public final class ServerFactory {

    private static final int DEFAULT_SERVER_PORT = 50000;
    public static final int RMI_SERVER_PORT = 1099;

    private ServerFactory() {
    }

    public static EchoServer getServer(UserInterfaceInputParameters.ImplementationType type)
            throws Exception {
    	
        System.out.println("Echoserver " + type.toString() + " wird gestartet");
        
        switch (type) {
            case TCPSingleThreaded:
                return new DefaultEchoServerImpl(Executors.newSingleThreadExecutor(), getDecoratedServerSocket(
                        new TcpServerSocket(DEFAULT_SERVER_PORT)), false);
            case TCPMultiThreaded:
                return new DefaultEchoServerImpl(Executors.newCachedThreadPool(), getDecoratedServerSocket(
                        new TcpServerSocket(DEFAULT_SERVER_PORT)), true);
                
            case EMSSingleThreaded:
                return new EMSEchoServerImpl();
       
             // Weitere Implementierungen hier einbauen 
            
            default:
                throw new RuntimeException("Unknown type: " + type);
        }
    }

    private static ServerSocket getDecoratedServerSocket(ServerSocket serverSocket) {
        return new DecoratingServerSocket(serverSocket);
    }

    /**
     * Startet den ausgewaehlten Server.
     * Muss aufgerufen werden, bevor ein Test ueber die GUI gestartet werden kann.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

    	/* Hinweis:
         * Im ImplementationType der naechsten Anweisungen muss der Server, 
    	 * der gestartet werden soll, angegeben werden, hier nur TCPSingleThreaded und TCPMultiThreaded implementiert
         */   	
        getServer(UserInterfaceInputParameters.ImplementationType.EMSSingleThreaded).start();
    }

    private static class DecoratingServerSocket implements ServerSocket {

        private final ServerSocket wrappedServerSocket;

        DecoratingServerSocket(ServerSocket wrappedServerSocket) {
            this.wrappedServerSocket = wrappedServerSocket;
        }

        @Override
        public Connection accept() throws Exception {
            return new LoggingConnectionDecorator(wrappedServerSocket.accept());
        }

        @Override
        public void close() throws Exception {
            wrappedServerSocket.close();
        }

        @Override
        public boolean isClosed() {
            return wrappedServerSocket.isClosed();
        }
    }
}
