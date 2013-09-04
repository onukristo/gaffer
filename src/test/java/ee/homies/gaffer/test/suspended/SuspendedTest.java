package ee.homies.gaffer.test.suspended;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.UnexpectedRollbackException;

import ee.homies.gaffer.test.suspended.app.ClientsService;
import ee.homies.gaffer.test.suspended.app.DatabasesManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/ee/homies/gaffer/test/suspended/applicationContext.xml" })
public class SuspendedTest {
  @Resource(name = "clientsService")
  private ClientsService clientsService;

  @Resource(name = "databasesManager")
  private DatabasesManager databasesManager;

  @Test
  public void testSuccess() {
    clientsService.createClient("Aadu");
    Assert.assertEquals(1, databasesManager.getTableRowsCount("clients.clients"));
  }

  @Test
  public void testError() {
    boolean wasRollback = false;
    try {
      clientsService.createClient("Invalid name");
    } catch (UnexpectedRollbackException e) {
      wasRollback = true;
    }
    Assert.assertEquals(true, wasRollback);
    Assert.assertEquals(0, databasesManager.getTableRowsCount("clients.clients"));
    Assert.assertEquals(1, databasesManager.getTableRowsCount("logs.logs"));
  }
}
