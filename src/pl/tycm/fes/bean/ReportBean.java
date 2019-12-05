package pl.tycm.fes.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class ReportBean {

	private List<String> messageBody = new ArrayList<String>();
	
	public void addReport(String message) {
		messageBody.add(message);
	}
	
	public String getReport() {
		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		String dateString = dateFormat.format(currentDate);
		
		String reportMessage = "---------- RAPORT " + dateString + " ----------\t\n\t\n";
		
		for (String s : messageBody) {
			reportMessage += s + "\t\n";
		}
		return reportMessage;
	}
}
