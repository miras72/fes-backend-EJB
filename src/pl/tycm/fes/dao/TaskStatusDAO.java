package pl.tycm.fes.dao;

import java.util.List;

import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskStatusDataModel;

public interface TaskStatusDAO {

	public TaskStatusDataModel getTaskStatus(int id, StatusMessage statusMessage);

	public TaskStatusDataModel deleteTaskStatus(int id, StatusMessage statusMessage);

	public TaskStatusDataModel createTaskStatus(TaskStatusDataModel taskStatusModel, StatusMessage statusMessage);

	public boolean updateTaskStatus(TaskStatusDataModel taskStatusModel, StatusMessage statusMessage);

	public List<TaskStatusDataModel> getAllTaskStatus(StatusMessage statusMessage);
}
