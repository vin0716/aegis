package aegis.site.isms.folder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTree;

import aegis.origin.Aegis;
import athena.application.hierarchy.HierarchyPanel;
import athena.origin.AthenaOperation;
import athena.origin.AthenaOperationDialog;
import athena.service.ComponentService;

import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.VerticalLayout;

public class FolderDialog extends AthenaOperationDialog {
	private JPanel panel;
	private JXTree tree;
	private String level;
	private int length;
	private String[] codes;

	public FolderDialog(Aegis aegis) {
		super(aegis);
		setModal(true);
	}

	public void deploy() {
		mainPanel.setLayout(new VerticalLayout());
		mainPanel.add("top.bind", createTemplatePanel());
		mainPanel.add("top.bind", createTreePanel());
	}

	private JPanel createTemplatePanel() {
		JPanel panel = new JPanel(new PropertyLayout());
		ComponentService.deployComponent(hashtable, panel, new String[] {"template"});
		return panel;
	}

	private JPanel createTreePanel() {
		panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(600, 350));
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return panel;
	}

	public void setListener() {
		super.setListener();
		JComboBox template = (JComboBox)getComponent("template");
		template.addItemListener(this);
	}

	protected boolean isPrepared() {
		if (tree == null){
			setStatus("Athena", "SelectFolder");
			return false;
		}
		return true;
	}

	protected AthenaOperation createOperation() {
		return new FolderOperation(athena, session, tree);
	}

	protected void comboBoxStateChanged(JComboBox comboBox, String actionCommand) {
		panel.removeAll();
		if (comboBox.getSelectedIndex() > 0) {
			HierarchyPanel hierarchyPanel = new HierarchyPanel(athena, (String)comboBox.getSelectedItem());
			athena.createPanel(hierarchyPanel);
			tree = hierarchyPanel.getTree();
			panel.add(BorderLayout.CENTER, hierarchyPanel);
		} else {
			tree = null;
		}
		panel.revalidate();
		panel.repaint();
	}
}
