package aegis.site.isms.search;

import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import aegis.origin.Aegis;
import athena.component.CoupledButton;
import athena.find.FindUserDialog;
import athena.origin.search.AthenaSearchTCTableDialog;
import athena.service.ComponentService;

public class SearchPartDialog extends AthenaSearchTCTableDialog {
	public SearchPartDialog(Aegis aegis) {
		super(aegis);
	}

	public void deploy() {
		super.deploy();
		searchPanel.add("2.3.left", createFindTableDialog((JTextField)getComponent("item_id")));
		searchPanel.add("5.3.left", createFindTableDialog((JTextField)getComponent("User.user_id")));
	}

	protected void findTable(CoupledButton button) {
		JTextField textField = button.getTextField();
		if (getComponent("item_id").equals(textField)) {
			athena.launchDialog("FindPartDialog", new Object[] {this, button.getTextField(), button});
		} else if (getComponent("User.user_id").equals(textField)) {
			FindUserDialog dialog = new FindUserDialog(athena, this, new JTextField[] {button.getTextField()}, button, new String[] {"user_id"});
			athena.launch(dialog);
		}
	}

	protected boolean isSearchValueValid() {
		super.isSearchValueValid();
		String value = ComponentService.getComponentValue(getComponent("item_id"));
		if (value.indexOf("*") > -1) {
			setStatus("AthenaSearchDialog", "AsteriskNotAllowed", " : ID");
			getComponent("item_id").grabFocus();
			return false;
		}
		return true;
	}

	protected void open() {
		// long aid = (Long)table.getModel().getValueAt(table.getSelectedRow(), ComponentService.getColumnIndex(table, "aid"));
		// athena.getCurrentApplication().open(new AthenaObject(aid));
	}

	public void mouseDoubleClicked(MouseEvent mouseEvent) {
		if (mouseEvent.getSource().equals(table) && table.getSelectedRow() >= 0) {
			open();
		}
	}
}
