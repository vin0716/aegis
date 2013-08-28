package aegis.site.wiq.search;

import java.awt.event.MouseEvent;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import aegis.find.FindValuesDialog;
import aegis.origin.Aegis;
import aegis.site.wiq.find.FindClientDrawingDialog;
import aegis.site.wiq.find.FindEquipmentDialog;
import athena.component.CoupledButton;
import athena.find.FindUserDialog;
import athena.origin.AthenaDialog;
import athena.origin.search.AthenaSearchTCTableDialog;
import athena.service.ComponentService;
import athena.service.DatabaseService;

import com.teamcenter.rac.aif.commands.open.OpenCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;

public class SearchPartDialog extends AthenaSearchTCTableDialog {
	public SearchPartDialog(Aegis aegis) {
		super(aegis);
	}

	protected void deployColumns(JPanel searchPanel) {
		String[] displayColumns = new String[] {"item_id", "object_name", "User.user_id", "R.creation_date:1", "R.creation_date:2", "R.object_desc", "a2PartType_", "a2PartClassification_", "R.a2Equipment", "R.a2DrawingSize", "R.a2Material", "R.a2StandardProcess", "R.a2ToleranceLimit", "R.a2Client", "R.a2ClientDrawingNumber"};
		ComponentService.deployComponent(hashtable, searchPanel, displayColumns);
	}

	public void addFindButton() {
		searchPanel.add("3.3.left", createFindTableDialog((JTextField)getComponent("User.user_id")));
		searchPanel.add("8.3.left", createFindTableDialog((JTextField)getComponent("R.a2PartClassification")));
		searchPanel.add("9.3.left", createFindTreeDialog((JTextField)getComponent("R.a2Equipment")));
		searchPanel.add("15.3.left", createFindTableDialog((JTextField)getComponent("R.a2ClientDrawingNumber")));
	}

	public void setListener() {
		super.setListener();
		JComboBox a2PartType = (JComboBox)getComponent("a2PartType_");
		a2PartType.addItemListener(this);
		a2PartType.setActionCommand("a2PartType_");
	}

	protected void findTable(CoupledButton button) {
		JTextField textField = button.getTextField();
		if (getComponent("User.user_id").equals(textField)) {
			FindUserDialog dialog = new FindUserDialog(athena, this, new JTextField[] {button.getTextField()}, button, new String[] {"user_id"});
			athena.launch(dialog);
		} else if (getComponent("R.a2PartClassification").equals(textField)) {
			JTextField[] textFields = new JTextField[] {button.getTextField(), (JTextField)getComponent("a2PartClassification_")};
			String[] identifiers = new String[] {"value", "description"};
			AthenaDialog dialog = new FindValuesDialog((Aegis)athena, this, textFields, button, identifiers, "PartClassification");
			athena.launch(dialog);
		} else if (getComponent("R.a2ClientDrawingNumber").equals(textField)) {
			JTextField[] textFields = new JTextField[] {button.getTextField(), (JTextField)getComponent("R.a2Client")};
			String[] identifiers = new String[] {"item_id", "a2Client"};
			AthenaDialog dialog = new FindClientDrawingDialog((Aegis)athena, this, textFields, button, identifiers);
			athena.launch(dialog);
		}
	}

	protected void findTree(CoupledButton button) {
		JTextField textField = button.getTextField();
		if (getComponent("R.a2Equipment").equals(textField)) {
			AthenaDialog dialog = new FindEquipmentDialog((Aegis)athena, this, new JTextField[] {button.getTextField()}, button, new String[] {"value"});
			athena.launch(dialog);
		}
	}

	protected void comboBoxStateChanged(JComboBox comboBox, String actionCommand) {
		if (actionCommand.equals("a2PartType_")) {
			JTextField a2PartType = (JTextField)getComponent("R.a2PartType");
			if (comboBox.getSelectedItem().equals("")) {
				a2PartType.setText("");
			} else {
				String a2PartTypeValue = DatabaseService.getValues("PartType", comboBox.getSelectedIndex());
				a2PartType.setText(a2PartTypeValue);
			}
		}
	}

	protected void open() {
		if (table.getSelectedRow() == -1) {
			return;
		}
		String id = (String)table.getValueAt(table.getSelectedRow(), 0);
		try {
			TCComponent[] components = session.search("Item ID", new String[] {textService.getTextValue("ItemID")}, new String[] {id});
			if (components.length == 0) {
				return;
			}
			OpenCommand opencommand = new OpenCommand(AIFUtility.getActiveDesktop(), components[0]);
			opencommand.executeModeless();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void mouseDoubleClicked(MouseEvent mouseEvent) {
		if (mouseEvent.getSource().equals(table) && table.getSelectedRow() >= 0) {
			open();
		}
	}
}
