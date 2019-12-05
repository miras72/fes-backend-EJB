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
import pl.tycm.fes.model.ServerDataModel;
import pl.tycm.fes.model.StatusMessage;

@Stateless
@LocalBean
public class ServerDAOImpl implements ServerDAO {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public List<ServerDataModel> getServers(int taskID, StatusMessage statusMessage) {

		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "select ID, TASK_ID, SERVER_ADDRESS, SERVER_LOGIN, SERVER_PASSWORD, SERVER_DIRECTORY from SERVERS where TASK_ID = ? ORDER BY ID";

		List<ServerDataModel> allServer = new ArrayList<ServerDataModel>();

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, taskID);
			rs = ps.executeQuery();
			while (rs.next()) {
				ServerDataModel serverDataModel = new ServerDataModel();
				serverDataModel.setId(rs.getInt("ID"));
				serverDataModel.setTaskID(rs.getInt("TASK_ID"));
				serverDataModel.setServerAddress(rs.getString("SERVER_ADDRESS"));
				serverDataModel.setServerLogin(rs.getString("SERVER_LOGIN"));
				serverDataModel.setServerPassword(rs.getString("SERVER_PASSWORD"));
				serverDataModel.setServerDirectory(rs.getString("SERVER_DIRECTORY"));
				allServer.add(serverDataModel);
			}
			if (allServer.isEmpty()) {
				logger.error(String.format("Servers with ID of %d is not found.", taskID));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - SER01: Brak danych.");
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
		return allServer;
	}

	@Override
	public boolean deleteServers(int taskID, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "delete from SERVERS where TASK_ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, taskID);
			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error(String.format("Unable to DELETE Servers with taskID of %d.", taskID));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - SER02: Nie można usunąć danych.");
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
		logger.info(String.format("Successfully deleted Servers with taskID of %d.", taskID));
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("Dane zostały prawidłowo usunięte.");
		return true;
	}

	@Override
	public boolean createServer(ServerDataModel serverDataModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "insert into SERVERS (SERVER_ADDRESS, SERVER_LOGIN, SERVER_PASSWORD, SERVER_DIRECTORY, TASK_ID) values (?,?,?,?,?)";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, serverDataModel.getServerAddress() == null ? null : serverDataModel.getServerAddress().trim());
			ps.setString(2, serverDataModel.getServerLogin() == null ? null : serverDataModel.getServerLogin().trim());
			ps.setString(3, serverDataModel.getServerPassword() == null ? null : serverDataModel.getServerPassword().trim());
			ps.setString(4, serverDataModel.getServerDirectory() == null ? null : serverDataModel.getServerDirectory().trim());
			ps.setInt(5, serverDataModel.getTaskID());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to create Server.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - SER03: Nie można utworzyć zadania.");
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
		logger.info("Successfully created Servers.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("SER03: Prawidłowo dodano dane.");
		return true;
	}

	@Override
	public boolean updateServer(ServerDataModel serverDataModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update SERVERS set SERVER_ADDRESS = ?, SERVER_LOGIN = ?, SERVER_PASSWORD = ?, SERVER_DIRECTORY = ? "
				+ "where ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, serverDataModel.getServerAddress() == null ? null : serverDataModel.getServerAddress().trim());
			ps.setString(2, serverDataModel.getServerLogin() == null ? null : serverDataModel.getServerLogin().trim());
			ps.setString(3, serverDataModel.getServerPassword() == null ? null : serverDataModel.getServerPassword().trim());
			ps.setString(4, serverDataModel.getServerDirectory() == null ? null : serverDataModel.getServerDirectory().trim());
			ps.setInt(5, serverDataModel.getId());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to update Server.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - SER04: Nie można uaktualnić zadania.");
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
		logger.info("Successfully uptated Servers.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("SER05: Prawidłowo zaktualizowano dane.");
		return true;
	}
}
