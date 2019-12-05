package pl.tycm.fes.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import pl.tycm.fes.model.UserDataModel;


@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

	private static final String REALM = "DefaultRealm";
	private static final String AUTHENTICATION_SCHEME = "Bearer";

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		// Get the Authorization header from the request
		String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

		// Validate the Authorization header
		if (!isTokenBasedAuthentication(authorizationHeader)) {
			abortWithUnauthorized(requestContext, "Unable to find token in the header.");
			return;
		}

		// Extract the token from the Authorization header
		String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

		try {
			// Validate the token
			JwtClaims jwtClaims = validateToken(token);
			String principal = jwtClaims.getSubject();
			List<String> roles = jwtClaims.getStringListClaimValue("roles");
			
			UserDataModel userModel = new UserDataModel();
			userModel.setUsername(principal);
			userModel.setRolesList(new ArrayList<>(roles));
			UserSecurityContext securityContext = new UserSecurityContext(userModel);

			requestContext.setSecurityContext(securityContext);
			
		} catch (InvalidJwtException e) {
			if (e.hasExpired()) {
				try {
					logger.fatal("JWT expired at " + e.getJwtContext().getJwtClaims().getExpirationTime());
				} catch (MalformedClaimException em) {
					logger.fatal("JWT Fatal: " + em.getMessage());
					em.printStackTrace();
				}
				abortWithUnauthorized(requestContext, e.getMessage());
			}
		} catch (MalformedClaimException e) {
			
		}
	}

	private boolean isTokenBasedAuthentication(String authorizationHeader) {

		// Check if the Authorization header is valid
		// It must not be null and must be prefixed with "Bearer" plus a whitespace
		// The authentication scheme comparison must be case-insensitive
		return authorizationHeader != null
				&& authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
	}

	private void abortWithUnauthorized(ContainerRequestContext requestContext, String errorDescryption) {

		// Abort the filter chain with a 401 status code response
		// The WWW-Authenticate header is sent along with the response
		requestContext
				.abortWith(Response.status(Response.Status.UNAUTHORIZED)
						.header(HttpHeaders.WWW_AUTHENTICATE,
								AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\","
										+ " error=\"invalid_token\", error_description=\"" + errorDescryption + "\"")
						.build());
	}

	private JwtClaims validateToken(String token) throws InvalidJwtException {

		JsonWebKeySet jwks = new JsonWebKeySet(AuthenticationEndpointRestService.jwkList);
		JsonWebKey jwk = jwks.findJsonWebKey("1", null, null, null);
		logger.info("JWK (1) ===> " + jwk.toJson());

		// Validate Token's authenticity and check claims
		JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime().setAllowedClockSkewInSeconds(30)
				.setRequireSubject().setExpectedIssuer("mikroserwisy.it.local").setVerificationKey(jwk.getKey())
				.build();

		// Validate the JWT and process it to the Claims
		JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
		logger.info("JWT validation succeeded! ");
		return jwtClaims;     
		
	}
}
