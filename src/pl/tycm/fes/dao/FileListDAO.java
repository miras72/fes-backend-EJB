package pl.tycm.fes.dao;

import java.util.List;

import pl.tycm.fes.model.FileListDataModel;
import pl.tycm.fes.model.StatusMessage;

public interface FileListDAO {

	public List<FileListDataModel> getFileList(int taskID, StatusMessage statusMessage);

	public boolean deleteFileList(int taskID, StatusMessage statusMessage);

	public boolean createFileList(FileListDataModel fileList, StatusMessage statusMessage);

	public boolean updateFileList(FileListDataModel fileList, StatusMessage statusMessage);
}
