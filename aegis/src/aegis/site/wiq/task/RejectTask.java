package aegis.site.wiq.task;

import java.util.Vector;

import athena.origin.Athena;
import athena.origin.AthenaTask;
import athena.service.ResourceService;
import athena.utility.Mail;

import com.teamcenter.rac.kernel.TCComponentEnvelope;
import com.teamcenter.rac.kernel.TCComponentEnvelopeType;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class RejectTask extends AthenaTask {
	private TCSession session;
	private TCComponentTask performSignoffTask;

	public RejectTask(Athena athena, TCComponentTask performSignoffTask) {
		super(athena);
		session = athena.getSession();
		this.performSignoffTask = performSignoffTask;
	}

	public int execute() {
		try {
			TCComponentProcess process = performSignoffTask.getProcess();
			Vector<TCComponentUser> vector = getPreviousUser(performSignoffTask);
			TCComponentEnvelopeType type = (TCComponentEnvelopeType)session.getTypeComponent("Envelope");
			String contents = getRejectContents(performSignoffTask);
			TCComponentEnvelope envelope = type.create(ResourceService.getString(this, "Reject") + "[" + process.getName() + "]", contents, "Envelope");
			envelope.addReceivers((TCComponentUser[])vector.toArray(new TCComponentUser[vector.size()]));
			envelope.send();
			String mailSuffix = ResourceService.getString(this, "MailSuffix");
			for (int i = 0; i < vector.size(); i++) {
				Mail mail = new Mail(session.getCredentials().getUserName() + mailSuffix, session.getCredentials().getPassword());
				mail.sendEmail(session.getCredentials().getUserName() + mailSuffix, vector.get(i).getUserId() + mailSuffix, "", ResourceService.getString(this, "Reject") + "[" + process.getName() + "]", contents);
			}
		} catch (TCException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	private Vector<TCComponentUser> getPreviousUser(TCComponentTask task) throws TCException {
		TCComponentTask rootTask = task.getRoot();
		rootTask.refresh();
		Vector<TCComponentUser> vector = new Vector();
		TCComponentUser user = (TCComponentUser)rootTask.getReferenceProperty("owning_user");
		vector.addElement(user);
		TCComponentTask[] tasks = rootTask.getSubtasks();
		for (int i = 0; i < tasks.length; i++) {
			if (tasks[i].equals(task.getParent())) {
				return vector;
			}
			if (tasks[i].getTaskType().equals("EPMDoTask")) {
				vector.addElement((TCComponentUser)tasks[i].getResponsibleParty());
			} else if (tasks[i].getTaskType().equals("EPMReviewTask")) {
				TCComponentTask[] subTask = tasks[i].getSubtasks();
				TCComponentSignoff[] signoffs = subTask[1].getValidSignoffs();
				for (int j = 0; j < signoffs.length; j++) {
					TCComponentSignoff signoff = signoffs[j];
					signoff.refresh();
					vector.addElement(signoff.getGroupMember().getUser());
				}
			}
		}
		return vector;
	}

	public String getRejectContents(TCComponentTask performSignoffTask) {
		StringBuffer stringBuffer = new StringBuffer();
		try {
			stringBuffer.append(ResourceService.getString(this, "Reject") + "\n");
			stringBuffer.append(ResourceService.getString(this, "Process") + " : " + performSignoffTask.getProcess().getName() + "\n");
			stringBuffer.append(ResourceService.getString(this, "Task") + " : " + performSignoffTask.getParent().getName() + "\n");
			stringBuffer.append(ResourceService.getString(this, "Comments") + " : " + performSignoffTask.getInstructions());
		} catch (TCException e) {
			e.printStackTrace();
		}
		return stringBuffer.toString();
	}
}
