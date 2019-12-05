package pl.tycm.fes.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;

import pl.tycm.fes.dao.EventDAO;
import pl.tycm.fes.dao.EventDAOImpl;
import pl.tycm.fes.model.EventDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.util.MTTools;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ArchiveBean {

	//private String archiveDirectory;
	//private String workingDirectory;
	//private String dateFile;
	//private String zipFileName;

	private final Logger logger = Logger.getLogger(this.getClass());

	private EventDAO eventDAO = new EventDAOImpl();
	private EventDataModel eventModel = new EventDataModel();
	private StatusMessage statusMessage = new StatusMessage();

	/*public String getArchiveDirectory() {
		return archiveDirectory;
	}

	public void setArchiveDirectory(String archiveDirectory) {
		if (archiveDirectory.endsWith(File.separator)) {
			this.archiveDirectory = archiveDirectory.substring(0, archiveDirectory.length() - 1);
		} else {
			this.archiveDirectory = archiveDirectory;
		}
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		if (workingDirectory.endsWith(File.separator)) {
			this.workingDirectory = workingDirectory.substring(0, workingDirectory.length() - 1);
		} else {
			this.workingDirectory = workingDirectory;
		}
	}*/

	/*public String getDateFile() {
		return dateFile;
	}

	public void setDateFile(String dateFile) {
		this.dateFile = dateFile;
	}*/

	public boolean archiveFiles(String workingDirectory, String archiveDirectory, List<String> receiveFileList, int fileExchangeStatusID, ReportBean reportBean) {

		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		Date dataTime = new Date();
		String dateFile = dateFormat.format(dataTime);
		String zipFileName = dateFile + ".zip";

		boolean isOK = true;
		
		if (archiveDirectory.endsWith(File.separator))
			archiveDirectory = archiveDirectory.substring(0, archiveDirectory.length() - 1);

		try {
			if (!Files.isDirectory(Paths.get(archiveDirectory))) {
				logger.info("Tworzę katalog archiwum: " + archiveDirectory);
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(
						MTTools.getLogDate() + "INFO  " + "Tworzę katalog archiwum: " + archiveDirectory + "...");
				eventDAO.createEvent(eventModel, statusMessage);

				if (!new File(archiveDirectory).mkdirs()) {
					logger.error("Nie można utworzyć katalogu archiwum.");
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Nie można utworzyć katalogu archiwum.");
					eventDAO.createEvent(eventModel, statusMessage);
					isOK = false;
					return isOK;
				}

				logger.info("Katalog utworzony.");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Katalog utworzony.");
				eventDAO.createEvent(eventModel, statusMessage);
			}
			FileOutputStream fos = new FileOutputStream(archiveDirectory + File.separator + zipFileName);
			ZipOutputStream zos = new ZipOutputStream(fos);

			logger.info("Tworzę archiwum: " + zipFileName);
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Tworzę archiwum: " + zipFileName + "...");
			eventDAO.createEvent(eventModel, statusMessage);

			for (String fileName : receiveFileList) {

				logger.info("Dodaję do archiwum plik: " + fileName);
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Dodaję do archiwum plik: " + fileName);
				eventDAO.createEvent(eventModel, statusMessage);

				ZipEntry zipEntry = new ZipEntry(fileName);
				zos.putNextEntry(zipEntry);
				FileInputStream in = new FileInputStream(workingDirectory + File.separator + fileName);

				int read;
				int progressSign = 0;
				byte[] buffer = new byte[16 * 1024];
				while ((read = in.read(buffer, 0, buffer.length)) > 0) {
					zos.write(buffer, 0, read);
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
				// System.out.println("\rOK");
				logger.info("Plik został dodany do archiwum.");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik został dodany do archiwum.");
				eventDAO.createEvent(eventModel, statusMessage);
			}
			zos.closeEntry();
			zos.close();
			logger.info("Archiwum gotowe");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Archiwum gotowe");
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean.addReport("- Pliki skopiowano do archiwum.");

		} catch (IOException ex) {
			logger.error(ex.getMessage());
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "ERROR " + ex.getMessage());
			eventDAO.createEvent(eventModel, statusMessage);

			reportBean.addReport("-> Błąd: Nie powiodła się operacja skopiowania plików do archiwum.");
			isOK = false;
			ex.printStackTrace();
		}
		return isOK;
	}
}
