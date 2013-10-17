package edu.hm.dako.echo.benchmarking;

public class UserInterfaceResultData {

    private long numberOfSentRequests;  // Anzahl gesendeter Requests
    private long numberOfResponses;     // Anzahl empfangener Responses
    private long numberOfLostResponses; // Anzahl verlorener (nicht empfangener) Responses
    private String endTime;             // Testende als Datum/Uhrzeit-String
    private long elapsedTime;           // Testdauer in Sekunden
    private double avgRTT;              // Mittlere RTT in ms
    private double maxRTT;              // Maximale RTT in ms
    private double minRTT;              // Minimale RTT in ms
    private double avgServerTime;       // Mittlere Serverbearbeitungszeit in ms
    private long maxHeapSize;           // Maximale Heap-Belegung waehrend des Testlaufs in MByte
    private float maxCpuUsage;          // Maximale CPU-Auslastung waehrend des Testlaufs in Prozent


    public long getNumberOfSentRequests() {
        return numberOfSentRequests;
    }

    public void setNumberOfSentRequests(long numberOfSentRequests) {
        this.numberOfSentRequests = numberOfSentRequests;
    }

    public long getNumberOfResponses() {
        return numberOfResponses;
    }

    public void setNumberOfResponses(long numberOfResponses) {
        this.numberOfResponses = numberOfResponses;
    }

    public long getNumberOfLostResponses() {
        return numberOfLostResponses;
    }

    public void setNumberOfLostResponses(long numberOfLostResponses) {
        this.numberOfLostResponses = numberOfLostResponses;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getAvgRTT() {
        return avgRTT;
    }

    public void setAvgRTT(double avgRTT) {
        this.avgRTT = avgRTT;
    }

    public double getMaxRTT() {
        return maxRTT;
    }

    public void setMaxRTT(double maxRTT) {
        this.maxRTT = maxRTT;
    }

    public double getMinRTT() {
        return minRTT;
    }

    public void setMinRTT(double minRTT) {
        this.minRTT = minRTT;
    }

    public double getAvgServerTime() {
        return avgServerTime;
    }

    public void setAvgServerTime(double avgServerTime) {
        this.avgServerTime = avgServerTime;
    }

    public long getMaxHeapSize() {
        return maxHeapSize;
    }

    public void setMaxHeapSize(long maxHeapSize) {
        this.maxHeapSize = maxHeapSize;
    }

    public float getMaxCpuUsage() {
        return maxCpuUsage;
    }

    public void setMaxCpuUsage(float maxCpuUsage) {
        this.maxCpuUsage = maxCpuUsage;
    }
}