package edu.hm.dako.echo.connection.ems;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.echo.connection.Connection;

public class EMSConnection implements Connection {

	private static Log log = LogFactory.getLog(EMSConnection.class);
	
	@Override
	public Serializable receive() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void send(Serializable message) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
	}

}
