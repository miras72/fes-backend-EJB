package pl.tycm.fes.rest;

import java.util.ArrayList;
import java.util.List;

import pl.tycm.fes.model.TaskStatusDataModel;

public class TaskStatusDataService {
	
	private List<TaskStatusDataModel> tasks = new ArrayList<TaskStatusDataModel>();

	public TaskStatusDataService () {
//		tasks.add(new TaskStatusDataModel(1, 1, "TGE", "download", "OK", "2017.09.05 12:28", "2017.09.06 12:28", "08:30 pt STY", true));
//		tasks.add(new TaskStatusDataModel(2, 2, "GPW (FIRDS)", "download", "Error", "2017.09.04 16:28", "2017.09.05 16:28",
//				"08:* pn,sr STY,LUT", false));
//		tasks.add(new TaskStatusDataModel(3, 3, "GPW (UTP)", "download", "Running", "2017.09.04 17:28", "2017.09.05 17:28",
//				"*:*/15 pn,wt,sr,cz,pt,so,nd STY,LUT,MAR,KWI,MAJ,CZE,LIP,SIE,WRZ,PA�,LIS,GRU", false));
	}
	
	public List<TaskStatusDataModel> getTasks(){
		
		// System.out.println(tasks);
		return tasks;
	}
	
}
