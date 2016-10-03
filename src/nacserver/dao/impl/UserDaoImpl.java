package nacserver.dao.impl;

import nacserver.dao.UserDao;
import nacserver.entity.User;

import org.springframework.stereotype.Repository;


@Repository("userDao")
public class UserDaoImpl extends BaseDaoHibernate<User> implements UserDao {

}
