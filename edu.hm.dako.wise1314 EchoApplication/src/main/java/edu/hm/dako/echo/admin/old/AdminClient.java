package edu.hm.dako.echo.admin.old;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.echo.common.CONSTANTS;

/**
 * @author Christoph Friegel
 * @version 1.0
 */

public class AdminClient {
  private static Log log = LogFactory.getLog(AdminClient.class);
    
  private Connection connect = null;
  private Statement statement = null;
  private ResultSet resultSet = null;

  public Count getClientCount(int clientId) throws Exception {
    try {
      Class.forName("com.mysql.jdbc.Driver");

      connect = DriverManager.getConnection("jdbc:mysql://" + CONSTANTS.DB_HOST + "/"
      + CONSTANTS.DB_NAME + "?user=" + CONSTANTS.DB_USER_NAME + "&password=" + CONSTANTS.DB_PASSWORD );

      statement = connect.createStatement();
      resultSet = statement.executeQuery("select count(count) as COUNTNR, MIN(CTIMESTAMP) as CTMAX,"+
      " MAX(CTIMESTAMP) as CTMIN FROM "+CONSTANTS.COUNT_TABLE_NAME);

      return writeCountTable(resultSet);
    } catch (Exception e) {
        log.error(e.getMessage());
    } finally {
      close();
    }
	return null;
  }
  
  public void deleteAllData() throws Exception {
	    try {
	      Class.forName("com.mysql.jdbc.Driver");

	      connect = DriverManager.getConnection("jdbc:mysql://" + CONSTANTS.DB_HOST + "/"
	      + CONSTANTS.DB_NAME + "?user=" + CONSTANTS.DB_USER_NAME + "&password=" + CONSTANTS.DB_PASSWORD );

	      statement = connect.createStatement();
	      //delete trace db
	      statement.executeUpdate("truncate table "+CONSTANTS.TRACE_TABLE_NAME);
	      //delete count db
	      statement.executeUpdate("truncate table "+CONSTANTS.COUNT_TABLE_NAME);

	    } catch (Exception e) {
	        log.error(e.getMessage());
	    } finally {
	    	close();
	    }
	  }

  private Count writeCountTable(ResultSet rs) throws SQLException {
	  Count obj = new Count();
	  
	  rs.next();
	  if(rs.getInt(1)==0) //rs in case "count(*)" is never null 
		return obj;
	  else 
		rs.beforeFirst(); //sets pointer back

	  	while (rs.next()) {
	      String countNr = rs.getString("COUNTNR");
	      Timestamp ctimestampMax = rs.getTimestamp("CTMAX");
	      Timestamp ctimestampMin = rs.getTimestamp("CTMIN");
	      
	      obj.setCountNr(countNr);
	      obj.setMaxDate( formatDateTime( ctimestampMax ) );
	      obj.setMinDate( formatDateTime( ctimestampMin ) );
		}

	  	return obj;
  }

  private void close() {
    try {
      if (resultSet != null) {
        resultSet.close();
      }

      if (statement != null) {
        statement.close();
      }

      if (connect != null) {
        connect.close();
      }
    } catch (Exception e) {
        log.error(e.getMessage());
    }
  }
  
private String formatDateTime(Timestamp ts) {
	return new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(ts);
  }

}  
      
      