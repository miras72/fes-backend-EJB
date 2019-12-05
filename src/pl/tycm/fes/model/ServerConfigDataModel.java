package pl.tycm.fes.model;

public class ServerConfigDataModel {
	
	private String mailServer;
	private String workDirectory;
	private String archDirectory;

	@Override
	public String toString() {
		return "ServerConfigDataModel [mailServer=" + mailServer + ", workDirectory=" + workDirectory
				+ ", archDirectory=" + archDirectory + "]";
	}
	public String getMailServer() {
		return mailServer;
	}
	public void setMailServer(String mailServer) {
		this.mailServer = mailServer;
	}
	public String getWorkDirectory() {
		return workDirectory;
	}
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}
	public String getArchDirectory() {
		return archDirectory;
	}
	public void setArchDirectory(String archDirectory) {
		this.archDirectory = archDirectory;
	}
}
