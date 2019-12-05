package pl.tycm.fes.dao;

import java.util.List;

import pl.tycm.fes.model.ServerDataModel;
import pl.tycm.fes.model.StatusMessage;

public interface ServerDAO {

	public List<ServerDataModel> getServers(int taskID, StatusMessage statusMessage);

	public boolean deleteServers(int taskID, StatusMessage statusMessage);

	public boolean createServer(ServerDataModel serverDataModel, StatusMessage statusMessage);

	public boolean updateServer(ServerDataModel serverDataModel, StatusMessage statusMessage);
}
