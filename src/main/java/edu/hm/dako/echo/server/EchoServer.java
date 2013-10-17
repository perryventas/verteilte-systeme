package edu.hm.dako.echo.server;

/**
 * Einheitliche Schnittstelle aller Server
 */
public interface EchoServer {

    /**
     * Startet den Server
     */
    void start();

    /**
     * Stoppt den Server
     *
     * @throws Exception
     */
    void stop() throws Exception;
}
