package nacserver.cache;

import nacserver.entity.Player;
import net.ojava.network.tcp.TCPNetSessionContext;

public class PlayerCacheItem {
	private Player player;
	private TCPNetSessionContext netContext;
	
	public Player getPlayer() {
		return player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public TCPNetSessionContext getNetContext() {
		return netContext;
	}
	
	public void setNetContext(TCPNetSessionContext netContext) {
		this.netContext = netContext;
	}
}
