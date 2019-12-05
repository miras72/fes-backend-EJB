package pl.tycm.fes.model;

import java.io.Serializable;
import java.util.List;

public class TaskConfigDataModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1548239337536365939L;
	private int id;
	private String subjectName;
	private String subjectAddress;
	private String subjectLogin;
	private String subjectPassword;
	private String subjectDirectory;
	private String subjectExchangeProtocol;
	private int subjectEncryptionKeyID;
	private String subjectMode;
	private String subjectLoginForm;
	private String subjectLogoutForm;
	private String subjectPostOptions;
	private String subjectResponseString;

	private List<ServerDataModel> serversDataModel;

	private String sourceFileMode;
	private String decompressionMethod;
	private String decryptionMethod;
	private int decryptionKeyID;

	private List<FileListDataModel> fileList;

	private boolean fileArchive;
	private String dateFormat;

	private String minutes;
	private String hours;
	private int days;
	private int months;
	private boolean scheduledIsActive;

	private String mailFrom;
	private String mailSubject;
	private List<MailingListDataModel> mailingList;

	@Override
	public String toString() {
		return "TaskConfigDataModel [id=" + id + ", subjectName=" + subjectName + ", subjectAddress=" + subjectAddress
				+ ", subjectLogin=" + subjectLogin + ", subjectPassword=" + subjectPassword + ", subjectDirectory="
				+ subjectDirectory + ", subjectExchangeProtocol=" + subjectExchangeProtocol + ", subjectPrivateKeyID="
				+ subjectEncryptionKeyID + ", subjectMode=" + subjectMode + ", subjectLoginForm=" + subjectLoginForm
				+ ", subjectLogoutForm=" + subjectLogoutForm + ", subjectPostOptions=" + subjectPostOptions
				+ ", subjectResponseString=" + subjectResponseString + ", servers=" + serversDataModel
				+ ", sourceFileMode=" + sourceFileMode + ", decompressionMethod=" + decompressionMethod
				+ ", decryptionMethod=" + decryptionMethod + ", decryptionKeyID=" + decryptionKeyID + ", fileList="
				+ fileList + ", fileArchive=" + fileArchive + ", dateFormat=" + dateFormat + ", minutes=" + minutes
				+ ", hours=" + hours + ", days=" + days + ", months=" + months + ", scheduledIsActive="
				+ scheduledIsActive + ", mailFrom=" + mailFrom + ", mailSubject=" + mailSubject + ", mailingList="
				+ mailingList + "]";
	}

	@Override
	public boolean equals(Object anotherObj) {
		if (anotherObj instanceof TaskConfigDataModel) {
			return id == (((TaskConfigDataModel) anotherObj).id);
		}
		return false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getSubjectAddress() {
		return subjectAddress;
	}

	public void setSubjectAddress(String subjectAddress) {
		this.subjectAddress = subjectAddress;
	}

	public String getSubjectLogin() {
		return subjectLogin;
	}

	public void setSubjectLogin(String subjectLogin) {
		this.subjectLogin = subjectLogin;
	}

	public String getSubjectPassword() {
		return subjectPassword;
	}

	public void setSubjectPassword(String subjectPassword) {
		this.subjectPassword = subjectPassword;
	}

	public String getSubjectDirectory() {
		return subjectDirectory;
	}

	public void setSubjectDirectory(String subjectDirectory) {
		this.subjectDirectory = subjectDirectory;
	}

	public String getSubjectExchangeProtocol() {
		return subjectExchangeProtocol;
	}

	public void setSubjectExchangeProtocol(String subjectExchangeProtocol) {
		this.subjectExchangeProtocol = subjectExchangeProtocol;
	}

	public int getSubjectEncryptionKeyID() {
		return subjectEncryptionKeyID;
	}

	public void setSubjectEncryptionKeyID(int subjectEncryptionKeyID) {
		this.subjectEncryptionKeyID = subjectEncryptionKeyID;
	}

	public String getSubjectMode() {
		return subjectMode;
	}

	public void setSubjectMode(String subjectMode) {
		this.subjectMode = subjectMode;
	}

	public String getSubjectLoginForm() {
		return subjectLoginForm;
	}

	public void setSubjectLoginForm(String subjectLoginForm) {
		this.subjectLoginForm = subjectLoginForm;
	}

	public String getSubjectLogoutForm() {
		return subjectLogoutForm;
	}

	public void setSubjectLogoutForm(String subjectLogoutForm) {
		this.subjectLogoutForm = subjectLogoutForm;
	}

	public String getSubjectPostOptions() {
		return subjectPostOptions;
	}

	public void setSubjectPostOptions(String subjectPostOptions) {
		this.subjectPostOptions = subjectPostOptions;
	}

	public String getSubjectResponseString() {
		return subjectResponseString;
	}

	public void setSubjectResponseString(String subjectResponseString) {
		this.subjectResponseString = subjectResponseString;
	}

	public List<ServerDataModel> getServers() {
		return serversDataModel;
	}

	public void setServers(List<ServerDataModel> serversDataModel) {
		this.serversDataModel = serversDataModel;
	}

	public String getSourceFileMode() {
		return sourceFileMode;
	}

	public void setSourceFileMode(String sourceFileMode) {
		this.sourceFileMode = sourceFileMode;
	}

	public String getDecompressionMethod() {
		return decompressionMethod;
	}

	public void setDecompressionMethod(String decompressionMethod) {
		this.decompressionMethod = decompressionMethod;
	}

	public String getDecryptionMethod() {
		return decryptionMethod;
	}

	public void setDecryptionMethod(String decryptionMethod) {
		this.decryptionMethod = decryptionMethod;
	}

	public int getDecryptionKeyID() {
		return decryptionKeyID;
	}

	public void setDecryptionKeyID(int decryptionKeyID) {
		this.decryptionKeyID = decryptionKeyID;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public List<FileListDataModel> getFileList() {
		return fileList;
	}

	public void setFileList(List<FileListDataModel> fileList) {
		this.fileList = fileList;
	}

	public boolean isFileArchive() {
		return fileArchive;
	}

	public void setFileArchive(boolean fileArchive) {
		this.fileArchive = fileArchive;
	}

	public String getMinutes() {
		return minutes;
	}

	public void setMinutes(String minutes) {
		this.minutes = minutes;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public int getMonths() {
		return months;
	}

	public void setMonths(int months) {
		this.months = months;
	}

	public boolean isScheduledIsActive() {
		return scheduledIsActive;
	}

	public void setScheduledIsActive(boolean scheduledIsActive) {
		this.scheduledIsActive = scheduledIsActive;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	public List<MailingListDataModel> getMailingList() {
		return mailingList;
	}

	public void setMailingList(List<MailingListDataModel> mailingList) {
		this.mailingList = mailingList;
	}
}
