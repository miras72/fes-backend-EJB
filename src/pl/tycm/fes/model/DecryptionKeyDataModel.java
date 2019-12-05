package pl.tycm.fes.model;

import java.util.Arrays;

import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class DecryptionKeyDataModel {

	private int id;
	private String decryptionKeyName;	
	private byte[] decryptionKeyBinaryFile;

	@Override
	public String toString() {
		return "DecryptionKeyDataModel [id=" + id + ", decryptionKeyName=" + decryptionKeyName + ", decryptionKeyBinaryFile="
				+ Arrays.toString(decryptionKeyBinaryFile) + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDecryptionKeyName() {
		return decryptionKeyName;
	}

	@FormParam("decryptionFileName")
	public void setDecryptionKeyName(String decryptionKeyName) {
		this.decryptionKeyName = decryptionKeyName;
	}

	public byte[] getDecryptionKeyBinaryFile() {
		return decryptionKeyBinaryFile;
	}

	@FormParam("decryptionFile")
    @PartType("application/octet-stream")
	public void setDecryptionKeyBinaryFile(byte[] decryptionKeyBinaryFile) {
		this.decryptionKeyBinaryFile = decryptionKeyBinaryFile;
	}
}
