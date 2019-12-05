package pl.tycm.fes.bean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.ScheduleExpression;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;

import pl.tycm.fes.dao.TaskStatusDAO;
import pl.tycm.fes.dao.TaskStatusDAOImpl;
import pl.tycm.fes.model.FileListDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskConfigDataModel;
import pl.tycm.fes.model.TaskStatusDataModel;
import pl.tycm.fes.util.MTTools;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class JobSessionBean {
	
	@Resource
	TimerService timerService;

	@EJB
	private AppBean appBean;

	private final Logger logger = Logger.getLogger(this.getClass());

	@Timeout
	public void timeout(Timer timer) {
		StatusMessage statusMessage = new StatusMessage();
		DateFormat eventDateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
		DateFormat nextScheduledDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

		TaskConfigDataModel orginal = (TaskConfigDataModel) timer.getInfo();
		TaskConfigDataModel taskConfigModel = (TaskConfigDataModel) MTTools.deepClone(orginal);
		logger.info("Schedule occurred for Task: " + taskConfigModel.getSubjectName());
		
		Date nextScheduledDate = timer.getNextTimeout();
		logger.info("Next schedule will occurred: " + nextScheduledDate);
		TaskStatusDAO taskStatusDAO = new TaskStatusDAOImpl();
		TaskStatusDataModel taskStatusModel = new TaskStatusDataModel();

		taskStatusModel = taskStatusDAO.getTaskStatus(taskConfigModel.getId(), statusMessage);
		taskStatusModel.setNextScheduledDate(nextScheduledDateFormat.format(nextScheduledDate));
		taskStatusDAO.updateTaskStatus(taskStatusModel, statusMessage);
		
		if (taskConfigModel.isScheduledIsActive()) {
			logger.info("Run runApp: " + taskConfigModel.getSubjectName());
			
			List<FileListDataModel> fileList = taskConfigModel.getFileList();
			String fileDate = new SimpleDateFormat("ddMMyyyy").format(new Date());
			String dateFormat = taskConfigModel.getDateFormat();
			String subjectExchangeProtocol = taskConfigModel.getSubjectExchangeProtocol();
			logger.info("Konfiguracja taska: " + taskConfigModel);
			logger.info("Data plików: " + fileDate);
			logger.info("Format daty: " + dateFormat);
			logger.info("Lista plików przed konwersją: " + fileList);
			List<FileListDataModel> convertFileList = MTTools.getConvertFileList(fileList, fileDate, dateFormat,
					subjectExchangeProtocol);
			logger.info("Lista plików po konwersji: " + convertFileList);
			taskConfigModel.setFileList(convertFileList);
			
			String eventDateTime = eventDateFormat.format(new Date());
			appBean.runApp(taskConfigModel, eventDateTime);
		}
	}

	public void createJob(TaskConfigDataModel taskConfigModel) {
		StatusMessage statusMessage = new StatusMessage();
		DateFormat nextScheduledDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		ScheduleExpression schedule = new ScheduleExpression();
		TimerConfig timerConfig = new TimerConfig(taskConfigModel, false);
		schedule.minute(taskConfigModel.getMinutes());
		schedule.hour(taskConfigModel.getHours());
		schedule.dayOfWeek(dayOfWeekScheduleExpression(taskConfigModel));
		schedule.month(monthScheduleExpression(taskConfigModel));
		Timer timer = timerService.createCalendarTimer(schedule, timerConfig);
		logger.info("New schedule created for Task: " + taskConfigModel.getSubjectName());

		TaskStatusDAO taskStatusDAO = new TaskStatusDAOImpl();
		TaskStatusDataModel taskStatusModel = new TaskStatusDataModel();

		taskStatusModel = taskStatusDAO.getTaskStatus(taskConfigModel.getId(), statusMessage);
		taskStatusModel.setNextScheduledDate(nextScheduledDateFormat.format(timer.getNextTimeout()));
		taskStatusDAO.updateTaskStatus(taskStatusModel, statusMessage);
	}

	public void clearJobList() {
		Collection<Timer> timers = timerService.getTimers();
		for (Timer timer : timers) {
			logger.info("Canceling schedule for Task: " + timer.getInfo());
			timer.cancel();
		}
	}

	private Timer getTimer(TaskConfigDataModel taskConfigModel) {
		Collection<Timer> timers = timerService.getTimers();
		for (Timer timer : timers) {
			if (taskConfigModel.equals((TaskConfigDataModel) timer.getInfo())) {
				return timer;
			}
		}
		return null;
	}

	public void deleteJob(TaskConfigDataModel taskConfigModel) {
		Timer timer = getTimer(taskConfigModel);
		if (timer != null) {
			timer.cancel();
			TaskConfigDataModel taskConfigModelTimer = (TaskConfigDataModel) timer.getInfo();
			logger.info("Canceling schedule for Task: " + taskConfigModelTimer.getSubjectName());
		}
	}

	public void updateJob(TaskConfigDataModel taskConfigModel) throws Exception {
		Timer timer = getTimer(taskConfigModel);
		if (timer != null) {
			TaskConfigDataModel taskConfigModelTimer = (TaskConfigDataModel) timer.getInfo();
			logger.info("Removing schedule for Task: " + taskConfigModelTimer.getSubjectName());
			timer.cancel();
			createJob(taskConfigModel);
		} else {
			createJob(taskConfigModel);
		}
	}

	private String dayOfWeekScheduleExpression(TaskConfigDataModel taskConfigModel) {

		String daysOfWeek = null;

		if ((taskConfigModel.getDays() % 2) >= 1) {
			daysOfWeek = "Mon";
		}

		if ((taskConfigModel.getDays() % 4) >= 2) {
			if (daysOfWeek == null)
				daysOfWeek = "Tue";
			else
				daysOfWeek = daysOfWeek + ",Tue";
		}

		if ((taskConfigModel.getDays() % 8) >= 4) {
			if (daysOfWeek == null)
				daysOfWeek = "Wed";
			else
				daysOfWeek = daysOfWeek + ",Wed";
		}

		if ((taskConfigModel.getDays() % 16) >= 8) {
			if (daysOfWeek == null)
				daysOfWeek = "Thu";
			else
				daysOfWeek = daysOfWeek + ",Thu";
		}

		if ((taskConfigModel.getDays() % 32) >= 16) {
			if (daysOfWeek == null)
				daysOfWeek = "Fri";
			else
				daysOfWeek = daysOfWeek + ",Fri";
		}

		if ((taskConfigModel.getDays() % 64) >= 32) {
			if (daysOfWeek == null)
				daysOfWeek = "Sat";
			else
				daysOfWeek = daysOfWeek + ",Sat";
		}

		if ((taskConfigModel.getDays() % 128) >= 64) {
			if (daysOfWeek == null)
				daysOfWeek = "Sun";
			else
				daysOfWeek = daysOfWeek + ",Sun";
		}

		if (daysOfWeek == null)
			daysOfWeek = "*";
		return daysOfWeek;
	}

	private String monthScheduleExpression(TaskConfigDataModel taskConfigModel) {

		String month = null;

		if ((taskConfigModel.getMonths() % 2) >= 1) {
			month = "Jan";
		}

		if ((taskConfigModel.getMonths() % 4) >= 2) {
			if (month == null)
				month = "Feb";
			else
				month = month + ",Feb";
		}

		if ((taskConfigModel.getMonths() % 8) >= 4) {
			if (month == null)
				month = "Mar";
			else
				month = month + ",Mar";
		}

		if ((taskConfigModel.getMonths() % 16) >= 8) {
			if (month == null)
				month = "Apr";
			else
				month = month + ",Apr";
		}

		if ((taskConfigModel.getMonths() % 32) >= 16) {
			if (month == null)
				month = "May";
			else
				month = month + ",May";
		}

		if ((taskConfigModel.getMonths() % 64) >= 32) {
			if (month == null)
				month = "Jun";
			else
				month = month + ",Jun";
		}

		if ((taskConfigModel.getMonths() % 128) >= 64) {
			if (month == null)
				month = "Jul";
			else
				month = month + ",Jul";
		}

		if ((taskConfigModel.getMonths() % 256) >= 128) {
			if (month == null)
				month = "Aug";
			else
				month = month + ",Aug";
		}

		if ((taskConfigModel.getMonths() % 512) >= 256) {
			if (month == null)
				month = "Sep";
			else
				month = month + ",Sep";
		}

		if ((taskConfigModel.getMonths() % 1024) >= 512) {
			if (month == null)
				month = "Oct";
			else
				month = month + ",Oct";
		}

		if ((taskConfigModel.getMonths() % 2048) >= 1024) {
			if (month == null)
				month = "Nov";
			else
				month = month + ",Nov";
		}

		if ((taskConfigModel.getMonths() % 4096) >= 2048) {
			if (month == null)
				month = "Dec";
			else
				month = month + ",Dec";
		}

		if (month == null)
			month = "*";
		return month;
	}
}
