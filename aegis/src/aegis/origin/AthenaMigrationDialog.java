package aegis.origin;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import athena.interfaces.InterfaceOperationStopListener;
import athena.origin.Athena;
import athena.origin.AthenaOperationDialog;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.service.ExcelService;
import athena.utility.SimpleFilter;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.VerticalLayout;

public class AthenaMigrationDialog extends AthenaOperationDialog {
	protected JScrollPane scrollPane;
	protected JXTable table;
	protected DefaultTableModel model;
	protected JComboBox comboBox;
	protected JCheckBox checkBox;

	public AthenaMigrationDialog(Athena athena) {
		super(athena);
	}

	public void createComponents() {
		comboBox = new JComboBox(getStrings("MigrationType"));
		checkBox = new JCheckBox(getString("Athena", "GoOnError"));
	}

	public void deploy() {
		mainPanel.setLayout(new VerticalLayout());
		mainPanel.add("top.bind", createTablePanel());
		mainPanel.add("top.bind", createTypePanel());
	}

	private JScrollPane createTablePanel() {
		table = ComponentService.createTable();
		scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(800, 600));
		return scrollPane;
	}

	private JPanel createTypePanel() {
		JPanel panel = new JPanel(new HorizontalLayout());
		JPanel actionPanel = new JPanel(new ButtonLayout(ButtonLayout.HORIZONTAL, ButtonLayout.RIGHT, 10));
		for (int i = 0; i < actionButtons.length; i++) {
			actionPanel.add(actionButtons[i]);
		}
		panel.add("left.bind", new JLabel(getString("Type")));
		panel.add("left.bind", comboBox);
		panel.add("left.bind", checkBox);
		panel.add("right.bind", actionPanel);
		return panel;
	}

	public void setComponents() {
		stopButton.setVisible(true);
	}

	public void setListener() {
		super.setListener();
		comboBox.setActionCommand("MigrationType");
		comboBox.addItemListener(this);
	}

	protected void clearTable() {
		for (int i = table.getRowCount() - 1; i >= 0; i--) {
			((DefaultTableModel)table.getModel()).removeRow(i);
		}
	}

	protected void buttonPerformed(JButton button, String actionCommand) {
		super.buttonPerformed(button, actionCommand);
		if (actionCommand.equals("Upload")) {
			JFileChooser fileChooser = new JFileChooser(new File(DatabaseService.getPreference("Workspace")));
			int status = fileChooser.showOpenDialog(AthenaMigrationDialog.this);
			if (status == JFileChooser.APPROVE_OPTION) {
				try {
					upload(fileChooser.getSelectedFile());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else if (actionCommand.equals("Download")) {
			try {
				JFileChooser fileChooser = new JFileChooser();
				FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
				fileChooser.removeChoosableFileFilter(fileFilter);
				fileChooser.addChoosableFileFilter(new SimpleFilter("xls", "*.xls"));
				if (fileChooser.showSaveDialog(AIFUtility.getActiveDesktop()) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (file.exists()) {
						int decision = athena.showConfirmDialog(this, "Athena", "FileExistDoYouWantRewrite", JOptionPane.YES_NO_OPTION);
						if (decision == JOptionPane.YES_OPTION) {
							file.delete();
						} else {
							return;
						}
					}
					ExcelService.downloadTable(table, "MigrationDialog");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (actionCommand.equals("Stop")) {
			if (operation != null) {
				((InterfaceOperationStopListener)operation).operationStop();
			}
		}
	}

	protected void upload(File file) {
	}
}