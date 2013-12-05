package edu.hm.dako.echo.server;

/**
 * @author Tore Offermann
 * @version 1.0
 */

public interface EchoServer
{

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
