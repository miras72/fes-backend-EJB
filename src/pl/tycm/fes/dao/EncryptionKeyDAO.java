package pl.tycm.fes.dao;

import java.util.List;

import pl.tycm.fes.model.EncryptionKeyDataModel;
import pl.tycm.fes.model.PrivateKeyDataModel;
import pl.tycm.fes.model.PublicKeyDataModel;
import pl.tycm.fes.model.StatusMessage;

public interface EncryptionKeyDAO {

	public PrivateKeyDataModel getPrivateKey(int id, StatusMessage statusMessage);
	
	public boolean updatePrivateKey(PrivateKeyDataModel privateKeyModel, StatusMessage statusMessage);
	
	public PublicKeyDataModel getPublicKey(int id, StatusMessage statusMessage);
	
	public boolean updatePublicKey(PublicKeyDataModel publicKeyModel, StatusMessage statusMessage);
	
	public boolean createEncryptionKey(EncryptionKeyDataModel encryptionKeyDataModel, StatusMessage statusMessage);

	public boolean deleteEncryptionKey(int id, StatusMessage statusMessage);

	public List<EncryptionKeyDataModel> getAllEncryptionKeyName(StatusMessage statusMessage);
}
