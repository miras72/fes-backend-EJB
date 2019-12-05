package pl.tycm.fes.model;

import java.util.List;

public class ManualFileExchangeDataModel {
	private String eventDateTime;
	private int taskID;
	private String fileDate;

	private List<FileListDataModel> fileList;

	@Override
	public String toString() {
		return "ManualFileExchangeDataModel [eventDateTime=" + eventDateTime + ", taskID=" + taskID + ", fileDate=" + fileDate + ", fileList="
				+ fileList + "]";
	}

	public String getEventDateTime() {
		return eventDateTime;
	}

	public void setEventDateTime(String eventDateTime) {
		this.eventDateTime = eventDateTime;
	}

	public int getTaskID() {
		return taskID;
	}

	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	public String getFileDate() {
		return fileDate;
	}

	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}

	public List<FileListDataModel> getFileList() {
		return fileList;
	}

	public void setFileList(List<FileListDataModel> fileList) {
		this.fileList = fileList;
	}

}
