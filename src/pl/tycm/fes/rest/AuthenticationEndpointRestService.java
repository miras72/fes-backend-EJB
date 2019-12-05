package pl.tycm.fes.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import pl.tycm.fes.model.Credentials;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.UserDataModel;

@Path("/authenticate")
public class AuthenticationEndpointRestService {

	private final static Logger logger = Logger.getLogger(AuthenticationEndpointRestService.class);
	static List<JsonWebKey> jwkList = null;

	static {
		logger.info("Inside static initializer...");
		jwkList = new LinkedList<>();
		for (int kid = 1; kid <= 3; kid++) {
			JsonWebKey jwk = null;
			try {
				jwk = RsaJwkGenerator.generateJwk(2048);
				logger.info("PUBLIC KEY (" + kid + "): " + jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
			} catch (JoseException e) {
				e.printStackTrace();
			}
			jwk.setKeyId(String.valueOf(kid));
			jwkList.add(jwk);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticateUser(Credentials credentials) {

		String username = credentials.getUsername();
		String password = credentials.getPassword();

		StatusMessage statusMessage = new StatusMessage();

		logger.info("Authenticating User Credentials...");

		if (username == null) {
			logger.error("Username value is missing!!!");
			statusMessage.setStatus(Status.PRECONDITION_FAILED.getStatusCode());
			statusMessage.setMessage("Nie podano nazwy użytkownika!!!");
			return Response.status(Status.PRECONDITION_FAILED.getStatusCode()).entity(statusMessage).build();
		}

		if (password == null) {
			logger.error("Password value is missing!!!");
			statusMessage.setStatus(Status.PRECONDITION_FAILED.getStatusCode());
			statusMessage.setMessage("Nie podano hasła!!!");
			return Response.status(Status.PRECONDITION_FAILED.getStatusCode()).entity(statusMessage).build();
		}

		UserDataModel user = authenticate(username, password);
		if (user == null) {
			logger.error("Access Denied for this functionality!!!");
			statusMessage.setStatus(Status.PRECONDITION_FAILED.getStatusCode());
			statusMessage.setMessage("Nie masz odpowiednich uprawnień!!!");
			return Response.status(Status.FORBIDDEN.getStatusCode()).entity(statusMessage).build();
		}

		try {
			String jwtToken = issueToken(user);

			statusMessage.setStatus(Status.OK.getStatusCode());
			statusMessage.setMessage(jwtToken);
			return Response.status(Status.OK.getStatusCode()).entity(statusMessage).build();
		} catch (JoseException e) {
			logger.fatal("JWK Failed: " + e.getMessage());
			e.printStackTrace();
			statusMessage.setMessage("Dostęp Zabroniony!!!");
			return Response.status(Response.Status.FORBIDDEN).entity(statusMessage).build();
		}
	}

	private UserDataModel authenticate(String username, String password) {

		UserDataModel user = null;

		if (username.equals("admin") && password.equals("CBA18jim")) {
			user = new UserDataModel();
			user.setUsername(username);
			user.setRolesList(new ArrayList<>(Arrays.asList("ADMIN")));
		}
		return user;
	}

	private String issueToken(UserDataModel user) throws JoseException {
		RsaJsonWebKey senderJwk = (RsaJsonWebKey) jwkList.get(0);

		senderJwk.setKeyId("1");
		logger.info("JWK (1) ===> " + senderJwk.toJson());

		// Create the Claims, which will be the content of the JWT
		JwtClaims claims = new JwtClaims();
		claims.setIssuer("mikroserwisy.it.local");
		claims.setExpirationTimeMinutesInTheFuture(30);
		claims.setGeneratedJwtId();
		claims.setIssuedAtToNow();
		claims.setNotBeforeMinutesInThePast(2);
		claims.setSubject(user.getUsername());
		claims.setStringListClaim("roles", user.getRolesList());

		JsonWebSignature jws = new JsonWebSignature();

		jws.setPayload(claims.toJson());

		jws.setKeyIdHeaderValue(senderJwk.getKeyId());
		jws.setKey(senderJwk.getPrivateKey());

		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

		String jwtToken = jws.getCompactSerialization();

		return jwtToken;
	}
}
