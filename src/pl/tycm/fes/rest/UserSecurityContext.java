package pl.tycm.fes.rest;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import pl.tycm.fes.model.UserDataModel;

public class UserSecurityContext implements SecurityContext {

	private final UserDataModel userDetails;

	public UserSecurityContext(UserDataModel userDetails) {
		this.userDetails = userDetails;
	}

	public Principal getUserPrincipal() {
		return new Principal() {

			public String getName() {
				return userDetails.getUsername();
			}
		};
	}

	@Override
	public String getAuthenticationScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserInRole(String role) {
		for (String item : userDetails.getRolesList()) {
			return item.equalsIgnoreCase(role);
		}
		return false;
	}

}
