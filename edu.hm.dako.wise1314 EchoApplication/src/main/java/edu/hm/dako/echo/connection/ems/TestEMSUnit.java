package edu.hm.dako.echo.connection.ems;

import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;

/**
 * @author Christoph Friegel
 * @version 1.0
 */
public class TestEMSUnit {

	String serverUrl = "tcp://moguai.org:7222";

	String userName = "dev";

	String password = "dev";

	String topicName = "dev.test";

	String queueName = "dev.request";

	protected TopicConnection topicConnection;

	protected TopicConnection getTopicConnect() throws JMSException {
		TopicConnectionFactory factory = new com.tibco.tibjms.TibjmsTopicConnectionFactory(serverUrl);
		return factory.createTopicConnection(userName, password);
	}

	protected TopicSession getTopicSession(TopicConnection connection) throws JMSException {
		return connection.createTopicSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

}
