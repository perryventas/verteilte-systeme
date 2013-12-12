package edu.hm.dako.echo.client;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.tibco.tibjms.TibjmsQueueConnectionFactory;

import edu.hm.dako.echo.common.CONSTANTS;
import edu.hm.dako.echo.common.EchoPDU;
import edu.hm.dako.echo.common.SharedClientStatistics;

public class EMSEchoReciever implements ExceptionListener, MessageListener
{
  private static Log log = LogFactory.getLog( EMSEchoReciever.class );

  private Session session;
  private MessageConsumer msgConsumer = null;
  private Destination destination = null;
  private QueueConnection connection2 = null;
  private String serverUrl = null;
  private final String userName = CONSTANTS.USER_NAME;
  private final String password = CONSTANTS.PASSWORD;
  private final String responseQueueName = CONSTANTS.RESPONSE_QUEUE_NAME;
  private SharedClientStatistics sharedData;
  private int clientNumber;

  public EMSEchoReciever( int serverPort, String remoteServerAddress,
      SharedClientStatistics sharedData, int clientNumber )
  {
    this.clientNumber = clientNumber;
    this.sharedData = sharedData;
    this.serverUrl = "tcp://" + remoteServerAddress + ":" + serverPort;
    PropertyConfigurator.configureAndWatch( "log4j.client.properties",
        60 * 1000 );
    log.debug("server: " + serverUrl);

    connectToQueue();

    startConsumer();
  }

  private void connectToQueue()
  {
    try
    {
      TibjmsQueueConnectionFactory factory = new TibjmsQueueConnectionFactory(
          serverUrl );
      connection2 = factory.createQueueConnection( userName, password );
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

  // same function like the one in AbstractClient
  protected final void postReceive( int i, EchoPDU receivedPdu, long rtt )
  {
    // Response-Zaehler erhoehen
    sharedData.incrReceivedMsgCounter( clientNumber, rtt,
        receivedPdu.getServerTime() );

    log.debug( receivedPdu.getClientThreadName() + ": RTT fuer Request "
        + ( i + 1 ) + ": " + rtt + " ns" );
    log.debug( receivedPdu.getClientThreadName()
        + ": Echo-Nachricht empfangen von  "
        + receivedPdu.getServerThreadName() + ":" + receivedPdu.getMessage() );
    log.debug( Thread.currentThread().getName() + "Benoetigte Serverzeit: "
        + receivedPdu.getServerTime() );
  }
}