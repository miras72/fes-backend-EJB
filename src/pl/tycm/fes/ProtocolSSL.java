package pl.tycm.fes;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;

import pl.tycm.fes.bean.ReportBean;
import pl.tycm.fes.dao.EventDAO;
import pl.tycm.fes.dao.EventDAOImpl;
import pl.tycm.fes.model.EventDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.util.MTTools;

public class ProtocolSSL {

	private String serverAddress;
	private String remoteLogin;
	private String remotePassword;
	private String loginForm;
	private String logoutForm;
	private String remoteDirectory;
	private String[] postList;
	private String responseString;
	private String workingDirectory;
	private List<String> fileList;
	private int fileExchangeStatusID;
	private ReportBean reportBean;

	private static final String ERROR_DIRECTORY = "html";

	private List<String> cookies;
	private HttpsURLConnection conn;

	private EventDAO eventDAO = new EventDAOImpl();
	private EventDataModel eventModel = new EventDataModel();
	private StatusMessage statusMessage = new StatusMessage();
	
	private final Logger logger = Logger.getLogger(this.getClass());

	public ProtocolSSL(String serverAddress, String remoteLogin, String remotePassword, String loginForm,
			String logoutForm, String remoteDirectory, String responseString,
			String workingDirectory, List<String> fileList, int fileExchangeStatusID, ReportBean reportBean) {
		this.serverAddress = serverAddress;
		this.remoteLogin = remoteLogin;
		this.remotePassword = remotePassword;
		this.loginForm = loginForm;
		this.logoutForm = logoutForm;
		this.remoteDirectory = remoteDirectory;
		this.responseString = responseString;
		this.workingDirectory = workingDirectory;
		this.fileList = fileList;
		this.fileExchangeStatusID = fileExchangeStatusID;
		this.reportBean = reportBean;
	}
	
	public ProtocolSSL(String serverAddress, String remoteLogin, String remotePassword, String loginForm,
			String logoutForm, String remoteDirectory, String[] postList, String responseString,
			String workingDirectory, List<String> fileList, int fileExchangeStatusID, ReportBean reportBean) {
		this.serverAddress = serverAddress;
		this.remoteLogin = remoteLogin;
		this.remotePassword = remotePassword;
		this.loginForm = loginForm;
		this.logoutForm = logoutForm;
		this.remoteDirectory = remoteDirectory;
		this.postList = postList;
		this.responseString = responseString;
		this.workingDirectory = workingDirectory;
		this.fileList = fileList;
		this.fileExchangeStatusID = fileExchangeStatusID;
		this.reportBean = reportBean;
	}

	/**
	 * @return the serverAddress
	 */
	public String getServerAddress() {
		return serverAddress;
	}

	/**
	 * @param serverAddress
	 *            the serverAddress to set
	 */
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	/**
	 * @return the remoteLogin
	 */
	public String getRemoteLogin() {
		return remoteLogin;
	}

	/**
	 * @param remoteLogin
	 *            the remoteLogin to set
	 */
	public void setRemoteLogin(String remoteLogin) {
		this.remoteLogin = remoteLogin;
	}

	/**
	 * @return the remotePassword
	 */
	public String getRemotePassword() {
		return remotePassword;
	}

	/**
	 * @param remotePassword
	 *            the remotePassword to set
	 */
	public void setRemotePassword(String remotePassword) {
		this.remotePassword = remotePassword;
	}

	/**
	 * @return the loginForm
	 */
	public String getLoginForm() {
		return loginForm;
	}

	/**
	 * @param loginForm
	 *            the loginForm to set
	 */
	public void setLoginForm(String loginForm) {
		this.loginForm = loginForm;
	}

	/**
	 * @return the logoutForm
	 */
	public String getLogoutForm() {
		return logoutForm;
	}

	/**
	 * @param logoutForm
	 *            the logoutForm to set
	 */
	public void setLogoutForm(String logoutForm) {
		this.logoutForm = logoutForm;
	}

	/**
	 * @return the remoteDirectory
	 */
	public String getRemoteDirectory() {
		return remoteDirectory;
	}

	/**
	 * @param remoteDirectory
	 *            the remoteDirectory to set
	 */
	public void setRemoteDirectory(String remoteDirectory) {
		this.remoteDirectory = remoteDirectory;
	}

	/**
	 * @return the workingDirectory
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	/**
	 * @param workingDirectory
	 *            the workingDirectory to set
	 */
	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/**
	 * @return the fileList
	 */
	public List<String> getFileList() {
		return fileList;
	}

	/**
	 * @param fileList
	 *            the fileList to set
	 */
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

	public void closeSSLConnection() {
		if (logoutForm != null) {
			try {
				URL obj = new URL(serverAddress + logoutForm);
				conn = (HttpsURLConnection) obj.openConnection();

				int responseCode = conn.getResponseCode();
				if (responseCode == 200) {
					logger.info("Wylogowanie z serwera poprawne. Kod odpowiedzi serwera: " + responseCode);
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Wylogowanie z serwera poprawne. Kod odpowiedzi serwera: " + responseCode);
					eventDAO.createEvent(eventModel, statusMessage);
					
				} else {
					logger.error("Błąd wylogowania z serwera. Kod odpowiedzi serwera https: " + responseCode);
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Błąd wylogowania z serwera. Kod odpowiedzi serwera https: " + responseCode);
					eventDAO.createEvent(eventModel, statusMessage);
				}
			} catch (IOException ex) {
				logger.fatal("Błąd: " + ex.getMessage());
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Błąd: " + ex.getMessage());
				eventDAO.createEvent(eventModel, statusMessage);
				ex.printStackTrace();
			}
		}
	}

	public boolean createSSLConnection() {
		String url = serverAddress;
		// make sure cookies is turn on
		CookieHandler.setDefault(new CookieManager());

		try {
			logger.info("Inicjalizacja połączenia....");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Inicjalizacja połączenia....");
			eventDAO.createEvent(eventModel, statusMessage);
			
			//if (loginForm != null || !loginForm.isEmpty()) {
			if (loginForm != null)
				url = MTTools.getConvertUrlForm(serverAddress, remoteLogin, remotePassword, loginForm);
			URL obj = new URL(url);
			conn = (HttpsURLConnection) obj.openConnection();

			// default is GET
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);

			//if (loginForm == null || loginForm.isEmpty()) {
			if (loginForm == null) {
				logger.info("Autoryzacja BasicAuth...");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Autoryzacja BasicAuth...");
				eventDAO.createEvent(eventModel, statusMessage);
				
				String userPass = remoteLogin + ":" + remotePassword;
				String basicAuth = "Basic " + new String(Base64.encodeBase64(userPass.getBytes()));
				conn.setRequestProperty("Authorization", basicAuth);
			}
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				logger.info("Autoryzacja na serwerzez https poprawna. Kod odpowiedzi serwera: " + responseCode);
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Autoryzacja na serwerzez https poprawna. Kod odpowiedzi serwera: " + responseCode);
				eventDAO.createEvent(eventModel, statusMessage);
				
			} else {
				logger.fatal("Błąd autoryzacji. Kod odpowiedzi serwera https: " + responseCode);
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Błąd autoryzacji. Kod odpowiedzi serwera https: " + responseCode);
				eventDAO.createEvent(eventModel, statusMessage);
				
				reportBean.addReport("-> Błąd: Nie powiodła się autoryzacja na serwerze");
				return false;
			}
			return true;
		} catch (ConnectException ex) {
			logger.fatal("Błąd połączenia: " + ex.getMessage());
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Błąd połączenia: " + ex.getMessage());
			eventDAO.createEvent(eventModel, statusMessage);
			
			reportBean.addReport("-> Błąd: Nie moża połączyć się z serwerem");
			return false;
		} catch (IOException ex) {
			logger.fatal("Błąd: " + ex.getMessage());
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Błąd: " + ex.getMessage());
			eventDAO.createEvent(eventModel, statusMessage);
			
			ex.printStackTrace();
			return false;
		}
	}

	public List<String> receiveFiles() {

		boolean statusConnection;
		boolean status = false;
		List<String> receiveFileList = new ArrayList<>();

		statusConnection = this.createSSLConnection();
		if (!statusConnection)
			return null;

		try {
			logger.info("Pobieram pliki z serwera Podmiotu (" + serverAddress + "):");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Pobieram pliki z serwera Podmiotu (" + serverAddress + "):");
			eventDAO.createEvent(eventModel, statusMessage);
			
			reportBean.addReport("- Lista plików pobranych z serwera Podmiotu (" + serverAddress + "):");
			for (String fileName : fileList) {
				

			//for (int i = 0; i < fileList.length; i++) {
				URL obj = new URL(remoteDirectory + fileName);
				conn = (HttpsURLConnection) obj.openConnection();
				conn.setUseCaches(false);
				conn.setRequestMethod("GET");
				logger.info("Pobieram plik: " + fileName + "...");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Pobieram plik: " + fileName + "...");
				eventDAO.createEvent(eventModel, statusMessage);
				
				int responseCode = conn.getResponseCode();

				if (responseCode == 200) {

					File downloadFile = new File(workingDirectory + File.separator + fileName);
					OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
					InputStream inputStream = (InputStream) conn.getInputStream();

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
					outputStream.close();
					inputStream.close();
					//System.out.println("\rOK");
					logger.info("Plik pobrany");
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik pobrany.");
					eventDAO.createEvent(eventModel, statusMessage);

					// Sprawdza czy pobrany plik jest w formacie html
					// jezeli jest to pobrany plik zawiera strone z bledem "brak pliku"
					BufferedReader br = null;
					boolean found = false;
					try {
						br = new BufferedReader(new InputStreamReader(
								new FileInputStream(workingDirectory + File.separator + fileName)));
						String line;
						while ((line = br.readLine()) != null) {
							if (line.contains("<!DOCTYPE")) {
								found = true;
								break;
							}
						}
					} finally {
						try {
							if (br != null)
								br.close();
						} catch (Exception e) {
							logger.error("Problem podaczas zamykania BufferReader " + e.toString());
							eventModel.setFileExchangeStatusID(fileExchangeStatusID);
							eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Problem podaczas zamykania BufferReader " + e.toString());
							eventDAO.createEvent(eventModel, statusMessage);
						}
					}

					if (!found) {
						receiveFileList.add(fileName);					
						
						reportBean.addReport(fileName);
						status = true;
					} else {
						File sourceFile = new File(workingDirectory + File.separator + fileName);
						
						if (!Files.isDirectory(Paths.get(workingDirectory + File.separator + ERROR_DIRECTORY))) {
							logger.info("Tworzę katalog na pliki html: " + ERROR_DIRECTORY);
							eventModel.setFileExchangeStatusID(fileExchangeStatusID);
							eventModel.setEventText(
									MTTools.getLogDate() + "INFO  " + "Tworzę katalog na pliki html: " + ERROR_DIRECTORY + "...");
							eventDAO.createEvent(eventModel, statusMessage);

							if (!new File(workingDirectory + File.separator + ERROR_DIRECTORY).mkdirs()) {
								logger.error("Nie można utworzyć katalogu na pliki html.");
								eventModel.setFileExchangeStatusID(fileExchangeStatusID);
								eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Nie można utworzyć katalogu na pliki html.");
								eventDAO.createEvent(eventModel, statusMessage);
							}

							logger.info("Katalog na pliki html utworzony.");
							eventModel.setFileExchangeStatusID(fileExchangeStatusID);
							eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Katalog na pliki html utworzony.");
							eventDAO.createEvent(eventModel, statusMessage);
						}
						File destinationFile = new File(workingDirectory + File.separator + ERROR_DIRECTORY
								+ File.separator + fileName + ".html");
						
						logger.info("Zmieniam nazwę pliku: " + fileName + " -> " + fileName + ".html");
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Zmieniam nazwę pliku: " + fileName + " -> " + fileName + ".html");
						eventDAO.createEvent(eventModel, statusMessage);
						if (!sourceFile.renameTo(destinationFile)) {
							logger.error("Błąd: Nie można zmienić nazwy pliku: " + fileName);
							eventModel.setFileExchangeStatusID(fileExchangeStatusID);
							eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Błąd: Nie można zmienić nazwy pliku: " + fileName);
							eventDAO.createEvent(eventModel, statusMessage);
						}
					}
				} else {
					logger.error("Problem z pobraniem pliku. Kod odpowiedzi serwera https: " + responseCode);
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Problem z pobraniem pliku. Kod odpowiedzi serwera https: " + responseCode);
					eventDAO.createEvent(eventModel, statusMessage);
					
					reportBean.addReport("-> Problem z pobraniem pliku: " + fileName);
				}
			}

		} catch (IOException ex) {
			logger.fatal("Błąd: " + ex.getMessage());
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Błąd: " + ex.getMessage());
			eventDAO.createEvent(eventModel, statusMessage);
			
			ex.printStackTrace();
			return null;
		} finally {
			this.closeSSLConnection();
		}
		if(!status) {
			logger.error("Brak plików do pobrania");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Brak plików do pobrania");
			eventDAO.createEvent(eventModel, statusMessage);
			
			reportBean.addReport("-> Brak plików do pobrania");
			return null;
		}
		return receiveFileList;
	}

	public boolean sendFiles() {

		boolean status;
		String lineEnd = "\r\n";
		String twoHyphens = "--";

		status = this.createSSLConnection();
		if (!status)
			return false;

		logger.info("Wysyłam pliki na serwer Podmiotu (" + serverAddress + "):");
		eventModel.setFileExchangeStatusID(fileExchangeStatusID);
		eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Wysyłam pliki na serwer Podmiotu (" + serverAddress + "):");
		eventDAO.createEvent(eventModel, statusMessage);
		
		reportBean.addReport("- Lista plików wysłanych na serwer Podmiotu (" + serverAddress + "):");

		//File directory = new File(workingDirectory);

		/*if (directory.list().length == 0)
			return false;*/

		for (String fileName : fileList) {
		//for (String fileName : directory.list()) {
			try {
				String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random
																				// value.
				URL obj = new URL(remoteDirectory + fileName);
				conn = (HttpsURLConnection) obj.openConnection();
				conn.setUseCaches(false);
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);

				conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
				logger.info("Przesyłam plik: " + fileName + "...");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Przesyłam plik: " + fileName + "...");
				eventDAO.createEvent(eventModel, statusMessage);

				File uploadFile = new File(workingDirectory + File.separator + fileName);
				FileInputStream inputStream = new FileInputStream(uploadFile);
				DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());

				// Start Header
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				for (String postOptions : postList) {
					if (!postOptions.contains("@")) {
						outputStream.writeBytes("Content-Disposition: form-data; name=\"" + postOptions.split("=", 2)[0]
								+ "\"" + lineEnd);
						outputStream.writeBytes(lineEnd);
						outputStream.writeBytes(postOptions.split("=", 2)[1]);
						outputStream.writeBytes(lineEnd);
						outputStream.writeBytes(twoHyphens + boundary + lineEnd);
					}
				}
				for (String postOptions : postList) {
					if (postOptions.contains("@")) {
						outputStream.writeBytes("Content-Disposition: form-data; name=\"" + postOptions.split("=", 2)[0]
								+ "\";filename=\"" + fileName + "\"" + lineEnd);
						outputStream.writeBytes(lineEnd);
					}
				}
				// End Header

				// create a buffer of maximum size
				int bytesAvailable = inputStream.available();

				int maxBufferSize = 1024;
				int bufferSize = Math.min(bytesAvailable, maxBufferSize);
				byte[] buffer = new byte[bufferSize];

				// read file and write it into form...
				int bytesRead = inputStream.read(buffer, 0, bufferSize);
				int progressSign = 0;
				while (bytesRead > 0) {
					outputStream.write(buffer, 0, bufferSize);
					bytesAvailable = inputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = inputStream.read(buffer, 0, bufferSize);
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
				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// close streams
				inputStream.close();
				outputStream.flush();
				//System.out.println("\rOK");
				logger.info("Plik skopiowany.");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik skopiowany.");
				eventDAO.createEvent(eventModel, statusMessage);

				int responseCode = conn.getResponseCode();
				logger.info("Kod odpowiedzi serwera https: " + responseCode);
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Kod odpowiedzi serwera https: " + responseCode);
				eventDAO.createEvent(eventModel, statusMessage);
				if (responseCode == 200) {
					// Sprawdza czy plik odpowiedzi serwera zawiera responseString
					// jezeli jest to plik zostal poprawnie przeslany na serwer
					BufferedReader br = null;
					boolean found = false;
					try {
						InputStream is = conn.getInputStream();
						br = new BufferedReader(new InputStreamReader(is));
						String line;
						while ((line = br.readLine()) != null) {
							if (line.contains(responseString)) {
								found = true;
								break;
							}
						}
					} finally {
						try {
							if (br != null)
								br.close();
						} catch (Exception e) {
							logger.error("Problem podaczas zamykania BufferReader " + e.toString());
							eventModel.setFileExchangeStatusID(fileExchangeStatusID);
							eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Problem podaczas zamykania BufferReader " + e.toString());
							eventDAO.createEvent(eventModel, statusMessage);
						}
					}

					if (found) {
						logger.info("Plik przesłany na serwer");
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik przesłany na serwer");
						eventDAO.createEvent(eventModel, statusMessage);
						
						reportBean.addReport(fileName);

					} else {
						logger.error("Problem z przesłaniem pliku: " + fileName);
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Problem z przesłaniem pliku: " + fileName);
						eventDAO.createEvent(eventModel, statusMessage);
						
						reportBean.addReport("-> Problem z przesłaniem pliku: " + fileName);
					}
				} else {
					logger.error("Problem z przesłaniem pliku. Kod odpowiedzi serwera https: " + responseCode);
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Problem z przesłaniem pliku. Kod odpowiedzi serwera https: " + responseCode);
					eventDAO.createEvent(eventModel, statusMessage);
					
					reportBean.addReport("-> Problem z przesłaniem pliku: " + fileName);
				}
			} catch (FileNotFoundException ex) {
				logger.fatal("Błąd: " + ex.getMessage());
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Błąd: " + ex.getMessage());
				eventDAO.createEvent(eventModel, statusMessage);
				
				reportBean.addReport("-> Błąd: Plik nie istnieje" + fileName);
			} catch (IOException ex) {
				logger.fatal("Błąd: " + ex.getMessage());
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Błąd: " + ex.getMessage());
				eventDAO.createEvent(eventModel, statusMessage);
				ex.printStackTrace();
			}
		}
		this.closeSSLConnection();
		return true;
	}
}
