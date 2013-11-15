package edu.hm.dako.echo.server;

import com.tibco.tibjms.TibjmsQueueConnectionFactory;
import com.tibco.tibjms.TibjmsTopicConnectionFactory;

import edu.hm.dako.echo.common.CONSTANTS;
import edu.hm.dako.echo.common.EchoPDU;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

public class EMSEchoServerImpl implements EchoServer, MessageListener {

    private static Log log = LogFactory.getLog(EMSEchoServerImpl.class);

    private QueueConnection connection = null;
    private Boolean isConnected = false;

    private QueueSession session = null;

    private Queue requestQueue = null;
    private MessageProducer producer = null;

    private QueueReceiver receiver = null;
    private Queue responseQueue = null;

    private final String userName = CONSTANTS.USER_NAME;
    private final String password = CONSTANTS.PASSWORD;

    private final String requestQueueName = CONSTANTS.REQUEST_QUEUE_NAME;
    private final String responseQueueName = CONSTANTS.RESPONSE_QUEUE_NAME;

    /* Topic part. */
    private TopicConnection tConnection = null;
    private TopicSession tSession = null;
    private TopicPublisher publisher = null;
    private MessageConsumer consumer = null;
    private Topic dbtatopic = null;
    private Topic dbtaacktopic = null;
    private final String dbTaTopicName = CONSTANTS.DB_TA_TOPIC_NAME;
    private final String dbTaAckTopicName = CONSTANTS.DB_TA_ACK_TOPIC_NAME;
    
    private Boolean connectToEms() {

        String serverUrl = CONSTANTS.SERVER_URL;

        TibjmsQueueConnectionFactory queueFactory = 
                new TibjmsQueueConnectionFactory(serverUrl);
        
        TibjmsTopicConnectionFactory topicFactory =
                new TibjmsTopicConnectionFactory(serverUrl);

        while (!isConnected) {
            try {
                this.connection = queueFactory.createQueueConnection(userName, password);
                this.session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
                this.requestQueue = session.createQueue(requestQueueName);
                this.responseQueue = session.createQueue(responseQueueName);

                this.tConnection = topicFactory.createTopicConnection(userName, password);
                this.tSession = tConnection.createTopicSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
                this.dbtatopic = tSession.createTopic(dbTaTopicName);
                this.publisher = tSession.createPublisher(dbtatopic);
                
                this.dbtaacktopic = tSession.createTopic(dbTaAckTopicName);
                this.consumer = tSession.createConsumer(dbtaacktopic);
                this.consumer.setMessageListener(this);
                
                this.producer = session.createProducer(responseQueue);
                this.receiver = session.createReceiver(requestQueue);
                
                this.connection.start();
                this.tConnection.start();
            } catch (JMSException e) {
                System.out.println("Error");
                return false;
            }
            this.isConnected = true;
        }
        return true;
    }
    
    

    @Override
	public void onMessage(Message message) {
    	
    	log.debug("response");
    	
		try {
			this.producer.send(message);
		} catch (JMSException e) {
			log.error(e.toString());
		}   	
	}
    

	@Override
    public void start() {
        PropertyConfigurator.configureAndWatch("log4j.server.properties", 60 * 1000);
        System.out.println("EMS-Echo-Server wartet auf requests...");

        this.connectToEms();
        if (this.isConnected) {
            new EchoWorker(this.connection, 
                           this.session, 
                           this.receiver, 
                           this.producer, 
                           this.publisher,
                           this.tSession).run();
        }

    }

    @Override
    public void stop() throws Exception {
        System.out.println("EchoServer beendet sich");
        Thread.currentThread().interrupt();
        connection.close();
    }

    private class EchoWorker implements Runnable {

        private final QueueConnection con;
        private final QueueSession sess;
        private final QueueReceiver rec;
        private final MessageProducer prod;
        private final TopicPublisher pub;
        private final TopicSession tSession;

        private boolean finished = false;

        long startTime;

        private EchoWorker(QueueConnection connection,
                QueueSession session,
                QueueReceiver rec,
                MessageProducer prod,
                TopicPublisher pub,
                TopicSession tSession) {
            this.sess = session;
            this.con = connection;
            this.rec = rec;
            this.prod = prod;
            this.pub = pub;
            this.tSession = tSession;
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

            //log.debug("before queue receive");
            Message message = rec.receive();
            //log.debug("after queue receive");

            log.debug("request");

            ObjectMessage objMsg = (ObjectMessage) message;
            EchoPDU receivedPdu = (EchoPDU) objMsg.getObject();
            startTime = System.nanoTime();

            EchoPDU pdu = EchoPDU.createServerEchoPDU(receivedPdu, startTime);
            ObjectMessage emsObj = this.sess.createObjectMessage(pdu);
           
            /* Publish PDU for the DTC. */
            pub.send(emsObj);
            
        }

        private void closeConnection() {
            try {
                con.close();
            } catch (JMSException e) {
                log.error("Error closing connection.");
            }
        }
    }

}
