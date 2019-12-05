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

import pl.tycm.fes.dao.EncryptionKeyDAO;
import pl.tycm.fes.dao.EncryptionKeyDAOImpl;
import pl.tycm.fes.model.EncryptionKeyDataModel;
import pl.tycm.fes.model.PrivateKeyDataModel;
import pl.tycm.fes.model.PublicKeyDataModel;
import pl.tycm.fes.model.StatusMessage;

@Path("/")
public class EncryptionKeyRestService {

	private final Logger logger = Logger.getLogger(this.getClass());

	@GET
	@NoCache
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Path("/download/private-key/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getStreamPrivateKey(@PathParam("id") int id) {

		StatusMessage statusMessage = new StatusMessage();
		EncryptionKeyDAO encryptionKeyDAO = new EncryptionKeyDAOImpl();
		PrivateKeyDataModel privateKeyDataModel = encryptionKeyDAO.getPrivateKey(id, statusMessage);
		if (privateKeyDataModel != null) {
			String filename = privateKeyDataModel.getPrivateKeyName();
			try {
				byte[] in = privateKeyDataModel.getPrivateKeyBinaryFile();
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
	@Path("/upload/private-key/{id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createStreamPrivateKey(@PathParam("id") int id,
			@MultipartForm PrivateKeyDataModel privateKeyDataModel) {
		privateKeyDataModel.setId(id);
		StatusMessage statusMessage = new StatusMessage();
		EncryptionKeyDAO encryptionKeyDAO = new EncryptionKeyDAOImpl();
		boolean isUpdated = encryptionKeyDAO.updatePrivateKey(privateKeyDataModel, statusMessage);
		if (isUpdated) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}

	@GET
	@NoCache
	@Path("/download/public-key/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getStreamPublicKey(@PathParam("id") int id) {

		StatusMessage statusMessage = new StatusMessage();
		EncryptionKeyDAO encryptionKeyDAO = new EncryptionKeyDAOImpl();
		PublicKeyDataModel publicKeyDataModel = encryptionKeyDAO.getPublicKey(id, statusMessage);

		if (publicKeyDataModel != null) {
			String filename = publicKeyDataModel.getPublicKeyName();
			try {
				byte[] in = publicKeyDataModel.getPublicKeyBinaryFile();
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
	@Path("/upload/public-key/{id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createStreamPublicKey(@PathParam("id") int id,
			@MultipartForm PublicKeyDataModel publicKeyDataModel) {
		publicKeyDataModel.setId(id);
		StatusMessage statusMessage = new StatusMessage();
		EncryptionKeyDAO encryptionKeyDAO = new EncryptionKeyDAOImpl();
		boolean isUpdated = encryptionKeyDAO.updatePublicKey(publicKeyDataModel, statusMessage);
		if (isUpdated) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}

	@GET
	@NoCache
	@Path("/encryption-keys")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonEncryptionKeys() {

		StatusMessage statusMessage = new StatusMessage();
		EncryptionKeyDAO encryptionKeyDAO = new EncryptionKeyDAOImpl();
		List<EncryptionKeyDataModel> encryptionKeyModel = encryptionKeyDAO.getAllEncryptionKeyName(statusMessage);

		if (encryptionKeyModel != null) {
			return Response.status(200).entity(encryptionKeyModel).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}

	@DELETE
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Path("/encryption-keys/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteJsonTaskConfig(@PathParam("id") int id) {

		StatusMessage statusMessage = new StatusMessage();
		EncryptionKeyDAO encryptionKeyDAO = new EncryptionKeyDAOImpl();

		boolean isDelete = encryptionKeyDAO.deleteEncryptionKey(id, statusMessage);
		if (isDelete) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}

	@POST
	@Secured
	@RolesAllowed({ "ADMIN" })
	@Path("/encryption-keys")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createStreamEncryptionKey(@MultipartForm EncryptionKeyDataModel encryptionKeyDataModel) {
		StatusMessage statusMessage = new StatusMessage();
		EncryptionKeyDAO encryptionKeyDAO = new EncryptionKeyDAOImpl();

		boolean isUpdated = encryptionKeyDAO.createEncryptionKey(encryptionKeyDataModel, statusMessage);
		if (isUpdated) {
			return Response.status(200).entity(statusMessage).build();
		} else {
			return Response.status(404).entity(statusMessage).build();
		}
	}
}
