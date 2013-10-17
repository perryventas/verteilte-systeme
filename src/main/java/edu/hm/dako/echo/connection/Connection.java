package edu.hm.dako.echo.connection;


import java.io.Serializable;

/**
 * Wird vom Client und Server verwendet um mit dem Partner zu kommunizieren.
 */
public interface Connection {

    /**
     * Blockiert bis eine Nachricht eintrifft.
     * @return Die erhaltene Nachricht des Kommunikationspartners.
     * @throws Exception
     */
    public Serializable receive() throws Exception;

    /**
     * Sendet eine Nachricht an den Kommunikationspartner.
     * @param message Die zu sendende Nachricht.
     * @throws Exception
     */
    public void send(Serializable message) throws Exception;

    /**
     * Baut die Verbindung zum Kommunikationspartner ab.
     * @throws Exception
     */
    public void close() throws Exception;
}
