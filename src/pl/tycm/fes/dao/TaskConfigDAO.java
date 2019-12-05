package pl.tycm.fes.dao;

import java.sql.Blob;
import java.util.List;

import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskConfigDataModel;


public interface TaskConfigDAO {

	public TaskConfigDataModel getTaskConfig(int id, StatusMessage statusMessage);

	public boolean deleteTaskConfig(int id, StatusMessage statusMessage);

	public TaskConfigDataModel createTaskConfig(TaskConfigDataModel taskConfig, StatusMessage statusMessage);

	public boolean updateTaskConfig(TaskConfigDataModel taskConfig, StatusMessage statusMessage);
	
	public List<TaskConfigDataModel> getAllTaskConfig(StatusMessage statusMessage);
	
	public Blob getTaskPGPFile(int id, StatusMessage statusMessage);

}
