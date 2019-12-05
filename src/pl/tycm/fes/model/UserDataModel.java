package pl.tycm.fes.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDataModel {

	private String username;
	private List<String> rolesList = new ArrayList<String>();

	@Override
	public String toString() {
		return "User [username=" + username + ", rolesList=" + rolesList + "]";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonProperty(value = "roles")
	public List<String> getRolesList() {
		return rolesList;
	}

	public void setRolesList(List<String> rolesList) {
		this.rolesList = rolesList;
	}
}
