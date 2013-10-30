package edu.hm.dako.echo.connection.ems;

import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

/**
 * @author Christoph Friegel
 * @version 1.0
 */
public class TestTopicPublisher extends TestEMSUnit {

	public void test() {
		try {
			//setTopicName("helloJunge");
			//setServerUrl("tcp://blub2:7222");
			TopicConnection topicConnect = getTopicConnect();
			TopicSession session = getTopicSession(topicConnect);
			Topic topic = session.createTopic(topicName);

			TopicPublisher publisher = session.createPublisher(topic);

			javax.jms.TextMessage message = session.createTextMessage();
			message.setText("waaaaaas geeeeht aaab?");
			publisher.publish(message);
			topicConnect.close();
			System.out.println("over");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TestTopicPublisher().test();
	}
}
