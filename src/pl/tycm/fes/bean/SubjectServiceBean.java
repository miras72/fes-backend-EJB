package pl.tycm.fes.bean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;

import pl.tycm.fes.ProtocolFTP;
import pl.tycm.fes.ProtocolSFTP;
import pl.tycm.fes.ProtocolSSL;
import pl.tycm.fes.dao.DecryptionKeyDAO;
import pl.tycm.fes.dao.DecryptionKeyDAOImpl;
import pl.tycm.fes.dao.EncryptionKeyDAO;
import pl.tycm.fes.dao.EncryptionKeyDAOImpl;
import pl.tycm.fes.dao.EventDAO;
import pl.tycm.fes.dao.EventDAOImpl;
import pl.tycm.fes.dao.ServerConfigDAO;
import pl.tycm.fes.dao.ServerConfigDAOImpl;
import pl.tycm.fes.model.DecryptionKeyDataModel;
import pl.tycm.fes.model.EventDataModel;
import pl.tycm.fes.model.FileListDataModel;
import pl.tycm.fes.model.PrivateKeyDataModel;
import pl.tycm.fes.model.ServerConfigDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskConfigDataModel;
import pl.tycm.fes.util.MTTools;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SubjectServiceBean {

	@EJB
	GzipFileBean gzipFileBean;

	@EJB
	ZipFileBean zipFileBean;

	@EJB
	PGPFileBean pgpFileBean;

	EventDAO eventDAO = new EventDAOImpl();
	EventDataModel eventModel = new EventDataModel();

	StatusMessage statusMessage = new StatusMessage();

	ServerConfigDAO serverConfigDAO = new ServerConfigDAOImpl();
	// ServerConfigDataModel serverConfigModel =
	// serverConfigDAO.getServerConfig(statusMessage);
	// String workingDirectory = serverConfigModel.getWorkDirectory();

	private final Logger logger = Logger.getLogger(this.getClass());

	public List<String> subjectReceiveFiles(TaskConfigDataModel taskConfigModel, int fileExchangeStatusID,
			ReportBean reportBean) {
		
		String remotePassword = "";
		ServerConfigDataModel serverConfigModel = serverConfigDAO.getServerConfig(statusMessage);
		String workingDirectory = serverConfigModel.getWorkDirectory();

		String serverAddress = taskConfigModel.getSubjectAddress();
		String remoteLogin = taskConfigModel.getSubjectLogin();
		String remoteDirectory = taskConfigModel.getSubjectDirectory();
		if(taskConfigModel.getSubjectPassword() !=null) 
			remotePassword = taskConfigModel.getSubjectPassword();

		List<String> fileList = new ArrayList<>();
		for (FileListDataModel fileListModel : taskConfigModel.getFileList()) {
			fileList.add(fileListModel.getFileName());
		}

		switch (taskConfigModel.getSubjectExchangeProtocol()) {
		case "sftp":
			String sourceFileMode = taskConfigModel.getSourceFileMode();

			if (taskConfigModel.getSubjectEncryptionKeyID() != 0) {
				EncryptionKeyDAO encryptionKeyDAO = new EncryptionKeyDAOImpl();
				PrivateKeyDataModel privateKeyDataModel = encryptionKeyDAO
						.getPrivateKey(taskConfigModel.getSubjectEncryptionKeyID(), statusMessage);
				String privateKeyName = privateKeyDataModel.getPrivateKeyName();
				byte[] privateKeyBinaryFile = privateKeyDataModel.getPrivateKeyBinaryFile();
				ProtocolSFTP sftp = new ProtocolSFTP(serverAddress, remoteLogin, remotePassword, remoteDirectory,
						privateKeyName, privateKeyBinaryFile, workingDirectory, fileList, sourceFileMode,
						fileExchangeStatusID, reportBean);
				return sftp.receiveFiles();
			} else {
				ProtocolSFTP sftp = new ProtocolSFTP(serverAddress, remoteLogin, remotePassword, remoteDirectory,
						workingDirectory, fileList, sourceFileMode, fileExchangeStatusID, reportBean);
				return sftp.receiveFiles();
			}

		case "ssl":
			String loginForm = taskConfigModel.getSubjectLoginForm();
			String logoutForm = taskConfigModel.getSubjectLogoutForm();
			String responseString = taskConfigModel.getSubjectResponseString();
			if (taskConfigModel.getSubjectPostOptions() != null) {
				String[] postList = taskConfigModel.getSubjectPostOptions().split(",");
				ProtocolSSL ssl = new ProtocolSSL(serverAddress, remoteLogin, remotePassword, loginForm, logoutForm,
						remoteDirectory, postList, responseString, workingDirectory, fileList, fileExchangeStatusID,
						reportBean);
				return ssl.receiveFiles();
			} else {
				ProtocolSSL ssl = new ProtocolSSL(serverAddress, remoteLogin, remotePassword, loginForm, logoutForm,
						remoteDirectory, responseString, workingDirectory, fileList, fileExchangeStatusID, reportBean);
				return ssl.receiveFiles();
			}
		case "ftp":
			ProtocolFTP ftp = new ProtocolFTP(serverAddress, remoteLogin, remotePassword, remoteDirectory,
					workingDirectory, fileList, fileExchangeStatusID, reportBean);
			return ftp.receiveFiles();
		default:
			logger.fatal("Nieprawidłowy protokół wymiany plików");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Nieprawidłowy protokół wymiany plików");
			eventDAO.createEvent(eventModel, statusMessage);
			return null;
		}
	}

	public boolean subjectSendFiles(List<String> fileList, TaskConfigDataModel taskConfigModel,
			int fileExchangeStatusID, ReportBean reportBean) {

		ServerConfigDataModel serverConfigModel = serverConfigDAO.getServerConfig(statusMessage);
		String workingDirectory = serverConfigModel.getWorkDirectory();

		String serverAddress = taskConfigModel.getSubjectAddress();
		String remoteLogin = taskConfigModel.getSubjectLogin();
		String remoteDirectory = taskConfigModel.getSubjectDirectory();
		String remotePassword = taskConfigModel.getSubjectPassword();

		switch (taskConfigModel.getSubjectExchangeProtocol()) {
		case "sftp":
			String sourceFileMode = taskConfigModel.getSourceFileMode();
			if (taskConfigModel.getSubjectEncryptionKeyID() != 0) {
				EncryptionKeyDAO encryptionKeyDAO = new EncryptionKeyDAOImpl();
				PrivateKeyDataModel privateKeyDataModel = encryptionKeyDAO
						.getPrivateKey(taskConfigModel.getSubjectEncryptionKeyID(), statusMessage);
				String privateKeyName = privateKeyDataModel.getPrivateKeyName();
				byte[] privateKeyBinaryFile = privateKeyDataModel.getPrivateKeyBinaryFile();		
				ProtocolSFTP sftp = new ProtocolSFTP(serverAddress, remoteLogin, remotePassword, remoteDirectory,
						privateKeyName, privateKeyBinaryFile, workingDirectory, fileList, sourceFileMode,
						fileExchangeStatusID, reportBean);
				return sftp.sendFiles();
			} else {
				ProtocolSFTP sftp = new ProtocolSFTP(serverAddress, remoteLogin, remotePassword, remoteDirectory,
						workingDirectory, fileList, sourceFileMode, fileExchangeStatusID, reportBean);
				return sftp.sendFiles();
			}
			/*
			 * case "ftp": ProtokolFTP ftp = new ProtokolFTP(serverAddress, remoteLogin,
			 * remotePassword, remoteDirectory, workingDirectory, fileList); boolean
			 * statusFTP = ftp.sendFiles(); return statusFTP;
			 */
		case "ssl":
			String loginForm = taskConfigModel.getSubjectLoginForm();
			String logoutForm = taskConfigModel.getSubjectLogoutForm();
			String responseString = taskConfigModel.getSubjectResponseString();
			if (taskConfigModel.getSubjectPostOptions() != null) {
				String[] postList = taskConfigModel.getSubjectPostOptions().split(",");
				ProtocolSSL ssl = new ProtocolSSL(serverAddress, remoteLogin, remotePassword, loginForm, logoutForm,
						remoteDirectory, postList, responseString, workingDirectory, fileList, fileExchangeStatusID,
						reportBean);
				return ssl.sendFiles();
			} else {
				ProtocolSSL ssl = new ProtocolSSL(serverAddress, remoteLogin, remotePassword, loginForm, logoutForm,
						remoteDirectory, responseString, workingDirectory, fileList, fileExchangeStatusID, reportBean);
				return ssl.sendFiles();
			}
		default:
			logger.fatal("Nieprawidłowy protokół wymiany plików");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Nieprawidłowy protokół wymiany plików");
			eventDAO.createEvent(eventModel, statusMessage);
			return false;
		}
	}

	public List<String> decompressFiles(List<String> fileList, TaskConfigDataModel taskConfigModel,
			int fileExchangeStatusID, ReportBean reportBean) {

		List<String> receiveFileList = new ArrayList<>();

		ServerConfigDataModel serverConfigModel = serverConfigDAO.getServerConfig(statusMessage);
		String workingDirectory = serverConfigModel.getWorkDirectory();

		switch (taskConfigModel.getDecompressionMethod()) {
		case "gzip":
			receiveFileList = gzipFileBean.decompressGzipFile(workingDirectory, fileList, fileExchangeStatusID,
					reportBean);
			return receiveFileList;
		case "zip":
			receiveFileList = zipFileBean.decompressZipFile(workingDirectory, fileList, fileExchangeStatusID,
					reportBean);
			return receiveFileList;
		default:
			logger.fatal("Nieprawidłowa metoda dekompresji");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Nieprawidłowa metoda dekompresji");
			eventDAO.createEvent(eventModel, statusMessage);
			return null;
		}
	}

	public List<String> decryptFiles(List<String> fileList, TaskConfigDataModel taskConfigModel,
			int fileExchangeStatusID, ReportBean reportBean) {

		List<String> receiveFileList = new ArrayList<>();

		ServerConfigDataModel serverConfigModel = serverConfigDAO.getServerConfig(statusMessage);
		String workingDirectory = serverConfigModel.getWorkDirectory();

		switch (taskConfigModel.getDecryptionMethod()) {
		case "pgp":

			DecryptionKeyDAO decryptionKeyDAO = new DecryptionKeyDAOImpl();
			DecryptionKeyDataModel decryptionKeyModel = decryptionKeyDAO
					.getDecryptionKey(taskConfigModel.getDecryptionKeyID(), statusMessage);
			byte[] decryptionKey = decryptionKeyModel.getDecryptionKeyBinaryFile();
			if (decryptionKey == null)
				return null;
			receiveFileList = pgpFileBean.decryptPGPFile(workingDirectory, decryptionKey, fileList,
					fileExchangeStatusID, reportBean);
			return receiveFileList;
		default:
			logger.fatal("Nieprawidłowa metoda dekrypcji");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Nieprawidłowa metoda dekrypcji");
			eventDAO.createEvent(eventModel, statusMessage);
			return null;
		}
	}
}
