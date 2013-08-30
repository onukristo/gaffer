package ee.homies.gaffer.jms;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

public class ConnectionWrapper implements Connection {
	private Connection connection;

	@Override
	public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
		return connection.createSession(transacted, acknowledgeMode);
	}

	@Override
	public String getClientID() throws JMSException {
		return connection.getClientID();
	}

	@Override
	public void setClientID(String clientID) throws JMSException {
		connection.setClientID(clientID);
	}

	@Override
	public ConnectionMetaData getMetaData() throws JMSException {
		return connection.getMetaData();
	}

	@Override
	public ExceptionListener getExceptionListener() throws JMSException {
		return connection.getExceptionListener();
	}

	@Override
	public void setExceptionListener(ExceptionListener listener) throws JMSException {
		connection.setExceptionListener(listener);
	}

	@Override
	public void start() throws JMSException {
		connection.start();
	}

	@Override
	public void stop() throws JMSException {
		connection.stop();
	}

	@Override
	public void close() throws JMSException {
		connection.close();
	}

	@Override
	public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector, ServerSessionPool sessionPool, int maxMessages)
	    throws JMSException {
		return connection.createConnectionConsumer(destination, messageSelector, sessionPool, maxMessages);
	}

	@Override
	public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool,
	    int maxMessages) throws JMSException {
		return connection.createDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
	}

	public ConnectionWrapper() {
	}

	public ConnectionWrapper(Connection connection) {
		this.connection = connection;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
