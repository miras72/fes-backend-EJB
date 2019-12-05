package pl.tycm.fes.bean;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;

import pl.tycm.fes.dao.EventDAO;
import pl.tycm.fes.dao.EventDAOImpl;
import pl.tycm.fes.dao.FileExchangeStatusDAO;
import pl.tycm.fes.dao.FileExchangeStatusDAOImpl;
import pl.tycm.fes.dao.MailingListDAO;
import pl.tycm.fes.dao.MailingListDAOImpl;
import pl.tycm.fes.dao.ServerConfigDAO;
import pl.tycm.fes.dao.ServerConfigDAOImpl;
import pl.tycm.fes.dao.TaskStatusDAO;
import pl.tycm.fes.dao.TaskStatusDAOImpl;
import pl.tycm.fes.model.EventDataModel;
import pl.tycm.fes.model.FileExchangeStatusDataModel;
import pl.tycm.fes.model.FileListDataModel;
import pl.tycm.fes.model.MailingListDataModel;
import pl.tycm.fes.model.ServerConfigDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskConfigDataModel;
import pl.tycm.fes.model.TaskStatusDataModel;
import pl.tycm.fes.util.MTTools;

@Stateless
@LocalBean
public class AppBean {

	private final Logger logger = Logger.getLogger(this.getClass());
	//private List<String> receiveFileList;
	private String reportMessage;

	private DateFormat lastStatusDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private String lastDataStatus = lastStatusDateFormat.format(new Date());

	private StatusMessage statusMessage = new StatusMessage();

	private TaskStatusDAO taskStatusDAO = new TaskStatusDAOImpl();
	private ServerConfigDAO serverConfigDAO = new ServerConfigDAOImpl();
	//private ServerConfigDataModel serverConfigModel = serverConfigDAO.getServerConfig(statusMessage);
	//private String workingDirectory = serverConfigModel.getWorkDirectory();

	private EventDAO eventDAO = new EventDAOImpl();

	@Resource
	SessionContext myContext;

	@EJB
	SubjectServiceBean subjectServiceBean;

	@EJB
	GzipFileBean gzipFileBean;

	@EJB
	PGPFileBean pgpFileBean;

	@EJB
	ServerBean serverBean;

	@EJB
	ArchiveBean archiveBean;

	@EJB
	MailBean mailBean;

	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Future<Integer> runApp(TaskConfigDataModel taskConfigModel, String eventDateTime) {

		boolean isArchiveCreated = true;
		boolean isSend = false;
		
		List<String> receiveFileList;
		ServerConfigDataModel serverConfigModel = serverConfigDAO.getServerConfig(statusMessage);
		String workingDirectory = serverConfigModel.getWorkDirectory();

		TaskStatusDataModel taskStatusModel = taskStatusDAO.getTaskStatus(taskConfigModel.getId(), statusMessage);

		taskStatusModel.setLastStatus("Running");

		taskStatusModel.setLastDataStatus(lastDataStatus);
		taskStatusDAO.updateTaskStatus(taskStatusModel, statusMessage);

		MailingListDAO mailingListDAO = new MailingListDAOImpl();
		List<MailingListDataModel> mailingListModel = mailingListDAO.getMailingList(taskConfigModel.getId(),
				statusMessage);

		FileExchangeStatusDAO fileExchangeStatusDAO = new FileExchangeStatusDAOImpl();
		FileExchangeStatusDataModel fileExchangeStatusModel = new FileExchangeStatusDataModel();

		EventDataModel eventModel = new EventDataModel();

		fileExchangeStatusModel.setTaskID(taskConfigModel.getId());
		fileExchangeStatusModel.setEventDateTime(eventDateTime);

		int fileExchangeStatusID = fileExchangeStatusDAO.createFileExchangeStatus(fileExchangeStatusModel,
				statusMessage);

		eventModel.setFileExchangeStatusID(fileExchangeStatusID);
		eventModel.setEventText("-------");
		eventDAO.createEvent(eventModel, statusMessage);

		logger.info("Start logowania zdarzeń");
		eventModel.setFileExchangeStatusID(fileExchangeStatusID);
		eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Start logowania zdarzeń");
		eventDAO.createEvent(eventModel, statusMessage);

		logger.info(String.format("Wymiana danych z podmiotem %s...", taskConfigModel.getSubjectName()));
		eventModel.setFileExchangeStatusID(fileExchangeStatusID);
		eventModel.setEventText(MTTools.getLogDate() + "INFO  "
				+ String.format("Wymiana danych z podmiotem %s...", taskConfigModel.getSubjectName()));
		eventDAO.createEvent(eventModel, statusMessage);

		if (!Files.isDirectory(Paths.get(workingDirectory))) {
			logger.info("Tworzę katalog roboczy: " + workingDirectory);
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(
					MTTools.getLogDate() + "INFO  " + "Tworzę katalog roboczy: " + workingDirectory + "...");
			eventDAO.createEvent(eventModel, statusMessage);

			if (!new File(workingDirectory).mkdirs()) {
				logger.error("Nie można utworzyć katalogu roboczego.");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Nie można utworzyć katalogu roboczego.");
				eventDAO.createEvent(eventModel, statusMessage);

				taskStatusModel.setLastStatus("ERROR");
				lastDataStatus = lastStatusDateFormat.format(new Date());
				taskStatusModel.setLastDataStatus(lastDataStatus);
				taskStatusDAO.updateTaskStatus(taskStatusModel, statusMessage);
				return new AsyncResult<Integer>(2);
			}

			logger.info("Katalog utworzony.");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Katalog utworzony.");
			eventDAO.createEvent(eventModel, statusMessage);
		}

		ReportBean reportBean = new ReportBean();

		switch (taskConfigModel.getSubjectMode()) {
		case "download":
			logger.info("Tryb wymiany: download");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Tryb wymiany: download");
			eventDAO.createEvent(eventModel, statusMessage);

			if (myContext.wasCancelCalled()) {
				cancelTask(fileExchangeStatusID, eventModel, taskStatusModel);
				return new AsyncResult<Integer>(2);
			}

			receiveFileList = subjectServiceBean.subjectReceiveFiles(taskConfigModel, fileExchangeStatusID, reportBean);

			if (myContext.wasCancelCalled()) {
				cancelTask(fileExchangeStatusID, eventModel, taskStatusModel);

				deleteFiles(receiveFileList, fileExchangeStatusID, eventModel);
				return new AsyncResult<Integer>(2);
			}

			if (taskConfigModel.getDecompressionMethod() != null && receiveFileList != null)
				receiveFileList = subjectServiceBean.decompressFiles(receiveFileList, taskConfigModel,
						fileExchangeStatusID, reportBean);

			if (myContext.wasCancelCalled()) {
				cancelTask(fileExchangeStatusID, eventModel, taskStatusModel);

				deleteFiles(receiveFileList, fileExchangeStatusID, eventModel);
				return new AsyncResult<Integer>(2);
			}
			if (taskConfigModel.getDecryptionMethod() != null && receiveFileList != null)
				receiveFileList = subjectServiceBean.decryptFiles(receiveFileList, taskConfigModel,
						fileExchangeStatusID, reportBean);

			if (myContext.wasCancelCalled()) {
				cancelTask(fileExchangeStatusID, eventModel, taskStatusModel);

				deleteFiles(receiveFileList, fileExchangeStatusID, eventModel);
				return new AsyncResult<Integer>(2);
			}

			if (receiveFileList != null) {
				isSend = serverBean.sendFiles(taskConfigModel.getServers(), workingDirectory, receiveFileList, fileExchangeStatusID, reportBean);
			}

			if (myContext.wasCancelCalled()) {
				cancelTask(fileExchangeStatusID, eventModel, taskStatusModel);

				deleteFiles(receiveFileList, fileExchangeStatusID, eventModel);
				return new AsyncResult<Integer>(2);
			}

			if (receiveFileList != null && taskConfigModel.isFileArchive()) {
				//archiveBean.setWorkingDirectory(workingDirectory);
				String archiveDirectory = serverConfigModel.getArchDirectory();
				if (!archiveDirectory.endsWith("/")) {
					archiveDirectory = archiveDirectory + File.separator + taskConfigModel.getSubjectName() + "."
							+ taskConfigModel.getSubjectMode();
				} else {
					archiveDirectory = archiveDirectory + taskConfigModel.getSubjectName() + "."
							+ taskConfigModel.getSubjectMode();
				}
				//archiveBean.setArchiveDirectory(archiveDirectory);
				isArchiveCreated = archiveBean.archiveFiles(workingDirectory, archiveDirectory, receiveFileList, fileExchangeStatusID, reportBean);
			}

			if (receiveFileList != null && isArchiveCreated && isSend) {
				taskStatusModel.setLastStatus("OK");
				lastDataStatus = lastStatusDateFormat.format(new Date());
				taskStatusModel.setLastDataStatus(lastDataStatus);
				taskStatusDAO.updateTaskStatus(taskStatusModel, statusMessage);
			} else {
				taskStatusModel.setLastStatus("Error");
				lastDataStatus = lastStatusDateFormat.format(new Date());
				taskStatusModel.setLastDataStatus(lastDataStatus);
				taskStatusDAO.updateTaskStatus(taskStatusModel, statusMessage);
			}
			if (receiveFileList != null)
				deleteFiles(receiveFileList, fileExchangeStatusID, eventModel);

			reportMessage = reportBean.getReport();

			mailBean.setMailingList(mailingListModel);
			mailBean.setMailFrom(taskConfigModel.getMailFrom());
			mailBean.setMailSubject(taskConfigModel.getMailSubject());
			mailBean.sendMail(reportMessage);

			logger.info("Koniec logowania zdarzeń");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Koniec logowania zdarzeń");
			eventDAO.createEvent(eventModel, statusMessage);

			break;

		case "upload":
			logger.info("Tryb wymiany: upload");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Tryb wymiany: upload");
			eventDAO.createEvent(eventModel, statusMessage);

			if (myContext.wasCancelCalled()) {
				cancelTask(fileExchangeStatusID, eventModel, taskStatusModel);

				return new AsyncResult<Integer>(2);
			}
			List<String> fileList = new ArrayList<>();
			for (FileListDataModel fileListModel : taskConfigModel.getFileList()) {
				fileList.add(fileListModel.getFileName());
			}

			receiveFileList = serverBean.receiveFiles(taskConfigModel.getServers(), workingDirectory, fileList, fileExchangeStatusID, reportBean);

			if (myContext.wasCancelCalled()) {
				cancelTask(fileExchangeStatusID, eventModel, taskStatusModel);

				deleteFiles(receiveFileList, fileExchangeStatusID, eventModel);
				return new AsyncResult<Integer>(2);
			}
			
			if (receiveFileList != null)
				isSend = subjectServiceBean.subjectSendFiles(receiveFileList, taskConfigModel, fileExchangeStatusID,
						reportBean);

			if (receiveFileList != null && isSend) {
				taskStatusModel.setLastStatus("OK");
				lastDataStatus = lastStatusDateFormat.format(new Date());
				taskStatusModel.setLastDataStatus(lastDataStatus);
				taskStatusDAO.updateTaskStatus(taskStatusModel, statusMessage);
			} else {
				taskStatusModel.setLastStatus("Error");
				lastDataStatus = lastStatusDateFormat.format(new Date());
				taskStatusModel.setLastDataStatus(lastDataStatus);
				taskStatusDAO.updateTaskStatus(taskStatusModel, statusMessage);
			}

			if (receiveFileList != null)
				deleteFiles(receiveFileList, fileExchangeStatusID, eventModel);

			reportMessage = reportBean.getReport();

			mailBean.setMailingList(mailingListModel);
			mailBean.setMailFrom(taskConfigModel.getMailFrom());
			mailBean.setMailSubject(taskConfigModel.getMailSubject());
			mailBean.sendMail(reportMessage);

			logger.info("Koniec logowania zdarzeń");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Koniec logowania zdarzeń");
			eventDAO.createEvent(eventModel, statusMessage);

			break;
		default:
			logger.fatal("PODMIOT.Tryb - nieprawidłowy tryb (download/upload)");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "FATAL " + "Nieprawidłowy tryb (download/upload)");
			eventDAO.createEvent(eventModel, statusMessage);
		}
		return new AsyncResult<Integer>(1);
	}

	private void deleteFiles(List<String> receiveFileList, int fileExchangeStatusID, EventDataModel eventModel) {

		ServerConfigDataModel serverConfigModel = serverConfigDAO.getServerConfig(statusMessage);
		String workingDirectory = serverConfigModel.getWorkDirectory();
		
		if (receiveFileList != null) {
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Kasuje pliki w katalogu roboczy...");
			eventDAO.createEvent(eventModel, statusMessage);
			for (String fileName : receiveFileList) {

				File file = new File(workingDirectory + File.separator + fileName);
				logger.info("Kasuje w katalogu roboczym plik: " + fileName + "...");

				if (file.delete()) {
					logger.info("Plik skasowany.");
				} else {
					logger.error("Błąd: Nie można skasować pliku: " + fileName);
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(
							MTTools.getLogDate() + "ERROR " + "Błąd: Nie można skasować pliku: " + fileName);
					eventDAO.createEvent(eventModel, statusMessage);
				}
			}
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Pliki w katalogu roboczy skasowane.");
			eventDAO.createEvent(eventModel, statusMessage);
		}
	}

	private void cancelTask(int fileExchangeStatusID, EventDataModel eventModel, TaskStatusDataModel taskStatusModel) {
		logger.info("Zadanie zostało przerwane");
		eventModel.setFileExchangeStatusID(fileExchangeStatusID);
		eventModel.setEventText(MTTools.getLogDate() + "ERROR " + "Zadanie zostało przerwane");
		eventDAO.createEvent(eventModel, statusMessage);

		taskStatusModel.setLastStatus("OK");
		lastDataStatus = lastStatusDateFormat.format(new Date());
		taskStatusModel.setLastDataStatus(lastDataStatus);
		taskStatusDAO.updateTaskStatus(taskStatusModel, statusMessage);
	}
}
