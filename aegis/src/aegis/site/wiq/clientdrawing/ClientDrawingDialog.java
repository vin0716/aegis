package aegis.site.wiq.clientdrawing;

import javax.swing.JPanel;
import javax.swing.JTextField;

import aegis.origin.Aegis;
import aegis.site.wiq.find.FindClientDialog;
import aegis.site.wiq.find.FindEquipmentDialog;
import athena.component.CoupledButton;
import athena.origin.AthenaDialog;
import athena.origin.AthenaOperation;
import athena.origin.AthenaOperationDialog;
import athena.service.ComponentService;

import com.teamcenter.rac.kernel.TCComponent;

public class ClientDrawingDialog extends AthenaOperationDialog {
	public ClientDrawingDialog(Aegis aegis) {
		super(aegis);
		setModal(true);
	}

	protected void deployColumns(JPanel mainPanel) {
		String[] displayColumns = new String[] {"item_id", "object_name", "R.a2Client", "R.a2ProductCode", "R.a2Equipment", "object_desc"};
		ComponentService.deployComponent(hashtable, mainPanel, displayColumns);
	}

	public void addFindButton() {
		mainPanel.add("3.3.left", createFindTableDialog((JTextField)getComponent("R.a2Client")));
		mainPanel.add("5.3.left", createFindTreeDialog((JTextField)getComponent("R.a2Equipment")));
	}

	protected AthenaOperation createOperation() {
		return new ClientDrawingOperation(athena, session, hashtable, columns);
	}

	protected void findTable(CoupledButton button) {
		JTextField textField = button.getTextField();
		if (getComponent("R.a2Client").equals(textField)) {
			JTextField[] textFields = new JTextField[] {button.getTextField()};
			String[] identifiers = new String[] {"name"};
			AthenaDialog dialog = new FindClientDialog((Aegis)athena, this, textFields, button, identifiers);
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

	protected boolean isUnique() {
		String item_id = ((JTextField)getComponent("item_id")).getText();
		try {
			TCComponent[] components = session.search("Item ID", new String[] {textService.getTextValue("ItemID")}, new String[] {item_id});
			if (components.length > 0) {
				setStatus("Athena", "IDDuplicated");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
