package ee.homies.gaffer.test.complextest1.app;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Throwables;

@Repository("usersDAO")
public class UsersDAO {
  @Resource(name = "secretsDataSource")
  private DataSource secretsDataSource;

  private JdbcTemplate usersJdbcTemplate;

  @Resource(name = "usersDataSource")
  public void setUsersDataSource(DataSource dataSource) {
    usersJdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Resource(name = "config")
  private Config config;

  @Transactional
  public void createUser(int id, int clientId, String email) {
    usersJdbcTemplate.update("insert into users (id, clientId, email) values(?,?,?)", id, clientId, email);
  }

  @Transactional
  public void createPassword(int id, int userId, String password) {
    try (Connection con = DataSourceUtils.getConnection(secretsDataSource);
        PreparedStatement stmt = con.prepareStatement("insert into passwords (id, userId, password) values(?,?,?)");) {
      stmt.setInt(1, id);
      stmt.setInt(2, userId);
      stmt.setString(3, password);
      stmt.execute();

      if (config.isFailPasswordCreation()) {
        throw new RuntimeException("Creating a password failed.");
      }
    } catch (Exception e) {
      Throwables.propagate(e);
    }
  }
}
