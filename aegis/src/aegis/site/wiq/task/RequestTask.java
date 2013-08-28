package aegis.site.wiq.task;

import java.util.Vector;

import athena.origin.Athena;
import athena.origin.AthenaTask;
import athena.service.ResourceService;
import athena.utility.Mail;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentEnvelope;
import com.teamcenter.rac.kernel.TCComponentEnvelopeType;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class RequestTask extends AthenaTask {
	private TCSession session;
	private TCComponentProcess process;

	public RequestTask(Athena athena, TCComponentProcess process) {
		super(athena);
		session = athena.getSession();
		this.process = process;
	}

	public int execute() {
		try {
			Vector<TCComponentUser> vector = new Vector<TCComponentUser>();
			TCComponentTask rootTask = process.getRootTask();
			TCComponentTask[] subtasks = rootTask.getSubtasks();
			for (int i = 0; i < subtasks.length; i++) {
				TCComponentTask previousTask = subtasks[i].getPrevious();
				if (previousTask == null) {
					if (subtasks[i].getTaskType().equals("EPMDoTask")) {
						vector.addElement((TCComponentUser)subtasks[i].getResponsibleParty());
					} else if (subtasks[i].getTaskType().equals("EPMReviewTask")) {
						TCComponentTask[] subTask = subtasks[i].getSubtasks();
						TCComponent[] signoffs = subTask[1].getValidSignoffs();
						if (signoffs.length == 0) {
							getNextUser(subtasks[i], vector);
						}
						for (int j = 0; j < signoffs.length; j++) {
							TCComponentSignoff signoff = (TCComponentSignoff)signoffs[j];
							signoff.refresh();
							vector.addElement(signoff.getGroupMember().getUser());
						}
					}
				}
			}
			String processName = ((TCComponentProcess)process).getName();
			TCComponentEnvelopeType type = (TCComponentEnvelopeType)session.getTypeComponent("Envelope");
			String contents = getApproveContents(processName);
			TCComponentEnvelope envelope = type.create(ResourceService.getString(this, "RequestApprove") + "[" + processName + "]", contents, "Envelope");
			envelope.addReceivers((TCComponentUser[])vector.toArray(new TCComponentUser[vector.size()]));
			envelope.send();
			String mailSuffix = ResourceService.getString(this, "MailSuffix");
			for (int i = 0; i < vector.size(); i++) {
				Mail mail = new Mail(session.getCredentials().getUserName() + mailSuffix, session.getCredentials().getPassword());
				mail.sendEmail(session.getCredentials().getUserName() + mailSuffix, vector.get(i).getUserId() + mailSuffix, "", ResourceService.getString(this, "RequestApprove") + "[" + processName + "]", contents);
			}
		} catch (TCException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	private Vector<TCComponentUser> getNextUser(TCComponentTask task, Vector<TCComponentUser> vector) throws TCException {
		TCComponent[] nextTask = task.getRelatedComponents("successors");
		if (nextTask.length == 0 || nextTask == null) {
			return vector;
		}
		for (int i = 0; i < nextTask.length; i++) {
			TCComponentTask tmpTask = (TCComponentTask)nextTask[i];
			if (tmpTask.getTaskType().equals("EPMDoTask")) {
				vector.addElement((TCComponentUser)tmpTask.getResponsibleParty());
			} else if (tmpTask.getTaskType().equals("EPMReviewTask")) {
				TCComponentTask[] subTask = tmpTask.getSubtasks();
				TCComponent[] signoffs = subTask[1].getRelatedComponents("signoff_attachments");
				if (signoffs.length == 0) {
					getNextUser(tmpTask, vector);
				}
				for (int j = 0; j < signoffs.length; j++) {
					TCComponentSignoff signoff = (TCComponentSignoff)signoffs[j];
					signoff.refresh();
					vector.addElement(signoff.getGroupMember().getUser());
				}
			}
		}
		return vector;
	}

	public String getApproveContents(String processName) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(ResourceService.getString(this, "RequestApprove") + "\n");
		stringBuffer.append(ResourceService.getString(this, "Process") + " : " + processName + "\n");
		return stringBuffer.toString();
	}
}
