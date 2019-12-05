package pl.tycm.fes.model;

import java.io.Serializable;

public class MailingListDataModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 763705440435866101L;
	private int id;
	private int taskID;
	private String recipientName;
	
	@Override
	public String toString() {
		return "MailingListDataModel [id=" + id + ", taskID=" + taskID + ", recipientName=" + recipientName + "]";
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
	public String getRecipientName() {
		return recipientName;
	}
	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}
}
