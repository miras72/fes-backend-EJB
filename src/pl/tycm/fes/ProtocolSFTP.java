package pl.tycm.fes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sshtools.net.SocketTransport;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import com.sshtools.sftp.FileTransferProgress;
import com.sshtools.sftp.SftpClient;
import com.sshtools.sftp.SftpStatusException;
import com.sshtools.ssh.HostKeyVerification;
import com.sshtools.ssh.PasswordAuthentication;
import com.sshtools.ssh.PublicKeyAuthentication;
import com.sshtools.ssh.SshAuthentication;
import com.sshtools.ssh.SshClient;
import com.sshtools.ssh.SshConnector;
import com.sshtools.ssh.SshException;
import com.sshtools.ssh.components.SshKeyPair;
import com.sshtools.ssh.components.SshPublicKey;
import com.sshtools.ssh2.Ssh2Client;

import pl.tycm.fes.bean.ReportBean;
import pl.tycm.fes.dao.EventDAO;
import pl.tycm.fes.dao.EventDAOImpl;
import pl.tycm.fes.model.EventDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.util.MTTools;

public class ProtocolSFTP {

	private String serverAddress;
	private String remoteLogin;
	private String remotePassword;
	private String remoteDirectory;
	private String privateKeyName;
	private byte[] privateKey;
	private String workingDirectory;
	private List<String> fileList;
	private String sourceFileMode;
	private int fileExchangeStatusID;
	private ReportBean reportBean;

	private SshClient ssh;
	private Ssh2Client ssh2;

	private EventDAO eventDAO = new EventDAOImpl();
	private EventDataModel eventModel = new EventDataModel();
	private StatusMessage statusMessage = new StatusMessage();

	boolean status = false;

	private final Logger logger = Logger.getLogger(this.getClass());

	public ProtocolSFTP(String serverAddress, String remoteLogin, String remotePassword, String remoteDirectory,
			String privateKeyName, byte[] privateKey, String workingDirectory, List<String> fileList,
			String sourceFileMode, int fileExchangeStatusID, ReportBean reportBean) {
		this.serverAddress = serverAddress;
		this.remoteLogin = remoteLogin;
		this.remotePassword = remotePassword;
		this.remoteDirectory = remoteDirectory;
		this.privateKeyName = privateKeyName;
		this.privateKey = privateKey;
		this.workingDirectory = workingDirectory;
		this.fileList = fileList;
		this.sourceFileMode = sourceFileMode;
		this.fileExchangeStatusID = fileExchangeStatusID;
		this.reportBean = reportBean;
	}

	public ProtocolSFTP(String serverAddress, String remoteLogin, String remotePassword, String remoteDirectory,
			String workingDirectory, List<String> fileList, String sourceFileMode, int fileExchangeStatusID,
			ReportBean reportBean) {
		this.serverAddress = serverAddress;
		this.remoteLogin = remoteLogin;
		this.remotePassword = remotePassword;
		this.remoteDirectory = remoteDirectory;
		this.workingDirectory = workingDirectory;
		this.fileList = fileList;
		this.sourceFileMode = sourceFileMode;
		this.fileExchangeStatusID = fileExchangeStatusID;
		this.reportBean = reportBean;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getRemoteLogin() {
		return remoteLogin;
	}

	public void setRemoteLogin(String remoteLogin) {
		this.remoteLogin = remoteLogin;
	}

	public String getRemoteDirectory() {
		return remoteDirectory;
	}

	public void setRemoteDirectory(String remoteDirectory) {
		this.remoteDirectory = remoteDirectory;
	}

	public String getPrivateKeyName() {
		return privateKeyName;
	}

	public void setPrivateKeyName(String privateKeyName) {
		this.privateKeyName = privateKeyName;
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}

	public String getWorkDirectory() {
		return workingDirectory;
	}

	public void setWorkDirectory(String workDirectory) {
		this.workingDirectory = workDirectory;
	}

	public List<String> getFileList() {
		return fileList;
	}

	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

	public String getSourceFileMode() {
		return sourceFileMode;
	}

	public void setSourceFileMode(String sourceFileMode) {
		this.sourceFileMode = sourceFileMode;
	}

	public boolean createSFTPConnection() {

		try {
			logger.info("Inicjalizacja połączenia....");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Inicjalizacja połączenia....");
			eventDAO.createEvent(eventModel, statusMessage);

			int idx = serverAddress.indexOf(':');
			int port = 22;
			if (idx > -1) {
				port = Integer.parseInt(serverAddress.substring(idx + 1));
				serverAddress = serverAddress.substring(0, idx);

			}

			logger.info("Trwa łączenie do " + serverAddress);
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Trwa łączenie do " + serverAddress);
			eventDAO.createEvent(eventModel, statusMessage);

			/**
			 * Create an SshConnector instance
			 */
			SshConnector con = SshConnector.createInstance();

			// con.setSupportedVersions(1);
			// Lets do some host key verification
			HostKeyVerification hkv = new HostKeyVerification() {
				public boolean verifyHost(String adresSerwera, SshPublicKey key) {
					try {
						logger.info("Klucz serwera (" + key.getAlgorithm() + "): ");
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(
								MTTools.getLogDate() + "INFO  " + "Klucz serwera (" + key.getAlgorithm() + "): ");
						eventDAO.createEvent(eventModel, statusMessage);

						logger.info(key.getFingerprint());
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(MTTools.getLogDate() + "INFO  " + key.getFingerprint());
						eventDAO.createEvent(eventModel, statusMessage);
					} catch (SshException e) {
						logger.fatal("Problem z połączeniem. Błąd: " + e.getMessage());
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(
								MTTools.getLogDate() + "FATAL " + "Problem z połączeniem. Błąd: " + e.getMessage());
						eventDAO.createEvent(eventModel, statusMessage);

						reportBean.addReport("-> Problem z połączeniem. Błąd: " + e.getMessage());
						return false;
					}
					return true;
				}
			};

			con.getContext().setHostKeyVerification(hkv);

			/**
			 * Connect to the host
			 */
			ssh = con.connect(new SocketTransport(serverAddress, port), remoteLogin);

			logger.info(serverAddress + "... połączony");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + serverAddress + "... połączony");
			eventDAO.createEvent(eventModel, statusMessage);

			logger.info("Trwa autoryzacja połączenia...");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Trwa autoryzacja połączenia...");
			eventDAO.createEvent(eventModel, statusMessage);
			ssh2 = (Ssh2Client) ssh;

			int authStatus;
			if (privateKey != null) {
				authStatus = privateKeyAuthentication();
				if (authStatus == SshAuthentication.FURTHER_AUTHENTICATION_REQUIRED) {
					authStatus = passwordAuthentication();
				}
			} else {
				authStatus = passwordAuthentication();
			}

			if (authStatus != SshAuthentication.COMPLETE && ssh.isConnected()) {
				return false;
			}

		} catch (NoRouteToHostException th) {
			logger.fatal(th.getMessage());
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + th.getMessage());
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean.addReport("-> " + serverAddress + ": " + th.getMessage());
			return false;
		} catch (FileNotFoundException th) {
			logger.fatal(th.getMessage());
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + th.getMessage());
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean.addReport("-> Błąd połączenia z serwerem " + serverAddress + ": " + th.getMessage());
			return false;
		} catch (SshException th) {
			logger.fatal(th.getMessage());
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + th.getMessage());
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean.addReport("-> Błąd połączenia z serwerem " + serverAddress + ": " + th.getMessage());
			return false;
		} catch (ConnectException th) {
			logger.fatal(th.getMessage());
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + th.getMessage());
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean.addReport("-> Błąd połączenia z serwerem " + serverAddress + ": " + th.getMessage());
			return false;
		} catch (Throwable th) {
			logger.fatal(th.getMessage());
			logger.fatal("Bark pliku");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + th.getMessage());
			eventDAO.createEvent(eventModel, statusMessage);

			return false;
		}
		logger.info("Autoryzacja poprawna.");
		eventModel.setFileExchangeStatusID(fileExchangeStatusID);
		eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Autoryzacja poprawna.");
		eventDAO.createEvent(eventModel, statusMessage);
		return true;
	}

	private int privateKeyAuthentication() throws IOException, InvalidPassphraseException, SshException {

		logger.info("Private key file: " + privateKeyName);
		eventModel.setFileExchangeStatusID(fileExchangeStatusID);
		eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Private key file: " + privateKeyName);
		eventDAO.createEvent(eventModel, statusMessage);
		/**
		 * Authenticate the user using public key authentication
		 */
		SshPrivateKeyFile pkfile = SshPrivateKeyFileFactory.parse((privateKey));
		SshKeyPair pair;
		pair = pkfile.toKeyPair(null);

		PublicKeyAuthentication pk = new PublicKeyAuthentication();
		pk.setPrivateKey(pair.getPrivateKey());
		pk.setPublicKey(pair.getPublicKey());

		int authStatus = ssh2.authenticate(pk);

		if (authStatus == SshAuthentication.FAILED) {
			logger.fatal("Niepoprawna autoryzacja kluczem publicznym. Kod błędu: " + authStatus);
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Niepoprawna autoryzacja kluczem publicznym.");
			eventDAO.createEvent(eventModel, statusMessage);
		}
		return authStatus;
	}

	private int passwordAuthentication() throws IOException, InvalidPassphraseException, SshException {
		logger.info("Password: " + "*****");
		eventModel.setFileExchangeStatusID(fileExchangeStatusID);
		eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Hasło: " + "*****");
		eventDAO.createEvent(eventModel, statusMessage);
		/**
		 * Authenticate the user using password authentication
		 */
		PasswordAuthentication pwd = new PasswordAuthentication();
		pwd.setPassword(remotePassword);
		int authStatus = ssh2.authenticate(pwd);

		switch (authStatus) {
		case SshAuthentication.FAILED:
			logger.fatal("Niepoprawna autoryzacja hasłem. Kod błędu: " + authStatus);
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Niepoprawna autoryzacja hasłem.");
			eventDAO.createEvent(eventModel, statusMessage);
			return authStatus;
		case SshAuthentication.FURTHER_AUTHENTICATION_REQUIRED:
			logger.fatal(
					"Wymagana jest dodatkowa autoryzacja przy pomocy klucza publicznego. Kod błędu: " + authStatus);
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL "
					+ "Wymagana jest dodatkowa autoryzacja przy pomocy klucza publicznego.");
			eventDAO.createEvent(eventModel, statusMessage);
			return authStatus;
		default:
			return authStatus;
		}
	}

	public List<String> receiveFiles() {

		boolean statusConnection = this.createSFTPConnection();
		List<String> receiveFileList = new ArrayList<>();

		if (statusConnection) {
			if (ssh.isAuthenticated()) {
				logger.info("Pobieram pliki z serwera Podmiotu (" + serverAddress + "):");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(
						MTTools.getLogDate() + "INFO  " + "Pobieram pliki z serwera Podmiotu (" + serverAddress + "):");
				eventDAO.createEvent(eventModel, statusMessage);

				reportBean.addReport("- Lista plików pobranych z serwera Podmiotu (" + serverAddress + "):");

				try {
					SftpClient sftp = new SftpClient(ssh2);
					/**
					 * Now perform some binary operations
					 */
					sftp.setTransferMode(SftpClient.MODE_BINARY);

					/**
					 * Change directory
					 */
					sftp.lcd(workingDirectory);
					sftp.cd(remoteDirectory);

					/**
					 * get all files in the remote directory
					 */
					sftp.setRegularExpressionSyntax(SftpClient.GlobSyntax);

					// create progress tracker
					FileTransferProgress progress = new FileTransferProgress() {
						int progressSign = 0;
						String remoteFileName = null;

						public void completed() {
							// System.out.println("\rOK");
							logger.info("Plik pobrany.");
							eventModel.setFileExchangeStatusID(fileExchangeStatusID);
							eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik pobrany.");
							eventDAO.createEvent(eventModel, statusMessage);

							receiveFileList.add(remoteFileName);
							reportBean.addReport(remoteFileName);

							status = true;
							if (sourceFileMode != null) {
								switch (sourceFileMode) {
								case "rename":
									try {
										logger.info("Zmieniam na serwerze nazwę pliku z: " + remoteFileName + " na "
												+ remoteFileName + ".downloaded");
										eventModel.setFileExchangeStatusID(fileExchangeStatusID);
										eventModel.setEventText(
												MTTools.getLogDate() + "INFO  " + "Zmieniam na serwerze nazwę pliku z: "
														+ remoteFileName + " na " + remoteFileName + ".downloaded");
										eventDAO.createEvent(eventModel, statusMessage);

										sftp.rename(remoteFileName, remoteFileName + ".downloaded");
									} catch (SftpStatusException e) {
										logger.error("Błąd zmiany nazwy plików: " + e.getMessage());
										eventModel.setFileExchangeStatusID(fileExchangeStatusID);
										eventModel.setEventText(MTTools.getLogDate() + "ERROR "
												+ "Błąd zmiany nazwy plików: " + e.getMessage());
										eventDAO.createEvent(eventModel, statusMessage);

										reportBean.addReport("-> Błąd zmiany nazwy pliku: " + remoteFileName);
										e.printStackTrace();
									} catch (SshException e) {
										logger.error("Błąd zmiany nazwy plików: " + e.getMessage());
										eventModel.setFileExchangeStatusID(fileExchangeStatusID);
										eventModel.setEventText(MTTools.getLogDate() + "ERROR "
												+ "Błąd zmiany nazwy plików: " + e.getMessage());
										eventDAO.createEvent(eventModel, statusMessage);

										reportBean.addReport("-> Błąd zmiany nazwy pliku: " + remoteFileName);
										e.printStackTrace();
									}
									break;
								case "delete":
									try {
										logger.info("Kasuję na serwerze plik: " + remoteFileName);
										eventModel.setFileExchangeStatusID(fileExchangeStatusID);
										eventModel.setEventText(MTTools.getLogDate() + "INFO  "
												+ "Kasuję na serwerze plik: " + remoteFileName);
										eventDAO.createEvent(eventModel, statusMessage);

										sftp.rm(remoteFileName);
									} catch (SftpStatusException e) {
										logger.error("Błąd kasowania plików: " + e.getMessage());
										eventModel.setFileExchangeStatusID(fileExchangeStatusID);
										eventModel.setEventText(MTTools.getLogDate() + "ERROR "
												+ "Błąd kasowania plików: " + e.getMessage());
										eventDAO.createEvent(eventModel, statusMessage);

										reportBean.addReport("-> Błąd kasowania pliku: " + remoteFileName);
										e.printStackTrace();
									} catch (SshException e) {
										logger.error("Błąd kasowania plików: " + e.getMessage());
										eventModel.setFileExchangeStatusID(fileExchangeStatusID);
										eventModel.setEventText(MTTools.getLogDate() + "ERROR "
												+ "Błąd kasowania plików: " + e.getMessage());
										eventDAO.createEvent(eventModel, statusMessage);

										reportBean.addReport("-> Błąd kasowania pliku: " + remoteFileName);
										e.printStackTrace();
									}
									break;
								}
							}
						}

						public boolean isCancelled() {
							return false;
						}

						public void progressed(long arg0) {
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
								System.out.print("\r–");
								progressSign++;
								break;
							case 3:
								System.out.print("\r\\");
								progressSign = 0;
								break;
							}
						}

						public void started(long arg0, String arg1) {
							remoteFileName = arg1.substring(arg1.lastIndexOf("/") + 1);
							logger.info("Pobieram plik: " + remoteFileName + "...");
							eventModel.setFileExchangeStatusID(fileExchangeStatusID);
							eventModel.setEventText(
									MTTools.getLogDate() + "INFO  " + "Pobieram plik: " + remoteFileName + "...");
							eventDAO.createEvent(eventModel, statusMessage);
						}
					};
					for (String fileName : fileList) {
						try {
							sftp.getFiles(fileName, progress);
						} catch (FileNotFoundException th) {
							logger.fatal("Plik nie istnieje: " + th.getMessage());
							eventModel.setFileExchangeStatusID(fileExchangeStatusID);
							eventModel.setEventText(
									MTTools.getLogDate() + "FATAL " + "Plik nie istnieje: " + th.getMessage());
							eventDAO.createEvent(eventModel, statusMessage);
						}
					}
				} catch (SftpStatusException th) {
					logger.fatal("Błąd pobierania plików: " + th.getMessage());
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(
							MTTools.getLogDate() + "FATAL " + "Błąd pobierania plików: " + th.getMessage());
					eventDAO.createEvent(eventModel, statusMessage);

					reportBean.addReport("-> Błąd pobierania plików: " + th.getMessage());
					return null;
				} catch (Throwable th) {
					logger.fatal(th.getMessage());
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "FATAL " + th.getMessage());
					eventDAO.createEvent(eventModel, statusMessage);

					th.printStackTrace();
					return null;
				}
				if (!status) {
					logger.error("Brak plików do pobrania");
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Brak plików do pobrania");
					eventDAO.createEvent(eventModel, statusMessage);

					reportBean.addReport("-> Błąd: Brak plików do pobrania");
					return null;
				}
				return receiveFileList;
			} else {
				logger.fatal("Problem z nawiązaniem połączenia.");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Problem z nawiązaniem połączenia.");
				eventDAO.createEvent(eventModel, statusMessage);

				reportBean.addReport("-> Problem z nawiązaniem połączenia.");
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean sendFiles() {
		boolean status = this.createSFTPConnection();

		if (status) {
			if (ssh.isAuthenticated()) {
				logger.info("Wysyłam pliki na serwer Podmiotu (" + serverAddress + "):");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(
						MTTools.getLogDate() + "INFO  " + "Wysyłam pliki na serwer Podmiotu (" + serverAddress + "):");
				eventDAO.createEvent(eventModel, statusMessage);

				reportBean.addReport("- Lista plików wysłanych na serwer Podmiotu (" + serverAddress + "):");
				try {
					SftpClient sftp = new SftpClient(ssh2);
					/**
					 * Now perform some binary operations
					 */
					sftp.setTransferMode(SftpClient.MODE_BINARY);

					/**
					 * Change directory
					 */
					sftp.lcd(workingDirectory);
					sftp.cd(remoteDirectory);

					/**
					 * get all files in the remote directory
					 */
					sftp.setRegularExpressionSyntax(SftpClient.GlobSyntax);

					// create progress tracker
					FileTransferProgress progress = new FileTransferProgress() {
						int progressSign = 0;
						String remoteFileName = null;

						public void completed() {
							System.out.println("\rOK");
						}

						public boolean isCancelled() {
							return false;
						}

						public void progressed(long arg0) {
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
								System.out.print("\r–");
								progressSign++;
								break;
							case 3:
								System.out.print("\r\\");
								progressSign = 0;
								break;
							}
						}

						public void started(long arg0, String arg1) {
							remoteFileName = arg1.substring(arg1.lastIndexOf("/") + 1);
							logger.info("Pobieram plik: " + remoteFileName + "...");
							eventModel.setFileExchangeStatusID(fileExchangeStatusID);
							eventModel.setEventText(
									MTTools.getLogDate() + "INFO  " + "Pobieram plik: " + remoteFileName + "...");
							eventDAO.createEvent(eventModel, statusMessage);

							reportBean.addReport(remoteFileName);
						}
					};
					for (String fileName : fileList) {
						sftp.putFiles(fileName, progress);
					}
				} catch (SftpStatusException th) {
					logger.fatal("Błąd przesyłania plików: " + th.getMessage());
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(
							MTTools.getLogDate() + "FATAL " + "Błąd przesyłania plików: " + th.getMessage());
					eventDAO.createEvent(eventModel, statusMessage);

					reportBean.addReport("-> Błąd przesyłania plików: " + th.getMessage());
					return false;
				} catch (Throwable th) {
					logger.fatal(th.getMessage());
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "FATAL " + th.getMessage());
					eventDAO.createEvent(eventModel, statusMessage);

					th.printStackTrace();
					return false;
				}
				return true;
			} else {
				logger.fatal("Problem z nawiązaniem połączenia.");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Problem z nawiązaniem połączenia.");
				eventDAO.createEvent(eventModel, statusMessage);

				reportBean.addReport("-> Problem z nawiązaniem połączenia.");
				return false;
			}
		} else {
			return false;
		}
	}

	public int getFileExchangeStatusID() {
		return fileExchangeStatusID;
	}

	public void setFileExchangeStatusID(int fileExchangeStatusID) {
		this.fileExchangeStatusID = fileExchangeStatusID;
	}
}
