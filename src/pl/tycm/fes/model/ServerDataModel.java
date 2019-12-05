package pl.tycm.fes.model;

import java.io.Serializable;

public class ServerDataModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7163695325272408987L;
	private int id;
	private int taskID;
	private String serverAddress;
	private String serverLogin;
	private String serverPassword;
	private String serverDirectory;
	
	@Override
	public String toString() {
		return "ServerDataModel [id=" + id + ", taskID=" + taskID + ", serverAddress=" + serverAddress
				+ ", serverLogin=" + serverLogin + ", serverPassword=" + serverPassword
				+ ", serverDirectory=" + serverDirectory + "]";
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getTaskID() {
		return taskID;
	}
	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}
	public String getServerAddress() {
		return serverAddress;
	}
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	public String getServerLogin() {
		return serverLogin;
	}
	public void setServerLogin(String serverLogin) {
		this.serverLogin = serverLogin;
	}
	public String getServerPassword() {
		return serverPassword;
	}
	public void setServerPassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}
	public String getServerDirectory() {
		return serverDirectory;
	}
	public void setServerDirectory(String serverDirectory) {
		this.serverDirectory = serverDirectory;
	}
}
