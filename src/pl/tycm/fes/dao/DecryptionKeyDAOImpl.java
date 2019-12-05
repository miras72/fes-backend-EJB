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
import pl.tycm.fes.model.DecryptionKeyDataModel;
import pl.tycm.fes.model.StatusMessage;

@Stateless
@LocalBean
public class DecryptionKeyDAOImpl implements DecryptionKeyDAO{
	
	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public DecryptionKeyDataModel getDecryptionKey(int id, StatusMessage statusMessage) {
		DecryptionKeyDataModel decryptionKeyModel = new DecryptionKeyDataModel();
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ID, DECRYPTION_KEY_NAME, DECRYPTION_KEY_BINARY_FILE from DECRYPTION_KEY where ID = ?";

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				decryptionKeyModel.setId(rs.getInt("ID"));
				decryptionKeyModel.setDecryptionKeyName(rs.getString("DECRYPTION_KEY_NAME"));
				decryptionKeyModel.setDecryptionKeyBinaryFile(rs.getBytes("DECRYPTION_KEY_BINARY_FILE"));
			} else {
				logger.error(String.format("Decryption Key with ID of %d is not found.", id));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage(String.format("Nie istnieje taki klucz dekrypcji."));
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

		return decryptionKeyModel;
	}

	@Override
	public boolean updateDecryptionKey(DecryptionKeyDataModel decryptionKeyModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update DECRYPTION_KEY set DECRYPTION_KEY_NAME = ?, DECRYPTION_KEY_BINARY_FILE = ? where ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, decryptionKeyModel.getDecryptionKeyName() == null ? null : decryptionKeyModel.getDecryptionKeyName().trim());
			ps.setBytes(2, decryptionKeyModel.getDecryptionKeyBinaryFile());
			ps.setInt(3, decryptionKeyModel.getId());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to upload Decryption Key.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - Nie można uaktualnić klucza dekrypcji.");
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
		statusMessage.setMessage("Klucz dekrypcji został zaktualizowany.");
		return true;
	}

	@Override
	public boolean createDecryptionKey(DecryptionKeyDataModel decryptionKeyModel, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "insert into DECRYPTION_KEY (DECRYPTION_KEY_NAME, DECRYPTION_KEY_BINARY_FILE) values (?,?)";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setString(1, decryptionKeyModel.getDecryptionKeyName() == null ? null : decryptionKeyModel.getDecryptionKeyName().trim());
			ps.setBytes(2, decryptionKeyModel.getDecryptionKeyBinaryFile());

			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error("Unable to create Decryption Key.");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("BŁĄD - ENCRYP01: Nie można utworzyć kluczy dekrypcji.");
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
		logger.info("Successfully created Decryption Key.");
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage("ENCRYP02: Prawidłowo dodano klucze dekrypcji.");
		return true;
	}

	@Override
	public boolean deleteDecryptionKey(int id, StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "delete from DECRYPTION_KEY where ID = ?";
		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			int rows = ps.executeUpdate();
			if (rows == 0) {
				logger.error(String.format("Unable to DELETE Decryption Key with ID of %d.", id));
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage(String.format("BŁĄD - Nie można usunąć Klucza Dekrypcji."));
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
		logger.info(String.format("Successfully deleted Decryption Key with ID of %d.", id));
		statusMessage.setStatus(Status.OK.getStatusCode());
		statusMessage.setMessage(String.format("Klucz Dekrypcji został usunięty."));
		return true;
	}

	@Override
	public List<DecryptionKeyDataModel> getAllDecryptionKeyName(StatusMessage statusMessage) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ID, DECRYPTION_KEY_NAME from DECRYPTION_KEY ORDER BY DECRYPTION_KEY_NAME";

		List<DecryptionKeyDataModel> allDecryptionKey = new ArrayList<DecryptionKeyDataModel>();

		try {
			c = ConnectionManager.getConnection();
			ps = c.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {

				DecryptionKeyDataModel decryptionKeyModel = new DecryptionKeyDataModel();
				decryptionKeyModel.setId(rs.getInt("id"));
				decryptionKeyModel.setDecryptionKeyName(rs.getString("DECRYPTION_KEY_NAME"));
				allDecryptionKey.add(decryptionKeyModel);
			}
			if (allDecryptionKey.isEmpty()) {
				logger.error("No Decryption Key Status Exists...");
				statusMessage.setStatus(Status.NOT_FOUND.getStatusCode());
				statusMessage.setMessage("Klucz Dekrypcji nie istnieje.");
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

		return allDecryptionKey;
	}

}
