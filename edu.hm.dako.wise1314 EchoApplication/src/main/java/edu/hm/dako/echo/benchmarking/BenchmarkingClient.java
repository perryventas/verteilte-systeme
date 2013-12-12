package edu.hm.dako.echo.benchmarking;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.echo.client.ClientFactory;
import edu.hm.dako.echo.client.EMSEchoReciever;
import edu.hm.dako.echo.common.CpuUtilisationWatch;
import edu.hm.dako.echo.common.SharedClientStatistics;

/**
 * Klasse BenchmarkingClient Basisklasse zum Starten eines Benchmarks
 * 
 * @author Mandl
 */
public class BenchmarkingClient implements BenchmarkingStartInterface
{
  private static Log log = LogFactory.getLog( BenchmarkingClient.class );

  // Daten aller Client-Threads zur Verwaltung der Statistik
  private SharedClientStatistics sharedData;
  private CpuUtilisationWatch cpuUtilisationWatch;

  /**
   * Methode liefert die aktuelle Zeit als String
   * 
   * @param cal
   *          Kalender
   * @return String mit Zeit
   */
  private String getCurrentTime( Calendar cal )
  {
    return ( cal.get( Calendar.DAY_OF_MONTH ) + "."
        + ( cal.get( Calendar.MONTH ) + 1 ) + "." + cal.get( Calendar.YEAR )
        + " " + cal.get( Calendar.HOUR_OF_DAY ) + ":"
        + cal.get( Calendar.MINUTE ) + ":" + cal.get( Calendar.SECOND ) );
    // + ":" + cal.get(Calendar.MILLISECOND) );
  }

  @Override
  public void executeTest( UserInterfaceInputParameters parm,
      BenchmarkingClientUserInterface clientGui )
  {
    clientGui.setMessageLine( parm.mapImplementationTypeToString( parm
        .getImplementationType() ) + ": Benchmark gestartet" );

    // Anzahl aller erwarteten Requests ermitteln
    long numberOfAllMessages = parm.getNumberOfClients()
        * parm.getNumberOfMessages();

    // Gemeinsamen Datenbereich fuer alle Threads anlegen
    sharedData = new SharedClientStatistics( parm.getNumberOfClients(),
        parm.getNumberOfMessages(), parm.getClientThinkTime() );

    /**
     * Startzeit ermitteln
     */
    long startTime = 0;
    Calendar cal = Calendar.getInstance();
    startTime = cal.getTimeInMillis();
    String startTimeAsString = getCurrentTime( cal );

    /**
     * Laufzeitzaehler-Thread erzeugen
     */
    TimeCounterThread timeCounterThread = new TimeCounterThread( clientGui );
    timeCounterThread.start();

    cpuUtilisationWatch = new CpuUtilisationWatch();

    /**
     * ResponseQueueReciever starten
     */

    EMSEchoReciever emsQueue = ClientFactory.checkConnection( parm, sharedData );

    /**
     * Client-Threads in Abhaengigkeit des Implementierungstyps instanziieren
     * und starten
     */
    ExecutorService executorService = Executors.newFixedThreadPool( parm
        .getNumberOfClients() );
    for ( int i = 0; i < parm.getNumberOfClients(); i++ )
    {
      executorService.submit( ClientFactory.getClient( parm, i, sharedData ) );
    }

    /**
     * Startwerte anzeigen
     */
    UserInterfaceStartData startData = new UserInterfaceStartData();
    startData.setNumberOfRequests( numberOfAllMessages );
    startData.setStartTime( getCurrentTime( cal ) );
    clientGui.showStartData( startData );

    clientGui.setMessageLine( "Alle " + parm.getNumberOfClients()
        + " Clients-Threads gestartet" );

    /**
     * Auf das Ende aller Clients warten
     */
    if ( emsQueue != null )
    {
      // wait until we got all messages
      while ( numberOfAllMessages > sharedData.getNumberOfReceivedResponses() )
        ;
    }
    executorService.shutdown();

    try
    {
      executorService.awaitTermination( 10, TimeUnit.MINUTES );
    }
    catch ( InterruptedException e )
    {
      log.error( "Das Beenden des ExecutorService wurde unterbrochen" );
      e.printStackTrace();
    }

    /**
     * Laufzeitzaehler-Thread beenden
     */
    timeCounterThread.stopThread();

    /**
     * Analyse der Ergebnisse durchfuehren, Statistikdaten berechnen und
     * ausgeben
     */
    // sharedData.printStatistic();

    /**
     * Testergebnisse ausgeben
     */

    clientGui.setMessageLine( "Alle Clients-Threads beendet" );

    UserInterfaceResultData resultData = getResultData( parm, startTime );

    clientGui.showResultData( resultData );
    clientGui.setMessageLine( parm.mapImplementationTypeToString( parm
        .getImplementationType() ) + ": Benchmark beendet" );

    /**
     * Datensatz fuer Benchmark-Lauf auf Protokolldatei schreiben
     */

    sharedData.writeStatisticSet( "Benchmarking-EchoApp-Protokolldatei",
        parm.mapImplementationTypeToString( parm.getImplementationType() ),
        parm.mapMeasurementTypeToString( parm.getMeasurementType() ),
        startTimeAsString, resultData.getEndTime() );

  }

  private UserInterfaceResultData getResultData(
      UserInterfaceInputParameters parm, long startTime )
  {
    Calendar cal;
    UserInterfaceResultData resultData = new UserInterfaceResultData();

    resultData.setAvgRTT( sharedData.getAverageRTT() / 1000000d );
    resultData.setMaxRTT( sharedData.getMaximumRTT() / 1000000d );
    resultData.setMinRTT( sharedData.getMinimumRTT() / 1000000d );
    resultData.setAvgServerTime( sharedData.getAverageServerTime()
        / parm.getNumberOfClients() / 1000000d );

    cal = Calendar.getInstance();
    resultData.setEndTime( getCurrentTime( cal ) );

    long elapsedTimeInSeconds = ( cal.getTimeInMillis() - startTime ) / 1000;
    resultData.setElapsedTime( elapsedTimeInSeconds );

    resultData.setMaxCpuUsage( cpuUtilisationWatch.getAverageCpuUtilisation() );

    resultData.setMaxHeapSize( sharedData.getMaxHeapSize() / ( 1024 * 1024 ) );

    resultData.setNumberOfResponses( sharedData.getSumOfAllReceivedMessages() );
    resultData.setNumberOfSentRequests( sharedData.getNumberOfSentRequests() );
    resultData.setNumberOfLostResponses( sharedData.getNumberOfLostResponses() );
    return resultData;
  }
}