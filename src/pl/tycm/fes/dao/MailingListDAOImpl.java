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
import pl.tycm.fes.model.MailingListDataModel;
import pl.tycm.fes.model.StatusMessage;

@Stateless
@LocalBean
public class MailingListDAOImpl implements MailingListDAO {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public List<MailingListDataModel> getMailingList(int id, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "select ID, RECIPIENT_NAME from MAILING_LIST where TASK_ID = ?";

		List<MailingListDataModel> allMailingList = new ArrayList<MailingListDataModel>();

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while (rs.next()) {
				MailingListDataModel mailingList = new MailingListDataModel();
				mailingList.setId(rs.getInt("id"));
				mailingList.setRecipientName(rs.getString("RECIPIENT_NAME"));
				allMailingList.add(mailingList);
			}
			if (allMailingList.isEmpty()) {
				logger.error(String.format("Recipents with ID of %d is not found.", id));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - MAIL01: Brak danych.");
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
		return allMailingList;
	}

	@Override
	public boolean deleteMailingList(int taskID, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "delete from MAILING_LIST where TASK_ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, taskID);
			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error(String.format("Unable to DELETE Mailing List with taskID of %d.", taskID));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - MAIL02: Nie można usunąć danych.");
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
		logger.info(String.format("Successfully deleted Mailing List with taskID of %d.", taskID));
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("Dane zostały prawidłowo usunięte.");
		return true;
	}

	@Override
	public boolean createMailingList(MailingListDataModel mailingList, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "insert into MAILING_LIST (RECIPIENT_NAME, TASK_ID) values (?,?)";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, mailingList.getRecipientName() == null ? null : mailingList.getRecipientName().trim());
			ps.setInt(2, mailingList.getTaskID());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to create Mailing List.");
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
		logger.info("Successfully created Mailing List.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("MAIL03: Prawidłowo dodano dane.");
		return true;
	}

	@Override
	public boolean updateMailingList(MailingListDataModel mailingList, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update MAILING_LIST set RECIPIENT_NAME = ? where ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, mailingList.getRecipientName() == null ? null : mailingList.getRecipientName().trim());
			ps.setInt(2, mailingList.getId());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to update Mailing List.");
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
		logger.info("Successfully uptated Mailing List.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("FILE05: Prawidłowo zaktualizowano dane.");
		return true;
	}
}
