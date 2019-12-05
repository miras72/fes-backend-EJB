package pl.tycm.fes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import pl.tycm.fes.db.ConnectionManager;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskStatusDataModel;

@Stateless
@LocalBean
public class TaskStatusDAOImpl implements TaskStatusDAO {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public TaskStatusDataModel getTaskStatus(int id, StatusMessage statusMessage) {
		TaskStatusDataModel taskStatusModel = new TaskStatusDataModel();
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ID, LAST_STATUS, LAST_DATA_STATUS, NEXT_SCHEDULED_DATE, TASK_ID from TASK_STATUS where TASK_ID = ?";

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {

				taskStatusModel.setId(rs.getInt("id"));
				taskStatusModel.setLastStatus(rs.getString("LAST_STATUS"));
				taskStatusModel.setLastDataStatus(rs.getString("LAST_DATA_STATUS"));
				taskStatusModel.setNextScheduledDate(rs.getString("NEXT_SCHEDULED_DATE"));
				taskStatusModel.setTaskID(rs.getInt("TASK_ID"));
			} else {
				logger.error(String.format("Task Status with ID of %d is not found.", id));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage(String.format("Nie ma takiego zadania."));
				return null;
			}

		} catch (SQLException e) {
			logger.fatal(e.getMessage());
			statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
			statusMessage.setMessage(e.getMessage());
			return null;
		} finally {
			ConnectionManager.close(rs);
			ConnectionManager.close(ps);
			ConnectionManager.close(c);
		}

		return taskStatusModel;
	}

	@Override
	public TaskStatusDataModel deleteTaskStatus(int id, StatusMessage statusMessage) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TaskStatusDataModel createTaskStatus(TaskStatusDataModel taskStatusModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "insert into TASK_STATUS (TASK_ID) values (?)";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, taskStatusModel.getTaskID());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to create Task Status.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("Nie można pobrać statusów zadań.");
				return null;
			}
		} catch (SQLException e) {
			logger.fatal(e.getMessage());
			statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
			statusMessage.setMessage(e.getMessage());
			return null;
		} finally {
			ConnectionManager.close(rs);
			ConnectionManager.close(ps);
			ConnectionManager.close(c);
		}
		return taskStatusModel;
	}

	@Override
	public boolean updateTaskStatus(TaskStatusDataModel taskStatusModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update TASK_STATUS set LAST_STATUS = ?, LAST_DATA_STATUS = ?, NEXT_SCHEDULED_DATE = ? where TASK_ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, taskStatusModel.getLastStatus());
			ps.setString(2, taskStatusModel.getLastDataStatus());
			ps.setString(3, taskStatusModel.getNextScheduledDate());
			ps.setInt(4, taskStatusModel.getTaskID());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to update Task Status.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - Nie można uaktualnić zadania.");
				return false;
			}
		} catch (SQLException e) {
			logger.fatal(e.getMessage());
			statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
			statusMessage.setMessage(e.getMessage());
			return false;
		} finally {
			ConnectionManager.close(rs);
			ConnectionManager.close(ps);
			ConnectionManager.close(c);
		}
		return true;
	}

	@Override
	public List<TaskStatusDataModel> getAllTaskStatus(StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select TASK_STATUS.ID, TASK_STATUS.TASK_ID, TASK_STATUS.LAST_STATUS, TASK_STATUS.LAST_DATA_STATUS, TASK_STATUS.NEXT_SCHEDULED_DATE, "
				+ "TASK_CONFIG.SCHEDULED_IS_ACTIVE, TASK_CONFIG.SUBJECT_NAME, TASK_CONFIG.SUBJECT_MODE, "
				+ "TASK_CONFIG.MINUTES, TASK_CONFIG.HOURS, TASK_CONFIG.DAYS, TASK_CONFIG.MONTHS from TASK_STATUS "
				+ "INNER Join  TASK_CONFIG on TASK_CONFIG.ID = TASK_STATUS.TASK_ID ORDER BY TASK_CONFIG.SUBJECT_NAME";

		List<TaskStatusDataModel> allTaskStatus = new ArrayList<TaskStatusDataModel>();

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {

				TaskStatusDataModel taskStatusModel = new TaskStatusDataModel();
				taskStatusModel.setId(rs.getInt("ID"));
				taskStatusModel.setTaskID(rs.getInt("TASK_ID"));
				taskStatusModel.setSubjectName(rs.getString("SUBJECT_NAME"));
				taskStatusModel.setSubjectMode(rs.getString("SUBJECT_MODE"));
				taskStatusModel.setLastStatus(rs.getString("LAST_STATUS"));
				taskStatusModel.setLastDataStatus(rs.getString("LAST_DATA_STATUS"));
				taskStatusModel.setNextScheduledDate(rs.getString("NEXT_SCHEDULED_DATE"));

				int binaryDays = rs.getInt("DAYS");
				int binaryMonths = rs.getInt("MONTHS");
				taskStatusModel.setScheduledInterval(rs.getString("HOURS") + ":" + rs.getString("MINUTES") + " "
						+ decodeDays(binaryDays) + " " + decodeMonths(binaryMonths));

				taskStatusModel.setScheduledIsActive(rs.getBoolean("SCHEDULED_IS_ACTIVE"));

				allTaskStatus.add(taskStatusModel);
			}
			if (allTaskStatus.isEmpty()) {
				logger.error("No Task Status Exists.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("Brak zdefiniowanych zadań.");
				return null;
			}
		} catch (SQLException e) {
			logger.fatal(e.getMessage());
			statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
			statusMessage.setMessage(e.getMessage());
			return null;
		} finally {
			ConnectionManager.close(rs);
			ConnectionManager.close(ps);
			ConnectionManager.close(c);
		}

		return allTaskStatus;
	}

	private String decodeDays(int binaryDays) {

		String daysOfWeek = null;
		if ((binaryDays % 2) >= 1) {
			daysOfWeek = "pn";
		}

		if ((binaryDays % 4) >= 2) {
			if (daysOfWeek == null)
				daysOfWeek = "wt";
			else
				daysOfWeek = daysOfWeek + ",wt";
		}

		if ((binaryDays % 8) >= 4) {
			if (daysOfWeek == null)
				daysOfWeek = "śr";
			else
				daysOfWeek = daysOfWeek + ",śr";
		}

		if ((binaryDays % 16) >= 8) {
			if (daysOfWeek == null)
				daysOfWeek = "cz";
			else
				daysOfWeek = daysOfWeek + ",cz";
		}

		if ((binaryDays % 32) >= 16) {
			if (daysOfWeek == null)
				daysOfWeek = "pt";
			else
				daysOfWeek = daysOfWeek + ",pt";
		}

		if ((binaryDays % 64) >= 32) {
			if (daysOfWeek == null)
				daysOfWeek = "so";
			else
				daysOfWeek = daysOfWeek + ",so";
		}

		if ((binaryDays % 128) >= 64) {
			if (daysOfWeek == null)
				daysOfWeek = "nd";
			else
				daysOfWeek = daysOfWeek + ",nd";
		}
		return daysOfWeek;
	}

	private String decodeMonths(int binaryMonths) {

		String months = null;

		if ((binaryMonths % 2) >= 1) {
			months = "Sty";
		}

		if ((binaryMonths % 4) >= 2) {
			if (months == null)
				months = "Lut";
			else
				months = months + ",Lut";
		}

		if ((binaryMonths % 8) >= 4) {
			if (months == null)
				months = "Mar";
			else
				months = months + ",Mar";
		}

		if ((binaryMonths % 16) >= 8) {
			if (months == null)
				months = "Kwi";
			else
				months = months + ",Kwi";
		}

		if ((binaryMonths % 32) >= 16) {
			if (months == null)
				months = "Maj";
			else
				months = months + ",Maj";
		}

		if ((binaryMonths % 64) >= 32) {
			if (months == null)
				months = "Cze";
			else
				months = months + ",Cze";
		}

		if ((binaryMonths % 128) >= 64) {
			if (months == null)
				months = "Lip";
			else
				months = months + ",Lip";
		}

		if ((binaryMonths % 256) >= 128) {
			if (months == null)
				months = "Sie";
			else
				months = months + ",Sie";
		}

		if ((binaryMonths % 512) >= 256) {
			if (months == null)
				months = "Wrz";
			else
				months = months + ",Wrz";
		}

		if ((binaryMonths % 1024) >= 512) {
			if (months == null)
				months = "Paź";
			else
				months = months + ",Paź";
		}

		if ((binaryMonths % 2048) >= 1024) {
			if (months == null)
				months = "Lis";
			else
				months = months + ",Lis";
		}

		if ((binaryMonths % 4096) >= 2048) {
			if (months == null)
				months = "Gru";
			else
				months = months + ",Gru";
		}
		return months;
	}
}
