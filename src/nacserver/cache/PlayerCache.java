package nacserver.cache;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import nacserver.common.Global;
import nacserver.dto.PageInfo;
import nacserver.dto.PlayerTO;
import nacserver.entity.Player;
import net.ojava.network.tcp.TCPNetSessionContext;
import net.ojava.network.telnet.log.NetLogger;

public class PlayerCache {
	private static final NetLogger LOGGER = NetLogger.getLogger(PlayerCache.class);
	
	private static PlayerCache instance;
	private static Object instanceLock = new Object();
	
	private Hashtable<String, PlayerCacheItem> onlinePlayers = new Hashtable<String, PlayerCacheItem>();
	private Hashtable<String, PkCacheItem> onlinePkers = new Hashtable<String, PkCacheItem>();
	
	private PlayerCache() {
		
	}
	
	public static PlayerCache getInstance() {
		synchronized(instanceLock) {
			if(instance == null) {
				instance = new PlayerCache();
			}
		}
		
		return instance;
	}
	
	public synchronized void addOnlinePlayer(Player player, TCPNetSessionContext context) {
		String name = (String)context.getAttribute(Global.KEY_CONTEXT_USER);
		if(name != null) {
			PlayerCache.getInstance().removeOnlinePlayer(name);
		}
		
		if(player == null)
			return;
		
		PlayerCacheItem item = new PlayerCacheItem();
		item.setPlayer(player);
		item.setNetContext(context);
		
		onlinePlayers.put(player.getName(), item);
	}
	
	public synchronized void removeOnlinePlayer(String name) {
		PkCacheItem item = onlinePkers.get(name);
		if(item != null) {
			onlinePkers.remove(item.getPlayerName1());
			onlinePkers.remove(item.getPlayerName2());
		}
		
		onlinePlayers.remove(name);
	}
	
	public boolean isOnline(String name) {
		return onlinePlayers.containsKey(name);
	}
	
	public PlayerCacheItem getPlayerCacheItem(String name) {
		return onlinePlayers.get(name);
	}
	
	public PkCacheItem getPkCacheItem(String name) {
		return onlinePkers.get(name);
	}
	
	private synchronized PkCacheItem addPkers(String inviteName, String acceptName) {
		PkCacheItem item = new PkCacheItem();
		
		item.setStatus(PkCacheItem.STATUS_WAITACCEPT);
		item.setPlayerName1(inviteName);
		item.setPlayerName2(acceptName);
		item.setGameData(0);
		item.setCurTurn(PkCacheItem.getRandomTurn());
		
		onlinePkers.put(inviteName, item);
		onlinePkers.put(acceptName, item);
		
		return item;
	}
	
	private void removePkers(String name) {
		PkCacheItem item = onlinePkers.get(name);
		if(item != null) {
			onlinePkers.remove(item.getPlayerName1());
			onlinePkers.remove(item.getPlayerName2());
		}
	}
	
	public synchronized PkCacheItem buildPkCache(String inviterName, String accepterName) throws Exception {
		if(!isOnline(accepterName))
			throw new Exception("Rival is offline");
		
		if(!isOnline(inviterName))
			throw new Exception("Inviter is offline");;
		
		PkCacheItem item1 = onlinePkers.get(accepterName);
		PkCacheItem item2 = onlinePkers.get(inviterName);
		
		if(item2 != null) {
			if(item1 != null && item1 != item2) {
				throw new Exception("Rival is PK");
			}
			
			removePkers(inviterName);
		} else {
			if(item1 != null) {
				throw new Exception("Rival is PK");
			}
		}

			
		return addPkers(inviterName, accepterName);	
	}
	
	public synchronized List<PlayerTO> findPlayers(PageInfo pageInfo) {
		ArrayList<String> nameList = new ArrayList<String>();
		
		for(String tn : onlinePlayers.keySet()) {
			int i = 0;
			for(; i<nameList.size(); i++) {
				if(nameList.get(i).compareTo(tn) >= 0) {
					break;
				}
			}
			nameList.add(i, tn);
		}
		
		int startIndex = 0;
		int endIndex = nameList.size();
		if(pageInfo != null) {
			pageInfo.setTotal(nameList.size());
			if(pageInfo.getPageSize() > 0) {
				startIndex = (pageInfo.getPageNumber() - 1) * pageInfo.getPageSize();
				endIndex = startIndex + pageInfo.getPageSize();
				if(endIndex > nameList.size())
					endIndex = nameList.size();
			}
		}
		
		if(startIndex < 0)
			startIndex = 0;
		
		List<PlayerTO> ps = new LinkedList<PlayerTO>();
		for(int i=startIndex; i<endIndex; i++) {
			String name = nameList.get(i);
			PlayerCacheItem pci = onlinePlayers.get(name);
			if(pci != null) {
				try {
					PlayerTO tp = new PlayerTO();
					tp.setId(pci.getPlayer().getId());
					tp.setCreateTimeStr(pci.getPlayer().getCreateTimeStr());
					tp.setIp(pci.getNetContext().getRemoteAddress());
					tp.setLastTimeStr(pci.getPlayer().getLastTimeStr());
					tp.setName(name);
					PkCacheItem cc = onlinePkers.get(name);
					if(cc != null) {
						if(cc.getPlayerName1().equals(name))
							tp.setRival(cc.getPlayerName2());
						else
							tp.setRival(cc.getPlayerName1());
						tp.setStatus("Online Pking");
					} else {
						tp.setStatus("Not online pk");
					}
					
					ps.add(tp);
				} catch (Exception e) {
					LOGGER.debug("error", e);
				}
			}
		}
		
		return ps;
	}
}
