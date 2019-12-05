package pl.tycm.fes.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import pl.tycm.fes.model.FileListDataModel;

public class MTTools {

	public static List<FileListDataModel> getConvertFileList(List<FileListDataModel> fileList, String filedate,
			String datePattern, String exchangeProtocol) {

		final Logger logger = Logger.getLogger("MTTools");

		List<FileListDataModel> convertFileList = new ArrayList<FileListDataModel>();

		if (datePattern == null || datePattern.isEmpty())
			datePattern = "ddMMyyyy";

		if (filedate == null || filedate.isEmpty()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat(datePattern);
				Date dataTime = new Date();
				filedate = dateFormat.format(dataTime);
			} catch (IllegalArgumentException ex) {
				logger.fatal("Niepoprawny format daty. Dopuszczalne formaty: ddMMyyyy, yyyyMMdd");
			}

		}
		if (datePattern.equals("yyyyMMdd")) {
			String dayString = filedate.substring(0, 2);
			String monthString = filedate.substring(2, 4);
			String yearString = filedate.substring(4, 8);
			filedate = yearString + monthString + dayString;
		}

		for (FileListDataModel fileListModel : fileList) {
			if (fileListModel.getFileName().contains("<data>")) {
				String fileName = fileListModel.getFileName().replace("<data>", filedate);
				fileListModel.setFileName(fileName);
				convertFileList.add(fileListModel);
			} else
				convertFileList.add(fileListModel);
		}

		if (exchangeProtocol.equals("ssl")) {

			List<FileListDataModel> arrayFileListTmp = new ArrayList<FileListDataModel>();

			for (FileListDataModel fileListModel : convertFileList) {
				if (fileListModel.getFileName().contains("?")) {
					for (int j = 1; j < 10; j++) {
						FileListDataModel tmpfileListModel = new FileListDataModel();
						String fileName = fileListModel.getFileName().replaceFirst(Pattern.quote("?"),
								Integer.toString(j));
						tmpfileListModel.setFileName(fileName);
						arrayFileListTmp.add(tmpfileListModel);
					}
					FileListDataModel tmpfileListModel = new FileListDataModel();
					String fileName = fileListModel.getFileName().replaceFirst(Pattern.quote("?"), "0");
					tmpfileListModel.setFileName(fileName);
					arrayFileListTmp.add(tmpfileListModel);

				} else {
					FileListDataModel tmpfileListModel = new FileListDataModel();
					String fileName = fileListModel.getFileName();
					tmpfileListModel.setFileName(fileName);
					arrayFileListTmp.add(tmpfileListModel);
				}
			}
			convertFileList.clear();
			convertFileList.addAll(arrayFileListTmp);
		}
		return convertFileList;
	}

	public static String getLogDate() {

		DateFormat logDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS");
		Date logDataTime = new Date();
		String logDate = logDateFormat.format(logDataTime);

		return logDate + " ";
	}

	public static String getConvertUrlForm(String url, String remoteLogin, String remotePassword, String loginForm) {
		String urlForm;
		String insertLogin;
		String insertPassword;

		insertLogin = loginForm.replace("$Login", remoteLogin);
		insertPassword = insertLogin.replace("$Haslo", remotePassword);

		urlForm = url + insertPassword;
		return urlForm;
	}
	
	/**
	   * This method makes a "deep clone" of any object it is given.
	   */
	  public static Object deepClone(Object object) {
	    try {
	      ByteArrayOutputStream baos = new ByteArrayOutputStream();
	      ObjectOutputStream oos = new ObjectOutputStream(baos);
	      oos.writeObject(object);
	      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	      ObjectInputStream ois = new ObjectInputStream(bais);
	      return ois.readObject();
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	      return null;
	    }
	  }
}
