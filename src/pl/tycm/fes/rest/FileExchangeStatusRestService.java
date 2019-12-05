package pl.tycm.fes.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.NoCache;

import pl.tycm.fes.dao.FileExchangeStatusDAO;
import pl.tycm.fes.dao.FileExchangeStatusDAOImpl;
import pl.tycm.fes.model.FileExchangeStatusDataModel;
import pl.tycm.fes.model.StatusMessage;

@Path("/file-exchange-status")
public class FileExchangeStatusRestService {

	@GET
	@NoCache
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonFileExchangeStatus(@QueryParam("taskID") int taskID,
			@QueryParam("eventDateTime") String eventDateTime) {

		StatusMessage statusMessage = new StatusMessage();
		FileExchangeStatusDAO fileExchangeStatusDAO = new FileExchangeStatusDAOImpl();

		FileExchangeStatusDataModel fileExchangeStatusModel = fileExchangeStatusDAO.getFileExchangeStatus(taskID,
				eventDateTime, statusMessage);
		if (fileExchangeStatusModel != null) {
			return Response.status(200).entity(fileExchangeStatusModel).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}
}
