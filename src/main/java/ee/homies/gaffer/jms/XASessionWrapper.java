package ee.homies.gaffer.jms;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.jms.XASession;
import javax.transaction.xa.XAResource;

public class XASessionWrapper implements XASession {
	private Session session;
	private final XAResource xaResource;

	public XASessionWrapper(Session session, XAResource xaResource) {
		this.session = session;
		this.xaResource = xaResource;
	}

	@Override
	public XAResource getXAResource() {
		return xaResource;
	}

	@Override
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public BytesMessage createBytesMessage() throws JMSException {
		return session.createBytesMessage();
	}

	@Override
	public MapMessage createMapMessage() throws JMSException {
		return session.createMapMessage();
	}

	@Override
	public Message createMessage() throws JMSException {
		return session.createMessage();
	}

	@Override
	public ObjectMessage createObjectMessage() throws JMSException {
		return session.createObjectMessage();
	}

	@Override
	public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
		return session.createObjectMessage(object);
	}

	@Override
	public StreamMessage createStreamMessage() throws JMSException {
		return session.createStreamMessage();
	}

	@Override
	public TextMessage createTextMessage() throws JMSException {
		return session.createTextMessage();
	}

	@Override
	public TextMessage createTextMessage(String text) throws JMSException {
		return session.createTextMessage(text);
	}

	@Override
	public boolean getTransacted() throws JMSException {
		return session.getTransacted();
	}

	@Override
	public int getAcknowledgeMode() throws JMSException {
		return session.getAcknowledgeMode();
	}

	@Override
	public void commit() throws JMSException {
		session.commit();
	}

	@Override
	public void rollback() throws JMSException {
		session.rollback();
	}

	@Override
	public void close() throws JMSException {
		session.close();
	}

	@Override
	public void recover() throws JMSException {
		session.recover();
	}

	@Override
	public MessageListener getMessageListener() throws JMSException {
		return session.getMessageListener();
	}

	@Override
	public void setMessageListener(MessageListener listener) throws JMSException {
		session.setMessageListener(listener);
	}

	@Override
	public void run() {
		session.run();
	}

	@Override
	public MessageProducer createProducer(Destination destination) throws JMSException {
		return session.createProducer(destination);
	}

	@Override
	public MessageConsumer createConsumer(Destination destination) throws JMSException {
		return session.createConsumer(destination);
	}

	@Override
	public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
		return session.createConsumer(destination, messageSelector);
	}

	@Override
	public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean NoLocal) throws JMSException {
		return session.createConsumer(destination, messageSelector, NoLocal);
	}

	@Override
	public Queue createQueue(String queueName) throws JMSException {
		return session.createQueue(queueName);
	}

	@Override
	public Topic createTopic(String topicName) throws JMSException {
		return session.createTopic(topicName);
	}

	@Override
	public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
		return session.createDurableSubscriber(topic, name);
	}

	@Override
	public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
		return session.createDurableSubscriber(topic, name, messageSelector, noLocal);
	}

	@Override
	public QueueBrowser createBrowser(Queue queue) throws JMSException {
		return session.createBrowser(queue);
	}

	@Override
	public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
		return session.createBrowser(queue, messageSelector);
	}

	@Override
	public TemporaryQueue createTemporaryQueue() throws JMSException {
		return session.createTemporaryQueue();
	}

	@Override
	public TemporaryTopic createTemporaryTopic() throws JMSException {
		return session.createTemporaryTopic();
	}

	@Override
	public void unsubscribe(String name) throws JMSException {
		session.unsubscribe(name);
	}

}
