package pl.tycm.fes.model;

import java.util.Arrays;

import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class PrivateKeyDataModel {

	private int id;
	private String privateKeyName;	
	private byte[] privateKeyBinaryFile;

	@Override
	public String toString() {
		return "PrivateKeyDataModel [id=" + id + ", privateKeyName=" + privateKeyName + ", privateKeyBinaryFile="
				+ Arrays.toString(privateKeyBinaryFile) + "]";
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

	public byte[] getPrivateKeyBinaryFile() {
		return privateKeyBinaryFile;
	}

	@FormParam("privateFile")
    @PartType("application/octet-stream")
	public void setPrivateKeyBinaryFile(byte[] privateKeyBinaryFile) {
		this.privateKeyBinaryFile = privateKeyBinaryFile;
	}
}
