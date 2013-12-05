package edu.hm.dako.echo.client;

import edu.hm.dako.echo.common.CONSTANTS;
import edu.hm.dako.echo.common.EchoPDU;
import edu.hm.dako.echo.common.ExceptionHandler;
import edu.hm.dako.echo.common.SharedClientStatistics;
import edu.hm.dako.echo.connection.Connection;
import edu.hm.dako.echo.connection.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.tibco.tibjms.TibjmsQueueConnectionFactory;

import java.net.SocketTimeoutException;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.Session;

/**
 * Baut pro Client eine Verbindung zum Server auf und benutzt diese, um alle
 * Nachrichten zu versenden. Nachdem alle Nachrichten versendet wurden, wird die
 * Verbindung abgebaut.
 */

/**
 * @author Christoph Friegel
 * @author Tore Offermann
 * @version 1.0
 */

public class EMSConnectionReusingClient extends AbstractClient
{

  private static Log log = LogFactory.getLog( EMSConnectionReusingClient.class );

  private Connection connection;

  private QueueConnection connection2 = null;
  private String serverUrl = null;
  private final String userName = CONSTANTS.USER_NAME;
  private final String password = CONSTANTS.PASSWORD;
  private final String responseQueueName = CONSTANTS.RESPONSE_QUEUE_NAME;

  public EMSConnectionReusingClient( int serverPort,
      String remoteServerAddress, int numberOfClient, int messageLength,
      int numberOfMessages, int clientThinkTime,
      SharedClientStatistics sharedData, ConnectionFactory connectionFactory )
  {
    super( serverPort, remoteServerAddress, numberOfClient, messageLength,
        numberOfMessages, clientThinkTime, sharedData, connectionFactory );
    this.serverUrl = "tcp://" + remoteServerAddress + ":" + serverPort;
    PropertyConfigurator.configureAndWatch( "log4j.client.properties",
        60 * 1000 );
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
      receiveEcho();

      waitForOtherClients();

      connection = connectionFactory.connectToServer( remoteServerAddress,
          serverPort, localPort );

      for ( int i = 0; i < numberOfMessagesToSend; i++ )
      {
        try
        {
          doEcho( i );
        }
        catch ( SocketTimeoutException e )
        {
          log.debug( e.getMessage() );
        }
      }
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

  private void doEcho( int i ) throws Exception
  {
    sharedData.incrSentMsgCounter( clientNumber );
    EchoPDU pdu = constructEchoPDU( i );
    pdu.setMessageNumber( i );
    connection.send( pdu );
    Thread.sleep( clientThinkTime );
  }

  private void receiveEcho() throws Exception
  {
    TibjmsQueueConnectionFactory factory = new TibjmsQueueConnectionFactory(
        serverUrl );
    this.connection2 = factory.createQueueConnection( userName, password );

    new EMSQueueReciever( this.connection2, this.responseQueueName );
  }

  private class EMSQueueReciever implements ExceptionListener, MessageListener
  {

    private QueueConnection connection2;
    private Session session;
    private String responseQueueName;
    private MessageConsumer msgConsumer = null;
    private Destination destination = null;

    private EMSQueueReciever( QueueConnection connection2,
        String responseQueueName )
    {
      this.connection2 = connection2;
      this.responseQueueName = responseQueueName;

      connectToQueue();

      startConsumer();
    }

    private void connectToQueue()
    {
      try
      {
        session = connection2.createSession();
        connection2.setExceptionListener( this );
        destination = session.createQueue( this.responseQueueName );
        log.debug( "Subscribing to destination: " + this.responseQueueName );
      }
      catch ( JMSException e1 )
      {
        log.debug( "JMSException: " + e1.getMessage() );
      }
    }

    public void startConsumer()
    {
      try
      {
        msgConsumer = session.createConsumer( destination );
        msgConsumer.setMessageListener( this );
        connection2.start();
      }
      catch ( Exception e )
      {
        log.debug( "Exception: " + e.getMessage() );
      }

    }

    @Override
    public void onMessage( Message msg )
    {
      try
      {
        log.debug( "Received message: " + msg );

        ObjectMessage objMsg = (ObjectMessage) msg;
        EchoPDU receivedPdu = (EchoPDU) objMsg.getObject();

        long rtt = System.nanoTime() - receivedPdu.getClientTime();

        postReceive( receivedPdu.getMessageNumber(), receivedPdu, rtt );

        // log.debug(" PDU-ID: " + receivedPdu.getMessageNumber() + " RTT: " +
        // rtt + " ns");
        // log.debug(" Diese Zeit: " + System.nanoTime() + " PDU-Client-Zeit: "
        // + receivedPdu.getClientTime());
      }
      catch ( Exception e )
      {
        log.debug( "Unexpected exception in the message callback!" );
      }
    }

    @Override
    public void onException( JMSException e )
    {
      log.debug( "CONNECTION EXCEPTION: " + e.getMessage() );
    }
  }
}
