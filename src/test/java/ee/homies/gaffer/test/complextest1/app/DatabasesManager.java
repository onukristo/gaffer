package ee.homies.gaffer.test.complextest1.app;

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
  @Resource(name = "clientsAdminDataSource")
  private DataSource clientsDataSource;

  @Resource(name = "usersAdminDataSource")
  private DataSource usersDataSource;

  @Resource(name = "accountsAdminDataSource")
  private DataSource accountsDataSource;

  @Resource(name = "secretsAdminDataSource")
  private DataSource secretsDataSource;

  @PostConstruct
  public void createDatabases() {
    createDatabase(clientsDataSource, "create table clients(id INT NOT NULL, name VARCHAR(45) NOT NULL, PRIMARY KEY (id) )");
    createDatabase(usersDataSource, "create table users(id INT NOT NULL, clientId INT NOT NULL, email VARCHAR(45) NOT NULL, PRIMARY KEY (id) )");
    createDatabase(accountsDataSource, "create table accounts(id INT NOT NULL, clientId INT NOT NULL, referenceNumber VARCHAR(45) NOT NULL, PRIMARY KEY (id) )");
    createDatabase(secretsDataSource, "create table passwords(id INT NOT NULL, userId INT NOT NULL, password VARCHAR(45) NOT NULL, PRIMARY KEY (id) )");
  }

  @PreDestroy
  public void dropDatabases() {
    dropDatabases(clientsDataSource);
    dropDatabases(usersDataSource);
    dropDatabases(accountsDataSource);
    dropDatabases(secretsDataSource);
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
    case "users":
      return usersDataSource;
    case "accounts":
      return accountsDataSource;
    case "secrets":
      return secretsDataSource;
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
}
