package ee.homies.gaffer.test.suspended.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.stereotype.Component;

@Component("databasesManager")
public class DatabasesManager {
  @Resource(name = "clientsDataSource")
  private DataSource clientsDataSource;

  @Resource(name = "logsDataSource")
  private DataSource logsDataSource;

  @PostConstruct
  public void createDatabases() {
    createDatabase(clientsDataSource, "create table clients(id INT NOT NULL, name VARCHAR(45) NOT NULL, PRIMARY KEY (id) )");
    createDatabase(logsDataSource, "create table logs(id INT NOT NULL, message VARCHAR(255) NOT NULL, PRIMARY KEY (id) )");
  }

  @PreDestroy
  public void dropDatabases() {
    dropDatabases(clientsDataSource);
    dropDatabases(logsDataSource);
  }

  private void dropDatabases(DataSource dataSource) {
    try {
      try (Connection con = dataSource.getConnection();) {
        try (PreparedStatement stmt = con.prepareStatement("SHUTDOWN");) {
          stmt.execute();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void createDatabase(DataSource dataSource, String sql) {
    try {
      try (Connection con = dataSource.getConnection();) {
        try (PreparedStatement stmt = con.prepareStatement(sql);) {
          stmt.execute();
        }
        try (PreparedStatement stmt = con.prepareStatement("create table DUAL (id INT NOT NULL)");) {
          stmt.execute();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private DataSource getDataSourceByName(String name) {
    switch (name) {
    case "clients":
      return clientsDataSource;
    case "logs":
      return logsDataSource;
    }
    return null;
  }

  public int getTableRowsCount(String globalTableName) {
    String[] parts = globalTableName.split("\\.");
    String dbName = parts[0];
    String tableName = parts[1];
    return getTableRowsCount(getDataSourceByName(dbName), tableName);
  }

  private int getTableRowsCount(DataSource dataSource, String tableName) {
    try (Connection con = dataSource.getConnection();
        PreparedStatement stmt = con.prepareStatement("select count(*) from " + tableName);
        ResultSet rs = stmt.executeQuery();) {
      rs.next();
      return rs.getInt(1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteRows() {
    deleteRows(clientsDataSource, "clients");
    deleteRows(logsDataSource, "logs");
  }

  public void deleteRows(DataSource dataSource, String tableName) {
    try {
      try (Connection con = dataSource.getConnection(); PreparedStatement stmt = con.prepareStatement("delete from " + tableName);) {
        stmt.execute();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
