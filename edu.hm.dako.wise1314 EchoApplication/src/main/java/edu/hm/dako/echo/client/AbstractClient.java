package edu.hm.dako.echo.client;


import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.hm.dako.echo.common.EchoPDU;
import edu.hm.dako.echo.common.SharedClientStatistics;
import edu.hm.dako.echo.connection.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basis fuer konkrete Client-Implementierungen.
 */
public abstract class AbstractClient implements Runnable {

    private static Log log = LogFactory.getLog(AbstractClient.class);

    protected String threadName;

    protected int clientNumber;

    protected int messageLength;

    protected int numberOfMessagesToSend;

    protected int serverPort;

    protected int localPort;

    protected String remoteServerAddress;

    /**
     * Denkzeit des Clients zwischen zwei Requests in ms
     */
    protected int clientThinkTime;

    /**
     * Gemeinsame Daten der Threads
     */
    protected SharedClientStatistics sharedData;

    protected ConnectionFactory connectionFactory;

    /**
     * @param serverPort             Port des Servers
     * @param remoteServerAddress    Adresse des Servers
     * @param clientNumber           Laufende Nummer des Test-Clients
     * @param messageLength          Laenge einer Nachricht
     * @param numberOfMessagesToSend Anzahl zu sendender Nachrichten je Thread
     * @param clientThinkTime        Denkzeit des Test-Clients
     * @param sharedData             Gemeinsame Daten der Threads
     * @param connectionFactory      Der zu verwendende Client
     */
    public AbstractClient(int serverPort, String remoteServerAddress, int clientNumber, int messageLength,
                          int numberOfMessagesToSend, int clientThinkTime, SharedClientStatistics sharedData,
                          ConnectionFactory connectionFactory) {
        this.serverPort = serverPort;
        this.remoteServerAddress = remoteServerAddress;
        this.clientNumber = clientNumber;
        this.messageLength = messageLength;
        this.numberOfMessagesToSend = numberOfMessagesToSend;
        this.clientThinkTime = clientThinkTime;
        this.sharedData = sharedData;
        Thread.currentThread().setName("EchoClient-" + String.valueOf(clientNumber + 1));
        threadName = Thread.currentThread().getName();
        this.connectionFactory = connectionFactory;
    }

    /**
     * Synchronisation mit allen anderen Client-Threads:
     * Warten, bis alle Clients angemeldet sind und dann
     * erst mit der Lasterzeugung beginnen
     *
     * @throws InterruptedException falls sleep unterbrochen wurde
     */
    protected void waitForOtherClients() throws InterruptedException {
        sharedData.getStartSignal().countDown();
        sharedData.getStartSignal().await();
    }

    protected EchoPDU constructEchoPDU(int messageNumber) {
        // Echo-Nachricht aufbauen
        EchoPDU pdu = new EchoPDU();
        String computername;
		try {
			computername = InetAddress.getLocalHost().getHostName();
	        pdu.setClientName( computername );
		} catch (UnknownHostException e) {
			log.debug("Lokaler Name konnte nicht bezogen werden: " + e.getMessage());
		}

        pdu.setClientThreadName(Thread.currentThread().getName());
        
        String echoMsg = "";
        for (int j = 0; j < messageLength; j++) {
            echoMsg += "+";
        }
        pdu.setMessage(echoMsg);

        // Letzter Request?
        if (messageNumber == (numberOfMessagesToSend - 1)) {
            pdu.setLastRequest(true);
        }
        return pdu;
    }

    protected final void postReceive(int i, EchoPDU receivedPdu, long rtt) {
        // Response-Zaehler erhoehen
        sharedData.incrReceivedMsgCounter(clientNumber, rtt, receivedPdu.getServerTime());

        log.debug(threadName + ": RTT fuer Request " + (i + 1) + ": " + rtt + " ns");
        log.debug(threadName + ": Echo-Nachricht empfangen von  " + receivedPdu.getServerThreadName() + ":"
                + receivedPdu.getMessage());
        log.debug(Thread.currentThread().getName() + "Benoetigte Serverzeit: " + receivedPdu.getServerTime());
    }
}