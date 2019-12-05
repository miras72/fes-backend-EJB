package pl.tycm.fes.dao;

import java.sql.Blob;
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
import pl.tycm.fes.model.FileListDataModel;
import pl.tycm.fes.model.ServerDataModel;
import pl.tycm.fes.model.MailingListDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.model.TaskConfigDataModel;
import pl.tycm.fes.model.TaskStatusDataModel;

@Stateless
@LocalBean
public class TaskConfigDAOImpl implements TaskConfigDAO {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public TaskConfigDataModel getTaskConfig(int id, StatusMessage statusMessage) {

		TaskConfigDataModel taskConfigModel = new TaskConfigDataModel();
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ID, SUBJECT_NAME, SUBJECT_ADDRESS, SUBJECT_LOGIN, SUBJECT_PASSWORD, SUBJECT_DIRECTORY, SUBJECT_EXCHANGE_PROTOCOL, SUBJECT_ENCRYPTION_KEY_ID, "
				+ "SUBJECT_MODE, SUBJECT_LOGIN_FORM, SUBJECT_LOGOUT_FORM, SUBJECT_POST_OPTIONS, SUBJECT_RESPONSE_STRING, SOURCE_FILE_MODE, "
				+ "DECOMPRESSION_METHOD, DECRYPTION_METHOD, DECRYPTION_KEY_ID, FILE_ARCHIVE, DATE_FORMAT, "
				+ "MINUTES, HOURS, DAYS, MONTHS, SCHEDULED_IS_ACTIVE, MAIL_FROM, MAIL_SUBJECT from TASK_CONFIG where ID = ?";

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {

				taskConfigModel.setId(rs.getInt("id"));
				taskConfigModel.setSubjectName(rs.getString("SUBJECT_NAME"));
				taskConfigModel.setSubjectAddress(rs.getString("SUBJECT_ADDRESS"));
				taskConfigModel.setSubjectLogin(rs.getString("SUBJECT_LOGIN"));
				taskConfigModel.setSubjectPassword(rs.getString("SUBJECT_PASSWORD"));
				taskConfigModel.setSubjectDirectory(rs.getString("SUBJECT_DIRECTORY"));
				taskConfigModel.setSubjectExchangeProtocol(rs.getString("SUBJECT_EXCHANGE_PROTOCOL"));
				taskConfigModel.setSubjectEncryptionKeyID(rs.getInt("SUBJECT_ENCRYPTION_KEY_ID"));
				taskConfigModel.setSubjectMode(rs.getString("SUBJECT_MODE"));
				taskConfigModel.setSubjectLoginForm(rs.getString("SUBJECT_LOGIN_FORM"));
				taskConfigModel.setSubjectLogoutForm(rs.getString("SUBJECT_LOGOUT_FORM"));
				taskConfigModel.setSubjectPostOptions(rs.getString("SUBJECT_POST_OPTIONS"));
				taskConfigModel.setSubjectResponseString(rs.getString("SUBJECT_RESPONSE_STRING"));

				ServerDAO serverDAO = new ServerDAOImpl();
				taskConfigModel.setServers(serverDAO.getServers(id, statusMessage));
				if (taskConfigModel.getServers() == null) {
					return null;
				}

				taskConfigModel.setSourceFileMode(rs.getString("SOURCE_FILE_MODE"));
				taskConfigModel.setDecompressionMethod(rs.getString("DECOMPRESSION_METHOD"));
				taskConfigModel.setDecryptionMethod(rs.getString("DECRYPTION_METHOD"));
				taskConfigModel.setDecryptionKeyID(rs.getInt("DECRYPTION_KEY_ID"));

				FileListDAO fileListDAO = new FileListDAOImpl();
				taskConfigModel.setFileList(fileListDAO.getFileList(id, statusMessage));
				if (taskConfigModel.getFileList() == null) {
					return null;
				}

				taskConfigModel.setFileArchive(rs.getBoolean("FILE_ARCHIVE"));
				taskConfigModel.setDateFormat(rs.getString("DATE_FORMAT"));

				taskConfigModel.setMinutes(rs.getString("MINUTES"));
				taskConfigModel.setHours(rs.getString("HOURS"));
				taskConfigModel.setDays(rs.getInt("DAYS"));
				taskConfigModel.setMonths(rs.getInt("MONTHS"));
				taskConfigModel.setScheduledIsActive(rs.getBoolean("SCHEDULED_IS_ACTIVE"));

				MailingListDAO mailingListDAO = new MailingListDAOImpl();
				taskConfigModel.setMailingList(mailingListDAO.getMailingList(id, statusMessage));
				if (taskConfigModel.getMailingList() == null) {
					return null;
				}

				taskConfigModel.setMailFrom(rs.getString("MAIL_FROM"));
				taskConfigModel.setMailSubject(rs.getString("MAIL_SUBJECT"));
			} else {
				logger.error(String.format("Task with ID of %d is not found.", id));
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

		return taskConfigModel;
	}

	@Override
	public boolean deleteTaskConfig(int id, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "delete from TASK_CONFIG where ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error(String.format("Unable to DELETE Task with ID of %d.", id));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage(String.format("BŁĄD - Nie można usunąć zadania."));
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
		logger.info(String.format("Successfully deleted Task with ID of %d.", id));
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage(String.format("Zadanie zostało usunięte."));
		return true;
	}

	@Override
	public TaskConfigDataModel createTaskConfig(TaskConfigDataModel taskConfigModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int autoID = -1;
		String sql = "insert into TASK_CONFIG (SUBJECT_NAME, SUBJECT_ADDRESS, SUBJECT_LOGIN, SUBJECT_PASSWORD, "
				+ "SUBJECT_DIRECTORY, SUBJECT_EXCHANGE_PROTOCOL, SUBJECT_ENCRYPTION_KEY_ID, "
				+ "SUBJECT_MODE, SUBJECT_LOGIN_FORM, SUBJECT_LOGOUT_FORM, SUBJECT_POST_OPTIONS, SUBJECT_RESPONSE_STRING, SOURCE_FILE_MODE, "
				+ "DECOMPRESSION_METHOD, DECRYPTION_METHOD, DECRYPTION_KEY_ID, FILE_ARCHIVE, DATE_FORMAT, "
				+ "MINUTES, HOURS, DAYS, MONTHS, SCHEDULED_IS_ACTIVE, MAIL_FROM, MAIL_SUBJECT) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql, new String[] { "ID" });
			ps.setString(1, taskConfigModel.getSubjectName());
			ps.setString(2,
					taskConfigModel.getSubjectAddress() == null ? null : taskConfigModel.getSubjectAddress().trim());
			ps.setString(3,
					taskConfigModel.getSubjectLogin() == null ? null : taskConfigModel.getSubjectLogin().trim());
			ps.setString(4,
					taskConfigModel.getSubjectPassword() == null ? null : taskConfigModel.getSubjectPassword().trim());
			ps.setString(5, taskConfigModel.getSubjectDirectory() == null ? null
					: taskConfigModel.getSubjectDirectory().trim());
			ps.setString(6, taskConfigModel.getSubjectExchangeProtocol());
			if (taskConfigModel.getSubjectEncryptionKeyID() == 0) {
				ps.setNull(7, java.sql.Types.INTEGER);
			} else {
				ps.setInt(7, taskConfigModel.getSubjectEncryptionKeyID());
			}
			ps.setString(8, taskConfigModel.getSubjectMode());
			ps.setString(9, taskConfigModel.getSubjectLoginForm() == null ? null
					: taskConfigModel.getSubjectLoginForm().trim());
			ps.setString(10, taskConfigModel.getSubjectLogoutForm() == null ? null
					: taskConfigModel.getSubjectLogoutForm().trim());
			ps.setString(11, taskConfigModel.getSubjectPostOptions() == null ? null
					: taskConfigModel.getSubjectPostOptions().trim());
			ps.setString(12, taskConfigModel.getSubjectResponseString() == null ? null
					: taskConfigModel.getSubjectResponseString().trim());
			ps.setString(13, taskConfigModel.getSourceFileMode());
			ps.setString(14, taskConfigModel.getDecompressionMethod());
			ps.setString(15, taskConfigModel.getDecryptionMethod());
			ps.setInt(16, taskConfigModel.getDecryptionKeyID());
			ps.setBoolean(17, taskConfigModel.isFileArchive());
			ps.setString(18, taskConfigModel.getDateFormat() == null ? null : taskConfigModel.getDateFormat().trim());
			ps.setString(19, taskConfigModel.getMinutes() == null ? null : taskConfigModel.getMinutes().trim());
			ps.setString(20, taskConfigModel.getHours() == null ? null : taskConfigModel.getHours().trim());
			ps.setInt(21, taskConfigModel.getDays());
			ps.setInt(22, taskConfigModel.getMonths());
			ps.setBoolean(23, taskConfigModel.isScheduledIsActive());
			ps.setString(24, taskConfigModel.getMailFrom() == null ? null : taskConfigModel.getMailFrom().trim());
			ps.setString(25, taskConfigModel.getMailSubject());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to create Task Config.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - Nie można utworzyć zadania.");
				return null;
			}

			rs = ps.getGeneratedKeys();

			if (rs.next()) {
				autoID = rs.getInt(1);
				taskConfigModel.setId(autoID);
				TaskStatusDAO taskStatusDAO = new TaskStatusDAOImpl();
				TaskStatusDataModel taskStatuModel = new TaskStatusDataModel();
				taskStatuModel.setTaskID(autoID);
				if (taskStatusDAO.createTaskStatus(taskStatuModel, statusMessage) == null)
					return null;

				ServerDAO serverDAO = new ServerDAOImpl();
				for (ServerDataModel serverModel : taskConfigModel.getServers()) {
					serverModel.setTaskID(autoID);
					if (serverDAO.createServer(serverModel, statusMessage) == false)
						return null;
				}

				FileListDAO fileListDAO = new FileListDAOImpl();
				for (FileListDataModel fileListModel : taskConfigModel.getFileList()) {

					fileListModel.setTaskID(autoID);
					if (fileListDAO.createFileList(fileListModel, statusMessage) == false)
						return null;
				}

				MailingListDAO mailingListDAO = new MailingListDAOImpl();
				for (MailingListDataModel mailingListModel : taskConfigModel.getMailingList()) {
					mailingListModel.setTaskID(autoID);
					if (mailingListDAO.createMailingList(mailingListModel, statusMessage) == false)
						return null;
				}
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
		logger.info("Successfully created Task");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("Nowe zadanie zostało dodane.");

		return taskConfigModel;
	}

	@Override
	public boolean updateTaskConfig(TaskConfigDataModel taskConfigModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update TASK_CONFIG set SUBJECT_NAME = ?, SUBJECT_ADDRESS = ?, SUBJECT_LOGIN = ?, SUBJECT_PASSWORD = ?, "
				+ "SUBJECT_DIRECTORY = ?, SUBJECT_EXCHANGE_PROTOCOL = ?, SUBJECT_ENCRYPTION_KEY_ID = ?, "
				+ "SUBJECT_MODE = ?, SUBJECT_LOGIN_FORM = ?, SUBJECT_LOGOUT_FORM = ?, SUBJECT_POST_OPTIONS = ?, SUBJECT_RESPONSE_STRING = ?, SOURCE_FILE_MODE = ?, "
				+ "DECOMPRESSION_METHOD = ?, DECRYPTION_METHOD = ?, DECRYPTION_KEY_ID = ?, FILE_ARCHIVE = ?, DATE_FORMAT = ?, "
				+ "MINUTES = ?, HOURS = ?, DAYS = ?, MONTHS = ?, SCHEDULED_IS_ACTIVE = ?, MAIL_FROM = ?, MAIL_SUBJECT = ? where ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, taskConfigModel.getSubjectName());
			ps.setString(2,
					taskConfigModel.getSubjectAddress() == null ? null : taskConfigModel.getSubjectAddress().trim());
			ps.setString(3,
					taskConfigModel.getSubjectLogin() == null ? null : taskConfigModel.getSubjectLogin().trim());
			ps.setString(4,
					taskConfigModel.getSubjectPassword() == null ? null : taskConfigModel.getSubjectPassword().trim());
			ps.setString(5, taskConfigModel.getSubjectDirectory() == null ? null
					: taskConfigModel.getSubjectDirectory().trim());
			ps.setString(6, taskConfigModel.getSubjectExchangeProtocol());
			if (taskConfigModel.getSubjectEncryptionKeyID() == 0) {
				ps.setNull(7, java.sql.Types.INTEGER);
			} else {
				ps.setInt(7, taskConfigModel.getSubjectEncryptionKeyID());
			}
			ps.setString(8, taskConfigModel.getSubjectMode());
			ps.setString(9, taskConfigModel.getSubjectLoginForm() == null ? null
					: taskConfigModel.getSubjectLoginForm().trim());
			ps.setString(10, taskConfigModel.getSubjectLogoutForm() == null ? null
					: taskConfigModel.getSubjectLogoutForm().trim());
			ps.setString(11, taskConfigModel.getSubjectPostOptions() == null ? null
					: taskConfigModel.getSubjectPostOptions().trim());
			ps.setString(12, taskConfigModel.getSubjectResponseString() == null ? null
					: taskConfigModel.getSubjectResponseString().trim());
			ps.setString(13, taskConfigModel.getSourceFileMode());
			ps.setString(14, taskConfigModel.getDecompressionMethod());
			ps.setString(15, taskConfigModel.getDecryptionMethod());
			ps.setInt(16, taskConfigModel.getDecryptionKeyID());
			ps.setBoolean(17, taskConfigModel.isFileArchive());
			ps.setString(18, taskConfigModel.getDateFormat() == null ? null : taskConfigModel.getDateFormat().trim());
			ps.setString(19, taskConfigModel.getMinutes() == null ? null : taskConfigModel.getMinutes().trim());
			ps.setString(20, taskConfigModel.getHours() == null ? null : taskConfigModel.getHours().trim());
			ps.setInt(21, taskConfigModel.getDays());
			ps.setInt(22, taskConfigModel.getMonths());
			ps.setBoolean(23, taskConfigModel.isScheduledIsActive());
			ps.setString(24, taskConfigModel.getMailFrom() == null ? null : taskConfigModel.getMailFrom().trim());
			ps.setString(25, taskConfigModel.getMailSubject());
			ps.setInt(26, taskConfigModel.getId());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to update Task Config.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - Nie można uaktualnić zadania.");
				return false;
			}

			ServerDAO serverDAO = new ServerDAOImpl();
			if (!(serverDAO.deleteServers(taskConfigModel.getId(), statusMessage)))
				return false;
			for (ServerDataModel serverModel : taskConfigModel.getServers()) {
				serverModel.setTaskID(taskConfigModel.getId());
				if (serverDAO.createServer(serverModel, statusMessage) == false)
					return false;
			}

			FileListDAO fileListDAO = new FileListDAOImpl();
			if (!(fileListDAO.deleteFileList(taskConfigModel.getId(), statusMessage)))
				return false;
			for (FileListDataModel fileListModel : taskConfigModel.getFileList()) {
				fileListModel.setTaskID(taskConfigModel.getId());
				if (fileListDAO.createFileList(fileListModel, statusMessage) == false)
					return false;
			}

			MailingListDAO mailingListDAO = new MailingListDAOImpl();
			if (!(mailingListDAO.deleteMailingList(taskConfigModel.getId(), statusMessage)))
				return false;
			for (MailingListDataModel mailingListModel : taskConfigModel.getMailingList()) {
				mailingListModel.setTaskID(taskConfigModel.getId());
				if (mailingListDAO.createMailingList(mailingListModel, statusMessage) == false)
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
		logger.info("Successfully updated Task");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("Zadanie zostało zaktualizowane.");
		return true;
	}

	@Override
	public List<TaskConfigDataModel> getAllTaskConfig(StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ID, SUBJECT_NAME, SUBJECT_ADDRESS, SUBJECT_LOGIN, SUBJECT_PASSWORD, SUBJECT_DIRECTORY, SUBJECT_EXCHANGE_PROTOCOL, SUBJECT_ENCRYPTION_KEY_ID, "
				+ "SUBJECT_MODE, SUBJECT_LOGIN_FORM, SUBJECT_LOGOUT_FORM, SUBJECT_POST_OPTIONS, SUBJECT_RESPONSE_STRING, SOURCE_FILE_MODE, "
				+ "DECOMPRESSION_METHOD, DECRYPTION_METHOD, DECRYPTION_KEY_ID, FILE_ARCHIVE, DATE_FORMAT, "
				+ "MINUTES, HOURS, DAYS, MONTHS, SCHEDULED_IS_ACTIVE, MAIL_FROM, MAIL_SUBJECT from TASK_CONFIG";

		List<TaskConfigDataModel> allTaskConfig = new ArrayList<TaskConfigDataModel>();

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {

				TaskConfigDataModel taskConfigModel = new TaskConfigDataModel();
				taskConfigModel.setId(rs.getInt("id"));
				taskConfigModel.setSubjectName(rs.getString("SUBJECT_NAME"));
				taskConfigModel.setSubjectAddress(rs.getString("SUBJECT_ADDRESS"));
				taskConfigModel.setSubjectLogin(rs.getString("SUBJECT_LOGIN"));
				taskConfigModel.setSubjectPassword(rs.getString("SUBJECT_PASSWORD"));
				taskConfigModel.setSubjectDirectory(rs.getString("SUBJECT_DIRECTORY"));
				taskConfigModel.setSubjectExchangeProtocol(rs.getString("SUBJECT_EXCHANGE_PROTOCOL"));
				taskConfigModel.setSubjectEncryptionKeyID(rs.getInt("SUBJECT_ENCRYPTION_KEY_ID"));
				taskConfigModel.setSubjectMode(rs.getString("SUBJECT_MODE"));
				taskConfigModel.setSubjectLoginForm(rs.getString("SUBJECT_LOGIN_FORM"));
				taskConfigModel.setSubjectLogoutForm(rs.getString("SUBJECT_LOGOUT_FORM"));
				taskConfigModel.setSubjectPostOptions(rs.getString("SUBJECT_POST_OPTIONS"));
				taskConfigModel.setSubjectResponseString(rs.getString("SUBJECT_RESPONSE_STRING"));

				ServerDAO serverDAO = new ServerDAOImpl();
				taskConfigModel.setServers(serverDAO.getServers(taskConfigModel.getId(), statusMessage));
				if (taskConfigModel.getServers() == null) {
					return null;
				}

				taskConfigModel.setSourceFileMode(rs.getString("SOURCE_FILE_MODE"));
				taskConfigModel.setDecompressionMethod(rs.getString("DECOMPRESSION_METHOD"));
				taskConfigModel.setDecryptionMethod(rs.getString("DECRYPTION_METHOD"));
				taskConfigModel.setDecryptionKeyID(rs.getInt("DECRYPTION_KEY_ID"));

				FileListDAO fileListDAO = new FileListDAOImpl();
				taskConfigModel.setFileList(fileListDAO.getFileList(taskConfigModel.getId(), statusMessage));
				if (taskConfigModel.getFileList() == null) {
					return null;
				}

				taskConfigModel.setFileArchive(rs.getBoolean("FILE_ARCHIVE"));
				taskConfigModel.setDateFormat(rs.getString("DATE_FORMAT"));

				taskConfigModel.setMinutes(rs.getString("MINUTES"));
				taskConfigModel.setHours(rs.getString("HOURS"));
				taskConfigModel.setDays(rs.getInt("DAYS"));
				taskConfigModel.setMonths(rs.getInt("MONTHS"));
				taskConfigModel.setScheduledIsActive(rs.getBoolean("SCHEDULED_IS_ACTIVE"));

				MailingListDAO mailingListDAO = new MailingListDAOImpl();
				taskConfigModel.setMailingList(mailingListDAO.getMailingList(taskConfigModel.getId(), statusMessage));
				if (taskConfigModel.getMailingList() == null) {
					return null;
				}

				taskConfigModel.setMailFrom(rs.getString("MAIL_FROM"));
				taskConfigModel.setMailSubject(rs.getString("MAIL_SUBJECT"));

				allTaskConfig.add(taskConfigModel);
			}
			if (allTaskConfig.isEmpty()) {
				logger.error("No Task Config Exists.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("Brak zadań.");
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

		return allTaskConfig;
	}

	@Override
	public Blob getTaskPGPFile(int id, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		Blob decryptionKey;
		String sql = "select DECRYPTION_KEY_FILE from TASK_CONFIG where ID = ?";

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				decryptionKey = (rs.getBlob("DECRYPTION_KEY_FILE"));
			} else {
				logger.error(String.format("Decryption Key with ID of %d is not found.", id));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage(String.format("Nie ma takiego klucza dekrypcji."));
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
		return decryptionKey;
	}
}
