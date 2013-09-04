package ee.homies.gaffer.test.suspended.app;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("logsDAO")
public class LogsDAO {
  private JdbcTemplate jdbcTemplate;

  @Resource(name = "logsDataSource")
  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Transactional
  public void createLogMessage(int id, String message) {
    jdbcTemplate.update("insert into logs (id, message) values(?,?)", id, message);
  }

}
