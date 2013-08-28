package aegis.site.wiq.task;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import athena.origin.Athena;
import athena.origin.AthenaTask;
import athena.service.DatasetService;
import athena.service.ResourceService;
import athena.utility.Mail;

import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentEnvelope;
import com.teamcenter.rac.kernel.TCComponentEnvelopeType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;

public class ApproveTask extends AthenaTask {
	private TCSession session;
	private TCComponentTask performSignoffTask;

	public ApproveTask(Athena athena, TCComponentTask performSignoffTask) {
		super(athena);
		session = athena.getSession();
		this.performSignoffTask = performSignoffTask;
	}

	public int execute() {
		try {
			String processName = performSignoffTask.getRoot().getName();
			String taskName = performSignoffTask.getParent().getName();
			if ((processName.equals(ResourceService.getString(this, "DocumentDistribution")) || processName.equals(ResourceService.getString(this, "DocumentApprove"))) && taskName.equals(ResourceService.getString(this, "Approve"))) {
				Hashtable<String, String> taskInfoTable = getTaskInformation(performSignoffTask.getRoot());
				TCComponentItemRevision revision = (TCComponentItemRevision)getTarget(performSignoffTask, "WIQ2DocumentRevision");
				if (revision != null) {
					TCComponentDataset template = getAttachTemplate(revision);
					if (template != null) {
						// ExcelService.setDocumentTemplate(taskInfoTable, template);
					}
				}
			} else if (processName.equals(ResourceService.getString(this, "ECR")) && taskName.equals(ResourceService.getString(this, "TechnicalApprove"))) {
				Hashtable<String, String> taskInfoTable = getTaskInformation(performSignoffTask.getRoot());
				File file = getServerTemplate(ResourceService.getString(this, "ECRDocument"));
				TCComponentItem item = (TCComponentItem)getTarget(performSignoffTask, "WIQ2ECR");
				if (item != null) {
					// ExcelService.setECRTemplate(taskInfoTable, file, item);
					DatasetService.createDataset(item, "IMAN_reference", file.getAbsolutePath());
				}
			} else if (processName.equals(ResourceService.getString(this, "ECN")) && taskName.equals(ResourceService.getString(this, "Approve"))) {
				TCComponentItemRevision clientDrawingRevision = (TCComponentItemRevision)getTarget(performSignoffTask, "WIQ2ClientRevision");
				if (clientDrawingRevision != null) {
					return 0;
				}
				Hashtable<String, String> taskInfoTable = getTaskInformation(performSignoffTask.getRoot());
				File file = getServerTemplate(ResourceService.getString(this, "ECNDocument"));
				TCComponentItem item = (TCComponentItem)getTarget(performSignoffTask, "WIQ2ECN");
				// ExcelService.setECNTemplate(taskInfoTable, file, item);
				DatasetService.createDataset(item, "IMAN_reference", file.getAbsolutePath());
			} else {
				Vector<TCComponentUser> vector = new Vector();
				vector = getNextUser(performSignoffTask.getParent(), vector);
				if (vector.size() > 0) {
					TCComponentProcess process = performSignoffTask.getProcess();
					TCComponentEnvelopeType type = (TCComponentEnvelopeType)session.getTypeComponent("Envelope");
					String contents = getApproveContents(process.getName());
					TCComponentEnvelope envelope = type.create(ResourceService.getString(this, "RequestApprove") + "[" + process.getName() + "]", contents, "Envelope");
					envelope.addReceivers((TCComponentUser[])vector.toArray(new TCComponentUser[vector.size()]));
					envelope.send();
					String mailSuffix = ResourceService.getString(this, "MailSuffix");
					for (int i = 0; i < vector.size(); i++) {
						Mail mail = new Mail(session.getCredentials().getUserName() + mailSuffix, session.getCredentials().getPassword());
						mail.sendEmail(session.getCredentials().getUserName() + mailSuffix, vector.get(i).getUserId() + mailSuffix, "", ResourceService.getString(this, "RequestApprove") + "[" + process.getName() + "]", contents);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	public String getApproveContents(String processName) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(ResourceService.getString(this, "RequestApprove") + "\n");
		stringBuffer.append(ResourceService.getString(this, "Process") + " : " + processName + "\n");
		return stringBuffer.toString();
	}

	private Hashtable<String, String> getTaskInformation(TCComponentTask rootTask) throws TCException {
		rootTask.refresh();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm");
		Hashtable<String, String> taskInfoTable = new Hashtable<String, String>();
		Vector<String[]> vector = new Vector();
		TCComponentUser user = (TCComponentUser)rootTask.getReferenceProperty("owning_user");
		Date date = rootTask.getDateProperty("creation_date");
		taskInfoTable.put("make_user", user.getProperty("user_name"));
		taskInfoTable.put("make_date", dateFormat.format(date));
		TCComponentTask[] tasks = rootTask.getSubtasks();
		for (int i = 0; i < tasks.length; i++) {
			if (tasks[i].getTaskType().equals("EPMDoTask")) {
				Date idate = tasks[i].getDateProperty("last_mod_date");
				TCComponentUser iuser = (TCComponentUser)tasks[i].getResponsibleParty();
				taskInfoTable.put(tasks[i].toString() + "_user", iuser.getProperty("user_name"));
				taskInfoTable.put(tasks[i] + "_date", dateFormat.format(idate));
			} else if (tasks[i].getTaskType().equals("EPMReviewTask") || tasks[i].getTaskType().equals("EPMAcknowledgeTask")) {
				TCComponentTask[] subTask = tasks[i].getSubtasks();
				TCComponentSignoff[] signoffs = subTask[1].getValidSignoffs();
				if (signoffs.length == 0) {
					taskInfoTable.put(tasks[i].toString() + "_user", ResourceService.getString(this, "Pass"));
					taskInfoTable.put(tasks[i].toString() + "_date", "");
				} else {
					TCComponentSignoff signoff = signoffs[0];
					signoff.refresh();
					date = tasks[i].getDateProperty("last_mod_date");
					TCComponentUser luser = signoff.getGroupMember().getUser();
					taskInfoTable.put(tasks[i].toString() + "_user", luser.getProperty("user_name"));
					taskInfoTable.put(tasks[i].toString() + "_date", dateFormat.format(date));
				}
			}
		}
		return taskInfoTable;
	}

	private TCComponent getTarget(TCComponentTask performSignoffTask, String type) throws TCException {
		TCComponent[] targets = performSignoffTask.getAttachments(TCAttachmentScope.GLOBAL, TCAttachmentType.TARGET);
		for (int i = 0; i < targets.length; i++) {
			if (targets[i].getType().equals(type)) {
				return targets[i];
			}
		}
		return null;
	}

	private TCComponentDataset getAttachTemplate(TCComponent component) throws Exception {
		TCComponent[] relatedDataset = component.getRelatedComponents("IMAN_specification");
		TCComponent[] templates = session.search("Dataset...", new String[] {session.getTextService().getTextValue("Description")}, new String[] {"WIQ Document Template"});
		for (int i = 0; i < relatedDataset.length; i++) {
			for (int j = 0; j < templates.length; j++) {
				if (relatedDataset[i].getProperty("object_name").equals(templates[j].getProperty("object_name"))) {
					return (TCComponentDataset)relatedDataset[i];
				}
			}
		}
		return null;
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

	private File getServerTemplate(String name) throws Exception {
		TCTextService textService = session.getTextService();
		try {
			TCComponent[] templates = session.search("Dataset...", new String[] {textService.getTextValue("Name"), textService.getTextValue("Description")}, new String[] {name, "WIQ Document Template"});
			if (templates.length > 0) {
				TCComponentDataset dataset = (TCComponentDataset)templates[0];
				File[] files = DatasetService.getFiles(dataset);
				return files[0];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
