package pl.tycm.fes.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.NoCache;

import pl.tycm.fes.dao.ServerConfigDAO;
import pl.tycm.fes.dao.ServerConfigDAOImpl;
import pl.tycm.fes.model.ServerConfigDataModel;
import pl.tycm.fes.model.StatusMessage;

@Path("/server-config")
public class ServerRestService {

	@GET
	@NoCache
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonServerConfig() {

		StatusMessage statusMessage = new StatusMessage();
		ServerConfigDAO serverConfigDAO = new ServerConfigDAOImpl();
		ServerConfigDataModel response = serverConfigDAO.getServerConfig(statusMessage);

		if (response != null) {
			return Response.status(200).entity(response).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}

	@PUT
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateJsonTaskConfig(ServerConfigDataModel serverConfigDataModel) {
		StatusMessage statusMessage = new StatusMessage();
		ServerConfigDAO serverConfigDAO = new ServerConfigDAOImpl();

		boolean isUpdate = serverConfigDAO.updateServeConfig(serverConfigDataModel, statusMessage);
		if (isUpdate) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}
}
