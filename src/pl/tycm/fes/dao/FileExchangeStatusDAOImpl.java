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
import pl.tycm.fes.model.FileExchangeStatusDataModel;
import pl.tycm.fes.model.StatusMessage;

@Stateless
@LocalBean
public class FileExchangeStatusDAOImpl implements FileExchangeStatusDAO {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public FileExchangeStatusDataModel getFileExchangeStatus(int taskID, String eventDateTime, StatusMessage statusMessage) {

		FileExchangeStatusDataModel fileExchangeStatusModel = new FileExchangeStatusDataModel();
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<EventDataModel> events = new ArrayList<EventDataModel>();
		
		String	sql = "select ID, TASK_ID, EVENT_DATE_TIME from FILE_EXCHANGE_STATUS where TASK_ID = ? and EVENT_DATE_TIME LIKE ? ORDER BY ID";

		try {
			c = ConnectionManager.getConnection();
			
			ps = c.prepareStatement(sql);
			ps.setInt(1, taskID);
			ps.setString(2, eventDateTime + "%");
			rs = ps.executeQuery();
			if(!rs.next()) {
				fileExchangeStatusModel.setId(1);
				fileExchangeStatusModel.setTaskID(taskID);
				fileExchangeStatusModel.setEventDateTime(eventDateTime);
				EventDataModel eventModel = new EventDataModel();
				eventModel.setId(1);
				eventModel.setFileExchangeStatusID(1);
				eventModel.setEventText("Brak Zdarzeń");
				events.add(eventModel);
				fileExchangeStatusModel.setEvents(events);
				return fileExchangeStatusModel;
			} else {
				fileExchangeStatusModel.setId(rs.getInt("id"));
				fileExchangeStatusModel.setTaskID(rs.getInt("TASK_ID"));
				fileExchangeStatusModel.setEventDateTime(rs.getString("EVENT_DATE_TIME"));
				
				EventDAO eventDAO = new EventDAOImpl();
				events.addAll(eventDAO.getEvents(fileExchangeStatusModel.getId(), statusMessage));
			}
			
			while (rs.next()) {

				fileExchangeStatusModel.setId(rs.getInt("id"));
				fileExchangeStatusModel.setTaskID(rs.getInt("TASK_ID"));
				fileExchangeStatusModel.setEventDateTime(rs.getString("EVENT_DATE_TIME"));

				EventDAO eventDAO = new EventDAOImpl();
				events.addAll(eventDAO.getEvents(fileExchangeStatusModel.getId(), statusMessage));
				// fileExchangeStatusModel.setEvents(eventDAO.getEvents(fileExchangeStatusModel.getId(), statusMessage));
				/*if (fileExchangeStatusModel.getEvents() == null) {
					return null;
				}*/
			}
			fileExchangeStatusModel.setEvents(events);

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

		return fileExchangeStatusModel;
	}

	@Override
	public boolean deleteFileExchangeStatus(int id, StatusMessage statusMessage) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
	public int createFileExchangeStatus(FileExchangeStatusDataModel fileExchangeStatusModel,
			StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int autoID = -1;
		String sql = "insert into FILE_EXCHANGE_STATUS (EVENT_DATE_TIME, TASK_ID) values (?,?)";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql, new String[] { "ID" });
			ps.setString(1, fileExchangeStatusModel.getEventDateTime());
			ps.setInt(2, fileExchangeStatusModel.getTaskID());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to create File Exchange Status.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - Nie można utworzyć zadania.");
				return autoID;
			}

			rs = ps.getGeneratedKeys();

			if (rs.next()) {
				autoID = rs.getInt(1);
				/*EventDAO eventDAO = new EventDAOImpl();
				EventDataModel eventModel = new EventDataModel();
				eventModel.setFileExchangeStatusID(autoID);
				eventModel.setEventText("-");
				if (eventDAO.createEvent(eventModel, statusMessage) == false)
					return -1;*/
			}
		} catch (SQLException e) {
			logger.fatal(e.getMessage());
			statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
			statusMessage.setMessage(e.getMessage());
			return autoID;
		} finally {
			ConnectionManager.close(rs);
			ConnectionManager.close(ps);
			ConnectionManager.close(c);
		}
		return autoID;
	}

	@Override
	public boolean updateFileExchangeStatus(FileExchangeStatusDataModel fileExchangeStatusModel,
			StatusMessage statusMessage) {
		// TODO Auto-generated method stub
		return false;
	}

}
