package aegis.site.wiq.migration;

import java.io.File;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import aegis.origin.Aegis;
import aegis.origin.AthenaMigrationDialog;
import athena.origin.AthenaOperation;
import athena.service.ComponentService;
import athena.service.ExcelService;
import athena.service.ResourceService;

import com.teamcenter.rac.kernel.TCComponent;

public class MigrationDialog extends AthenaMigrationDialog {
	public MigrationDialog(Aegis aegis) {
		super(aegis);
	}

	protected void comboBoxStateChanged(JComboBox comboBox, String actionCommand) {
		if (actionCommand.equals("MigrationType")) {
			clearTable();
			if (comboBox.getSelectedIndex() == 0) {
				return;
			}
			String[] columnData = getStrings(comboBox.getSelectedItem() + ".TableColumns");
			String[] tableColumns = new String[columnData.length];
			String[] tableHeaders = new String[columnData.length];
			int[] tableColumnsWidth = new int[columnData.length];
			for (int i = 0; i < columnData.length; i++) {
				String[] strings = ResourceService.parse(columnData[i], ':');
				tableColumns[i] = strings[0];
				tableHeaders[i] = strings[0];
				tableColumnsWidth[i] = Integer.parseInt(strings[1]);
			}
			table = ComponentService.createTable(tableColumns, tableHeaders, tableColumnsWidth);
			for (int i = 0; i < table.getColumnCount(); i++) {
				TableColumn column = table.getColumnModel().getColumn(i);
				column.setCellRenderer(new MigrationTableRenderer());
			}
			model = (DefaultTableModel)table.getModel();
			scrollPane.setViewportView(table);
		}
	}

	protected void upload(File file) {
		clearTable();
		if (comboBox.getSelectedIndex() == 0) {
			return;
		}
		int startRow = getInt(comboBox.getSelectedItem() + ".StartRow");
		Vector<String[]> vector = ExcelService.importExcel(file, getString(comboBox.getSelectedItem() + ".SheetName"), startRow);
		for (int i = 0; i < vector.size(); i++) {
			((DefaultTableModel)table.getModel()).addRow(vector.elementAt(i));
		}
	}

	protected boolean check() {
		if (!checkMandatoryField()) {
			return false;
		}
		if (!checkDuplicate()) {
			return false;
		}
		if (comboBox.getSelectedItem() == getString("Client Drawing")) {
			if (!checkClientDrawingValidate()) {
				return false;
			}
		} else if (comboBox.getSelectedItem() == getString("Document Template")) {
			if (!checkDocumentTemplateValidate()) {
				return false;
			}
		}
		return true;
	}

	public boolean checkClientDrawingValidate() {
		boolean check = true;
		for (int i = 0; i < model.getRowCount(); i++) {
			String path = ((String)model.getValueAt(i, 12)).trim();
			String name = ((String)model.getValueAt(i, 7)).trim();
			if (name.length() > 0 && path.length() > 0) {
				File file = new File(path + java.io.File.separator + name);
				if (!file.exists()) {
					check = false;
					model.setValueAt(new String("Fail"), i, model.getColumnCount() - 2);
					model.setValueAt("File is not Exist", i, model.getColumnCount() - 1);
				}
			}
		}
		return check;
	}

	public boolean checkDocumentTemplateValidate() {
		boolean check = true;
		for (int i = 0; i < model.getRowCount(); i++) {
			String path = ((String)model.getValueAt(i, 5)).trim();
			String name = ((String)model.getValueAt(i, 4)).trim();
			if (name.length() > 0 && path.length() > 0) {
				File file = new File(path + java.io.File.separator + name);
				if (!file.exists()) {
					check = false;
					model.setValueAt(new String("Fail"), i, model.getColumnCount() - 2);
					model.setValueAt("File is not Exist", i, model.getColumnCount() - 1);
				}
			}
		}
		return check;
	}

	protected boolean checkMandatoryField() {
		int[] mandatoryColumns = getInts(comboBox.getSelectedItem() + ".MandatoryFields");
		boolean check = true;
		for (int i = 0; i < model.getRowCount(); i++) {
			for (int j = 0; j < mandatoryColumns.length; j++) {
				String columValue = (String)model.getValueAt(i, mandatoryColumns[j]);
				if (columValue.equals("") || columValue.length() == 0) {
					check = false;
					model.setValueAt(new String("Fail"), i, model.getColumnCount() - 2);
					model.setValueAt(model.getColumnName(mandatoryColumns[j]) + " is Empty", i, model.getColumnCount() - 1);
				}
			}
		}
		return check;
	}

	protected boolean checkDuplicate() {
		boolean check = true;
		for (int i = 0; i < model.getRowCount(); i++) {
			String id = (String)model.getValueAt(i, 0);
			if (isDuplicated(id)) {
				check = false;
				model.setValueAt(new String("Fail"), i, model.getColumnCount() - 2);
				model.setValueAt(model.getColumnName(0) + " is Duplicated", i, model.getColumnCount() - 1);
			}
			table.repaint();
		}
		return check;
	}

	private boolean isDuplicated(String id) {
		TCComponent[] components;
		try {
			components = session.search("Item ID", new String[] {textService.getTextValue("ItemID")}, new String[] {id});
			return components.length > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	protected AthenaOperation createOperation() {
		if (check()){
			if (comboBox.getSelectedItem().equals("Client Drawing")) {
				return new ClientDrawingMigrationOperation(athena, table, checkBox.isSelected());
			} else if (comboBox.getSelectedItem().equals("Document Template")) {
				return new DocumentTemplateMigrationOperation(athena, table, checkBox.isSelected());
			}
		}
		return null;
	}
}
