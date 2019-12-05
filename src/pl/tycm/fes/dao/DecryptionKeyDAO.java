package pl.tycm.fes.dao;

import java.util.List;

import pl.tycm.fes.model.DecryptionKeyDataModel;
import pl.tycm.fes.model.StatusMessage;

public interface DecryptionKeyDAO {

	public DecryptionKeyDataModel getDecryptionKey(int id, StatusMessage statusMessage);
	
	public boolean updateDecryptionKey(DecryptionKeyDataModel decryptionKeyModel, StatusMessage statusMessage);
	
	public boolean createDecryptionKey(DecryptionKeyDataModel decryptionKeyDataModel, StatusMessage statusMessage);

	public boolean deleteDecryptionKey(int id, StatusMessage statusMessage);

	public List<DecryptionKeyDataModel> getAllDecryptionKeyName(StatusMessage statusMessage);
}
