package pl.tycm.fes.bean;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import pl.tycm.fes.dao.EventDAO;
import pl.tycm.fes.dao.EventDAOImpl;
import pl.tycm.fes.model.EventDataModel;
import pl.tycm.fes.model.StatusMessage;
import pl.tycm.fes.util.MTTools;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PGPFileBean {

	private final Logger logger = Logger.getLogger(this.getClass());

	private EventDAO eventDAO = new EventDAOImpl();
	private EventDataModel eventModel = new EventDataModel();
	private StatusMessage statusMessage = new StatusMessage();

	public List<String> decryptPGPFile(String workingDirectory, byte[] decryptionKey, List<String> receiveFileList,
			int fileExchangeStatusID, ReportBean reportBean) {
		List<String> newReceiveFileList = new ArrayList<>();
		
		for (String fileName : receiveFileList) {

			String outputFileName = fileName + ".tmp";

			logger.info("Dekrypcja pliku: " + fileName + "...");
			eventModel.setFileExchangeStatusID(fileExchangeStatusID);
			eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Dekrypcja pliku: " + fileName + "...");
			eventDAO.createEvent(eventModel, statusMessage);

			InputStream keyIn = new ByteArrayInputStream(decryptionKey);

			FileInputStream in = null;
			FileOutputStream out = null;
			try {
				in = new FileInputStream(workingDirectory + File.separator + fileName);
				out = new FileOutputStream(workingDirectory + File.separator + outputFileName);

				char[] passwd = null;

				decryptFile(in, keyIn, passwd, workingDirectory, out);

				logger.info("Plik z dekryptowany.");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Plik z dekryptowany.");
				eventDAO.createEvent(eventModel, statusMessage);
				
				logger.info("Zmieniam nazwę pliku: " + outputFileName + " -> " + fileName + "...");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Zmieniam nazwę pliku: " + outputFileName + " -> " + fileName + "...");
				eventDAO.createEvent(eventModel, statusMessage);
				
				File sourceFile = new File(workingDirectory + File.separator + outputFileName);
				File destinationFile = new File(workingDirectory + File.separator + fileName);
				sourceFile.renameTo(destinationFile);
				logger.info("Nazwa pliku została zmieniona.");
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "INFO  " + "Nazwa pliku została zmieniona.");
				eventDAO.createEvent(eventModel, statusMessage);
				
				newReceiveFileList.add(fileName);
			} catch (PGPException ex) {
				logger.error(ex.getMessage());
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "ERROR " + ex.getMessage());
				eventDAO.createEvent(eventModel, statusMessage);
				
				reportBean.addReport("-> Błąd: Nie powiodła się operacja dekrypcji pliku: " + fileName);
				ex.printStackTrace();
			} catch (IllegalArgumentException ex) {
				logger.error(ex.getMessage());
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "ERROR " + ex.getMessage());
				eventDAO.createEvent(eventModel, statusMessage);
				
				reportBean.addReport("-> Błąd: Nie powiodła się operacja dekrypcji pliku: " + fileName);
				ex.printStackTrace();
			} catch (NoSuchProviderException ex) {
				logger.error(ex.getMessage());
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "ERROR " + ex.getMessage());
				eventDAO.createEvent(eventModel, statusMessage);
				
				reportBean.addReport("-> Błąd: Nie powiodła się operacja dekrypcji pliku: " + fileName);
				ex.printStackTrace();
			} catch (IOException ex) {
				logger.error(ex.getMessage());
				eventModel.setFileExchangeStatusID(fileExchangeStatusID);
				eventModel.setEventText(MTTools.getLogDate() + "ERROR " + ex.getMessage());
				eventDAO.createEvent(eventModel, statusMessage);
				
				reportBean.addReport("-> Bład: Nie powiodła się operacja dekrypcji pliku: " + fileName);
				ex.printStackTrace();
			} finally {
				try {
					in.close();
				} catch (IOException ex) {
					logger.error(ex.getMessage());
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "ERROR " + ex.getMessage());
					eventDAO.createEvent(eventModel, statusMessage);
					
					ex.printStackTrace();
				}
				try {
					out.close();
				} catch (IOException ex) {
					logger.error(ex.getMessage());
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "ERROR " + ex.getMessage());
					eventDAO.createEvent(eventModel, statusMessage);
					
					ex.printStackTrace();
				}
				try {
					keyIn.close();
				} catch (IOException ex) {
					logger.error(ex.getMessage());
					eventModel.setFileExchangeStatusID(fileExchangeStatusID);
					eventModel.setEventText(MTTools.getLogDate() + "ERROR " + ex.getMessage());
					eventDAO.createEvent(eventModel, statusMessage);
					
					ex.printStackTrace();
				}
			}
	
		}
		return newReceiveFileList;
	}

	/**
	 * decrypt the passed in message stream
	 */
	private void decryptFile(InputStream in, InputStream keyIn, char[] passwd, String directory, OutputStream out)
			throws IOException, NoSuchProviderException, PGPException, IllegalArgumentException {

		Security.addProvider(new BouncyCastleProvider());
		in = PGPUtil.getDecoderStream(in);

		try {
			JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(in);
			PGPEncryptedDataList enc;

			Object o = pgpF.nextObject();
			//
			// the first object might be a PGP marker packet.
			//
			if (o instanceof PGPEncryptedDataList) {
				enc = (PGPEncryptedDataList) o;
			} else {
				enc = (PGPEncryptedDataList) pgpF.nextObject();
			}

			//
			// find the secret key
			//
			Iterator<?> it = enc.getEncryptedDataObjects();
			PGPPrivateKey sKey = null;
			PGPPublicKeyEncryptedData pbe = null;
			PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn),
					new JcaKeyFingerprintCalculator());

			while (sKey == null && it.hasNext()) {
				pbe = (PGPPublicKeyEncryptedData) it.next();

				sKey = this.findSecretKey(pgpSec, pbe.getKeyID(), passwd);
			}

			if (sKey == null) {
				throw new IllegalArgumentException("secret key for message not found.");
			}

			InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));

			JcaPGPObjectFactory plainFact = new JcaPGPObjectFactory(clear);

			PGPCompressedData cData = (PGPCompressedData) plainFact.nextObject();

			InputStream compressedStream = new BufferedInputStream(cData.getDataStream());
			JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(compressedStream);

			Object message = pgpFact.nextObject();

			if (message instanceof PGPLiteralData) {
				PGPLiteralData ld = (PGPLiteralData) message;
				InputStream unc = ld.getInputStream();

				byte[] bytesArray = new byte[16 * 4096];
				int bytesRead = -1;
				int progressSign = 0;
				while ((bytesRead = unc.read(bytesArray)) != -1) {
					out.write(bytesArray, 0, bytesRead);
					switch (progressSign) {
					case 0:
						System.out.print("\r|");
						progressSign++;
						break;
					case 1:
						System.out.print("\r/");
						progressSign++;
						break;
					case 2:
						System.out.print("\r-");
						progressSign++;
						break;
					case 3:
						System.out.print("\r\\");
						progressSign = 0;
						break;
					}
				}
				//System.out.println("\rOK");
			} else if (message instanceof PGPOnePassSignatureList) {
				throw new PGPException("encrypted message contains a signed message - not literal data.");
			} else {
				throw new PGPException("message is not a simple encrypted file - type unknown.");
			}

			if (pbe.isIntegrityProtected()) {
				if (!pbe.verify()) {
					throw new PGPException("Błąd: Kontroli integralności pliku");
				} else {
					logger.info("OK: Kontrola integralności pliku");
				}
			} else {
				// logger.info("Brak kontroli integralności pliku");
			}
		} catch (PGPException e) {
			logger.error(e);
			if (e.getUnderlyingException() != null) {
				e.getUnderlyingException().printStackTrace();
			}
		}
	}

	/**
	 * Search a secret key ring collection for a secret key corresponding to keyID
	 * if it exists.
	 * 
	 * @param pgpSec
	 *            a secret key ring collection.
	 * @param keyID
	 *            keyID we want.
	 * @param pass
	 *            passphrase to decrypt secret key with.
	 * @return the private key.
	 * @throws PGPException
	 * @throws NoSuchProviderException
	 */
	private PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass)
			throws PGPException, NoSuchProviderException {
		PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

		if (pgpSecKey == null) {
			return null;
		}
		return pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pass));
	}

}
