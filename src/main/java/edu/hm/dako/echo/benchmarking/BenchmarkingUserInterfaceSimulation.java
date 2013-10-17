package edu.hm.dako.echo.benchmarking;

import org.apache.log4j.PropertyConfigurator;

/**
 * Diese Klasse simuliert eine Benutzeroberflaeche.
 *
 * @author mandl
 */
public class BenchmarkingUserInterfaceSimulation implements BenchmarkingClientUserInterface {

    private int timeCounter = 0;

    @Override
    public void showStartData(UserInterfaceStartData data) {

        System.out.println("Testbeginn: " + data.getStartTime());
        System.out.println("Geplante Requests: " + data.getNumberOfRequests());
    }

    @Override
    public void showResultData(UserInterfaceResultData data) {

        System.out.println("Testende: " + data.getEndTime());
        System.out.println("Testdauer in s: " + data.getElapsedTime());

        System.out.println("Gesendete Requests: " + data.getNumberOfSentRequests());
        System.out.println("Anzahl Responses: " + data.getNumberOfResponses());
        System.out.println("Anzahl verlorener Responses: " + data.getNumberOfLostResponses());

        System.out.println("Mittlere RTT in ms: " + data.getAvgRTT());
        System.out.println("Maximale RTT in ms: " + data.getMaxRTT());
        System.out.println("Minimale RTT in ms: " + data.getMinRTT());
        System.out.println("Mittlere Serverbearbeitungszeit in ms: " + data.getAvgServerTime());

        System.out.println("Maximale Heap-Belegung in MByte: " + data.getMaxHeapSize());
        System.out.println("Maximale CPU-Auslastung in %: " + data.getMaxCpuUsage());
    }

    @Override
    public void setMessageLine(String message) {
        System.out.println("*** Meldung: " + message + " ***");
    }

    @Override
    public void addCurrentRunTime(long sec) {
        // Feld Testdauer um sec erhoehen
        timeCounter += sec;
        System.out.println("Laufzeitzaehler: " + timeCounter);
    }

    @Override
    public void resetCurrentRunTime() {
        // Feld Testdauer auf 0 setzen
        timeCounter = 0;
    }

    /**
     * main
     *
     * @param args
     */
    public static void main(String args[]) {
        PropertyConfigurator.configureAndWatch("log4j.client.properties", 60 * 1000);
        new BenchmarkingUserInterfaceSimulation().doWork();

    }

    public void doWork() {
        // Input-parameter aus GUI
        UserInterfaceInputParameters parm = new UserInterfaceInputParameters();

        // GUI sammmelt Eingabedaten ...

        // Benchmarking-Client instanzieren und Benchmark starten

        BenchmarkingClient benchClient = new BenchmarkingClient();
        benchClient.executeTest(parm, this);
    }
}