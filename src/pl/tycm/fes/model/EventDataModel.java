package pl.tycm.fes.model;

public class EventDataModel {
	private int id;
	private int fileExchangeStatusID;
	private String eventText;
	
	@Override
	public String toString() {
		return "EventDataModel [id=" + id + ", fileExchangeStatusID=" + fileExchangeStatusID + ", eventText="
				+ eventText + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFileExchangeStatusID() {
		return fileExchangeStatusID;
	}

	public void setFileExchangeStatusID(int fileExchangeStatusID) {
		this.fileExchangeStatusID = fileExchangeStatusID;
	}

	public String getEventText() {
		return eventText;
	}

	public void setEventText(String eventText) {
		this.eventText = eventText;
	}
}
