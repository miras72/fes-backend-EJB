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
import pl.tycm.fes.model.EncryptionKeyDataModel;
import pl.tycm.fes.model.PrivateKeyDataModel;
import pl.tycm.fes.model.PublicKeyDataModel;
import pl.tycm.fes.model.StatusMessage;

@Stateless
@LocalBean
public class EncryptionKeyDAOImpl implements EncryptionKeyDAO {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public PrivateKeyDataModel getPrivateKey(int id, StatusMessage statusMessage) {

		PrivateKeyDataModel privateKeyModel = new PrivateKeyDataModel();
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ID, PRIVATE_KEY_NAME, PRIVATE_KEY_BINARY_FILE from ENCRYPTION_KEY where ID = ?";

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				privateKeyModel.setId(rs.getInt("ID"));
				privateKeyModel.setPrivateKeyName(rs.getString("PRIVATE_KEY_NAME"));
				privateKeyModel.setPrivateKeyBinaryFile(rs.getBytes("PRIVATE_KEY_BINARY_FILE"));
			} else {
				logger.error(String.format("Private Key with ID of %d is not found.", id));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage(String.format("Nie istnieje taki klucz prywatny."));
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

		return privateKeyModel;
	}

	@Override
	public boolean updatePrivateKey(PrivateKeyDataModel privateKeyModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update ENCRYPTION_KEY set PRIVATE_KEY_NAME = ?, PRIVATE_KEY_BINARY_FILE = ? where ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, privateKeyModel.getPrivateKeyName() == null ? null : privateKeyModel.getPrivateKeyName().trim());
			ps.setBytes(2, privateKeyModel.getPrivateKeyBinaryFile());
			ps.setInt(3, privateKeyModel.getId());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to upload Private Key.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - Nie można uaktualnić klucza prywatnego.");
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
		logger.info("Successfully Uploaded Private Key.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("Klucz prywatny został zaktualizowany.");
		return true;
	}

	@Override
	public PublicKeyDataModel getPublicKey(int id, StatusMessage statusMessage) {

		PublicKeyDataModel publicKeyModel = new PublicKeyDataModel();
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ID, PUBLIC_KEY_NAME, PUBLIC_KEY_BINARY_FILE from ENCRYPTION_KEY where ID = ?";

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				publicKeyModel.setId(rs.getInt("ID"));
				publicKeyModel.setPublicKeyName(rs.getString("PUBLIC_KEY_NAME"));
				publicKeyModel.setPublicKeyBinaryFile(rs.getBytes("PUBLIC_KEY_BINARY_FILE"));
			} else {
				logger.error(String.format("Public Key with ID of %d is not found.", id));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage(String.format("Nie istnieje taki klucz publiczny."));
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

		return publicKeyModel;
	}

	@Override
	public boolean updatePublicKey(PublicKeyDataModel publicKeyModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update ENCRYPTION_KEY set PUBLIC_KEY_NAME = ?, PUBLIC_KEY_BINARY_FILE = ? where ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, publicKeyModel.getPublicKeyName() == null ? null : publicKeyModel.getPublicKeyName().trim());
			ps.setBytes(2, publicKeyModel.getPublicKeyBinaryFile());
			ps.setInt(3, publicKeyModel.getId());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to upload Public Key.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - Nie można uaktualnić klucza publicznego.");
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
		logger.info("Successfully Uploaded Public Key.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("Klucz publiczny został zaktualizowany.");
		return true;
	}

	@Override
	public boolean deleteEncryptionKey(int id, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "delete from ENCRYPTION_KEY where ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error(String.format("Unable to DELETE Encryption Key with ID of %d.", id));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage(String.format("BŁĄD - Nie można usunąć Kluczy Enkrypcji."));
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
		logger.info(String.format("Successfully deleted Encryption Key with ID of %d.", id));
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage(String.format("Klucze Enkrypcji zostały usunięte."));
		return true;
	}

	@Override
	public List<EncryptionKeyDataModel> getAllEncryptionKeyName(StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ID, PRIVATE_KEY_NAME, PUBLIC_KEY_NAME from ENCRYPTION_KEY ORDER BY PRIVATE_KEY_NAME";

		List<EncryptionKeyDataModel> allEncryptionKey = new ArrayList<EncryptionKeyDataModel>();

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {

				EncryptionKeyDataModel encryptionKeyModel = new EncryptionKeyDataModel();
				encryptionKeyModel.setId(rs.getInt("id"));
				encryptionKeyModel.setPrivateKeyName(rs.getString("PRIVATE_KEY_NAME"));
				encryptionKeyModel.setPublicKeyName(rs.getString("PUBLIC_KEY_NAME"));
				allEncryptionKey.add(encryptionKeyModel);
			}
			if (allEncryptionKey.isEmpty()) {
				logger.error("No Encryption Key Status Exists...");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("Klucz Enkrypcji nie istnieje.");
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

		return allEncryptionKey;
	}

	@Override
	public boolean createEncryptionKey(EncryptionKeyDataModel encryptionKeyDataModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "insert into ENCRYPTION_KEY (PRIVATE_KEY_NAME, PRIVATE_KEY_BINARY_FILE, PUBLIC_KEY_NAME, PUBLIC_KEY_BINARY_FILE) values (?,?,?,?)";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, encryptionKeyDataModel.getPrivateKeyName() == null ? null : encryptionKeyDataModel.getPrivateKeyName().trim());
			ps.setBytes(2, encryptionKeyDataModel.getPrivateKeyBinaryFile());
			ps.setString(3, encryptionKeyDataModel.getPublicKeyName() == null ? null : encryptionKeyDataModel.getPublicKeyName().trim());
			ps.setBytes(4, encryptionKeyDataModel.getPublicKeyBinaryFile());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to create Encryption Keys.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - ENCRYP01: Nie można utworzyć kluczy enkrypcji.");
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
		logger.info("Successfully created Encryption Keys.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("ENCRYP02: Prawidłowo dodano klucze enkrypcji.");
		return true;
	}
}
