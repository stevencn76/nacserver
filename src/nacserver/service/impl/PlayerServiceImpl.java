package nacserver.service.impl;

import java.util.List;

import nacserver.dao.PlayerDao;
import nacserver.dto.PageInfo;
import nacserver.entity.Player;
import nacserver.service.PlayerService;
import nacserver.util.TimeUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;



@Transactional(propagation=Propagation.REQUIRED)
@Service("playerService")
public class PlayerServiceImpl implements PlayerService {

	@Autowired
	private PlayerDao playerDao;


	@Override
	public Player createPlayer(Player p) throws Exception {
		Player tp = playerDao.findByProperty("name", p.getName());
		if(tp != null)
			throw new Exception(p.getName() + " has been registered");
		
		p.setCreateTime(TimeUtil.getCurTime());
		p.setLastTime(p.getCreateTime());
		
		playerDao.save(p);
		
		return p;
	}

	@Override
	public List<Player> findPlayers(PageInfo pageInfo) throws Exception {
		return playerDao.findAll(pageInfo);
	}

	@Override
	public Player findPlayer(String name, String password) throws Exception {
		Player tp = playerDao.findByProperty("name", name);
		if(tp == null)
			return null;

		if(tp.getPassword().equals(password))
			return tp;
		
		return null;
	}
}
