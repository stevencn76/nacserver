package nacserver.service;

import nacserver.entity.User;


public interface UserService {
	
	public User login(String username, String password) throws Exception;
	
	public User findUser(int userId) throws Exception;
	
	public User passwordUser(int userId, String originPwd, String newPwd) throws Exception;
}
