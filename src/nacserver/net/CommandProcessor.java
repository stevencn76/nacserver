package nacserver.net;

import nacserver.cache.PkCacheItem;
import nacserver.cache.PlayerCache;
import nacserver.cache.PlayerCacheItem;
import nacserver.common.Global;
import nacserver.entity.Player;
import nacserver.service.PlayerService;
import net.ojava.network.tcp.TCPNetSessionContext;
import net.ojava.network.tcp.TCPRequest;
import net.ojava.network.tcp.TCPResponse;
import net.ojava.network.telnet.log.NetLogger;
import net.ojava.noughtsandcrosses.command.InviteCommand;
import net.ojava.noughtsandcrosses.command.LoginCommand;
import net.ojava.noughtsandcrosses.command.PkData;
import net.ojava.noughtsandcrosses.command.RegisterCommand;
import net.ojava.noughtsandcrosses.command.StepCommand;

import org.springframework.web.context.WebApplicationContext;

public class CommandProcessor {
	private static final NetLogger LOGGER = NetLogger.getLogger(CommandProcessor.class);
	
	private static CommandProcessor instance;
	private static Object instanceLock = new Object();
	
	private WebApplicationContext springContext;
	
	private CommandProcessor() {
		
	}
	
	public static CommandProcessor getInstance() {
		synchronized(instanceLock) {
			if(instance == null) {
				instance = new CommandProcessor();
			}
		}
		
		return instance;
	}

	public WebApplicationContext getSpringContext() {
		return springContext;
	}

	public void setSpringContext(WebApplicationContext springContext) {
		this.springContext = springContext;
	}
	
	public void doRegister(TCPNetSessionContext context, RegisterCommand cmd) {
		try {
			PlayerService service = (PlayerService)springContext.getBean("playerService");
			Player p = new Player();
			p.setName(cmd.name);
			p.setPassword(cmd.password);
			p.setIp(context.getRemoteAddress());
			
			service.createPlayer(p);
			
			cmd.result = "ok";
		} catch (Exception e) {
			cmd.result = "Register failed: " + e.getMessage();
			LOGGER.debug("register failed", e);
		}
	}
	
	public void doLogin(TCPNetSessionContext context, LoginCommand cmd) {
		try {
			PlayerService service = (PlayerService)springContext.getBean("playerService");
			
			Player tp = service.findPlayer(cmd.name, cmd.password);
			if(tp != null) {
				context.setAttribute(Global.KEY_CONTEXT_USER, cmd.name);
				cmd.result = "ok";
				
				PlayerCache.getInstance().addOnlinePlayer(tp, context);
			} else {
				cmd.result = "Login failed: Name or password error";
			}
		} catch (Exception e) {
			cmd.result = "Login failed: " + e.getMessage();
			LOGGER.debug("Login failed", e);
		}
	}
	
	public void doInvite(TCPNetSessionContext context, InviteCommand cmd) {
		try {
			PkCacheItem item = PlayerCache.getInstance().buildPkCache(cmd.inviterName, cmd.accepterName);
			
			PlayerCacheItem pi = PlayerCache.getInstance().getPlayerCacheItem(cmd.accepterName);
			
			item.setCurTurn(PkCacheItem.getRandomTurn());
			PkData pkData = makePkData(item);
			
			StepCommand stepCmd = new StepCommand();
			stepCmd.pkData = pkData;
			
			TCPRequest req = new TCPRequest();
			req.setData(stepCmd);
			TCPResponse res = pi.getNetContext().sendRequest(req);
			if(res.getData() instanceof StepCommand) {
				StepCommand tc = (StepCommand)res.getData();
				if(tc.result != null && tc.result.equals("ok")) {
					cmd.result = "ok";
					cmd.pkData = pkData;
				} else {
					cmd.result = tc.result;
				}
			} else {
				cmd.result = "Forward command failed";
			}
		} catch (Exception e) {
			cmd.result = "Error: " + e.getMessage();
		}
	}
	
	public void doStep(TCPNetSessionContext context, StepCommand cmd) {
		try {
			String name = (String)context.getAttribute(Global.KEY_CONTEXT_USER);
			PkCacheItem item = PlayerCache.getInstance().getPkCacheItem(name);
			if(item == null) {
				cmd.result = "Rival has left";
			} else {
				String otherName = item.getPlayerName1();
				if(otherName.equals(name))
					otherName = item.getPlayerName2();
				
				PlayerCacheItem other = PlayerCache.getInstance().getPlayerCacheItem(otherName);
				if(other == null) {
					cmd.result = "Rival is offline";
				} else {
					item.setGameData(cmd.pkData.gameData);
					item.setCurTurn(cmd.pkData.turnIds.get(otherName));
					
					cmd.pkData.nextTurnId = item.getCurTurn();
					TCPRequest req = new TCPRequest();
					StepCommand tcmd = new StepCommand();
					tcmd.pkData = makePkData(item);
					tcmd.pkData.nextTurnId = tcmd.pkData.turnIds.get(otherName);
					req.setData(tcmd);
					TCPResponse res = other.getNetContext().sendRequest(req);
					if(res.getData() instanceof StepCommand) {
						item.setStatus(PkCacheItem.STATUS_GAMING);
						cmd.result = "ok";
					} else {
						cmd.result = "Rival has left";
					}
				}
			}
		} catch (Exception e) {
			cmd.result = "Error: " + e.getMessage();
		}
	}
	
	private PkData makePkData(PkCacheItem item) {
		PkData pkData = new PkData();
		pkData.name1 = item.getPlayerName1();
		pkData.name2 = item.getPlayerName2();
		pkData.gameData = item.getGameData();
		pkData.gameOver = false;
		pkData.nextTurnId = item.getCurTurn();
		pkData.turnIds.put(pkData.name1, 0);
		pkData.turnIds.put(pkData.name2, 1);
		pkData.signIds.put(pkData.name1, 1);
		pkData.signIds.put(pkData.name2, 2);
		
		return pkData;
	}
}
