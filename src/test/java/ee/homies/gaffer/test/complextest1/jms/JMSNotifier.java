package ee.homies.gaffer.test.complextest1.jms;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("jmsNotifier")
public class JMSNotifier {
	private static final Logger log = LoggerFactory.getLogger(JMSNotifier.class);
	private JmsTemplate jmsTemplate;

	@Resource(name = "jmsFactory")
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		jmsTemplate = new JmsTemplate(connectionFactory);
	}

	@Resource(name = "clientNotificationsQueue")
	private Queue queue;

	@Transactional
	public void notifyClientCreation() {
		this.jmsTemplate.send(queue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage("Client added.");
			}
		});
		log.info("Sent notification message.");
	}
}
