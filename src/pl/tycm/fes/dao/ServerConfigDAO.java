package pl.tycm.fes.dao;

import pl.tycm.fes.model.ServerConfigDataModel;
import pl.tycm.fes.model.StatusMessage;

public interface ServerConfigDAO {

	public ServerConfigDataModel getServerConfig(StatusMessage statusMessage);
	
	public boolean updateServeConfig(ServerConfigDataModel serverConfigModel, StatusMessage statusMessage);
}
