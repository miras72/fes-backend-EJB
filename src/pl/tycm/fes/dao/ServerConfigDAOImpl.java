package pl.tycm.fes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import pl.tycm.fes.db.ConnectionManager;
import pl.tycm.fes.model.ServerConfigDataModel;
import pl.tycm.fes.model.StatusMessage;

@Stateless
@LocalBean
public class ServerConfigDAOImpl implements ServerConfigDAO {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public ServerConfigDataModel getServerConfig(StatusMessage statusMessage) {

		ServerConfigDataModel serverConfigModel = new ServerConfigDataModel();
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select MAIL_SERVER, WORK_DIRECTORY, ARCH_DIRECTORY from SERVER_CONFIG";

		try {
			c = ConnectionManager.getConnection();
			//c = dataSource.getConnection();
			ps = c.prepareStatement(sql);
			rs = ps.executeQuery();
			if (rs.next()) {
				serverConfigModel.setMailServer(rs.getString("MAIL_SERVER"));
				serverConfigModel.setWorkDirectory(rs.getString("WORK_DIRECTORY"));
				serverConfigModel.setArchDirectory(rs.getString("ARCH_DIRECTORY"));

			} else {
				logger.error("Server configuration not found.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("Brak konfiguracji Serwera");
				return null;
			}

		} catch (SQLException e) {
			logger.fatal(e.getMessage());
			statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
			statusMessage.setMessage(e.getMessage());
			return null;
		} finally {
			//try { if (rs != null) rs.close(); } catch (Exception e) {e.printStackTrace(); logger.error(e.getMessage(), e);}
			//try { if (ps != null) ps.close(); } catch (Exception e) {e.printStackTrace(); logger.error(e.getMessage(), e);}
			//try { if (c != null) c.close(); } catch (Exception e) {e.printStackTrace(); logger.error(e.getMessage(), e);}
			ConnectionManager.close(rs);
			ConnectionManager.close(ps);
			ConnectionManager.close(c);
		}

		return serverConfigModel;
	}

	@Override
	public boolean updateServeConfig(ServerConfigDataModel serverConfigModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update SERVER_CONFIG set MAIL_SERVER = ?, WORK_DIRECTORY = ?, ARCH_DIRECTORY = ?";
		try {
			c = ConnectionManager.getConnection();
			//c = dataSource.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, serverConfigModel.getMailServer() == null ? null : serverConfigModel.getMailServer().trim());
			ps.setString(2, serverConfigModel.getWorkDirectory() == null ? null : serverConfigModel.getWorkDirectory().trim());
			ps.setString(3, serverConfigModel.getArchDirectory() == null ? null : serverConfigModel.getArchDirectory().trim());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to update Server Config.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - SERWER01: Nie można zapisać konfiguracji Serwera.");
				return false;
			}
		} catch (SQLException e) {
			logger.fatal(e.getMessage());
			statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
			statusMessage.setMessage(e.getMessage());
			return false;
		} finally {
			//try { if (rs != null) rs.close(); } catch (Exception e) {e.printStackTrace(); logger.error(e.getMessage(), e);}
			//try { if (ps != null) ps.close(); } catch (Exception e) {e.printStackTrace(); logger.error(e.getMessage(), e);}
			//try { if (c != null) c.close(); } catch (Exception e) {e.printStackTrace(); logger.error(e.getMessage(), e);}
			ConnectionManager.close(rs);
			ConnectionManager.close(ps);
			ConnectionManager.close(c);
		}
		logger.info("Successfully uptated Server Config.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("SERWER02: Prawidłowo zaktualizowano konfigurację Serwera.");
		return true;
	}

}
