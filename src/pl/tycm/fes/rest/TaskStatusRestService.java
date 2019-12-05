package pl.tycm.fes.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.NoCache;

import pl.tycm.fes.dao.TaskStatusDAO;
import pl.tycm.fes.dao.TaskStatusDAOImpl;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskStatusDataModel;

@Path("/task-status")
public class TaskStatusRestService {

	@GET
	@NoCache
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJsonTaskStatus() {

		StatusMessage statusMessage = new StatusMessage();
		TaskStatusDAO taskStatusDAO = new TaskStatusDAOImpl();
		List<TaskStatusDataModel> response = taskStatusDAO.getAllTaskStatus(statusMessage);

		if (response != null) {
			return Response.status(200).entity(response).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}
}
