package pl.tycm.fes.rest;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import pl.tycm.fes.bean.ManualFileExchangeServiceBean;
import pl.tycm.fes.model.ManualFileExchangeDataModel;
import pl.tycm.fes.model.StatusMessage;

@Path("/file")
public class ManualFileExchangeRestService {

	@EJB
	ManualFileExchangeServiceBean manualFileExchange;

	@POST
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJsonManualFileExchangeTask(ManualFileExchangeDataModel manualFileExchangeDataModel) {
		StatusMessage statusMessage = new StatusMessage();
		// ManualFileExchangeDAO manualFileExchangeDAO = new
		// ManualFileExchangeDAOImpl();
		boolean isStart = manualFileExchange.startManualFileExchange(manualFileExchangeDataModel, statusMessage);
		if (isStart) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}

	@DELETE
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteJsonManualFileExchangeTask(@PathParam("id") int id) {

		StatusMessage statusMessage = new StatusMessage();
		// ManualFileExchangeDAO manualFileExchangeDAO = new
		// ManualFileExchangeDAOImpl();

		boolean isStop = manualFileExchange.stopManualFileExchange(id, statusMessage);
		if (isStop) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}
}
