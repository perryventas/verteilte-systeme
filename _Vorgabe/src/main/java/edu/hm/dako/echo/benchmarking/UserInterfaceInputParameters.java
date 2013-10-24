package edu.hm.dako.echo.benchmarking;


/**
 * Konfigurationsparameter fuer Lasttest
 *
 * @author Mandl
 */
public class UserInterfaceInputParameters {

    private int numberOfClients;            // Anzahl zu startender Client-Threads
    private int messageLength;              // Nachrichtenlaenge
    private int clientThinkTime;            // Denkzeit zwischen zwei Requests
    private int numberOfMessages;           // Anzahl der Nachrichten pro Client-Thread
    // Typ der Implementierung
    private ImplementationType implementationType;
    // Typ der Messung fuer das Messprotokoll
    private MeasurementType measurementType;
    private int remoteServerPort;           // UDP- oder TCP-Port des Servers, Default: 50000
    private String remoteServerAddress;     // Server-IP-Adresse, Default: "127.0.0.1"

    /**
     * Implementierungsvarianten des Lasttests mit verschiedenen Transportprotokollen
     *
     * @author Mandl
     */
    public enum ImplementationType {
        TCPSingleThreaded,
        TCPMultiThreaded,
    }

    /**
     * Konstruktor
     * Belegung der Inputparameter mit Standardwerten
     */
    public UserInterfaceInputParameters() {
        numberOfClients = 2;
        clientThinkTime = 1;
        messageLength = 50;
        numberOfMessages = 5;
        remoteServerPort = 50000;
        remoteServerAddress = "127.0.0.1";
        implementationType = ImplementationType.TCPSingleThreaded;
        measurementType = MeasurementType.VarThreads;
    }

    /**
     * Abbildung der Implementierungstypen auf Strings
     *
     * @param type Implementierungstyp
     * @return Passender String fuer Implementierungstyp
     */
    public String mapImplementationTypeToString(ImplementationType type) {
        String returnString = null;

        switch (type) {
            case TCPSingleThreaded:
                returnString = "Single-threaded TCP";
                break;
            case TCPMultiThreaded:
                returnString = "Multi-threaded TCP";
                break;
            default:
                break;
        }

        return returnString;
    }

    /**
     * Typen von unterstuetzten Messungen
     *
     * @author Mandl
     */
    public enum MeasurementType {
        VarThreads,    // Variation der Threadanzahl
        VarMsgLength   // Variation der Nachrichtenlaenge
    }

    /**
     * Abbildung der Messungstypen auf Strings
     *
     * @param type Messungstyp
     * @return Passender String fuer Messungstyp
     */
    public String mapMeasurementTypeToString(MeasurementType type) {
        String returnString = null;

        switch (type) {
            case VarThreads:
                returnString = "Variation der Threadanzahl";
                break;
            case VarMsgLength:
                returnString = "Variation der Nachrichtenlaenge";
                break;
            default:
                break;
        }

        return returnString;
    }

    public int getNumberOfClients() {
        return numberOfClients;
    }

    public void setNumberOfClients(int numberOfClients) {
        this.numberOfClients = numberOfClients;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public void getMessageLength(int numberOfClients) {
        this.numberOfClients = numberOfClients;
    }

    public int getClientThinkTime() {
        return clientThinkTime;
    }

    public void setClientThinkTime(int clientThinkTime) {
        this.clientThinkTime = clientThinkTime;
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public ImplementationType getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(ImplementationType implementationType) {
        this.implementationType = implementationType;
    }

    public MeasurementType getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(MeasurementType measurementType) {
        this.measurementType = measurementType;
    }

    public int getRemoteServerPort() {
        return remoteServerPort;
    }

    public void setRemoteServerPort(int remoteServerPort) {
        this.remoteServerPort = remoteServerPort;
    }

    public String getRemoteServerAddress() {
        return remoteServerAddress;
    }

    public void setRemoteServerAddress(String remoteServerAddress) {
        this.remoteServerAddress = remoteServerAddress;
    }
}