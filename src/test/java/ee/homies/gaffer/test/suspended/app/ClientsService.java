package ee.homies.gaffer.test.suspended.app;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("clientsService")
public class ClientsService {
  @Resource(name = "clientsDAO")
  private ClientsDAO clientsDAO;

  @Resource(name = "logsService")
  private LogsService logsService;

  @Resource(name = "idGenerator")
  private IdGenerator idGenerator;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createClient2(String clientName) {
    int clientId = idGenerator.next();
    clientsDAO.createClient(clientId, clientName);
    logsService.appendLogNotSupported("Added client '" + clientName + "'.");
  }

  @Transactional
  public void createClient(String clientName) {
    try {
      int clientId = idGenerator.next();
      clientsDAO.createClient(clientId, clientName);
    } catch (Exception e) {
      logsService.appendError("Creating a client failed.");
    }
  }
}
