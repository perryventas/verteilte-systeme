package edu.hm.dako.echo.dtc;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.tibco.tibjms.TibjmsTopicConnectionFactory;

import edu.hm.dako.echo.common.CONSTANTS;
import edu.hm.dako.echo.common.EchoPDU;
import edu.hm.dako.echo.dtc.RestInterface.RESULT;

/**
 * @author Tore Offermann
 * @version 1.0
 */

public class TransactionCoordinator implements MessageListener
{

  private static Log log = LogFactory.getLog( TransactionCoordinator.class );

  private Boolean isConnected = false;

  private final String userName = CONSTANTS.USER_NAME;
  private final String password = CONSTANTS.PASSWORD;

  private TopicConnection tConnection = null;
  private TopicSession tSession = null;

  private MessageConsumer consumer = null;
  private MessageProducer producer = null;

  private Topic dbtatopic = null;
  private Topic dbtatopicOne = null;

  private final String dbTaTopicName = CONSTANTS.DB_TA_TOPIC_NAME;
  private final String dbTaAckTopicName = CONSTANTS.DB_TA_ACK_TOPIC_NAME;

  public TransactionCoordinator()
  {

    String serverUrl = CONSTANTS.SERVER_URL;

    TibjmsTopicConnectionFactory topicFactory = new TibjmsTopicConnectionFactory(
        serverUrl );

    while ( !isConnected )
    {
      try
      {

        this.tConnection = topicFactory.createTopicConnection( userName,
            password );
        this.tSession = tConnection.createTopicSession( false,
            Session.AUTO_ACKNOWLEDGE );
        this.dbtatopic = tSession.createTopic( dbTaTopicName );
        this.dbtatopicOne = tSession.createTopic( dbTaAckTopicName );

        this.producer = tSession.createProducer( dbtatopicOne );

        this.consumer = tSession.createConsumer( dbtatopic );
        this.consumer.setMessageListener( this );

        log.debug( "subscribing to: " + dbTaTopicName );

        this.tConnection.start();
        log.debug( "waiting for messages..." );

        /* DIRTY */
        while ( true )
        {
          try
          {
            Thread.sleep( 1000 );
          }
          catch ( Exception ex )
          {
          }
        }
      }
      catch ( JMSException e )
      {
        log.error( "Error: " + e.toString() );
        System.exit( 0 );
      }
      this.isConnected = true;
    }
    System.exit( 0 );
  }

  @Override
  public void onMessage( Message message )
  {

    log.debug( "received message" );

    ObjectMessage objMsg = (ObjectMessage) message;
    EchoPDU receivedPdu;
    try
    {
      receivedPdu = (EchoPDU) objMsg.getObject();

      log.debug( "starting transaction" );

      startTransaction( TRANSACTION_TYPE.COUNT, receivedPdu );
      startTransaction( TRANSACTION_TYPE.TRACE, receivedPdu );

      log.debug( "sending response" );
      ObjectMessage emsObj = this.tSession.createObjectMessage( receivedPdu );
      this.producer.send( emsObj );

      log.debug( "transaction complete" );

    }
    catch ( JMSException e )
    {
      log.error( "onMessage: " + e.toString() );
    }

  }

  private enum TRANSACTION_TYPE
  {
    COUNT, TRACE
  }

  private RestInterface.RESULT startTransaction( TRANSACTION_TYPE type,
      EchoPDU pdu )
  {
    RestInterface.RESULT res = RESULT.ERROR;

    switch ( type )
    {
      case COUNT:
        log.debug( "notifying COUNT server..." );
        res = RestInterface.notifyServer( pdu, CONSTANTS.COUNT_SERVER_URL );
        break;
      case TRACE:
        log.debug( "notifying TRACE server..." );
        res = RestInterface.notifyServer( pdu, CONSTANTS.TRACE_SERVER_URL );
        break;
    }

    switch ( res )
    {
      case OK:
        log.info( "result: OK" );
        return RESULT.OK;
      case ERROR:
        log.error( "result: ERROR" );
        return RESULT.ERROR;
    }
    return res;
  }

  public static void main( String[] args )
  {
    PropertyConfigurator.configureAndWatch( "log4j.dtc.properties", 60 * 1000 );
    log.info( "starting transation coordinator" );
    new TransactionCoordinator();
  }

}
