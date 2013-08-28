package aegis.site.wiq.task;

import athena.origin.Athena;
import athena.origin.AthenaTask;
import athena.service.ResourceService;

import com.teamcenter.rac.kernel.ResourceMember;

public class BeforeRequestTask extends AthenaTask {
	private String templateName;
	private ResourceMember[] selResourceList;

	public BeforeRequestTask(Athena athena, String templateName, ResourceMember[] selResourceList) {
		super(athena);
		this.templateName = templateName;
		this.selResourceList = selResourceList;
	}

	public int execute() {
		String tmpTask = ResourceService.getString(templateName, "MandatoryTask");
		if (tmpTask.equals("")) {
			return 0;
		}
		String[] mandatoryTasks = tmpTask.split(",");
		boolean flag = false;
		for (int i = 0; i < mandatoryTasks.length; i++) {
			flag = false;
			for (int j = 0; j < selResourceList.length; j++) {
				String assignTask = selResourceList[j].getTaskTemplate().toString();
				if (mandatoryTasks[i].equals(assignTask)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return -1;
			}
		}
		return 0;
	}
}
