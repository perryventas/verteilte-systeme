package edu.hm.dako.echo.connection;

import edu.hm.dako.echo.common.EchoPDU;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Stattet ein {@link Connection} Objekt mit automatischem Logging aus.
 * Umschliesst eine beliebige Connection-Instanz und bietet dieselbe Schnittstelle an.
 * Beim Aufruf einer Methode wird zunaechst eine Log-Ausgabe getaetigt und
 * danach die Methode der umschlossenen Connection aufgerufen.
 * Anschliessend erfolgt eine weitere Log-Ausgabe.
 *
 */
public class LoggingConnectionDecorator implements Connection {

    private static Log log = LogFactory.getLog(LoggingConnectionDecorator.class);

    private Connection wrappedClient;

    public LoggingConnectionDecorator(Connection wrappedClient) {
        this.wrappedClient = wrappedClient;
    }

    @Override
    public void send(Serializable message) throws Exception {
        EchoPDU echoPDU = (EchoPDU) message;
        log.debug("Sende Echo-Nachricht: " + echoPDU.getMessage());
        wrappedClient.send(echoPDU);
        log.debug("Echo-Nachricht gesendet - done: " + echoPDU.getMessage());
    }

    @Override
    public Serializable receive() throws Exception {
        log.debug("Empfange Echo-Nachricht...");
        EchoPDU receivedPdu = (EchoPDU) wrappedClient.receive();
        log.debug("Echo-Nachricht empfangen - done: " + receivedPdu.getMessage());
        return receivedPdu;
    }

    @Override
    public void close() throws Exception {
        log.debug("Schliesse Connection...");
        wrappedClient.close();
        log.debug("Connection geschlossen!");
    }
}
