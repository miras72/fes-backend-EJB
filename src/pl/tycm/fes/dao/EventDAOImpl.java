package pl.tycm.fes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import pl.tycm.fes.db.ConnectionManager;
import pl.tycm.fes.model.EventDataModel;
import pl.tycm.fes.model.StatusMessage;

@Stateless
@LocalBean
public class EventDAOImpl implements EventDAO {
	
	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public List<EventDataModel> getEvents(int fileExchangeStatusID, StatusMessage statusMessage) {

		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "select ID, FILE_EXCHANGE_STATUS_ID, EVENT_TEXT from EVENTS where FILE_EXCHANGE_STATUS_ID = ? ORDER BY ID";

		List<EventDataModel> events = new ArrayList<EventDataModel>();
		
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, fileExchangeStatusID);
			rs = ps.executeQuery();

			while (rs.next()) {
				EventDataModel eventModel = new EventDataModel();
				eventModel.setId(rs.getInt("ID"));
				eventModel.setFileExchangeStatusID(rs.getInt("FILE_EXCHANGE_STATUS_ID"));
				eventModel.setEventText(rs.getString("EVENT_TEXT"));
				events.add(eventModel);
			}
			if (events.isEmpty()) {
				logger.info(
						String.format("Event with FILE_EXCHANGE_STATUS_ID of %d is not found.", fileExchangeStatusID));
				EventDataModel eventModel = new EventDataModel();
				eventModel.setId(0);
				eventModel.setFileExchangeStatusID(0);
				eventModel.setEventText("Brak Zdarzeń");
				events.add(eventModel);

				/*
				 * statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				 * statusMessage.setMessage(String.format("Event with ID of %d is not found.",
				 * fileExchangeStatusID)); return null;
				 */
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
		return events;
	}

	@Override
	public boolean deleteEvents(int fileExchangeStatusID, StatusMessage statusMessage) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
	public boolean createEvent(EventDataModel eventModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "insert into EVENTS (FILE_EXCHANGE_STATUS_ID, EVENT_TEXT) values (?,?)";

		if (eventModel.getFileExchangeStatusID() == -1) {
			logger.error("Unable to create Events.");
			statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
			statusMessage.setMessage("BŁĄD - Nie można dodać zdarzenia.");
			return false;
		}
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, eventModel.getFileExchangeStatusID());
			ps.setString(2, eventModel.getEventText());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to create Events.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - Nie można dodać zdarzenia.");
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
		statusMessage.setMessage("Nowe zdzarzenie zostało dodane.");
		return true;
	}

	@Override
	public boolean updateEvent(EventDataModel eventModel, StatusMessage statusMessage) {
		// TODO Auto-generated method stub
		return false;
	}

}
