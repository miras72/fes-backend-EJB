package pl.tycm.fes.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import pl.tycm.fes.dao.EventDAO;
import pl.tycm.fes.dao.EventDAOImpl;
import pl.tycm.fes.model.EventDataModel;
import pl.tycm.fes.model.ServerDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.util.MTTools;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ServerBean {

	private EventDAO eventDAO = new EventDAOImpl();
	private EventDataModel eventModel = new EventDataModel();
	private StatusMessage statusMessage = new StatusMessage();

	private final Logger logger = Logger.getLogger(this.getClass());

	public boolean sendFiles(List<ServerDataModel> serversDataModel, String workingDirectory, List<String> receiveFileList, int fileExchangeStatusID, ReportBean reportBean) {

		boolean isOK = true;
		
		if (workingDirectory.endsWith("/"))
			workingDirectory = workingDirectory.substring(0, workingDirectory.length() - 1);
		
		File directory = new File(workingDirectory);
		for (ServerDataModel serverDataModel : serversDataModel) {
			logger.info("Kopiuje pliki na serwer (" + serverDataModel.getServerAddress() + "):");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Kopiuje pliki na serwer ("
					+ serverDataModel.getServerAddress() + ") ...");
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean
					.addReport("- Lista plików skopiowanych na serwer (" + serverDataModel.getServerAddress() + "):");

			for (String fileName : receiveFileList) {
				try {
					FileInputStream in = new FileInputStream(directory + File.separator + fileName);

					String domainName = serverDataModel.getServerLogin().substring(0,
							serverDataModel.getServerLogin().indexOf("/"));
					String userName = serverDataModel.getServerLogin()
							.substring(serverDataModel.getServerLogin().lastIndexOf("/") + 1);

					NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domainName, userName,
							serverDataModel.getServerPassword());

					SmbFile newFile = new SmbFile(
							"smb://" + serverDataModel.getServerAddress() + serverDataModel.getServerDirectory() + fileName,
							auth);
					SmbFileOutputStream smbfos = new SmbFileOutputStream(newFile);
					logger.info("Kopiuje plik: " + fileName);
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Kopiuje plik: " + fileName);
					eventDAO.createEvent(eventModel, statusMessage);

					byte[] bytesArray = new byte[16 * 4096];
					int bytesRead = -1;
					int progressSign = 0;
					while ((bytesRead = in.read(bytesArray)) != -1) {
						smbfos.write(bytesArray, 0, bytesRead);
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
					in.close();
					smbfos.close();
					
					logger.info("Plik skopiowany.");
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik skopiowany.");
					eventDAO.createEvent(eventModel, statusMessage);

					reportBean.addReport(fileName);
					isOK = true;
					
				} catch (SmbException ex) {
					switch (ex.getMessage()) {
					case "0xC000007F":
						logger.fatal("Brak miejsca na dysku kod błądu: " + ex.getMessage());
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Brak miejsca na dysku kod błądu: "
								+ ex.getMessage());
						eventDAO.createEvent(eventModel, statusMessage);

						reportBean
								.addReport("-> Błąd: Brak miejsca na dysku. Nie powiodła się próba skopiowania pliku: "
										+ fileName);
						isOK = false;
						break;
					default:
						logger.fatal(ex.getMessage());
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(MTTools.getLogDate() + "FATAL "
								+ "Nie powiodła się próba skopiowania pliku: " + fileName);
						eventDAO.createEvent(eventModel, statusMessage);

						reportBean.addReport("-> Błąd: Nie powiodła się próba skopiowania pliku: " + fileName);
						isOK = false;
						break;
					}
				} catch (IOException ex) {
					logger.fatal(ex.getMessage());
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(
							MTTools.getLogDate() + "FATAL " + "Nie powiodła się próba skopiowania pliku: " + fileName);
					eventDAO.createEvent(eventModel, statusMessage);

					reportBean.addReport("-> Błąd: Nie powiodła się próba skopiowania pliku: " + fileName);
					isOK = false;
				}
			}
		}
		return isOK;
	}

	public List<String> receiveFiles(List<ServerDataModel> serversDataModel, String workingDirectory, List<String> fileList, int fileExchangeStatusID, ReportBean reportBean) {

		if (workingDirectory.endsWith("/"))
			workingDirectory = workingDirectory.substring(0, workingDirectory.length() - 1);
		
		File directory = new File(workingDirectory);
		List<String> receiveFileList = new ArrayList<>();

		boolean status = false;

		for (ServerDataModel serverDataModel : serversDataModel) {
			logger.info("Pobieram pliki z serwera (" + serverDataModel.getServerAddress() + "):");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Pobieram pliki z serwera ("
					+ serverDataModel.getServerAddress() + ") ...");
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean.addReport("- Lista plików pobranych z serwera (" + serverDataModel.getServerAddress() + "):");

			try {

				String domainName = serverDataModel.getServerLogin().substring(0,
						serverDataModel.getServerLogin().indexOf("/"));
				String userName = serverDataModel.getServerLogin()
						.substring(serverDataModel.getServerLogin().lastIndexOf("/") + 1);

				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domainName, userName,
						serverDataModel.getServerPassword());

				SmbFile smbFile = new SmbFile(
						"smb://" + serverDataModel.getServerAddress() + serverDataModel.getServerDirectory(), auth);

				for (String pattern : fileList) {
					try {
						SmbFile[] smbListFiles = smbFile.listFiles(pattern);
						for (SmbFile in : smbListFiles) {
							if (!in.isDirectory()) {
								String fileName = in.getName();
								try {
									SmbFileInputStream smbfis = new SmbFileInputStream(in);
									FileOutputStream out = new FileOutputStream(directory + File.separator + fileName);
									logger.info("Pobieram plik: " + fileName);
									eventModel.setFileExchangeStatusID(fileExchangeStatusID);
									eventModel.setEventText(
											MTTools.getLogDate() + "INFO  " + "Pobieram plik: " + fileName);
									eventDAO.createEvent(eventModel, statusMessage);

									byte[] bytesArray = new byte[16 * 4096];
									int bytesRead = -1;
									int progressSign = 0;
									while ((bytesRead = smbfis.read(bytesArray)) != -1) {
										out.write(bytesArray, 0, bytesRead);
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
									out.close();
									smbfis.close();
									in.delete();
									// System.out.println("\rOK");
									logger.info("Plik pobrany.");
									eventModel.setFileExchangeStatusID(fileExchangeStatusID);
									eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik pobrany.");
									eventDAO.createEvent(eventModel, statusMessage);

									receiveFileList.add(fileName);

									reportBean.addReport(fileName);
									status = true;

								} catch (IOException ex) {
									logger.fatal(ex.getMessage());
									eventModel.setFileExchangeStatusID(fileExchangeStatusID);
									eventModel.setEventText(MTTools.getLogDate() + "FATAL "
											+ "Nie powiodła się próba pobrania pliku: " + fileName);
									eventDAO.createEvent(eventModel, statusMessage);

									reportBean.addReport("-> Błąd: Nie powiodła się próba pobrania pliku: " + fileName);
								}
							}
						}
					} catch (SmbException ex) {
						logger.error("Błąd pobierania plików (" + pattern + "): " + ex.getMessage());
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Błąd pobierania plików (" + pattern
								+ "): " + ex.getMessage());
						eventDAO.createEvent(eventModel, statusMessage);
					}
				}
			} catch (MalformedURLException ex) {
				logger.fatal(ex.getMessage());
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Nie powiodła się próba pobrania plików");
				eventDAO.createEvent(eventModel, statusMessage);

				reportBean.addReport("-> Błąd: Nie powiodła się próba pobrania plików");
			}
		}
		if (!status) {
			logger.error("Brak plików do pobrania.");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Brak plików do pobrania.");
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean.addReport("-> Błąd: Brak plików do pobrania");
			return null;
		}
		return receiveFileList;
	}
}
