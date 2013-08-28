package aegis.site.isms.change;

import java.awt.Dimension;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.table.TableColumnExt;

import aegis.origin.Aegis;
import athena.application.hierarchy.HierarchyPanel;
import athena.origin.AthenaOperation;
import athena.origin.AthenaOperationDialog;
import athena.origin.AthenaTreeNode;
import athena.origin.AthenaTreeNodeData;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.service.ResourceService;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCTextService;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.VerticalLayout;

public class ChangeIDDialog extends AthenaOperationDialog {
	private HierarchyPanel hierarchyPanel;
	private JXTree tree;
	private JXTable table;
	private TCComponentFolder folder;

	public ChangeIDDialog(Aegis aegis) {
		super(aegis);
		setModal(true);
	}

	public boolean isValidState() {
		TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
		return (target != null && (target instanceof TCComponentFolder));
	}

	public void createComponents() {
		hierarchyPanel = new HierarchyPanel(athena, "Parts");
		athena.createPanel(hierarchyPanel);
		tree = hierarchyPanel.getTree();
		hierarchyPanel.setBorder(BorderFactory.createTitledBorder(getString("Classification")));
	}

	public void deploy() {
		mainPanel.setLayout(new HorizontalLayout());
		mainPanel.add("left.bind", hierarchyPanel);
		mainPanel.add("left.bind", createTablePanel());
	}

	private JScrollPane createTablePanel() {
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
		table.setEditable(true);
		TableColumnExt columnExt = (TableColumnExt)table.getColumn("uid");
		columnExt.setVisible(false);
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600, 400));
		return scrollpane;
	}

	public void setListener() {
		super.setListener();
		tree.addTreeSelectionListener(this);
	}

	public void postAction() {
		folder = (TCComponentFolder)AIFUtility.getCurrentApplication().getTargetComponent();
		Vector<TCComponentItem> vector = new Vector();
		try {
			AIFComponentContext[] children = folder.getChildren();
			for (int i = 0; i < children.length; i++) {
				TCComponent child = (TCComponent)children[i].getComponent();
				if (child.isTypeOf("A2Part")) {
					vector.addElement((TCComponentItem)child);
				}
			}
			for (int i = 0; i < vector.size(); i++) {
				String[] strings = new String[4];
				TCComponentItem item = vector.elementAt(i);
				strings[1] = item.getProperty("item_id");
				strings[2] = item.getProperty("object_name");
				strings[3] = item.getUid();
				((DefaultTableModel)table.getModel()).addRow(strings);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	protected boolean isPrepared() {
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			int index = ComponentService.getColumnIndex(table, "code");
			if (((String)model.getValueAt(i, index)).equals("")) {
				setStatus("ChangeItemID");
				return false;
			}
		}
		return true;
	}

	protected boolean isUnique() {
		TCTextService textService = session.getTextService();
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			int codeIndex = ComponentService.getColumnIndex(table, "code");
			int serialIndex = ComponentService.getColumnIndex(table, "serial");
			String id = (String)model.getValueAt(i, codeIndex) + (String)model.getValueAt(i, serialIndex);
			try {
				TCComponent[] components = session.search("Item ID", new String[] {textService.getTextValue("ItemID")}, new String[] {id});
				if (components.length > 0) {
					setStatus("Athena", "IDDuplicated", id);
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	protected AthenaOperation createOperation() {
		return new ChangeIDOperation(athena, session, table);
	}

	public void valueChanged(TreeSelectionEvent e) {
		AthenaTreeNode node = (AthenaTreeNode)e.getPath().getLastPathComponent();
		if (node.isLeaf()) {
			String code = (String)((AthenaTreeNodeData)node.getUserObject()).get("code");
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			for (int i = 0; i < model.getRowCount(); i++) {
				String serial = DatabaseService.getSerial("Part", "None", 4, i);
				model.setValueAt(code, i, ComponentService.getColumnIndex(table, "code"));
				model.setValueAt(serial, i, ComponentService.getColumnIndex(table, "serial"));
			}
			table.updateUI();
		}
	}
}
