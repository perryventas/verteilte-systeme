package edu.hm.dako.echo.connection.ems;

import java.io.Serializable;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tibco.tibjms.TibjmsQueueConnectionFactory;

import edu.hm.dako.echo.common.CONSTANTS;
import edu.hm.dako.echo.common.EchoPDU;
import edu.hm.dako.echo.connection.Connection;

/**
 * @author Christoph Friegel
 * @author Tore Offermann
 * @version 1.0
 */

public class EMSConnection implements Connection, ExceptionListener
{

  private static Log log = LogFactory.getLog( EMSConnection.class );

  private QueueConnection connection = null;
  private QueueSession session = null;
  private Queue requestQueue = null;
  private MessageProducer producer = null;

  private QueueReceiver receiver = null;
  private Queue responseQueue = null;

  private String userName = CONSTANTS.USER_NAME;
  private String password = CONSTANTS.PASSWORD;

  private String requestQueueName = CONSTANTS.REQUEST_QUEUE_NAME;
  private String responseQueueName = CONSTANTS.RESPONSE_QUEUE_NAME;

  public EMSConnection( TibjmsQueueConnectionFactory factory )
  {
    try
    {
      this.connection = factory.createQueueConnection( userName, password );
    }
    catch ( JMSException e )
    {
      log.error( "Error creating queue." );
      return;
    }

    if ( this.connection != null )
    {
      log.info( Thread.currentThread().getName()
          + ": Verbindung mit neuem Client aufgebaut ueber EMS "
          + this.connection.toString() );
      try
      {
        connection.setExceptionListener( this );
      }
      catch ( JMSException e )
      {
        log.error( "Error setting exception listener." );
      }

      try
      {
        this.connection.start();
      }
      catch ( JMSException e )
      {
        log.error( "Error starting connection." );
      }
    }

    try
    {
      session = connection.createQueueSession( false, Session.AUTO_ACKNOWLEDGE );
    }
    catch ( JMSException e )
    {
      log.error( "Error creating session" );
      return;
    }

    try
    {
      requestQueue = session.createQueue( requestQueueName );
      responseQueue = session.createQueue( responseQueueName );
    }
    catch ( JMSException e )
    {
      log.error( "Error creating queues" );
      return;
    }

    try
    {
      producer = session.createProducer( requestQueue );
      receiver = session.createReceiver( responseQueue );
    }
    catch ( JMSException e )
    {
      log.error( "Error creating producer/receiver" );
    }

  }

  @Override
  public void onException( JMSException e )
  {
    log.error( e.getMessage() );
  }

  @Override
  public Serializable receive() throws Exception
  {

    /* blocking */
    Message message = this.receiver.receive();

    if ( message == null )
    {
      return null;
    }

    ObjectMessage objMsg = (ObjectMessage) message;
    EchoPDU pdu = (EchoPDU) objMsg.getObject();

    return pdu;
  }

  @Override
  public void send( Serializable message ) throws Exception
  {
    ObjectMessage msg = this.session.createObjectMessage( message );
    this.producer.send( msg );
  }

  @Override
  public void close() throws Exception
  {
    this.producer.close();
    this.receiver.close();
    this.connection.close();
  }

}
