package nacserver.dao;

import java.util.List;

import nacserver.dto.PageInfo;
import nacserver.entity.Player;

public interface PlayerDao extends AbstractDao<Player> {
	
	public List<Player> findAll(PageInfo info);
}
