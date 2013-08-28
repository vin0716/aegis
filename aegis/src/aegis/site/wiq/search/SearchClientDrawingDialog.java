package aegis.site.wiq.search;

import java.awt.event.MouseEvent;

import javax.swing.JTextField;

import aegis.origin.Aegis;
import aegis.site.wiq.find.FindClientDialog;
import aegis.site.wiq.find.FindEquipmentDialog;
import athena.component.CoupledButton;
import athena.origin.AthenaDialog;
import athena.origin.search.AthenaSearchTCTableDialog;

import com.teamcenter.rac.aif.commands.open.OpenCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;

public class SearchClientDrawingDialog extends AthenaSearchTCTableDialog {
	public SearchClientDrawingDialog(Aegis aegis) {
		super(aegis);
	}

	public void addFindButton() {
		searchPanel.add("7.3.left", createFindTableDialog((JTextField)getComponent("R.a2Client")));
		searchPanel.add("9.3.left", createFindTreeDialog((JTextField)getComponent("R.a2Equipment")));
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
