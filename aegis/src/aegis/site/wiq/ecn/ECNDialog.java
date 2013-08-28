package aegis.site.wiq.ecn;

import java.awt.Dimension;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import aegis.find.FindValuesDialog;
import aegis.origin.Aegis;
import aegis.site.wiq.find.FindClientDrawingDialog;
import athena.component.CoupledButton;
import athena.component.TCComponentField;
import athena.origin.AthenaDialog;
import athena.origin.AthenaOperation;
import athena.origin.AthenaOperationDialog;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.utility.ListCellRenderer;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.VerticalLayout;

public class ECNDialog extends AthenaOperationDialog {
	private TCComponentItem ecr;
	public Vector problemVector, solutionVector, fileVector, datasetVector;
	private JList problemList, solutionList, fileList, datasetList;
	private JPanel informationPanel;
	public TCComponentField ecrField;

	public ECNDialog(Aegis aegis) {
		super(aegis);
	}

	public void deploy() {
		mainPanel.setLayout(new HorizontalLayout());
		mainPanel.add("left.bind", createECNPanel());
		mainPanel.add("left.bind", createListPanel());
		addFindButton();
	}

	private JPanel createECNPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.add("top.bind", createInformationPanel());
		panel.add("top.bind", createChangePanel());
		return panel;
	}

	private JPanel createInformationPanel() {
		informationPanel = new JPanel(new PropertyLayout());
		informationPanel.setBorder(BorderFactory.createTitledBorder(getString("ECNInformation")));
		ComponentService.deployComponent(hashtable, informationPanel, new String[] {"object_name", "a2DevelopType", "a2Inventory", "a2ChangeReason", "a2PartClassification_", "a2ChangeType", "a2ApplyDate"});
		return informationPanel;
	}

	public void addFindButton() {
		informationPanel.add("5.3.left", createFindTableDialog((JTextField)getComponent("a2PartClassification")));
	}

	private JPanel createChangePanel() {
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("ChangeInformation")));
		ComponentService.deployComponent(hashtable, panel, new String[] {"a2ChangeBefore", "a2ChangeAfter"});
		return panel;
	}

	private JPanel createListPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.add("top.bind", createECRPanel());
		panel.add("top.bind", createProblemItemPanel());
		panel.add("top.bind", createSolutionItemPanel());
		panel.add("top.bind", createFilePanel());
		panel.add("top.bind", createDatasetPanel());
		return panel;
	}

	private JPanel createECRPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("RelatedECR")));
		panel.add("top.bind", ecrField = new TCComponentField());
		JPanel actionPanel = new JPanel(new ButtonLayout(ButtonLayout.HORIZONTAL, ButtonLayout.RIGHT, 10));
		actionPanel.add(actionButtons[0]);
		actionPanel.add(actionButtons[1]);
		panel.add("top.bind", actionPanel);
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
		actionPanel.add(actionButtons[2]);
		actionPanel.add(actionButtons[3]);
		panel.add("top.bind", actionPanel);
		return panel;
	}

	private JPanel createSolutionItemPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("SolutionItem")));
		solutionVector = new Vector();
		solutionList = new JList();
		solutionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		solutionList.setVisibleRowCount(5);
		solutionList.setCellRenderer(new ListCellRenderer());
		JScrollPane scrollpane = new JScrollPane(solutionList);
		scrollpane.setPreferredSize(new Dimension(200, 40));
		panel.add("top.bind", scrollpane);
		JPanel actionPanel = new JPanel(new ButtonLayout(ButtonLayout.HORIZONTAL, ButtonLayout.RIGHT, 10));
		actionPanel.add(actionButtons[4]);
		actionPanel.add(actionButtons[5]);
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
		actionPanel.add(actionButtons[6]);
		actionPanel.add(actionButtons[7]);
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
		actionPanel.add(actionButtons[8]);
		actionPanel.add(actionButtons[9]);
		panel.add("top.bind", actionPanel);
		return panel;
	}

	public void postAction() {
		TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
		if (target == null) {
			return;
		}
		try {
			if (target.getType().equals("A2ECR") && target.getProperty("release_status_list").length() > 0) {
				AIFComponentContext[] context = target.whereReferenced();
				for (int i = 0; i < context.length; i++) {
					if (context[i].getComponent().getType().equals("A2ECN")) {
						TCComponent[] components = ((TCComponent)context[i].getComponent()).getRelatedComponents("EC_reference_item_rel");
						for (int j = 0; j < components.length; j++) {
							if (components[j].equals(target)) {
								return;
							}
						}
					}
				}
				setECR((TCComponentItem)target);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	protected AthenaOperation createOperation() {
		return new ECNOperation(athena, session, hashtable, columns, problemVector, solutionVector, fileVector, datasetVector, ecr);
	}

	public void setECR(TCComponentItem ecr) {
		this.ecr = ecr;
		if (ecr == null) {
			ecrField.removeTCComponent();
		} else {
			ecrField.setTCComponent(ecr);
		}
	}

	protected void buttonPerformed(JButton button, String actionCommand) {
		super.buttonPerformed(button, actionCommand);
		AIFClipboard clipBoard = AIFPortal.getClipboard();
		if (actionCommand.equals("Add1")) {
			if (clipBoard.isEmpty()) {
				setStatus("NoECR");
				return;
			}
			Vector<TCComponent> vector = clipBoard.toVector();
			TCComponent component = vector.elementAt(0);
			try {
				// if (component.isTypeOf("A2ECR") && component.getProperty("release_status_list").length() > 0) {
				AIFComponentContext[] context = component.whereReferenced();
				for (int i = 0; i < context.length; i++) {
					if (context[i].getComponent().getType().equals("A2ECN")) {
						TCComponent[] components = ((TCComponent)context[i].getComponent()).getRelatedComponents("EC_reference_item_rel");
						for (int j = 0; j < components.length; j++) {
							if (components[j].equals(component)) {
								return;
							}
						}
					}
				}
				setECR((TCComponentItem)component);
				// }
			} catch (TCException e) {
				e.printStackTrace();
			}
		} else if (actionCommand.equals("Remove1")) {
			setECR(null);
		} else if (actionCommand.equals("Add2")) {
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
		} else if (actionCommand.equals("Remove2")) {
			int i = problemList.getSelectedIndex();
			if (i == -1) {
				setStatus("Athena", "SelectRevision");
				return;
			}
			problemVector.removeElementAt(i);
			problemList.setListData(problemVector);
		} else if (actionCommand.equals("Add3")) {
			Vector<TCComponentItemRevision> vector = getClipboardItem();
			for (int i = 0; i < vector.size(); i++) {
				TCComponentItemRevision revision = vector.elementAt(i);
				try {
					if (revision.getProperty("release_status_list").length() == 0) {
						if (!solutionVector.contains(revision)) {
							solutionVector.addElement(revision);
							TCComponentItem item = revision.getItem();
							TCComponentItemRevision[] releasedItemRevisions = item.getReleasedItemRevisions();
							if (releasedItemRevisions.length > 0) {
								if (!problemVector.contains(releasedItemRevisions)) {
									problemVector.addElement(releasedItemRevisions[0]);
									problemList.setListData(problemVector);
								} else {
									setStatus("DuplicateRevision");
								}
							}
						}
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			solutionList.setListData(solutionVector);
		} else if (actionCommand.equals("Remove3")) {
			int i = solutionList.getSelectedIndex();
			if (i == -1) {
				setStatus("Athena", "SelectRevision");
				return;
			}
			solutionVector.removeElementAt(i);
			solutionList.setListData(solutionVector);
		} else if (actionCommand.equals("Add4")) {
			JFileChooser fileChooser = new JFileChooser(new File(DatabaseService.getPreference("Workspace")));
			int status = fileChooser.showOpenDialog(this);
			if (status == JFileChooser.APPROVE_OPTION) {
				String path = fileChooser.getSelectedFile().getAbsolutePath();
				if (!fileVector.contains(path)) {
					fileVector.addElement(path);
				}
				fileList.setListData(fileVector);
			}
		} else if (actionCommand.equals("Remove4")) {
			int i = fileList.getSelectedIndex();
			if (i == -1) {
				setStatus("Athena", "SelectFile");
				return;
			}
			fileVector.removeElementAt(i);
			fileList.setListData(fileVector);
		} else if (actionCommand.equals("Add5")) {
			Vector<TCComponentDataset> vector = new Vector<TCComponentDataset>();
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
		} else if (actionCommand.equals("Remove5")) {
			int i = datasetList.getSelectedIndex();
			if (i == -1) {
				setStatus("Athena", "SelectDataset");
				return;
			}
			datasetVector.removeElementAt(i);
			datasetList.setListData(datasetVector);
		}
	}

	protected void findTable(CoupledButton button) {
		JTextField textField = button.getTextField();
		if (getComponent("a2PartClassification").equals(textField)) {
			JTextField[] textFields = new JTextField[] {button.getTextField(), (JTextField)getComponent("a2PartClassification_")};
			String[] identifiers = new String[] {"value", "description"};
			AthenaDialog dialog = new FindValuesDialog((Aegis)athena, this, textFields, button, identifiers, "PartClassification");
			athena.launch(dialog);
		} else if (getComponent("RelatedECR").equals(textField)) {
			JTextField[] textFields = new JTextField[] {button.getTextField(), (JTextField)getComponent("R.a2Client")};
			String[] identifiers = new String[] {"item_id", "a2Client"};
			AthenaDialog dialog = new FindClientDrawingDialog((Aegis)athena, this, textFields, button, identifiers);
			athena.launch(dialog);
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