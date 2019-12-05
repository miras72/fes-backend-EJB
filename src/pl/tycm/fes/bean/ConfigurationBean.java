package pl.tycm.fes.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import pl.tycm.fes.dao.TaskConfigDAO;
import pl.tycm.fes.dao.TaskConfigDAOImpl;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskConfigDataModel;

@Singleton
@Startup
public class ConfigurationBean {

	@EJB
	JobSessionBean jobSessionBean;

	@PostConstruct
	public void initialize() {
		jobSessionBean.clearJobList();
		StatusMessage statusMessage = new StatusMessage();
		TaskConfigDAO taskConfigDAO = new TaskConfigDAOImpl();
		List<TaskConfigDataModel> tasksConfigModel = taskConfigDAO.getAllTaskConfig(statusMessage);
		if (tasksConfigModel != null) {
			for (TaskConfigDataModel taskConfigModel : tasksConfigModel) {
				jobSessionBean.createJob(taskConfigModel);
			}
		}
	}
}
