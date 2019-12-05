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
import pl.tycm.fes.model.FileListDataModel;
import pl.tycm.fes.model.StatusMessage;

@Stateless
@LocalBean
public class FileListDAOImpl implements FileListDAO {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public List<FileListDataModel> getFileList(int taskID, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "select ID, TASK_ID, FILE_NAME from FILE_LIST where TASK_ID = ? ORDER BY ID";

		List<FileListDataModel> allFileList = new ArrayList<FileListDataModel>();

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, taskID);
			rs = ps.executeQuery();
			while (rs.next()) {
				FileListDataModel fileList = new FileListDataModel();
				fileList.setId(rs.getInt("ID"));
				fileList.setTaskID(rs.getInt("TASK_ID"));
				fileList.setFileName(rs.getString("FILE_NAME"));
				allFileList.add(fileList);
			}
			if (allFileList.isEmpty()) {
				logger.error(String.format("File List with ID of %d is not found.", taskID));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - FILE01: Brak danych.");
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
		return allFileList;
	}

	@Override
	public boolean deleteFileList(int taskID, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "delete from FILE_LIST where TASK_ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, taskID);
			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error(String.format("Unable to DELETE File List with taskID of %d.", taskID));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - FILE02: Nie można usunąć danych.");
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
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("Dane zostały prawidłowo usunięte.");
		return true;
	}

	@Override
	public boolean createFileList(FileListDataModel fileList, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "insert into FILE_LIST (FILE_NAME, TASK_ID) values (?,?)";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, fileList.getFileName() == null ? null : fileList.getFileName().trim());
			ps.setInt(2, fileList.getTaskID());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to create File List.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - FILE03: Nie można utworzyć zadania.");
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
		logger.info("Successfully created File List.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("FILE03: Prawidłowo dodano dane.");
		return true;
	}

	@Override
	public boolean updateFileList(FileListDataModel fileList, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update FILE_LIST set FILE_NAME = ? where ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, fileList.getFileName() == null ? null : fileList.getFileName().trim());
			ps.setInt(2, fileList.getId());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to update File List.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - FILE04: Nie można uaktualnić zadania.");
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
		logger.info("Successfully uptated File List.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("FILE05: Prawidłowo zaktualizowano dane.");
		return true;
	}
}
