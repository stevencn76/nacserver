package nacserver.net;

import nacserver.cache.PkCacheItem;
import nacserver.cache.PlayerCache;
import nacserver.cache.PlayerCacheItem;
import nacserver.common.Global;
import net.ojava.network.tcp.TCPMessage;
import net.ojava.network.tcp.TCPNetServerListener;
import net.ojava.network.tcp.TCPNetSessionContext;
import net.ojava.network.tcp.TCPRequest;
import net.ojava.network.tcp.TCPResponse;
import net.ojava.network.telnet.log.NetLogger;
import net.ojava.noughtsandcrosses.command.InviteCommand;
import net.ojava.noughtsandcrosses.command.LoginCommand;
import net.ojava.noughtsandcrosses.command.RegisterCommand;
import net.ojava.noughtsandcrosses.command.RivalOfflineMessage;
import net.ojava.noughtsandcrosses.command.StepCommand;

public class TCPNetServerListenerImpl implements TCPNetServerListener {
	private static final NetLogger LOGGER = NetLogger.getLogger(TCPNetServerListenerImpl.class);

	@Override
	public void onConnected(TCPNetSessionContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClosed(TCPNetSessionContext context) {
		// TODO Auto-generated method stub
		String name = (String)context.getAttribute(Global.KEY_CONTEXT_USER);
		if(name != null) {
			PkCacheItem pkItem = PlayerCache.getInstance().getPkCacheItem(name);
			PlayerCache.getInstance().removeOnlinePlayer(name);
			if(pkItem != null) {
				PlayerCacheItem playerItem = null;
				if(pkItem.getPlayerName1().equals(name)) {
					playerItem = PlayerCache.getInstance().getPlayerCacheItem(pkItem.getPlayerName2());
				} else {
					playerItem = PlayerCache.getInstance().getPlayerCacheItem(pkItem.getPlayerName1());
				}
				if(playerItem != null) {
					try {
						RivalOfflineMessage msgData = new RivalOfflineMessage();
						msgData.rivalName = name;
						TCPMessage msg = new TCPMessage();
						msg.setData(msgData);
						playerItem.getNetContext().postMessage(msg);
					} catch (Exception e) {
						LOGGER.debug("post message error", e);
					}
				}
			}
		}
	}

	@Override
	public void onMessage(TCPNetSessionContext context, TCPMessage message) {
		if(message == null || message.getData() == null)
			return;
		
	}

	@Override
	public void onRequest(TCPNetSessionContext context, TCPRequest request,
			TCPResponse response) {
		if(request == null || request.getData() == null)
			return;

		if(request.getData() instanceof RegisterCommand) {
			RegisterCommand cmd = (RegisterCommand)(request.getData());
			CommandProcessor.getInstance().doRegister(context, cmd);
			response.setData(cmd);
		} else if(request.getData() instanceof LoginCommand) {
			LoginCommand cmd = (LoginCommand)(request.getData());
			CommandProcessor.getInstance().doLogin(context, cmd);
			response.setData(cmd);
		} else if(request.getData() instanceof InviteCommand) {
			InviteCommand cmd = (InviteCommand)request.getData();
			CommandProcessor.getInstance().doInvite(context, cmd);
			response.setData(cmd);
		} else if(request.getData() instanceof StepCommand) {
			StepCommand cmd = (StepCommand)request.getData();
			CommandProcessor.getInstance().doStep(context, cmd);
			response.setData(cmd);
		}
	}

}
