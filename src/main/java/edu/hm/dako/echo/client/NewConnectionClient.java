package edu.hm.dako.echo.client;

import edu.hm.dako.echo.common.EchoPDU;
import edu.hm.dako.echo.common.ExceptionHandler;
import edu.hm.dako.echo.common.SharedClientStatistics;
import edu.hm.dako.echo.connection.Connection;
import edu.hm.dako.echo.connection.ConnectionFactory;

/**
 * Baut fuer jede zu versendene Nachricht eine Verbindung zum Server auf und ab.
 */
public class NewConnectionClient extends AbstractClient {

    private Connection connection;

    public NewConnectionClient(int serverPort, String remoteServerAddress, int numberOfClient,
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
        } catch (InterruptedException e) {
            ExceptionHandler.logException(e);
        }
        for (int i = 0; i < numberOfMessagesToSend; i++) {
            doEcho(i);
        }
    }

    private void doEcho(int i) {
        try {
            // RTT-Startzeit ermitteln
            long rttStartTime = System.nanoTime();
            connection = connectionFactory.connectToServer(remoteServerAddress, serverPort, localPort);
            connection.send(constructEchoPDU(i));
            EchoPDU receivedPdu = (EchoPDU) connection.receive();
            long rtt = System.nanoTime() - rttStartTime;
            // Response-Zaehler erhoehen
            sharedData.incrSentMsgCounter(clientNumber);
            postReceive(i, receivedPdu, rtt);
            Thread.sleep(clientThinkTime);
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
}
