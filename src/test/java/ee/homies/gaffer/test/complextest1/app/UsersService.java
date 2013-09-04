package ee.homies.gaffer.test.complextest1.app;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("usersService")
public class UsersService {
	@Resource(name = "usersDAO")
	private UsersDAO usersDAO;

	@Resource(name = "config")
	private Config config;

	@Resource(name = "idGenerator")
	private IdGenerator idGenerator;

	@Transactional
	public void createUser(int clientId, String email) {
		int userId = idGenerator.next();
		int passwordId = idGenerator.next();
		usersDAO.createUser(userId, clientId, email);

		if (config.isCreatePassword()) {
			usersDAO.createPassword(passwordId, userId, "Salasona");
		}
	}
}
