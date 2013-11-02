package edu.hm.dako.echo.server;

import edu.hm.dako.echo.common.EchoPDU;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.TopicSession;
import org.json.simple.JSONObject;

/**
 *
 * @author toffermann
 */
public class EMSEchoServerUtility {

    public static TextMessage createTraceJsonMessage( EchoPDU receivedPdu, 
                                                      TopicSession ts ) throws JMSException
    {
        if ( receivedPdu == null )
            return null;
        
        TextMessage tm = ts.createTextMessage();
        
        JSONObject root = new JSONObject();
        JSONObject data = new JSONObject();
            
        data.put("clientThreadName", receivedPdu.getClientName());
        data.put("clientTime", receivedPdu.getClientTime());
        data.put("message", receivedPdu.getMessage()); 
            
        root.put("data", data);
        
        tm.setText(root.toJSONString());
        
        return tm;
    }
    
    
}
