package pl.tycm.fes.dao;

import java.util.List;

import pl.tycm.fes.model.MailingListDataModel;
import pl.tycm.fes.model.StatusMessage;

public interface MailingListDAO {

	public List<MailingListDataModel> getMailingList(int taskID, StatusMessage statusMessage);

	public boolean deleteMailingList(int taskID, StatusMessage statusMessage);

	public boolean createMailingList(MailingListDataModel mailingList, StatusMessage statusMessage);

	public boolean updateMailingList(MailingListDataModel mailingList, StatusMessage statusMessage);
}
