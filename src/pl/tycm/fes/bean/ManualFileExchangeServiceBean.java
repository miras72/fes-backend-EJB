package pl.tycm.fes.bean;

import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import pl.tycm.fes.dao.TaskConfigDAO;
import pl.tycm.fes.dao.TaskConfigDAOImpl;
import pl.tycm.fes.model.FileListDataModel;
import pl.tycm.fes.model.ManualFileExchangeDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskConfigDataModel;
import pl.tycm.fes.util.MTTools;

@Singleton
@LocalBean
public class ManualFileExchangeServiceBean {

	private final Logger logger = Logger.getLogger(this.getClass());
	private Future<Integer> future;

	@EJB
	private AppBean appBean;

	public boolean startManualFileExchange(ManualFileExchangeDataModel manualFileExchangeModel,
			StatusMessage statusMessage) {

		TaskConfigDataModel taskConfigModel = new TaskConfigDataModel();
		TaskConfigDAO taskConfigDAO = new TaskConfigDAOImpl();
		int taskID = manualFileExchangeModel.getTaskID();		
		
		taskConfigModel = taskConfigDAO.getTaskConfig(taskID, statusMessage);
		if (taskConfigModel == null) {
			return false;
		}
		List<FileListDataModel> fileList = manualFileExchangeModel.getFileList();
		String fileDate = manualFileExchangeModel.getFileDate();
		String dateFormat = taskConfigModel.getDateFormat();
		String subjectExchangeProtocol = taskConfigModel.getSubjectExchangeProtocol();
		List<FileListDataModel> convertFileList = MTTools.getConvertFileList(fileList, fileDate, dateFormat,
				subjectExchangeProtocol);
		taskConfigModel.setFileList(convertFileList);

		future = appBean.runApp(taskConfigModel, manualFileExchangeModel.getEventDateTime());
		
		logger.info(String.format("Zadanie pobierania plików z %s uruchomione...", taskConfigModel.getSubjectName()));
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("Zadanie pobierania plików uruchomione...");
		return true;
	}

	public boolean stopManualFileExchange(int id, StatusMessage statusMessage) {
		future.cancel(true);
		logger.info("Zadanie pobierania plików zostało zatrzymane.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("Zadanie pobierania plików zostało zatrzymane.");
		return true;
	}
}
