package aegis.site.wiq.document;

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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.swingx.JXTree;

import aegis.origin.Aegis;
import athena.application.hierarchy.HierarchyPanel;
import athena.origin.AthenaOperation;
import athena.origin.AthenaOperationDialog;
import athena.origin.AthenaTreeNode;
import athena.service.ComponentService;
import athena.service.DatabaseService;

import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.VerticalLayout;

public class DocumentDialog extends AthenaOperationDialog {
	private HierarchyPanel hierarchyPanel;
	private JXTree tree;
	private String tc_uid;
	private Vector<String> vector;
	private JList list;

	public DocumentDialog(Aegis aegis) {
		super(aegis);
		setModal(true);
	}

	public void createComponents() {
		hierarchyPanel = new HierarchyPanel(athena, "Documents");
		athena.createPanel(hierarchyPanel);
		tree = hierarchyPanel.getTree();
		hierarchyPanel.setBorder(BorderFactory.createTitledBorder(getString("Classification")));
	}

	public void deploy() {
		mainPanel.setLayout(new HorizontalLayout());
		mainPanel.add("left.bind", createLeftPanel());
		mainPanel.add("left.bind", hierarchyPanel);
	}

	private JPanel createLeftPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.add("top.bind", createInformationPanel());
		panel.add("top.bind", createReferencePanel());
		return panel;
	}

	private JPanel createInformationPanel() {
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("Information")));
		ComponentService.deployComponent(hashtable, panel, new String[] {"R.a2DocumentType", "R.a2ProjectCode", "projectName", "object_name", "object_desc"});
		return panel;
	}

	private JPanel createReferencePanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("Reference")));
		vector = new Vector();
		list = new JList(vector);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setVisibleRowCount(5);
		JScrollPane scrollpane = new JScrollPane(list);
		scrollpane.setPreferredSize(new Dimension(200, 100));
		panel.add("top.bind", scrollpane);
		JPanel actionPanel = new JPanel(new ButtonLayout(ButtonLayout.HORIZONTAL, ButtonLayout.RIGHT, 10));
		for (int i = 0; i < actionButtons.length; i++) {
			actionPanel.add(actionButtons[i]);
		}
		panel.add("top.bind", actionPanel);
		return panel;
	}

	public void setListener() {
		super.setListener();
		tree.addTreeSelectionListener(this);
	}

	protected AthenaOperation createOperation() {
		return new DocumentOperation(athena, session, hashtable, columns, tc_uid, vector);
	}

	protected boolean isPrepared() {
		return tc_uid != null;
	}

	protected void buttonPerformed(JButton button, String actionCommand) {
		super.buttonPerformed(button, actionCommand);
		if (actionCommand.equals("Add")) {
			JFileChooser fileChooser = new JFileChooser(new File(DatabaseService.getPreference("Workspace")));
			int status = fileChooser.showOpenDialog(this);
			if (status == JFileChooser.APPROVE_OPTION) {
				String path = fileChooser.getSelectedFile().getAbsolutePath();
				vector.addElement(path);
				list.setListData(vector);
			}
		} else if (actionCommand.equals("Remove")) {
			int i = list.getSelectedIndex();
			if (i == -1) {
				setStatus("Athena", "SelectFile");
				return;
			}
			vector.removeElementAt(i);
			list.setListData(vector);
		}
	}

	public void valueChanged(TreeSelectionEvent e) {
		AthenaTreeNode node = (AthenaTreeNode)e.getPath().getLastPathComponent();
		if (node.isLeaf()) {
			JTextField a2DocumentType = (JTextField)getComponent("R.a2DocumentType");
			a2DocumentType.setText((String)node.get("value"));
			tc_uid = (String)node.get("tc_uid");
		} else {
			tc_uid = null;
		}
	}
}