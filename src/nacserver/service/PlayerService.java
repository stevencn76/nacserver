package nacserver.service;

import java.util.List;

import nacserver.dto.PageInfo;
import nacserver.entity.Player;


public interface PlayerService {
	public Player createPlayer(Player p) throws Exception;
	
	public List<Player> findPlayers(PageInfo pageInfo) throws Exception;
	
	public Player findPlayer(String name, String password) throws Exception;
}
