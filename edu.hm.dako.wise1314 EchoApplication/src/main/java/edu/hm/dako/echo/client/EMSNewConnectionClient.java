package edu.hm.dako.echo.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.echo.common.EchoPDU;
import edu.hm.dako.echo.common.ExceptionHandler;
import edu.hm.dako.echo.common.SharedClientStatistics;
import edu.hm.dako.echo.connection.Connection;
import edu.hm.dako.echo.connection.ConnectionFactory;

/**
 * Baut fuer jede zu versendene Nachricht eine Verbindung zum Server auf und ab.
 */

/**
 * @author Christoph Friegel
 * @author Tore Offermann
 * @version 1.0
 */

public class EMSNewConnectionClient extends AbstractClient
{
  private static Log log = LogFactory.getLog( EMSNewConnectionClient.class );
  private Connection connection;

  public EMSNewConnectionClient( int serverPort, String remoteServerAddress,
      int numberOfClient, int messageLength, int numberOfMessages,
      int clientThinkTime, SharedClientStatistics sharedData,
      ConnectionFactory connectionFactory )
  {
    super( serverPort, remoteServerAddress, numberOfClient, messageLength,
        numberOfMessages, clientThinkTime, sharedData, connectionFactory );
  }

  /**
   * Client-Thread sendet hier alle Requests und wartet auf Antworten
   */
  @Override
  public void run()
  {
    Thread.currentThread().setName( "Client-Thread-" + clientNumber );
    try
    {
      waitForOtherClients();
    }
    catch ( InterruptedException e )
    {
      log.debug( e.getMessage() );
    }
    catch ( Exception e )
    {
      log.debug( e.getMessage() );
    }
    for ( int i = 0; i < numberOfMessagesToSend; i++ )
    {
      doEcho( i );
    }
  }

  private void doEcho( int i )
  {
    try
    {
      sharedData.incrSentMsgCounter( clientNumber );
      EchoPDU pdu = constructEchoPDU( i );
      pdu.setMessageNumber( i );
      connection = connectionFactory.connectToServer( remoteServerAddress,
          serverPort, localPort );
      connection.send( pdu );
      log.debug( "Sent request, " + pdu.getClientThreadName() + ", Request# "
          + ( i + 1 ) );
      Thread.sleep( clientThinkTime );
    }
    catch ( Exception e )
    {
      ExceptionHandler.logExceptionAndTerminate( e );
    }
    finally
    {
      try
      {
        connection.close();
      }
      catch ( Exception e )
      {
        ExceptionHandler.logException( e );
      }
    }
  }
}
