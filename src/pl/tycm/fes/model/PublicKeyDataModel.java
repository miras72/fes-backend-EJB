package pl.tycm.fes.model;

import java.util.Arrays;

import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class PublicKeyDataModel {

	private int id;
	private String publicKeyName;
	private byte[] publicKeyBinaryFile;

	@Override
	public String toString() {
		return "PublicKeyDataModel [id=" + id + ", publicKeyName=" + publicKeyName + ", publicKeyBinaryFile="
				+ Arrays.toString(publicKeyBinaryFile) + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPublicKeyName() {
		return publicKeyName;
	}

	@FormParam("publicFileName")
	public void setPublicKeyName(String publicKeyName) {
		this.publicKeyName = publicKeyName;
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
