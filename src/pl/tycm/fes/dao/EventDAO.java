package pl.tycm.fes.dao;

import java.util.List;

import pl.tycm.fes.model.EventDataModel;
import pl.tycm.fes.model.StatusMessage;

public interface EventDAO {

	public List<EventDataModel> getEvents(int fileExchangeStatusID, StatusMessage statusMessage);

	public boolean deleteEvents(int fileExchangeStatusID, StatusMessage statusMessage);

	public boolean createEvent(EventDataModel eventModel, StatusMessage statusMessage);

	public boolean updateEvent(EventDataModel eventModel, StatusMessage statusMessage);
}
