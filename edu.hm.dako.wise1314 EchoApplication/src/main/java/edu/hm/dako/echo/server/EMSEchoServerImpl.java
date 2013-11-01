package edu.hm.dako.echo.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.tibco.tibjms.TibjmsQueueConnectionFactory;

import edu.hm.dako.echo.common.EchoPDU;

public class EMSEchoServerImpl implements EchoServer {
	
	private static Log log = LogFactory.getLog(EMSEchoServerImpl.class);
	
	private QueueConnection connection 		   = null;
	private Boolean 		isConnected		   = false;
	
	private QueueSession 	session	  		   = null;
	
	private Queue			requestQueue 	   = null;
	private MessageProducer producer   		   = null;
	
	private QueueReceiver 	receiver 		   = null;
	private Queue		    responseQueue	   = null;
	
	private String userName  				   = "dev";
	private String password                    = "dev";
	
	private String requestQueueName            = "dev.request";
	private String responseQueueName		   = "dev.response";
	
	
	
	private Boolean connectToEms() {
		
		String serverUrl = "tcp://moguai.org:7222";
		
		TibjmsQueueConnectionFactory factory = new TibjmsQueueConnectionFactory(serverUrl);
		
		while (!isConnected) {
			try {
				this.connection = factory.createQueueConnection( userName, password );
				this.session    = connection.createQueueSession( false, Session.AUTO_ACKNOWLEDGE );
				this.requestQueue    = session.createQueue( requestQueueName );
				this.responseQueue   = session.createQueue( responseQueueName );	
				this.producer = session.createProducer(responseQueue);
				this.receiver = session.createReceiver(requestQueue);
				this.connection.start();
			} catch( Exception e ) {
				System.out.println("Error");
				return false;
			}
			this.isConnected = true;
		}
		return true;
	}
	
	@Override
	public void start() {
		PropertyConfigurator.configureAndWatch("log4j.server.properties", 60 * 1000);
        System.out.println("Echoserver wartet auf Clients...");
        
        this.connectToEms();
        
        if (this.isConnected) {
        	
        	new EchoWorker(this.connection, this.session, this.receiver, this.producer).run();
        }
		
	}

	@Override
	public void stop() throws Exception {
		System.out.println("EchoServer beendet sich");
        Thread.currentThread().interrupt();
        connection.close();
	}
	
	
	private class EchoWorker implements Runnable {

		private final QueueConnection   con;
		private final QueueSession		sess;
		private final QueueReceiver		rec;
        private final MessageProducer 	prod;

        private boolean finished = false;

        long startTime;

        private EchoWorker(QueueConnection connection, 
        				   QueueSession session,
        				   QueueReceiver rec, 
        				   MessageProducer prod) {
        	this.sess = session;
        	this.con  = connection;
            this.rec = rec;
            this.prod = prod;
        }

        @Override
        public void run() {
            while (!finished && !Thread.currentThread().isInterrupted()) {
                try {
                    echo();
                } catch (Exception e) {
                    closeConnection();
                    throw new RuntimeException(e);
                }
            }
            log.debug(Thread.currentThread().getName() + " beendet sich");
            closeConnection();
        }

        private void echo() throws Exception {
           
        	System.out.println("before queue receive");
        	Message message = rec.receive();
        	System.out.println("after queue receive");
        	
        	if (message == null)
                return;
        	
        	System.out.println("received object message.");
        	
        	ObjectMessage objMsg = (ObjectMessage) message;
            EchoPDU receivedPdu = (EchoPDU) objMsg.getObject();
            startTime = System.nanoTime();
            
            EchoPDU pdu = EchoPDU.createServerEchoPDU(receivedPdu, startTime);
            
            ObjectMessage emsObj = this.sess.createObjectMessage( pdu );
            
            prod.send( emsObj );
            
            if (receivedPdu.getLastRequest()) {
                log.debug("Letzter Request des Clients " + receivedPdu.getClientName());
                finished = true;
            }
        }

        private void closeConnection() {          
        	try {
                con.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
	
	

}
