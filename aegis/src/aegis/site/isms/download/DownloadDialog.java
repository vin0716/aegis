package aegis.site.isms.download;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

import aegis.origin.Aegis;
import athena.application.bom.BOMApplication;
import athena.origin.AthenaOperation;
import athena.origin.AthenaOperationDialog;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.service.ResourceService;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.VerticalLayout;

public class DownloadDialog extends AthenaOperationDialog {
	private TCComponentFolder folder;
	private TCComponentItemRevision revision;
	private Hashtable<TCComponentDataset, TCComponentItem> datasetHashtable;
	private JXTable table;
	private DefaultTableModel model;
	private String[] tableColumnsText;
	private int[] tableColumnsWidth;
	private File dir;
	private JPanel pathPanel;

	public DownloadDialog(Aegis aegis) {
		super(aegis);
		setModal(true);
	}

	public boolean isValidState() {
		TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
		if (target == null) {
			setStatus("Athena", "SelectObject");
			return false;
		}
		if ((target instanceof TCComponentFolder || target instanceof TCComponentItem || target instanceof TCComponentItemRevision)) {
			return true;
		} else {
			setStatus("SelectValidTarget");
			return false;
		}
	}

	public void initialize() {
		super.initialize();
		TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
		try {
			if (target instanceof TCComponentFolder) {
				folder = (TCComponentFolder)target;
			} else if (target instanceof TCComponentItem) {
				revision = ((TCComponentItem)target).getLatestItemRevision();
			} else if (target instanceof TCComponentItemRevision) {
				revision = (TCComponentItemRevision)target;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public void deploy() {
		mainPanel.setLayout(new VerticalLayout());
		mainPanel.add("top.bind", createTablePanel());
		mainPanel.add("top.bind", createPathPanel());
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
//		table.setModel(new TableModel(tableColumnsText, 0));
		TableColumn column = table.getColumnModel().getColumn(0);
		column.setCellRenderer(new CheckCellRenderer());
		TableCellEditor editor = new DefaultCellEditor(new JCheckBox());
		column.setCellEditor(editor);
		TableColumnExt columnExt = (TableColumnExt)table.getColumn("uid");
		columnExt.setVisible(false);
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600, 400));
		return scrollpane;
	}

	public class TableModel extends DefaultTableModel {
		public TableModel(String[] tableColumnsText, int i) {
			super(tableColumnsText, i);
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 0) {
				return true;
			} else {
				return false;
			}
		}
	}

	class CheckCellRenderer extends JCheckBox implements TableCellRenderer {
		public CheckCellRenderer() {
			super();
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof Boolean) {
				Boolean b = (Boolean)value;
				setSelected(b.booleanValue());
			}
			setBackground(isSelected && !hasFocus ? table.getSelectionBackground() : table.getBackground());
			setForeground(isSelected && !hasFocus ? table.getSelectionForeground() : table.getForeground());
			setFont(table.getFont());
			return this;
		}
	}

	private JPanel createPathPanel() {
		pathPanel = new JPanel(new PropertyLayout());
		pathPanel.setBorder(BorderFactory.createTitledBorder(getString("DownloadPath")));
		ComponentService.deployComponent(hashtable, pathPanel, new String[] {"path"});
		pathPanel.add("1.3.left", actionButtons[0]);
		return pathPanel;
	}

	public void postAction() {
		datasetHashtable = new Hashtable();
		try {
			if (folder != null) {
				getDataset(folder);
			} else if (revision != null) {
				getDataset(revision);
			}
			Enumeration enumeration = datasetHashtable.keys();
			int i = 1;
			while (enumeration.hasMoreElements()) {
				TCComponentDataset dataset = (TCComponentDataset)enumeration.nextElement();
				TCComponentItem item = datasetHashtable.get(dataset);
				Vector vector = new Vector();
				TCComponent user = item.getRelatedComponent("owning_user");
				vector.addElement(new Boolean(true));
				vector.addElement(dataset.getProperty("object_name"));
				vector.addElement(item.getProperty("item_id"));
				vector.addElement(item.getProperty("object_name"));
				vector.addElement(user.getProperty("user_id"));
				vector.addElement(user.getProperty("user_name"));
				vector.addElement(dataset.getUid());
				((DefaultTableModel)table.getModel()).addRow(vector);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		ComponentService.setComponentSize(pathPanel, 300, 20);
	}

	private void getDataset(TCComponentItemRevision revision) throws TCException {
		AIFComponentContext[] context = revision.getChildren();
		for (int i = 0; i < context.length; i++) {
			if (context[i].getComponent() instanceof TCComponentDataset) {
				TCComponentDataset dataset = (TCComponentDataset)context[i].getComponent();
				datasetHashtable.put((TCComponentDataset)context[i].getComponent(), revision.getItem());
			}
		}
	}

	private void getDataset(TCComponentFolder parent) throws TCException {
		AIFComponentContext[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			TCComponent child = (TCComponent)children[i].getComponent();
			if (child instanceof TCComponentFolder) {
				getDataset((TCComponentFolder)child);
			} else if (child instanceof TCComponentItem) {
				TCComponentItem item = (TCComponentItem)child;
				TCComponentItemRevision revision = item.getLatestItemRevision();
				AIFComponentContext[] context = revision.getChildren();
				for (int j = 0; j < context.length; j++) {
					if (context[j].getComponent() instanceof TCComponentDataset) {
						datasetHashtable.put((TCComponentDataset)context[j].getComponent(), item);
					}
				}
			}
		}
	}

	protected AthenaOperation createOperation() {
		return new DownloadOperation(athena, session, datasetHashtable, (DefaultTableModel)table.getModel(), folder, revision, dir);
	}

	protected void buttonPerformed(JButton button, String actionCommand) {
		super.buttonPerformed(button, actionCommand);
		if (actionCommand.equals("Folder")) {
			JFileChooser fileChooser = new JFileChooser(new File(DatabaseService.getPreference("Workspace")));
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int status = fileChooser.showOpenDialog(this);
			if (status == JFileChooser.APPROVE_OPTION) {
				dir = fileChooser.getSelectedFile();
				String path = fileChooser.getSelectedFile().getAbsolutePath();
				((JTextField)getComponent("path")).setText(path);
			}
		}
	}
}
