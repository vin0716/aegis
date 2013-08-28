package aegis.utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.osgi.framework.Bundle;

import athena.Activator;
import athena.service.ResourceService;

import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;

public class TemplateParser {
	protected Hashtable<String, String> hashtable;
	protected Vector<Vector> vector;
	protected Vector<String[]> workflowVector;
	protected StringBuilder[] strings;
	private SimpleDateFormat dateFormat;

	public TemplateParser() {
	}

	public String parseWorkflow(TCComponentProcess process) {
		try {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm");
			hashtable = new Hashtable<String, String>();
			hashtable.put("@Title", process.getName());
			hashtable.put("@Decision", process.getRootTask().getTaskState());
			hashtable.put("@Creator", process.getRelatedComponent("owning_user").toString());
			hashtable.put("@Date", getDateInfomation(process.getRootTask(), true));
			hashtable.put("@Comments", process.getInstructions());
			TCComponent[] components = process.getRootTask().getAttachments(TCAttachmentScope.GLOBAL, TCAttachmentType.TARGET);
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < components.length; i++) {
				stringBuilder.append(components[i].toDisplayString());
				if (i < components.length - 1) {
					stringBuilder.append("\n");
				}
			}
			hashtable.put("@Attachments", stringBuilder.toString());
			workflowVector = new Vector<String[]>();
			analyzeWorkflow(process.getRootTask());
			vector = new Vector<Vector>();
			vector.addElement(workflowVector);
			strings = new StringBuilder[vector.size()];
			for (int i = 0; i < vector.size(); i++) {
				strings[i] = new StringBuilder();
			}
			URL url = ResourceService.getFileURL("image/wiq/workflow.html");
			return parseContents(url);
		} catch (Exception e) {
			e.printStackTrace();
			return new String();
		}
	}

	public void analyzeWorkflow(TCComponentTask task) {
		try {
			task.refresh();
			if (task.getTaskType().equals("EPMTask") || task.getTaskType().equals("EPMReviewTask") || task.getTaskType().equals("EPMAcknowledgeTask")) { // AcknowledgeTask Ãß°¡
				// TCProperty property = task.getProcess().getTCProperty("owning_user");
				// TCComponentUser user = (TCComponentUser)property.getReferenceValue();
				// model.addRow(new String[] {task.getName(), user.toString(), task.getTaskState(), task.getInstructions(), getDateInfomation(task, true)});
				TCComponentTask[] subTask = task.getSubtasks();
				if (subTask.length > 0) {
					for (int i = 0; i < subTask.length; i++)
						analyzeWorkflow(subTask[i]);
				}
			} else if (task.getTaskType().equals("EPMDoTask")) {
				TCComponentUser user = (TCComponentUser)task.getResponsibleParty();
				workflowVector.addElement(new String[] {task.getName(), user.toString(), task.getTaskState(), getDateInfomation(task, false), task.getInstructions()});
			} else if (task.getTaskType().equals("EPMPerformSignoffTask")) {
				TCComponentSignoff[] signoffs = task.getValidSignoffs();
				if (signoffs.length == 0) {
					workflowVector.addElement(new String[] {task.getParent().getName(), "Pass", "Pass", "Pass", "Pass"});
				}
				for (int j = 0; j < signoffs.length; j++) {
					TCComponentSignoff signoff = signoffs[j];
					signoff.refresh();
					String state = task.getProperty("real_state");
					if (task.getTaskState().equals("Started")) {
						if (signoff.getDecision().equals(TCCRDecision.REJECT_DECISION))
							state = signoff.getDecision().toString();
					}
					TCComponentUser user = signoff.getGroupMember().getUser();
					workflowVector.addElement(new String[] {task.getParent().getName(), user.toString(), task.getTaskState(), getDateInfomation(task, false), task.getValidSignoffs()[0].getComments()});
				}
			} else if (task.getTaskType().equals("EPMAddStatusTask")) {
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public String getDateInfomation(TCComponentTask task, boolean isCreation) {
		String dateProperty = "";
		if (isCreation) {
			dateProperty = "creation_date";
		} else {
			dateProperty = "last_mod_date";
		}
		try {
			return dateFormat.format(task.getDateProperty(dateProperty));
		} catch (TCException e) {
			return null;
		}
	}

	public String parseContents(URL url) {
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		try {
			InputStream inputStream = url.openStream();
			Reader reader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(reader);
			setVector();
			while ((line = bufferedReader.readLine()) != null) {
				line = parseHashtableData(line);
				line = parseRowDatas(line);
				stringBuilder.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}

	private String parseHashtableData(String line) {
		Enumeration enumeration = hashtable.keys();
		while (enumeration.hasMoreElements()) {
			String key = (String)enumeration.nextElement();
			line = line.replace(key, hashtable.get(key));
		}
		return line;
	}

	private String parseRowDatas(String line) {
		if (line.contains("@Line")) {
			int i = line.indexOf("@Line");
			int index = Integer.parseInt(line.substring(i + 5).trim());
			line = line.replace(line, strings[index - 1]);
		}
		return line;
	}

	private void setVector() {
		for (int i = 0; i < vector.size(); i++) {
			Vector<String[]> rowVector = vector.elementAt(i);
			for (int j = 0; j < rowVector.size(); j++) {
				String[] rowData = rowVector.elementAt(j);
				if (j % 2 == 0) {
					strings[i].append("<TR class='odd'>");
				} else {
					strings[i].append("<TR>");
				}
				for (int k = 0; k < rowData.length; k++) {
					strings[i].append("<TH>");
					strings[i].append(rowData[k]);
					strings[i].append("</TH>");
				}
				strings[i].append("</TR>");
			}
		}
	}
}
