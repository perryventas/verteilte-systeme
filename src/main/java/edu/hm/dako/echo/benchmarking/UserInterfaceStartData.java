package edu.hm.dako.echo.benchmarking;

public class UserInterfaceStartData {

    long numberOfRequests;      // Anzahl geplanter Requests
    String startTime;           // Zeit des Testbeginns


    public long getNumberOfRequests() {
        return numberOfRequests;
    }

    public void setNumberOfRequests(long numberOfRequests) {
        this.numberOfRequests = numberOfRequests;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}