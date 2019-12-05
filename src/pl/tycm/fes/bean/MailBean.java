package pl.tycm.fes.bean;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import pl.tycm.fes.model.MailingListDataModel;

@Stateless
@LocalBean
public class MailBean {
	
	@Resource(name = "java:/jboss/mail/exchange")
	private Session session;
	
	private String mailFrom;
	private List<MailingListDataModel> mailingList;
	private String mailSubject;

	private final Logger logger = Logger.getLogger(this.getClass());

	/**
	 * @return the mailFrom
	 */
	public String getMailFrom() {
		return mailFrom;
	}

	/**
	 * @param mailFrom the mailFrom to set
	 */
	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	/**
	 * @return the mailingList
	 */
	public List<MailingListDataModel> getMailingList() {
		return mailingList;
	}

	/**
	 * @param mailingListModel the mailingList to set
	 */
	public void setMailingList(List<MailingListDataModel> mailingListModel) {
		this.mailingList = mailingListModel;
	}

	/**
	 * @return the mailSubject
	 */
	public String getMailSubject() {
		return mailSubject;
	}

	/**
	 * @param mailSubject the mailSubject to set
	 */
	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	public void sendMail(String raportMessage ) {
		try{
			logger.info("Wysyłam raport....");
			// Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(mailFrom));

	         // Set To: header field of the header.
	         InternetAddress[] addressTo = new InternetAddress[mailingList.size()];
	         int i =0;
	         for (MailingListDataModel mailling : mailingList) {
	        	 addressTo[i] = new InternetAddress(mailling.getRecipientName());
	        	 i++;
			}

	         message.addRecipients(Message.RecipientType.TO,addressTo);

	         // Set Subject: header field
	         message.setSubject(mailSubject);

	         // Now set the actual message
	         message.setText(raportMessage );
	         
	         // Send message
	         Transport.send(message);
	         logger.info("Raport został wysłany.");
	      }catch (MessagingException mex) {
	    	  logger.error("Cannot send mail: " + mex.getMessage());
	    	  mex.printStackTrace();
	      }
	}
}
