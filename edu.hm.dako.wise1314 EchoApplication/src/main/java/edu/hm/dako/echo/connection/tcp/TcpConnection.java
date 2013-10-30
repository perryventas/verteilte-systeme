package edu.hm.dako.echo.connection.tcp;

import edu.hm.dako.echo.connection.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;

public class TcpConnection implements Connection {

    private static Log log = LogFactory.getLog(TcpConnection.class);

    // Groesse des Empfangspuffers einer TCP-Verbindung in Byte
    private static final int RECEIVE_BUFFER_SIZE = 300000;

    // Ein- und Ausgabestrom der Verbindung
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    
    // Verwendetes TCP-Socket
    private final Socket socket;

    public TcpConnection(Socket socket) {
        this.socket = socket;

        log.info(Thread.currentThread().getName() + ": Verbindung mit neuem Client aufgebaut, Remote-TCP-Port " +
                socket.getPort());

        try {
            // Ein- und Ausgabe-Objektstroeme erzeugen
        	
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            log.debug("Standardgroesse des Empfangspuffers der Verbindung: " + socket.getReceiveBufferSize() +
                    " Byte");
            socket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
            log.debug("Eingestellte Groesse des Empfangspuffers der Verbindung: " + socket.getReceiveBufferSize() +
                    " Byte");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Serializable receive() throws Exception {
        return (Serializable) in.readObject();
    }

    @Override
    public void send(Serializable message) throws Exception {
    	out.writeObject(message); 
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.flush();
        socket.close(); 
    }
}
