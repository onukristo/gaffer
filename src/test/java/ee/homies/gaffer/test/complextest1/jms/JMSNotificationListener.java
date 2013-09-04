package ee.homies.gaffer.test.complextest1.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMSNotificationListener {
	private static final Logger log = LoggerFactory.getLogger(JMSNotificationListener.class);

	public void handleMessage(String message) {
		log.info("Text message received." + message);
	}
}
