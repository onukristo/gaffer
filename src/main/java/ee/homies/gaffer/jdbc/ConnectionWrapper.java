package ee.homies.gaffer.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionWrapper implements Connection {
	private Connection connnection;

	public ConnectionWrapper() {
	}

	public ConnectionWrapper(Connection con) {
		this.connnection = con;
	}

	public Connection getConnection() {
		return connnection;
	}

	public void setConnection(Connection con) {
		this.connnection = con;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return connnection.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return connnection.isWrapperFor(iface);
	}

	@Override
	public Statement createStatement() throws SQLException {
		return connnection.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connnection.prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return connnection.prepareCall(sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return connnection.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connnection.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return connnection.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		connnection.commit();
	}

	@Override
	public void rollback() throws SQLException {
		connnection.rollback();
	}

	@Override
	public void close() throws SQLException {
		connnection.close();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return connnection.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return connnection.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		connnection.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return connnection.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		connnection.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return connnection.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		connnection.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return connnection.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return connnection.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		connnection.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return connnection.createStatement(resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return connnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return connnection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return connnection.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		connnection.setTypeMap(map);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		connnection.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return connnection.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return connnection.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return connnection.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		connnection.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connnection.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return connnection.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return connnection.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return connnection.prepareStatement(sql, columnNames);
	}

	@Override
	public Clob createClob() throws SQLException {
		return connnection.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return connnection.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return connnection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return connnection.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return connnection.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		connnection.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		connnection.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return connnection.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return connnection.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return connnection.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return connnection.createStruct(typeName, attributes);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		connnection.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		return connnection.getSchema();
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		connnection.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		connnection.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return connnection.getNetworkTimeout();
	}
}
