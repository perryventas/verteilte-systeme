package edu.hm.dako.echo.benchmarking;
/**
 * Schnittstelle zum Benchmarking-Client
 */

/**
 * Interface zur Uebergabe von Daten fuer die Ausgabe im
 * Benchmarking-Gui-Client
 *
 * @author Mandl
 */
public interface BenchmarkingClientUserInterface {

    /**
     * Uebergabe der Startdaten an die GUI
     *
     * @param data Startdaten
     */
    public void showStartData(UserInterfaceStartData data);

    /**
     * Uebergabe der Ergebnisdaten an die GUI
     *
     * @param data Testergebnisse
     */
    public void showResultData(UserInterfaceResultData data);

    /**
     * Uebergabe einer Nachricht an die GUI zur Ausgabe in der Messagezeile
     *
     * @param message Nachrichtentext
     */
    public void setMessageLine(String message);

    /**
     * Zuruecksetzen des Laufzeitzaehlers auf 0
     */
    public void resetCurrentRunTime();

    /**
     * Erhoehung des Laufzeitzaehlers
     *
     * @param sec Laufzeiterhoehung in Sekunden
     */
    public void addCurrentRunTime(long sec);
}