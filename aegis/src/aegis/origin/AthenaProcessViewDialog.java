package aegis.origin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import athena.origin.Athena;
import athena.origin.AthenaDialog;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.service.ResourceService;
import athena.utility.SimpleFilter;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;

public class AthenaProcessViewDialog extends AthenaDialog {
	protected JXTable table;
	protected DefaultTableModel model;
	protected Vector<TCComponentProcess> processVector;
	protected JComboBox processComboBox;

	public AthenaProcessViewDialog(Athena athena) {
		super(athena);
	}

	public boolean isValidState() {
		processVector = new Vector();
		TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
		if (target == null){
			athena.setStatus("Athena", "SelectObject");
			return false;
		}else	if (target instanceof TCComponentProcess) {
			processVector.addElement((TCComponentProcess)target);
		} else if (target instanceof TCComponent) {
			try {
				AIFComponentContext[] context = target.whereReferenced();
				for (int i = 0; i < context.length; i++) {
					if (context[i].getComponent() instanceof TCComponentProcess) {
						processVector.addElement((TCComponentProcess)context[i].getComponent());
					}
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		if (processVector.size() > 0) {
			return true;
		} else {
			athena.setStatus("AthenaProcessViewDialog", "SelectedObjectHasNoProcess");
			return false;
		}
	}

	public void createComponents() {
		processComboBox = new JComboBox(processVector);
	}

	public void deploy() {
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, createProcessPanel());
		mainPanel.add(BorderLayout.CENTER, createTablePanel());
	}

	protected JPanel createProcessPanel() {
		JPanel panel = new JPanel();
		panel.add(new JLabel(getString("Process")));
		panel.add(processComboBox);
		return panel;
	}

	protected JScrollPane createTablePanel() {
		String[] columnData = getStrings("TableColumns");
		String[] tableColumns = new String[columnData.length];
		String[] tableHeaders = new String[columnData.length];
		int[] tableColumnsWidth = new int[columnData.length];
		for (int i = 0; i < columnData.length; i++) {
			String[] strings = ResourceService.parse(columnData[i], ':');
			tableColumns[i] = strings[0];
			tableHeaders[i] = strings[1];
			tableColumnsWidth[i] = Integer.parseInt(strings[2]);
		}
		table = ComponentService.createTable(tableColumns, tableHeaders, tableColumnsWidth);
		model = (DefaultTableModel)table.getModel();
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(800, 600));
		return scrollPane;
	}

	public void postAction() {
		okButton.setVisible(false);
		applyButton.setVisible(false);
		saveButton.setVisible(true);
		try {
			analyze(processVector.elementAt(0).getRootTask());
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public void analyze(TCComponentTask task) throws TCException {
		task.refresh();
		if (task.getTaskType().equals("EPMTask") || task.getTaskType().equals("EPMReviewTask") || task.getTaskType().equals("EPMAcknowledgeTask")) {
			TCComponentTask[] subTask = task.getSubtasks();
			if (subTask.length > 0) {
				for (int i = 0; i < subTask.length; i++)
					analyze(subTask[i]);
			}
		} else if (task.getTaskType().equals("EPMDoTask")) {
			TCComponentUser user = (TCComponentUser)task.getResponsibleParty();
			model.addRow(new String[] {task.getName(), user.toString(), task.getTaskState(), task.getInstructions(), getDateInfomation(task, true)});
		} else if (task.getTaskType().equals("EPMPerformSignoffTask")) {
			TCComponentSignoff[] signoffs = task.getValidSignoffs();
			if (signoffs.length == 0) {
				model.addRow(new String[] {task.getParent().getName(), getString("NoDecisior"), task.getTaskState(), "", ""});
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
				model.addRow(new String[] {task.getParent().getName(), user.toString(), task.getTaskState(), task.getValidSignoffs()[0].getComments(), getDateInfomation(task, true)});
			}
		} else if (task.getTaskType().equals("EPMAddStatusTask")) {
		}
	}

	public String[] getUserInfomation(TCComponentUser user) {
		try {
			TCComponentGroupMember[] groupMember = user.getGroupMembers();
			String[] userInformation = {user.toString(), groupMember[0].getGroup().toString(), groupMember[0].getRole().toString()};
			return userInformation;
		} catch (TCException e) {
			return null;
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

	protected void clearTable() {
		for (int i = table.getRowCount() - 1; i >= 0; i--) {
			((DefaultTableModel)table.getModel()).removeRow(i);
		}
	}

	protected void save(TCComponentProcess process, File file) {
	}

	protected void buttonPerformed(JButton button, String actionCommand) {
		super.buttonPerformed(button, actionCommand);
		if (actionCommand.equals("Save")) {
			TCComponentProcess process = (TCComponentProcess)processComboBox.getSelectedItem();
			try {
				JFileChooser fileChooser = new JFileChooser(new File(DatabaseService.getPreference("Workspace")));
				FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
				fileChooser.removeChoosableFileFilter(fileFilter);
				fileChooser.addChoosableFileFilter(new SimpleFilter("html", "*.html"));
				fileChooser.setSelectedFile(new File(process.getProperty("object_name") + ".html"));
				if (fileChooser.showSaveDialog(AIFUtility.getActiveDesktop()) == JFileChooser.APPROVE_OPTION) {
					File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
					if (file.exists()) {
						int decision = athena.showConfirmDialog(this, "Athena", "FileExistDoYouWantRewrite", JOptionPane.YES_NO_OPTION);
						if (decision == JOptionPane.YES_OPTION) {
							file.delete();
						} else {
							return;
						}
					}
					save(process, file);
				}
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
	}

	protected void comboBoxStateChanged(JComboBox comboBox, String actionCommand) {
		TCComponentProcess process = (TCComponentProcess)comboBox.getSelectedItem();
		try {
			clearTable();
			analyze(process.getRootTask());
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
}