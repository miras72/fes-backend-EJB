package pl.tycm.fes.model;

import java.io.Serializable;

public class Credentials implements Serializable {

	private static final long serialVersionUID = -5897161773154498770L;

	private String username;
	private String password;

	@Override
	public String toString() {
		return "Credentials [username=" + username + ", password=" + password + "]";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
