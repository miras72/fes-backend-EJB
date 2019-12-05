package pl.tycm.fes.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import pl.tycm.fes.model.UserDataModel;

@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		UserDataModel userModel = new UserDataModel();
		userModel.setRolesList(new ArrayList<>(Arrays.asList("operator")));
		UserSecurityContext securityContext = new UserSecurityContext(userModel);

		requestContext.setSecurityContext(securityContext);
	}
}
