package pl.tycm.fes.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

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
public class GzipFileBean {

	private EventDAO eventDAO = new EventDAOImpl();
	private EventDataModel eventModel = new EventDataModel();
	private StatusMessage statusMessage = new StatusMessage();

	private final Logger logger = Logger.getLogger(this.getClass());

	public List<String> decompressGzipFile(String workingDirectory, List<String> receiveFileList,
			int fileExchangeStatusID, ReportBean reportBean) {

		List<String> newReceiveFileList = new ArrayList<>();
		
		for (String fileName : receiveFileList) {
			if (fileName.contains(".gz")) {
				String newFileName = fileName.replace(".gz", "");
				try {
					logger.info("Dekompresuje plik: " + fileName + "...");
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Dekompresuje plik: " + fileName + "...");
					eventDAO.createEvent(eventModel, statusMessage);

					FileInputStream fis = new FileInputStream(workingDirectory + File.separator + fileName);
					GZIPInputStream gis = new GZIPInputStream(fis);
					FileOutputStream fos = new FileOutputStream(workingDirectory + File.separator + newFileName);

					byte[] bytesArray = new byte[16 * 4096];
					int bytesRead = -1;
					int progressSign = 0;
					while ((bytesRead = gis.read(bytesArray)) != -1) {
						fos.write(bytesArray, 0, bytesRead);
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
					fos.close();
					gis.close();
					// System.out.println("\rOK");
					logger.info("Plik z dekompresowany.");
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik z dekompresowany.");
					eventDAO.createEvent(eventModel, statusMessage);
					
					newReceiveFileList.add(newFileName);
					/*
					 * } catch(ZipException ex) { logger.error(ex.getMessage());
					 */
				} catch (IOException ex) {
					logger.error(ex.getMessage());
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "ERROR " + ex.getMessage());
					eventDAO.createEvent(eventModel, statusMessage);

					reportBean.addReport("-> Błąd: Nie powiodła się operacja dekompresji pliku: " + fileName);
				} finally {
					File file = new File(workingDirectory + File.separator + fileName);
					logger.info("Kasuje plik: " + fileName + "...");
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Kasuje plik: " + fileName + "...");
					eventDAO.createEvent(eventModel, statusMessage);
					if (file.delete()) {
						logger.info("Plik skasowany.");
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik skasowany.");
						eventDAO.createEvent(eventModel, statusMessage);
					} else {
						logger.error("Błąd: Nie można skasować pliku: " + fileName);
						eventModel.setFileExchangeStatusID(fileExchangeStatusID);
						eventModel.setEventText(
								MTTools.getLogDate() + "ERROR " + "Błąd: Nie można skasować pliku: " + fileName);
						eventDAO.createEvent(eventModel, statusMessage);
					}
				}
			} else {
				newReceiveFileList.add(fileName);
			}
		}
		return newReceiveFileList;
	}
}
