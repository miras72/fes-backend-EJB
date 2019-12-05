package pl.tycm.fes.model;

import java.util.List;

public class FileExchangeStatusDataModel {
	private int id;
	private int taskID;
	private String eventDateTime;
	
	private List<EventDataModel> events;

	@Override
	public String toString() {
		return "FileExchangeStatusDataModel [id=" + id + ", taskID=" + taskID + ", eventDateTime=" + eventDateTime + ", events="
				+ events + "]";
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

	public String getEventDateTime() {
		return eventDateTime;
	}

	public void setEventDateTime(String eventDateTime) {
		this.eventDateTime = eventDateTime;
	}

	public List<EventDataModel> getEvents() {
		return events;
	}

	public void setEvents(List<EventDataModel> events) {
		this.events = events;
	}
	
}
