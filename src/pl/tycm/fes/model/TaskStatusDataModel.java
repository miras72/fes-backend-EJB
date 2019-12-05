package pl.tycm.fes.model;

public class TaskStatusDataModel {

	private int id;
	private int taskID;
	private String subjectName;
	private String subjectMode;
	private String lastStatus;
	private String lastDataStatus;
	private String nextScheduledDate;
	private String scheduledInterval;
	private boolean scheduledIsActive;
	
	@Override
	public String toString() {
		return "TaskStatusDataModel [id=" + id + ", taskID=" + taskID + ", subjectName=" + subjectName
				+ ", subjectMode=" + subjectMode + ", lastStatus=" + lastStatus + ", lastDataStatus=" + lastDataStatus
				+ ", nextScheduledDate=" + nextScheduledDate + ", scheduledInterval=" + scheduledInterval
				+ ", scheduledIsActive=" + scheduledIsActive + "]";
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

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getSubjectMode() {
		return subjectMode;
	}

	public void setSubjectMode(String subjectMode) {
		this.subjectMode = subjectMode;
	}

	public String getLastStatus() {
		return lastStatus;
	}

	public void setLastStatus(String lastStatus) {
		this.lastStatus = lastStatus;
	}

	public String getLastDataStatus() {
		return lastDataStatus;
	}

	public void setLastDataStatus(String lastDataStatus) {
		this.lastDataStatus = lastDataStatus;
	}

	public String getNextScheduledDate() {
		return nextScheduledDate;
	}

	public void setNextScheduledDate(String nextScheduledDate) {
		this.nextScheduledDate = nextScheduledDate;
	}

	public String getScheduledInterval() {
		return scheduledInterval;
	}

	public void setScheduledInterval(String scheduledInterval) {
		this.scheduledInterval = scheduledInterval;
	}

	public boolean isScheduledIsActive() {
		return scheduledIsActive;
	}

	public void setScheduledIsActive(boolean scheduledIsActive) {
		this.scheduledIsActive = scheduledIsActive;
	}
}
