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
import ee.homies.gaffer.util.FormatLogger;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/ee/homies/gaffer/test/suspended/applicationContext.xml" })
public class SuspendedTest {
  private static final FormatLogger log = new FormatLogger(SuspendedTest.class);
  @Resource(name = "clientsService")
  private ClientsService clientsService;

  @Resource(name = "databasesManager")
  private DatabasesManager databasesManager;

  @Test
  public void testSuccess2() {
    clientsService.createClient2("Aadu");
    try {
      Thread.sleep(20000);
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
    Assert.assertEquals(1, databasesManager.getTableRowsCount("clients.clients"));
  }

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
