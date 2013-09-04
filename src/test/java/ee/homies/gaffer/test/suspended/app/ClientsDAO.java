package ee.homies.gaffer.test.suspended.app;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("clientsDAO")
public class ClientsDAO {
  private JdbcTemplate jdbcTemplate;

  @Resource(name = "clientsDataSource")
  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Transactional
  public void createClient(int id, String name) {
    if ("Invalid name".equals(name)) {
      throw new IllegalStateException("Invalid name '" + name + "' provided.");
    }
    jdbcTemplate.update("insert into clients (id, name) values(?,?)", id, name);
  }
}
