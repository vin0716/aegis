package aegis.site.wiq.ecr;

import java.awt.Dimension;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import aegis.origin.Aegis;
import athena.origin.AthenaOperation;
import athena.origin.AthenaOperationDialog;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.utility.ListCellRenderer;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.VerticalLayout;

public class ECRDialog extends AthenaOperationDialog {
	private Vector problemVector, fileVector, datasetVector;
	private JList problemList, fileList, datasetList;

	public ECRDialog(Aegis aegis) {
		super(aegis);
	}

	public void deploy() {
		mainPanel.setLayout(new HorizontalLayout());
		mainPanel.add("left.bind", createECRPanel());
		mainPanel.add("left.bind", createListPanel());
	}

	private JPanel createECRPanel() {
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("ECRInformation")));
		ComponentService.deployComponent(hashtable, panel, new String[] {"object_name", "a2ChangeReason", "a2ApplyDate", "a2Problem", "a2ChangeRequest"});
		// ComponentService.setComponentSize(panel, 200, 20);
		return panel;
	}

	private JPanel createListPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.add("top.bind", createProblemItemPanel());
		panel.add("top.bind", createFilePanel());
		panel.add("top.bind", createDatasetPanel());
		return panel;
	}

	private JPanel createProblemItemPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("ProblemItem")));
		problemVector = new Vector();
		problemList = new JList();
		problemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		problemList.setVisibleRowCount(5);
		problemList.setCellRenderer(new ListCellRenderer());
		JScrollPane scrollpane = new JScrollPane(problemList);
		scrollpane.setPreferredSize(new Dimension(200, 40));
		panel.add("top.bind", scrollpane);
		JPanel actionPanel = new JPanel(new ButtonLayout(ButtonLayout.HORIZONTAL, ButtonLayout.RIGHT, 10));
		actionPanel.add(actionButtons[0]);
		actionPanel.add(actionButtons[1]);
		panel.add("top.bind", actionPanel);
		return panel;
	}

	private JPanel createFilePanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("Athena", "MyComputerFile")));
		fileVector = new Vector();
		fileList = new JList();
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.setVisibleRowCount(5);
		fileList.setCellRenderer(new ListCellRenderer());
		JScrollPane scrollpane = new JScrollPane(fileList);
		scrollpane.setPreferredSize(new Dimension(200, 40));
		panel.add("top.bind", scrollpane);
		JPanel actionPanel = new JPanel(new ButtonLayout(ButtonLayout.HORIZONTAL, ButtonLayout.RIGHT, 10));
		actionPanel.add(actionButtons[2]);
		actionPanel.add(actionButtons[3]);
		panel.add("top.bind", actionPanel);
		return panel;
	}

	private JPanel createDatasetPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("Athena", "Dataset")));
		datasetVector = new Vector();
		datasetList = new JList();
		datasetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		datasetList.setVisibleRowCount(5);
		datasetList.setCellRenderer(new ListCellRenderer());
		JScrollPane scrollpane = new JScrollPane(datasetList);
		scrollpane.setPreferredSize(new Dimension(200, 40));
		panel.add("top.bind", scrollpane);
		JPanel actionPanel = new JPanel(new ButtonLayout(ButtonLayout.HORIZONTAL, ButtonLayout.RIGHT, 10));
		actionPanel.add(actionButtons[4]);
		actionPanel.add(actionButtons[5]);
		panel.add("top.bind", actionPanel);
		return panel;
	}

	protected AthenaOperation createOperation() {
		return new ECROperation(athena, session, hashtable, columns, problemVector, fileVector, datasetVector);
	}

	protected void buttonPerformed(JButton button, String actionCommand) {
		super.buttonPerformed(button, actionCommand);
		if (actionCommand.equals("Add1")) {
			Vector<TCComponentItemRevision> vector = getClipboardItem();
			for (int i = 0; i < vector.size(); i++) {
				TCComponentItemRevision revision = vector.elementAt(i);
				try {
					if (revision.getProperty("release_status_list").length() > 0) {
						if (!problemVector.contains(revision)) {
							problemVector.addElement(revision);
						} else {
							setStatus("DuplicateRevision");
						}
					} else {
						setStatus("NoReleaseRevision");
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			problemList.setListData(problemVector);
		} else if (actionCommand.equals("Remove1")) {
			int i = problemList.getSelectedIndex();
			if (i == -1) {
				setStatus("Athena", "SelectRevision");
				return;
			}
			problemVector.removeElementAt(i);
			problemList.setListData(problemVector);
		} else if (actionCommand.equals("Add2")) {
			JFileChooser fileChooser = new JFileChooser(new File(DatabaseService.getPreference("Workspace")));
			int status = fileChooser.showOpenDialog(this);
			if (status == JFileChooser.APPROVE_OPTION) {
				String path = fileChooser.getSelectedFile().getAbsolutePath();
				if (!fileVector.contains(path)) {
					fileVector.addElement(path);
				}
				fileList.setListData(fileVector);
			}
		} else if (actionCommand.equals("Remove2")) {
			int i = fileList.getSelectedIndex();
			if (i == -1) {
				setStatus("Athena", "SelectFile");
				return;
			}
			fileVector.removeElementAt(i);
			fileList.setListData(fileVector);
		} else if (actionCommand.equals("Add3")) {
			Vector<TCComponentDataset> vector = new Vector<TCComponentDataset>();
			AIFClipboard clipBoard = AIFPortal.getClipboard();
			boolean isDatasetExist = false;
			if (!clipBoard.isEmpty()) {
				vector = clipBoard.toVector();
				for (int i = 0; i < vector.size(); i++) {
					if (vector.elementAt(i) instanceof TCComponentDataset) {
						TCComponentDataset dataset = (TCComponentDataset)vector.elementAt(i);
						if (!datasetVector.contains(dataset)) {
							datasetVector.addElement(dataset);
						}
						isDatasetExist = true;
					}
				}
				if (!isDatasetExist) {
					setStatus("NoDataset");
				}
			} else {
				setStatus("Athena", "ClipBoardEmpty");
			}
			datasetList.setListData(datasetVector);
		} else if (actionCommand.equals("Remove3")) {
			int i = datasetList.getSelectedIndex();
			if (i == -1) {
				setStatus("Athena", "SelectDataset");
				return;
			}
			datasetVector.removeElementAt(i);
			datasetList.setListData(datasetVector);
		}
	}

	private Vector<TCComponentItemRevision> getClipboardItem() {
		AIFClipboard clipBoard = AIFPortal.getClipboard();
		Vector<TCComponentItemRevision> revisionVector = new Vector();
		boolean isRevisionExist = false;
		if (!clipBoard.isEmpty()) {
			Vector<TCComponent> vector = clipBoard.toVector();
			for (int i = 0; i < vector.size(); i++) {
				if (vector.elementAt(i) instanceof TCComponentItemRevision) {
					TCComponentItemRevision revision = (TCComponentItemRevision)vector.elementAt(i);
					revisionVector.addElement(revision);
					isRevisionExist = true;
				}
			}
			if (!isRevisionExist) {
				setStatus("NoItemRevision");
			}
		} else {
			setStatus("Athena", "ClipBoardEmpty");
		}
		return revisionVector;
	}
}