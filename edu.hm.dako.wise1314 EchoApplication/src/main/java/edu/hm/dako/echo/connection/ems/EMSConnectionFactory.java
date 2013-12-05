package edu.hm.dako.echo.connection.ems;

import com.tibco.tibjms.TibjmsQueueConnectionFactory;

import edu.hm.dako.echo.connection.Connection;
import edu.hm.dako.echo.connection.ConnectionFactory;

/**
 * @author Christoph Friegel
 * @author Tore Offermann
 * @version 1.0
 */

public class EMSConnectionFactory implements ConnectionFactory
{

  @Override
  public Connection connectToServer( String remoteServerAddress,
      int serverPort, int localPort ) throws Exception
  {

    String serverUrl = "tcp://" + remoteServerAddress + ":" + serverPort;

    EMSConnection emsConnection = null;

    boolean connected = false;
    while ( !connected )
    {
      try
      {
        emsConnection = new EMSConnection( new TibjmsQueueConnectionFactory(
            serverUrl ) );
        connected = true;
      }
      catch ( Exception e )
      {
        // try again
      }
    }
    return emsConnection;
  }

}
