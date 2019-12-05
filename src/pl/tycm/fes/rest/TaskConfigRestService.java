package pl.tycm.fes.rest;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.NoCache;

import pl.tycm.fes.bean.JobSessionBean;
import pl.tycm.fes.dao.TaskConfigDAO;
import pl.tycm.fes.dao.TaskConfigDAOImpl;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskConfigDataModel;

@Path("/task")
public class TaskConfigRestService {

	@EJB
	JobSessionBean jobSessionBean;
	
	@GET
	@NoCache
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonTaskConfig(@PathParam("id") int id) {

		StatusMessage statusMessage = new StatusMessage();
		TaskConfigDAO taskConfigDAO = new TaskConfigDAOImpl();
		
		TaskConfigDataModel taskConfigModel = taskConfigDAO.getTaskConfig(id, statusMessage);
		if (taskConfigModel != null) {
			return Response.status(200).entity(taskConfigModel).build();
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
	public Response deleteJsonTaskConfig(@PathParam("id") int id) {

		StatusMessage statusMessage = new StatusMessage();
		TaskConfigDAO taskConfigDAO = new TaskConfigDAOImpl();
		
		boolean isDelete = taskConfigDAO.deleteTaskConfig(id, statusMessage);
		if (isDelete) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}

	@PUT
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateJsonTaskConfig(TaskConfigDataModel taskConfigDataModel) {
		StatusMessage statusMessage = new StatusMessage();
		TaskConfigDAO taskConfigDAO = new TaskConfigDAOImpl();
		
		boolean isCreated = taskConfigDAO.updateTaskConfig(taskConfigDataModel, statusMessage);
		if (isCreated) {
			try {
				jobSessionBean.updateJob(taskConfigDataModel);
			} catch (Exception e) {
				return Response.status(404).entity(e.getMessage()).build();
			}
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}
	
	@POST
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJsonTaskConfig(TaskConfigDataModel newTaskConfigDataModel) {
		StatusMessage statusMessage = new StatusMessage();
		TaskConfigDAO taskConfigDAO = new TaskConfigDAOImpl();
		
		TaskConfigDataModel taskConfigModel = taskConfigDAO.createTaskConfig(newTaskConfigDataModel, statusMessage);
		if (taskConfigModel != null) {
			try {
				jobSessionBean.createJob(taskConfigModel);
			} catch (Exception e) {
				return Response.status(404).entity(e.getMessage()).build();
			}
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}
	
}
