package edu.hm.dako.echo.server;

import edu.hm.dako.echo.common.EchoPDU;
import edu.hm.dako.echo.connection.Connection;
import edu.hm.dako.echo.connection.ServerSocket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Tore Offermann
 * @version 1.0
 */

public class DefaultEchoServerImpl implements EchoServer
{

  private static Log log = LogFactory.getLog( DefaultEchoServerImpl.class );

  private final ExecutorService executorService;

  private ServerSocket socket;

  private final boolean singleConnectionForClient;

  public DefaultEchoServerImpl( ExecutorService executorService,
      ServerSocket socket, boolean singleConnectionForClient )
  {
    this.singleConnectionForClient = singleConnectionForClient;
    this.executorService = executorService;
    this.socket = socket;
  }

  @Override
  public void start()
  {
    PropertyConfigurator.configureAndWatch( "log4j.server.properties",
        60 * 1000 );
    System.out.println( "Echoserver wartet auf Clients..." );
    while ( !Thread.currentThread().isInterrupted() && !socket.isClosed() )
    {
      try
      {
        // Auf ankommende Verbindungsaufbauwuensche warten
        Connection connection = socket.accept();

        // Neuen Workerthread starten
        executorService.submit( new EchoWorker( connection ) );
      }
      catch ( Exception e )
      {
        log.error( "Exception beim Entgegennehmen von Verbindungswuenschen: "
            + e );
      }
    }
  }

  @Override
  public void stop() throws Exception
  {
    System.out.println( "EchoServer beendet sich" );
    Thread.currentThread().interrupt();
    socket.close();
    executorService.shutdown();
    try
    {
      executorService.awaitTermination( 10, TimeUnit.MINUTES );
    }
    catch ( InterruptedException e )
    {
      log.error( "Das beenden des ExecutorService wurde unterbrochen" );
      e.printStackTrace();
    }
  }

  private class EchoWorker implements Runnable
  {

    private final Connection con;

    private boolean finished = false;

    long startTime;

    private EchoWorker( Connection con )
    {
      this.con = con;
    }

    @Override
    public void run()
    {
      while ( !finished && !Thread.currentThread().isInterrupted() )
      {
        try
        {
          echo();
        }
        catch ( Exception e )
        {
          closeConnection();
          throw new RuntimeException( e );
        }
      }
      log.debug( Thread.currentThread().getName() + " beendet sich" );
      closeConnection();
    }

    private void echo() throws Exception
    {

      EchoPDU receivedPdu = (EchoPDU) con.receive();
      startTime = System.nanoTime();
      con.send( EchoPDU.createServerEchoPDU( receivedPdu, startTime ) );
      if ( receivedPdu.getLastRequest() || !singleConnectionForClient )
      {
        log.debug( "Letzter Request des Clients " + receivedPdu.getClientName() );
        finished = true;
      }
    }

    private void closeConnection()
    {
      try
      {
        con.close();
      }
      catch ( Exception e )
      {
        throw new RuntimeException( e );
      }
    }
  }
}
