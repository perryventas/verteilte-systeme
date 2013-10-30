package edu.hm.dako.echo.connection.ems;

/**
 * @author Christoph Friegel
 * @version 1.0
 */
public class TestQueueReceiver extends TestEMSUnit {
	
	tibjmsQueueReceiver monitor;

	public void test() {
		try {
            tibjmsQueueReceiver t = new tibjmsQueueReceiver(serverUrl, userName, password, queueName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TestQueueReceiver().test();
	}
}
