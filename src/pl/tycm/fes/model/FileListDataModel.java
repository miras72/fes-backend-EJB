package pl.tycm.fes.model;

import java.io.Serializable;

public class FileListDataModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6059499242536509605L;
	private int id;
	private int taskID;
	private String fileName;
	
	@Override
	public String toString() {
		return "FileListDataModel [id=" + id + ", taskID=" + taskID + ", fileName=" + fileName + "]";
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
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
