package nacserver.resource.impl;

import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.core.Context;

import nacserver.cache.PlayerCache;
import nacserver.dto.DataGridTO;
import nacserver.dto.JsonResponse;
import nacserver.dto.PageInfo;
import nacserver.dto.PlayerTO;
import nacserver.entity.Player;
import nacserver.resource.GameResource;
import nacserver.service.PlayerService;
import net.ojava.network.telnet.log.NetLogger;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;

@WebService(endpointInterface = "nacserver.resource.GameResource")
public class GameResourceImpl implements GameResource {
	private static final NetLogger LOGGER = NetLogger.getLogger(GameResourceImpl.class);

	@Context 
	private MessageContext context;
	
	@Autowired
	private PlayerService playerService;

	@Override
	public JsonResponse findOnlinePlayers(PageInfo pageInfo) {
		JsonResponse res = new JsonResponse();
		List<PlayerTO> players = PlayerCache.getInstance().findPlayers(pageInfo);
		DataGridTO dgto = new DataGridTO();
		dgto.setTotal(pageInfo.getTotal());
		if(players != null) {
			for(PlayerTO tp : players) {
				dgto.getRows().add(tp);
			}
		}
		res.setResult("ok");
		res.setData(dgto);
		return res;
	}

	@Override
	public JsonResponse findAllPlayers(PageInfo pageInfo) {
		JsonResponse res = new JsonResponse();
		
		try {
			if(pageInfo == null)
				pageInfo = new PageInfo();
			
			List<Player> players = playerService.findPlayers(pageInfo);
			DataGridTO dgto = new DataGridTO();
			dgto.setTotal(pageInfo.getTotal());
			if(players != null) {
				for(Player tp : players) {
					PlayerTO tpo = PlayerTO.from(tp);
					if(PlayerCache.getInstance().isOnline(tp.getName())) {
						tpo.setStatus("online");
					} else {
						tpo.setStatus("offline");
					}
					dgto.getRows().add(tpo);
				}
			}
			res.setResult("ok");
			res.setData(dgto);
		} catch (Throwable t) {
			LOGGER.debug("find player fail", t);
			res.setResult("Find player error: " + t.getMessage());
		}
		return res;
	}
}
