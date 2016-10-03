package nacserver.resource;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nacserver.dto.JsonResponse;
import nacserver.dto.PageInfo;

import org.springframework.context.annotation.Scope;

@WebService
@Path("/game")
@Produces( { MediaType.APPLICATION_JSON })
@Consumes( { MediaType.APPLICATION_JSON })
@Scope("singleton")
public interface GameResource {
	
	@WebMethod
	@POST
	@Path("/onlinelist")
	public JsonResponse findOnlinePlayers(PageInfo pageInfo);
	
	@WebMethod
	@POST
	@Path("/playerlist")
	public JsonResponse findAllPlayers(PageInfo pageInfo);
}
