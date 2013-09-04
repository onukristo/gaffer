package ee.homies.gaffer.test.suspended.app;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("logsService")
public class LogsService {
  @Resource(name = "logsDAO")
  private LogsDAO logsDAO;

  @Resource(name = "idGenerator")
  private IdGenerator idGenerator;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void appendError(String message) {
    logsDAO.createLogMessage(idGenerator.next(), message);
  }
}
