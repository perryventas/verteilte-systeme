package edu.hm.dako.echo.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.concurrent.CountDownLatch;

/**
 * Klasse SharedClientStatistics
 * <p/>
 * Die Klasse sammelt Statistikdaten zur Ermittlung von Round Trip Times (RTT) fuer einen
 * Test zur Kommunikation zwischen mehreren Client-Threads und einem Server.
 * <p/>
 * Die Daten werden in einem Array gesammelt, das einen Eintrag fuer jeden Client
 * enthaelt. Jeder Client erhaelt eine Nummer, die als Zugriffsindex auf das Array verwendet
 * wird.
 *
 * @author Mandl
 */
public class SharedClientStatistics {
    private static Log log = LogFactory.getLog(SharedClientStatistics.class);

    // Anzahl von Clients
    private int numberOfClients;

    // Anzahl der Nachrichten (Requests) eines Clients
    private int numberOfMessages;

    // Denkzeit eines Clients zwischen zwei Requests in ms
    private int clientThinkTime;

    // Alle Antwortnachrichten, die fuer den Test empfangen werden muessen
    private int numberOfAllMessages;

    // Zaehlt angemeldete Clients
    private int numberOfLoggedInClients;

    // Kann benutzt werden um ein gleichzeitiges Starten aller Client-Threads zu ermöglichen
    private CountDownLatch startSignal;

    // Statistikdaten eines Clients
    private class ClientStatistics {
        int sentRequests;         // Anzahl gesendeter Nachrichten
        int receivedResponses;  // Anzahl empfangener Antworten
        long averageRTT;         // Durchschnittliche Round Trip Time in ns
        long sumRTT;             // Summe aller RTTs in ns
        long sumServerTime;     // Zeit, die der Server insgesamt fuer alle Requests benoetigt in ns
        long maxHeapSize;        // Maximale Heap-Groesse in Bytes waehrend eines Testlaufs
    }

    // Statistik-Tabelle fuer die empfangenen Respoonse-Nachrichten aller Clients
    private ClientStatistics clientStatistics[];

    /**
     * Test, ob Client-Id im gueltigen Bereich ist
     *
     * @param i Client-Id
     * @return true, falls Client-Id im gueltigen Bereich ist. Sonst false.
     */
    private boolean inRange(int i) {
        if ((i < 0) || (i > numberOfClients)) {
            log.error("Client-Id nicht im gueltigen Bereich");
            return false;
        } else {
            return true;
        }
    }

    public SharedClientStatistics(int numberOfClients, int numberOfMessages, int clientThinkTime) {
        this.numberOfClients = numberOfClients;
        this.numberOfMessages = numberOfMessages;
        this.clientThinkTime = clientThinkTime;
        this.numberOfAllMessages = numberOfClients * numberOfMessages;
        startSignal = new CountDownLatch(numberOfClients);
        clientStatistics = new ClientStatistics[numberOfClients];

        // Initialisieren der Statistik-Tabelle
        for (int i = 0; i < numberOfClients; i++) {
            clientStatistics[i] = new ClientStatistics();
            clientStatistics[i].receivedResponses = 0;
            clientStatistics[i].sentRequests = 0;
            clientStatistics[i].averageRTT = 0;
            clientStatistics[i].sumRTT = 0;
            clientStatistics[i].sumServerTime = 0;
            clientStatistics[i].maxHeapSize = 0;
        }
    }

    public CountDownLatch getStartSignal() {
        return startSignal;
    }

    /**
     * Anzahl der angemeldeten Clients erhoehen
     */
    public synchronized void incrNumberOfLoggedInClients() {
        numberOfLoggedInClients++;
        if (numberOfLoggedInClients == numberOfClients) {
            log.debug("Alle " + numberOfClients + " Test-Clients angemeldet");
        }
    }

    /**
     * Anzahl der gesendeten Nachrichten eines Clients erhoehen
     *
     * @param i Client-Id
     */
    public synchronized void incrSentMsgCounter(int i) {
        if (!inRange(i)) return;
        clientStatistics[i].sentRequests++;
    }

    /**
     * Anzahl der empfangenen Nachrichten eines Clients erhoehen
     *
     * @param i          Client-Id
     * @param rtt        RoundTrip Time
     * @param serverTime Die Zeit, die der Server benötigt hat
     */
    public synchronized void incrReceivedMsgCounter(int i, long rtt, long serverTime) {
        if (!inRange(i)) return;
        clientStatistics[i].receivedResponses++;
        clientStatistics[i].sumRTT = clientStatistics[i].sumRTT + rtt;
        clientStatistics[i].averageRTT = clientStatistics[i].sumRTT / clientStatistics[i].receivedResponses;
        clientStatistics[i].sumServerTime = clientStatistics[i].sumServerTime + serverTime;
        if (clientStatistics[i].maxHeapSize < usedMemory()) {
            clientStatistics[i].maxHeapSize = usedMemory();
        }
    }

    /**
     * Test, ob alle Response-Nachrichten empfangen wurden
     *
     * @return true Alle erwarteten Nachrichten empfangen;
     *         false Noch nicht alle erwarteten Nachrichten empfangen
     */
    public synchronized boolean allMessagesReceived() {
        int sum = 0;
        for (int i = 0; i < numberOfClients; i++) {
            sum += clientStatistics[i].receivedResponses;
        }
        return sum == numberOfAllMessages;
    }

    /**
     * Test, ob alle Clients angemeldet sind
     *
     * @return true angemeldet;
     *         false nicht angemeldet
     */
    public synchronized boolean allClientsLoggedIn() {
        return numberOfLoggedInClients == numberOfClients;
    }

    /**
     * Test, ob alle Nachrichten eines Clients angekommen sind
     *
     * @param i: Client-Nummer
     * @return true Alle angekommen;
     *         false Nicht alle angekommen
     */
    public synchronized boolean allMessageReceived(int i) {
        if (!inRange(i)) return (false);
        return clientStatistics[i].receivedResponses == numberOfMessages;
    }

    /**
     * Anzahl aller empfangenen Nachrichten ermitteln
     *
     * @return Anzahl empfangender Nachrichten
     */
    public synchronized int getSumOfAllReceivedMessages() {
        int sum = 0;
        for (int i = 0; i < numberOfClients; i++) {
            sum += clientStatistics[i].receivedResponses;
        }
        return sum;
    }

    /**
     * Durchschnittliche RTT ermitteln
     *
     * @return Durchschnittliche RTT
     */
    public synchronized long getAverageRTT() {
        long sum = 0;
        int nrClients = 0;

        for (int i = 0; i < numberOfClients; i++) {
            // Nur Threads, die mindestens eine Antwort bekommen haben, verwenden
            if (clientStatistics[i].receivedResponses > 0) {
                sum = sum + clientStatistics[i].averageRTT;
                nrClients++;
            }
        }

        if (nrClients > 0)
            return (sum / nrClients);
        else
            return 0;
    }

    public long getAverageRTT(int i) {
        return clientStatistics[i].averageRTT;
    }

    /**
     * Minimale RTT ueber alle Clients ermitteln
     *
     * @return Minimale RTT
     */
    public synchronized long getMinimumRTT() {
        long min = Long.MAX_VALUE;
        for (int i = 0; i < numberOfClients; i++) {
            // Nur Threads, die mindestens eine Antwort bekommen haben, verwenden
            if (clientStatistics[i].receivedResponses > 0) {
                min = Math.min(clientStatistics[i].averageRTT, min);
            }
        }

        return min;
    }

    /**
     * Maximale RTT ueber alle Clients ermitteln
     *
     * @return Maximale RTT
     */
    public synchronized long getMaximumRTT() {
        long max = -1;

        for (int i = 0; i < numberOfClients; i++) {
            // Nur Threads, die mindestens eine Antwort bekommen haben, verwenden
            if (clientStatistics[i].receivedResponses > 0) {
                if (clientStatistics[i].averageRTT > max) {
                    max = clientStatistics[i].averageRTT;
                }
            }
        }

        return max;
    }

    /**
     * Anzahl der gesendeten Requests aller Clients liefern
     *
     * @return Anzahl gesendeter Requests
     */
    public synchronized int getNumberOfSentRequests() {
        int sum = 0;

        for (int i = 0; i < numberOfClients; i++) {
            sum += clientStatistics[i].sentRequests;
        }

        return sum;
    }

    /**
     * Anzahl der gesendeten Requests eines Clients liefern
     *
     * @param i Client-Id
     * @return Anzahl gesendeter Requests des Clients i
     */
    public synchronized int getNumberOfSentRequests(int i) {
        if (!inRange(i)) return (-1);
        return clientStatistics[i].sentRequests;
    }

    /**
     * Anzahl der empfangenden Responses liefern
     *
     * @return Anzahl empfangener Responses
     */
    public synchronized int getNumberOfReceivedResponses() {
        int sum = 0;

        for (int i = 0; i < numberOfClients; i++) {
            sum += clientStatistics[i].receivedResponses;
        }

        return sum;
    }

    /**
     * Anzahl der empfangenen Responses eines Clients liefern
     *
     * @param i Client-Id
     * @return Anzahl empfangenerResponses des Clients i
     */
    public synchronized int getNumberOfReceivedResponses(int i) {
        if (!inRange(i)) return (-1);
        return clientStatistics[i].receivedResponses;
    }

    /**
     * Anzahl der verlorenen Responses liefern
     *
     * @return Anzahl verlorenen Responses
     */
    public synchronized int getNumberOfLostResponses() {
        int sum = 0;

        for (int i = 0; i < numberOfClients; i++) {
            sum += clientStatistics[i].sentRequests;
        }

        return sum - getNumberOfReceivedResponses();
    }

    /**
     * Anzahl der verlorenen Responses eines Clients liefern
     *
     * @param i Client-Id
     * @return Anzahl verlorenen Responses des Clients i
     */
    public synchronized int getNumberOfLostResponses(int i) {
        if (!inRange(i)) return (-1);
        return clientStatistics[i].sentRequests - getNumberOfReceivedResponses(i);
    }

    /**
     * Gesamte RTT ueber einen Client ermitteln
     *
     * @return RTT
     */
    public synchronized long getSumRTT(int i) {
        if (!inRange(i)) return (-1);
        return clientStatistics[i].sumRTT;
    }

    /**
     * Gesamte RTT ueber alle Clients ermitteln
     *
     * @return RTT
     */
    public synchronized long getSumRTT() {
        long sum = 0;

        for (int i = 0; i < numberOfClients; i++) {
            sum += clientStatistics[i].sumRTT;
        }

        return sum;
    }

    /**
     * Serverzeit eines Clients ermitteln
     *
     * @return Serverzeit
     */
    public synchronized long getSumServerTime(int i) {
        if (!inRange(i)) return (-1);
        return clientStatistics[i].sumServerTime;
    }

    /**
     * Gesamte Serverzeit ueber alle Clients ermitteln
     *
     * @return Serverzeit
     */
    public synchronized long getSumServerTime() {
        long sum = 0;

        for (int i = 0; i < numberOfClients; i++) {
            sum += clientStatistics[i].sumServerTime;
        }

        return sum;
    }

    /**
     * Durchschnittliche Serverbearbeitungszeit ermitteln
     *
     * @return Serverbearbeitungszeit
     */
    public synchronized long getAverageServerTime() {
        long sum = 0;
        int nrClients = 0;

        for (int i = 0; i < numberOfClients; i++) {
            // Nur Threads, die mindestens eine Antwort bekommen haben, verwenden
            if (clientStatistics[i].receivedResponses > 0) {
                sum = sum + clientStatistics[i].sumServerTime;
                nrClients++;
            }
        }

        if (nrClients > 0)
            return (sum / nrClients);
        else
            return 0;
    }

    /**
     * Maximale Heap-Groesse ueber alle Clients ermitteln
     *
     * @return Maximale Heap-Groesse
     */
    public synchronized long getMaxHeapSize() {
        long max = -1;

        for (int i = 0; i < numberOfClients; i++) {
            // Nur Threads, die mindestens eine Antwort bekommen haben, verwenden
            if (clientStatistics[i].receivedResponses > 0) {
                if (clientStatistics[i].maxHeapSize > max) {
                    max = clientStatistics[i].maxHeapSize;
                }
            }
        }

        return max;
    }

    /**
     * Ausgabe Statistikdaten fuer einen Client
     *
     * @param i Client-Id
     */
    public synchronized void printClientStatistic(int i) {
        if (!inRange(i)) return;

        System.out.println("********************** Client-Statistik *****************************"
                + "\n" + "Sende-/Empfangsstatistik des Clients mit Id " + i + " (" + Thread.currentThread().getName() + ")"
                + "\n" + "Anzahl Requests gesendet: " + this.getNumberOfSentRequests(i)
                + "\n" + "Anzahl empfangener Responses: " + this.getNumberOfReceivedResponses(i)
                + "\n" + "Anzahl verlorener Responses: " + this.getNumberOfLostResponses(i)
                + "\n" + "Durchschnittliche RTT: " + this.getAverageRTT(i) + " ns = " + this.getAverageRTT(i) / 1000000 + " ms"
                + "\n" + "Gesamte RTT: " + this.getSumRTT(i) + " ns = " + this.getSumRTT(i) / 1000000 + " ms"
                + "\n" + "Gesamte Serverzeit: " + this.getSumServerTime(i) + " ns = " + this.getSumServerTime(i) / 1000000 + " ms"
                + "\n" + "Gesamte Kommunikationszeit: " + (this.getSumRTT(i) - this.getSumServerTime(i)) + " ns = " +
                (this.getSumRTT(i) - this.getSumServerTime(i) / 1000000) + " ms"
                + "\n" + "********************** Ende Client-Statistik ************************");
    }

    /**
     * Ausgabe aller Statistikdaten
     */
    public synchronized void printStatistic() {

        NumberFormat n = NumberFormat.getInstance();
        //n.setMaximumFractionDigits(2);
        String usedMemoryAsString = n.format(usedMemory() / (1024 * 1024));

        System.out.println("*********************************************************************"
                + "\n" + "***************************** Statistik *****************************"
                + "\n" + "Geplante Requests: " + numberOfAllMessages
                + "\n" + "Anzahl Clients: " + numberOfClients
                + "\n" + "Denkzeit des Clients zwischen zwei Requests: " + clientThinkTime + " ms"
                + "\n" + "Anzahl gesendeter Requests: " + this.getNumberOfSentRequests()
                + "\n" + "Anzahl empfangener Responses: " + this.getSumOfAllReceivedMessages() + " von erwarteten " + numberOfAllMessages
                + "\n"
                + "\n" + "Gesamte RTT ueber alle Clients: " + this.getSumRTT() + " ns ("
                + (this.getSumRTT() / 1000000) + " ms)"
                + "\n" + "Gesamte Serverzeit ueber alle Clients: " + this.getSumServerTime() + " ns  (" +
                (this.getSumServerTime() / 1000000) + " ms)"
                + "\n" + "Reine Kommunikationszeit ueber alle Clients: " + (this.getSumRTT() - this.getSumServerTime()) + " ns (" +
                ((this.getSumRTT() - this.getSumServerTime()) / 1000000) + " ms)"
                + "\n\n" + "Durchschnittswerte ueber alle Clients:"
                + "\n" + "RTT: " + this.getAverageRTT() + " ns (" + this.getAverageRTT() / 1000000 + " ms)"
                + "\n" + "Minimum RTT: " + this.getMinimumRTT() + " ns (" + this.getMinimumRTT() / 1000000 + " ms)"
                + "\n" + "Maximum RTT: " + this.getMaximumRTT() + " ns (" + this.getMaximumRTT() / 1000000 + " ms)"
                + "\n" + "Reine Serverzeit: " + this.getAverageServerTime() / numberOfClients + " ns (" +
                (this.getAverageServerTime() / numberOfClients) / 1000000 + " ms)"
                + "\n" + "Maximal erreichte Heap-Belegung: " + usedMemoryAsString + " MByte"

                + "\n" + "************************ Ende Statistik *****************************"
                + "\n" + "*********************************************************************");
    }

    /**
     * Ausgabe eines Auswertungssatzes fuer eine Messung (einen Benchmark-Lauf)
     * in eine Datei im CSV-Dateiformat in folgender Form:
     * <p/>
     * Messungstyp als String
     * Implementierungstyp als String
     * Anzahl Client-Threads
     * Durchschnittliche RTT
     * Maximale RTT
     * Minimale RTT
     * Durchschnittliche Serverbearbeitungszeit
     * Anzahl geplanter Requests/Responses
     * Anzahl gesendeter Requests
     * Anzahl empfangener Responses
     * Anzahl verlorener Echo Responses
     * Maximale Heap-Size des Clients
     * Startzeit der Messung
     * Endezeit der Messung
     * <p/>
     * Die Werte fuer die "Maximale Heap-Size des Clients" werden noch nicht ermittelt.
     * <p/>
     * Der Satz wird an das Ende einer bestehenden Datei angehaengt.
     * Die Datei kann zur Testauswertung in Excel weiterverarbeitet werden.
     *
     * @param fileName    Name der Datei
     * @param implType    Typ der Implementierung
     * @param measureType Typ der Messung
     * @param startTime   der Messung
     * @param endTime     der Messung
     */

    public synchronized void writeStatisticSet
    (String fileName, String implType, String measureType, String startTime, String endTime) {
        File file = new File(fileName);
        final String newLine = "\r\n";

        try {
            boolean exist = file.createNewFile();
            if (!exist) {
                log.debug("Datei " + fileName + " existierte bereits");
            } else {
                log.debug("Datei " + fileName + " erfolgreich angelegt");
            }

            // Datei zum Erweitern oeffnen
            FileWriter fstream = new FileWriter(fileName, true);
            BufferedWriter out = new BufferedWriter(fstream);

            out.write(measureType + ", " +
                    implType + ", " +
                    numberOfClients + ", " +
                    this.getAverageRTT() / 1000000 + ", " +
                    this.getMaximumRTT() / 1000000 + ", " +
                    this.getMinimumRTT() / 1000000 + ", " +
                    this.getAverageServerTime() / 1000000 + ", " +
                    numberOfAllMessages + ", " +
                    this.getNumberOfSentRequests() + ", " +
                    this.getNumberOfReceivedResponses() + ", " +
                    this.getNumberOfLostResponses() + ", " +
                    this.getMaxHeapSize() / (1024 * 1024) + ", " +
                    startTime + ", " +
                    endTime +
                    newLine);

            System.out.println("Auswertungssatz in Datei " + fileName + " geschrieben");

            out.flush();
            out.close();

        } catch (IOException e) {
            log.error("Fehler beim Schreiben des Auswertungssatzes in Datei " + fileName);
        }
    }

    /**
     * Berechnet den tatsaechlich benutzten Heap-Speicher
     * Heap-Groesse in MByte
     */
    private long usedMemory() {
        Runtime r = Runtime.getRuntime();
        long usedMemory = (r.totalMemory() - r.freeMemory());
        return (usedMemory);
    }
}