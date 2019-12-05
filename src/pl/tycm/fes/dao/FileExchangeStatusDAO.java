package pl.tycm.fes.dao;

import pl.tycm.fes.model.FileExchangeStatusDataModel;
import pl.tycm.fes.model.StatusMessage;

public interface FileExchangeStatusDAO {

	public FileExchangeStatusDataModel getFileExchangeStatus(int taskID, String eventDate, StatusMessage statusMessage);

	public boolean deleteFileExchangeStatus(int id, StatusMessage statusMessage);

	public int createFileExchangeStatus(FileExchangeStatusDataModel fileExchangeStatusModel, StatusMessage statusMessage);

	public boolean updateFileExchangeStatus(FileExchangeStatusDataModel fileExchangeStatusModel, StatusMessage statusMessage);
}
