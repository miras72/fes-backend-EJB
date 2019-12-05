package pl.tycm.fes.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import pl.tycm.fes.dao.DecryptionKeyDAO;
import pl.tycm.fes.dao.DecryptionKeyDAOImpl;
import pl.tycm.fes.model.DecryptionKeyDataModel;
import pl.tycm.fes.model.StatusMessage;

@Path("/")
public class DecryptionKeyRestService {

	private final Logger logger = Logger.getLogger(this.getClass());

	@GET
	@NoCache
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Path("/download/decryption-key/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getStreamDecryptionKey(@PathParam("id") int id) {

		StatusMessage statusMessage = new StatusMessage();
		DecryptionKeyDAO decryptionKeyDAO = new DecryptionKeyDAOImpl();
		DecryptionKeyDataModel decryptionKeyDataModel = decryptionKeyDAO.getDecryptionKey(id, statusMessage);
		if (decryptionKeyDataModel != null) {
			String filename = decryptionKeyDataModel.getDecryptionKeyName();
			try {
				byte[] in = decryptionKeyDataModel.getDecryptionKeyBinaryFile();
				ByteArrayOutputStream out = new ByteArrayOutputStream(in.length);
				out.write(in, 0, in.length);
				out.flush();

				Response.ResponseBuilder builder = Response.ok(out.toByteArray());
				builder.header("Content-Disposition", "attachment; filename=" + filename);
				return builder.build();
			} catch (IOException e) {
				logger.error(e.getMessage());
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage(e.getMessage());
			}
		} else {
			return Response.status(404).entity(statusMessage).type("application/json").build();
		}
		return Response.status(404).entity(statusMessage).type("application/json").build();
	}

	@PUT
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Path("/upload/decryption-key/{id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createStreamDecryptionKey(@PathParam("id") int id,
			@MultipartForm DecryptionKeyDataModel decryptionKeyDataModel) {
		decryptionKeyDataModel.setId(id);
		StatusMessage statusMessage = new StatusMessage();
		DecryptionKeyDAO decryptionKeyDAO = new DecryptionKeyDAOImpl();
		boolean isUpdated = decryptionKeyDAO.updateDecryptionKey(decryptionKeyDataModel, statusMessage);
		if (isUpdated) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}

	@GET
	@NoCache
	@Path("/decryption-key")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonDecryptionKeys() {

		StatusMessage statusMessage = new StatusMessage();
		DecryptionKeyDAO decryptionKeyDAO = new DecryptionKeyDAOImpl();
		List<DecryptionKeyDataModel> decryptionKeyModel = decryptionKeyDAO.getAllDecryptionKeyName(statusMessage);

		if (decryptionKeyModel != null) {
			return Response.status(200).entity(decryptionKeyModel).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}

	@DELETE
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Path("/decryption-key/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteJsonDecryptionKey(@PathParam("id") int id) {

		StatusMessage statusMessage = new StatusMessage();
		DecryptionKeyDAO decryptionKeyDAO = new DecryptionKeyDAOImpl();

		boolean isDelete = decryptionKeyDAO.deleteDecryptionKey(id, statusMessage);
		if (isDelete) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}

	@POST
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Path("/decryption-key")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createStreamDecryptionKey(@MultipartForm DecryptionKeyDataModel decryptionKeyDataModel) {
		StatusMessage statusMessage = new StatusMessage();
		DecryptionKeyDAO decryptionKeyDAO = new DecryptionKeyDAOImpl();

		boolean isUpdated = decryptionKeyDAO.createDecryptionKey(decryptionKeyDataModel, statusMessage);
		if (isUpdated) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}
}
