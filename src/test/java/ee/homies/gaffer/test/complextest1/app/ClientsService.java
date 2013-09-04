package ee.homies.gaffer.test.complextest1.app;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("clientsService")
public class ClientsService {
	@Resource(name = "clientsDAO")
	private ClientsDAO clientsDAO;

	@Resource(name = "usersService")
	private UsersService usersService;

	@Resource(name = "idGenerator")
	private IdGenerator idGenerator;

	public void createClientLegacy(String clientName) {
		createClientInner(clientName);
	}

	@Transactional
	public void createClient(String clientName) {
		createClientInner(clientName);
	}

	private void createClientInner(String clientName) {
		int clientId = idGenerator.next();
		int accountId = idGenerator.next();
		clientsDAO.createClient(clientId, clientName);
		clientsDAO.createAccount(accountId, clientId, "666");

		usersService.createUser(clientId, "bemmimehed@tallinn.ee");
	}
}
