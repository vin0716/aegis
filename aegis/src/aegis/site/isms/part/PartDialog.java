package aegis.site.isms.part;

import java.awt.Dimension;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;

import org.jdesktop.swingx.JXTree;

import aegis.origin.Aegis;
import athena.application.hierarchy.HierarchyPanel;
import athena.origin.AthenaOperation;
import athena.origin.AthenaOperationDialog;
import athena.origin.AthenaTreeNode;
import athena.origin.AthenaTreeNodeData;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.service.ResourceService;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCTextService;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.VerticalLayout;

public class PartDialog extends AthenaOperationDialog {
	private HierarchyPanel hierarchyPanel;
	private JXTree tree;
	private Vector<String> vector;
	private JList list;
	private String id;
	private JPanel referencePanel;
	private JComboBox[] cadComboBox;

	public PartDialog(Aegis aegis) {
		super(aegis);
		setModal(true);
	}

	public void createComponents() {
		hierarchyPanel = new HierarchyPanel(athena, "Parts");
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
		panel.add("top.bind", createSeedPartPanel());
		panel.add("top.bind", createReferencePanel());
		return panel;
	}

	private JPanel createInformationPanel() {
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("Information")));
		ComponentService.deployComponent(hashtable, panel, new String[] {"item_id", "object_name", "object_desc"});
		return panel;
	}

	private JPanel createSeedPartPanel() {
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setBorder(BorderFactory.createTitledBorder(getString("SeedPart")));
		ComponentService.deployComponent(hashtable, panel, new String[] {"nxTemplate", "seTemplate"});
		return panel;
	}

	private JPanel createReferencePanel() {
		referencePanel = new JPanel(new VerticalLayout());
		referencePanel.setBorder(BorderFactory.createTitledBorder(getString("Reference")));
		vector = new Vector();
		list = new JList(vector);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setVisibleRowCount(5);
		JScrollPane scrollpane = new JScrollPane(list);
		scrollpane.setPreferredSize(new Dimension(200, 100));
		referencePanel.add("top.bind", scrollpane);
		JPanel actionPanel = new JPanel(new ButtonLayout(ButtonLayout.HORIZONTAL, ButtonLayout.RIGHT, 10));
		for (int i = 0; i < actionButtons.length; i++) {
			actionPanel.add(actionButtons[i]);
		}
		referencePanel.add("top.bind", actionPanel);
		return referencePanel;
	}

	public void setComponents() {
		setSeedPartComponents();
	}

	private void setSeedPartComponents() {
		JComponent[] component = (JComponent[])getComponents(new String[] {"nxTemplate", "seTemplate"});
		String[] strings = ResourceService.parse(DatabaseService.getPreference("CAD.Template"), '|');
		for (int i = 0; i < component.length; i++) {
			JComboBox comboBox = (JComboBox)component[i];
			Vector datasetvector = new Vector();
			datasetvector.addElement(new String());
			try {
				Vector<Object[]> vector = DatabaseService.getHierarchyValues(strings[i]);
				for (int j = 0; j < vector.size(); j++) {
					Object[] datum = vector.elementAt(j);
					String tc_uid = (String)datum[5];
					TCComponent tc_component = session.stringToComponent(tc_uid);
					if (tc_component != null && tc_component instanceof TCComponentDataset) {
						datasetvector.addElement(tc_component);
					}
				}
				comboBox.setModel(new DefaultComboBoxModel(datasetvector));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setListener() {
		super.setListener();
		tree.addTreeSelectionListener(this);
		JComponent[] component = (JComponent[])getComponents(new String[] {"nxTemplate", "seTemplate"});
		cadComboBox = new JComboBox[component.length];
		for (int i = 0; i < cadComboBox.length; i++) {
			cadComboBox[i] = (JComboBox)component[i];
			cadComboBox[i].addItemListener(this);
		}
	}

	protected AthenaOperation createOperation() {
		JTextField item_id = (JTextField)getComponent("item_id");
		boolean clean = item_id.getText().trim().equals(id);
		TCComponentDataset seedPart = null;
		for (int i = 0; i < cadComboBox.length; i++) {
			if (seedPart != null) {
				continue;
			}
			seedPart = (cadComboBox[i].getSelectedIndex() == 0) ? null : (TCComponentDataset)cadComboBox[i].getSelectedItem();
		}
		return new PartOperation(athena, session, hashtable, columns, seedPart, vector, clean);
	}

	protected boolean isUnique() {
		JTextField item_id = (JTextField)getComponent("item_id");
		String id = item_id.getText().trim();
		TCTextService textService = session.getTextService();
		try {
			TCComponent[] components = session.search("Item ID", new String[] {textService.getTextValue("ItemID")}, new String[] {id});
			if (components.length > 0) {
				setStatus("Athena", "IDDuplicated", id);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
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

	protected void comboBoxStateChanged(JComboBox comboBox, String actionCommand) {
		for (int i = 0; i < cadComboBox.length; i++) {
			if (comboBox != cadComboBox[i]) {
				cadComboBox[i].removeItemListener(this);
				cadComboBox[i].setSelectedIndex(0);
				cadComboBox[i].addItemListener(this);
			}
		}
		boolean seed = false;
		for (int i = 0; i < cadComboBox.length; i++) {
			if (cadComboBox[i].getSelectedIndex() > 0) {
				seed = true;
				break;
			}
		}
		if (seed) {
			vector.clear();
		}
		ComponentService.setPanelEnable(referencePanel, !seed);
	}

	public void valueChanged(TreeSelectionEvent e) {
		AthenaTreeNode node = (AthenaTreeNode)e.getPath().getLastPathComponent();
		if (node.isLeaf()) {
			JTextField item_id = (JTextField)getComponent("item_id");
			id = (String)((AthenaTreeNodeData)node.getUserObject()).get("code") + DatabaseService.getSerial("Part", "None", 4);
			item_id.setText(id);
		}
	}
}