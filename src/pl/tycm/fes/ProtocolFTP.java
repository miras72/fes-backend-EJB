package pl.tycm.fes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.log4j.Logger;

import pl.tycm.fes.bean.ReportBean;
import pl.tycm.fes.dao.EventDAO;
import pl.tycm.fes.dao.EventDAOImpl;
import pl.tycm.fes.model.EventDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.util.MTTools;

public class ProtocolFTP {

	private String serverAddress;
	private String remoteLogin;
	private String remotePassword;
	private String remoteDirectory;
	private String workingDirectory;
	private List<String> fileList;
	private int fileExchangeStatusID;
	private ReportBean reportBean;

	private EventDAO eventDAO = new EventDAOImpl();
	private EventDataModel eventModel = new EventDataModel();
	private StatusMessage statusMessage = new StatusMessage();

	int port = 21;

	private final Logger logger = Logger.getLogger(this.getClass());

	public ProtocolFTP(String serverAddress, String remoteLogin, String remotePassword, String remoteDirectory,
			String workingDirectory, List<String> fileList, int fileExchangeStatusID, ReportBean reportBean) {
		this.serverAddress = serverAddress;
		this.remoteLogin = remoteLogin;
		this.remotePassword = remotePassword;
		this.remoteDirectory = remoteDirectory.replaceFirst("^/", ""); // usuwa pierwszy /
		this.workingDirectory = workingDirectory;
		this.fileList = fileList;
		this.fileExchangeStatusID = fileExchangeStatusID;
		this.reportBean = reportBean;
	}

	public List<String> receiveFiles() {
		FTPClient ftpClient = new FTPClient();
		List<String> receiveFileList = new ArrayList<>();

		try {
			logger.info("Pobieram pliki z serwera Podmiotu (" + serverAddress + "):");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(
					MTTools.getLogDate() + "INFO  " + "Pobieram pliki z serwera Podmiotu (" + serverAddress + "):");
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean.addReport("- Lista plików pobranych z serwera Podmiotu (" + serverAddress + "):");

			ftpClient.connect(serverAddress, port);
			ftpClient.login(remoteLogin, remotePassword);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFileFilter filter = new FTPFileFilter() {
				public boolean accept(FTPFile ftpFile) {
					boolean resultMatch = false;
					for (String fileName : fileList) {
						/**
						 * Zwraca tylko pliki ktore pasuja do wzorca w zmiennej fileList
						 */
						FileSystem fileSystem = FileSystems.getDefault();
						PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + fileName); // Ustawiamy glob
																									// pattern zamiast
																									// regular
																									// expression
						Path path = Paths.get(ftpFile.getName());
						resultMatch = pathMatcher.matches(path);
						if (resultMatch) {
							break;
						}
					}
					return ftpFile.isFile() && resultMatch;
				}
			};

			FTPFile[] result = ftpClient.listFiles(remoteDirectory, filter);

			if (result != null && result.length > 0) {
				for (FTPFile fileName : result) {
					logger.info("Pobieram plik: " + fileName.getName() + "...");
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(
							MTTools.getLogDate() + "INFO  " + "Pobieram plik: " + fileName.getName() + "...");
					eventDAO.createEvent(eventModel, statusMessage);

					String remoteFile = fileName.getName();
					File downloadFile = new File(workingDirectory + File.separator + fileName.getName());
					OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
					InputStream inputStream = ftpClient.retrieveFileStream(remoteFile);

					byte[] bytesArray = new byte[16 * 4096];
					int bytesRead = -1;
					int progressSign = 0;
					while ((bytesRead = inputStream.read(bytesArray)) != -1) {
						outputStream.write(bytesArray, 0, bytesRead);
						switch (progressSign) {
						case 0:
							System.out.print("\r|");
							progressSign++;
							break;
						case 1:
							System.out.print("\r/");
							progressSign++;
							break;
						case 2:
							System.out.print("\r-");
							progressSign++;
							break;
						case 3:
							System.out.print("\r\\");
							progressSign = 0;
							break;
						}
					}
					boolean success = ftpClient.completePendingCommand();
					if (success) {
						logger.info("Plik pobrany");
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik pobrany.");
						eventDAO.createEvent(eventModel, statusMessage);

						receiveFileList.add(remoteFile);

						reportBean.addReport(remoteFile);
					}
					outputStream.close();
					inputStream.close();
				}
			} else {
				logger.error("Brak plików do pobrania");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Brak plików do pobrania");
				eventDAO.createEvent(eventModel, statusMessage);

				reportBean.addReport("-> Brak plików do pobrania");
				return null;
			}
		} catch (IOException ex) {
			logger.fatal("Błąd: " + ex.getMessage());
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Błąd: " + ex.getMessage());
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean.addReport("-> Błąd: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				logger.fatal("Błąd: " + ex.getMessage());
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Błąd: " + ex.getMessage());
				eventDAO.createEvent(eventModel, statusMessage);

				ex.printStackTrace();
				return null;
			}
		}
		return receiveFileList;
	}
}
