package nacserver.service.impl;

import nacserver.dao.UserDao;
import nacserver.entity.User;
import nacserver.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Transactional(propagation=Propagation.REQUIRED)
@Service("userService")
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	@Override
	public User login(String username, String password) throws Exception {
		User u = userDao.findByProperty("username", username);
		if(u == null)
			return null;
		
		if(u.getPassword().equals(password))
			return u;
		
		return null;
	}

	@Override
	public User findUser(int userId) throws Exception {
		return userDao.findById(userId);
	}

	@Override
	public User passwordUser(int userId, String originPwd, String newPwd) throws Exception {
		User u = userDao.findById(userId);
		if(u == null)
			throw new Exception("用户不存在");
		
		if(originPwd == null || originPwd.length() == 0)
			throw new Exception("原密码不能为空");
		
		if(newPwd == null || newPwd.length() == 0)
			throw new Exception("新密码不能为空");
		
		if(!u.getPassword().equals(originPwd))
			throw new Exception("原密码不正确");
		
		u.setPassword(newPwd);
		userDao.save(u);
		
		return u;
	}

}
