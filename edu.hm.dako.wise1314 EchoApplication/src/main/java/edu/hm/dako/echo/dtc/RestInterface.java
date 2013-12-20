package edu.hm.dako.echo.dtc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import edu.hm.dako.echo.common.EchoPDU;

/**
 * @author Tore Offermann
 * @version 1.0
 */

public class RestInterface
{

  private static Log log = LogFactory.getLog( RestInterface.class );  
	
  public enum RESULT
  {
    OK, ERROR
  }

  public static RESULT notifyServer( EchoPDU receivedPdu, String serverUrl )
  {
    try
    {

      String body = createJsonMessage( receivedPdu );

      URL url = new URL( serverUrl );
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod( "POST" );
      connection.setDoInput( true );
      connection.setDoOutput( true );
      connection.setUseCaches( false );
      connection.setRequestProperty( "Content-Type", "application/json" );
      connection.setRequestProperty( "Content-Length",
          String.valueOf( body.length() ) );

      OutputStreamWriter writer = new OutputStreamWriter(
          connection.getOutputStream() );
      writer.write( body );
      writer.flush();

      BufferedReader reader = new BufferedReader( new InputStreamReader(
          connection.getInputStream() ) );
      
      String response = reader.readLine();
      /* An error occurred. Add the error to the PDU. */
      if ( ! response.equals( "OK" ) )
      {
        receivedPdu.setErrorMessage( response );
        log.info( "Error " + response );
      }
      writer.close();
      reader.close();

      return RESULT.OK;

    }
    catch ( Exception e )
    {
      return RESULT.ERROR;
    }
  }

  @SuppressWarnings( "unchecked" )
  private static String createJsonMessage( EchoPDU receivedPdu )
  {
    if ( receivedPdu == null )
      return null;

    String tm = null;

    JSONObject root = new JSONObject();
    JSONObject data = new JSONObject();

    data.put( "clientThreadName", receivedPdu.getClientThreadName() );
    data.put( "clientTime", receivedPdu.getClientTime() );
    data.put( "message", receivedPdu.getMessage() );
    data.put( "clientName", receivedPdu.getClientName() );
    data.put( "serverThreadName", receivedPdu.getServerThreadName() );

    root.put( "data", data );

    tm = root.toJSONString();

    return tm;
  }

}
