package edu.hm.dako.echo.client;

import edu.hm.dako.echo.common.EchoPDU;
import edu.hm.dako.echo.common.ExceptionHandler;
import edu.hm.dako.echo.common.SharedClientStatistics;
import edu.hm.dako.echo.connection.Connection;
import edu.hm.dako.echo.connection.ConnectionFactory;
import edu.hm.dako.echo.connection.ems.tibjmsQueueReceiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tibco.tibjms.TibjmsQueueConnectionFactory;

import java.net.SocketTimeoutException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;

/**
 * Baut pro Client eine Verbindung zum Server auf und benutzt diese, um alle Nachrichten zu versenden.
 * Nachdem alle Nachrichten versendet wurden, wird die Verbindung abgebaut.
 */
public class EMSConnectionReusingClient extends AbstractClient {

    private static Log log = LogFactory.getLog(EMSConnectionReusingClient.class);

    private Connection connection;
    
    private QueueConnection connection2 = null;
    private QueueSession session = null;
    private QueueReceiver receiver = null;
    private Queue responseQueue = null;
    private String serverUrl = null;
    private final String userName = "dev";
    private final String password = "dev";
    private final String responseQueueName = "dev.response";

    public EMSConnectionReusingClient(int serverPort, String remoteServerAddress, int numberOfClient,
                                   int messageLength, int numberOfMessages, int clientThinkTime,
                                   SharedClientStatistics sharedData, ConnectionFactory connectionFactory) {
        super(serverPort, remoteServerAddress, numberOfClient, messageLength, numberOfMessages, clientThinkTime,
                sharedData, connectionFactory);
        serverUrl = "tcp://" + remoteServerAddress + ":" + serverPort;
    }

    /**
     * Client-Thread sendet hier alle Requests und wartet auf Antworten
     */
    @Override
    public void run() {
        Thread.currentThread().setName("Client-Thread-" + clientNumber);
        try {
        	receiveEcho();
        	
            waitForOtherClients();
            connection = connectionFactory.connectToServer(remoteServerAddress, serverPort, localPort);
            for (int i = 0; i < numberOfMessagesToSend; i++) {
                try {
                    doEcho(i);
                } catch (SocketTimeoutException e) {
                   log.debug(e.getMessage());
                }
            }
        } catch (Exception e) {
            ExceptionHandler.logExceptionAndTerminate(e);
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                ExceptionHandler.logException(e);
            }
        }
    }
    
    private void doEcho(int i) throws Exception {
        sharedData.incrSentMsgCounter(clientNumber);
        connection.send(constructEchoPDU(i));
        Thread.sleep(clientThinkTime);
    }
    
    private void receiveEcho() throws Exception {
    	//connection first
        TibjmsQueueConnectionFactory factory = new TibjmsQueueConnectionFactory(serverUrl);
        this.connection2 = factory.createQueueConnection(userName, password);
        this.session = connection2.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        this.responseQueue = session.createQueue(responseQueueName);
        this.receiver = session.createReceiver(responseQueue);
        
        //thread second, gimme pdus derp
		new EMSQueueReciever(this.numberOfMessagesToSend,this.connection2, this.receiver).run();
    }
    
    
    


    private class EMSQueueReciever implements Runnable {

        private QueueConnection connection2;
        private QueueReceiver receiver;
        private int numberOfMessagesToSend;
        private int receivedMessageNum = 0;

        private EMSQueueReciever(int numberOfMessagesToSend,
        		QueueConnection connection2,
                QueueReceiver receiver) {
        	this.numberOfMessagesToSend = numberOfMessagesToSend;
            this.connection2 = connection2;
            this.receiver = receiver;
        }

        @Override
        public void run() {
        	startConnection();
           
            while (receivedMessageNum < numberOfMessagesToSend && !Thread.currentThread().isInterrupted()) { //was wenn nur 90/100 ankommen?
                try {
                    javax.jms.Message message = receiver.receive();
                    if (message == null)
                        break;

                    log.debug("Received message: "+message);
                    
                    receivedMessageNum++;
                    
                    ObjectMessage objMsg = (ObjectMessage) message;
                    EchoPDU receivedPdu = (EchoPDU) objMsg.getObject();
                    long rtt = System.nanoTime() - receivedPdu.getClientTime();
                    postReceive(receivedMessageNum, receivedPdu, rtt);

                } catch (Exception e) {
                    closeConnection();
                    throw new RuntimeException(e);
                }
            }
            closeConnection();
            log.debug(Thread.currentThread().getName() + " beendet sich");
        }
        
        private void startConnection() {
            try {
        		connection2.start();
                log.debug("Warte auf PDUs...putt-putt-putt-putt...");
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }

        private void closeConnection() {
            try {
            	connection2.close();
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
  