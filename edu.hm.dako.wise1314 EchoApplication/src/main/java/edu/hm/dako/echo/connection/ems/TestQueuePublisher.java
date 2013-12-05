package edu.hm.dako.echo.connection.ems;

import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.tibco.tibjms.TibjmsQueueConnectionFactory;

/**
 * @author Christoph Friegel
 * @version 1.0
 */
public class TestQueuePublisher extends TestEMSUnit
{

  public void test()
  {
    try
    {
      TibjmsQueueConnectionFactory factory = new TibjmsQueueConnectionFactory(
          serverUrl );
      QueueConnection connection = factory.createQueueConnection( userName,
          password );
      QueueSession session = connection.createQueueSession( false,
          Session.AUTO_ACKNOWLEDGE );

      Queue queue = session.createQueue( queueName );
      MessageProducer producer = session.createProducer( queue );
      TextMessage message = session.createTextMessage();

      for ( int i = 0; i < 4; i++ )
      {
        message.setText( "Chris-Test-Nachricht: "
            + new java.util.Date().toString() );
        producer.send( message );
      }

      connection.close();
      System.out.println( "over" );
    }
    catch ( Exception e )
    {
      e.printStackTrace();
    }
  }

  public static void main( String[] args )
  {
    new TestQueuePublisher().test();
  }
}
