package edu.hm.dako.echo.client;

import edu.hm.dako.echo.common.EchoPDU;
import edu.hm.dako.echo.common.ExceptionHandler;
import edu.hm.dako.echo.common.SharedClientStatistics;
import edu.hm.dako.echo.connection.Connection;
import edu.hm.dako.echo.connection.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.SocketTimeoutException;

/**
 * Baut pro Client eine Verbindung zum Server auf und benutzt diese, um alle Nachrichten zu versenden.
 * Nachdem alle Nachrichten versendet wurden, wird die Verbindung abgebaut.
 */
public class ConnectionReusingClient extends AbstractClient {

    private static Log log = LogFactory.getLog(ConnectionReusingClient.class);

    private Connection connection;

    public ConnectionReusingClient(int serverPort, String remoteServerAddress, int numberOfClient,
                                   int messageLength, int numberOfMessages, int clientThinkTime,
                                   SharedClientStatistics sharedData, ConnectionFactory connectionFactory) {
        super(serverPort, remoteServerAddress, numberOfClient, messageLength, numberOfMessages, clientThinkTime,
                sharedData, connectionFactory);
    }

    /**
     * Client-Thread sendet hier alle Requests und wartet auf Antworten
     */
    @Override
    public void run() {
        Thread.currentThread().setName("Client-Thread-" + clientNumber);
        try {
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
        // RTT-Startzeit ermitteln
        long rttStartTime = System.nanoTime();
        sharedData.incrSentMsgCounter(clientNumber);
        connection.send(constructEchoPDU(i));
        EchoPDU receivedPdu = (EchoPDU) connection.receive();
        long rtt = System.nanoTime() - rttStartTime;
        postReceive(i, receivedPdu, rtt);
        Thread.sleep(clientThinkTime);
    }
}
  