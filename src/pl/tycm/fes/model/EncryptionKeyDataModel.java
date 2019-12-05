package pl.tycm.fes.model;

import java.util.Arrays;

import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class EncryptionKeyDataModel {

	private int id;
	private String privateKeyName;
	private byte[] privateKeyBinaryFile;
	private String publicKeyName;
	private byte[] publicKeyBinaryFile;

	@Override
	public String toString() {
		return "EncryptionKeyDataModel [id=" + id + ", privateKeyName=" + privateKeyName + ", privateKeyBinaryFile="
				+ Arrays.toString(privateKeyBinaryFile) + ", publicKeyName=" + publicKeyName + ", publicKeyBinaryFile="
				+ Arrays.toString(publicKeyBinaryFile) + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPrivateKeyName() {
		return privateKeyName;
	}

	@FormParam("privateFileName")
	public void setPrivateKeyName(String privateKeyName) {
		this.privateKeyName = privateKeyName;
	}

	public String getPublicKeyName() {
		return publicKeyName;
	}

	@FormParam("publicFileName")
	public void setPublicKeyName(String publicKeyName) {
		this.publicKeyName = publicKeyName;
	}
	
	public byte[] getPrivateKeyBinaryFile() {
		return privateKeyBinaryFile;
	}

	@FormParam("privateFile")
    @PartType("application/octet-stream")
	public void setPrivateKeyBinaryFile(byte[] privateKeyBinaryFile) {
		this.privateKeyBinaryFile = privateKeyBinaryFile;
	}
	
	public byte[] getPublicKeyBinaryFile() {
		return publicKeyBinaryFile;
	}

	@FormParam("publicFile")
    @PartType("application/octet-stream")
	public void setPublicKeyBinaryFile(byte[] publicKeyBinaryFile) {
		this.publicKeyBinaryFile = publicKeyBinaryFile;
	}
}
