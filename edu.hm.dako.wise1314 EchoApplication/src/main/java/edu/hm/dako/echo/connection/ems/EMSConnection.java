package edu.hm.dako.echo.connection.ems;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tibco.tibjms.TibjmsQueueConnectionFactory;

import edu.hm.dako.echo.connection.Connection;

public class EMSConnection implements Connection {

	private static Log log = LogFactory.getLog(EMSConnection.class);
	
	private QueueConnection connection = null;
	private QueueSession 	session	   = null;
	private Queue			queue 	   = null;
	private MessageProducer producer   = null;
	
	private String userName   = "dev";
	private String password   = "dev";
	private String queueName  = "dev.request";
	
	public EMSConnection( TibjmsQueueConnectionFactory factory )
	{
		try {
			this.connection = factory.createQueueConnection( userName, password );
		} catch (JMSException e) {
			log.error("Error creating queue.");
			return;
		}
	
		if ( this.connection != null )
		{
			log.info(Thread.currentThread().getName() + 
					": Verbindung mit neuem Client aufgebaut ueber EMS " +
	                this.connection.toString());
		}
		
		try {
			session = connection.createQueueSession( false, Session.AUTO_ACKNOWLEDGE );
		} catch (JMSException e) {
			log.error("Error creating session");
			return;
		}
		
		try {
			queue = session.createQueue( queueName );
		} catch (JMSException e) {
			log.error("Error creating queue");
			return;
		}
		
		try {
			producer = session.createProducer(queue);
		} catch (JMSException e) {
			log.error("Error creating producer");
		}
		
	}
	
	@Override
	public Serializable receive() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void send(Serializable message) throws Exception {
		ObjectMessage msg = this.session.createObjectMessage( message );
		this.producer.send(msg);
	}

	@Override
	public void close() throws Exception {
		this.connection.close();
	}

}
