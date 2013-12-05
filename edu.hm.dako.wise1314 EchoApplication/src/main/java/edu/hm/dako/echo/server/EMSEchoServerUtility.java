package edu.hm.dako.echo.server;

import edu.hm.dako.echo.common.EchoPDU;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.TopicSession;

import org.json.simple.JSONObject;

/**
 * @author Tore Offermann
 * @version 1.0
 */

public class EMSEchoServerUtility
{

  @SuppressWarnings( "unchecked" )
  public static TextMessage createTraceJsonMessage( EchoPDU receivedPdu,
      EchoPDU currentPdu, TopicSession ts ) throws JMSException
  {
    if ( receivedPdu == null )
      return null;

    TextMessage tm = ts.createTextMessage();

    JSONObject root = new JSONObject();
    JSONObject data = new JSONObject();

    data.put( "clientThreadName", receivedPdu.getClientThreadName() );
    data.put( "clientTime", receivedPdu.getClientTime() );
    data.put( "message", receivedPdu.getMessage() );
    data.put( "clientName", receivedPdu.getClientName() );
    data.put( "serverThreadName", currentPdu.getServerThreadName() );

    root.put( "data", data );

    tm.setText( root.toJSONString() );

    return tm;
  }

}
