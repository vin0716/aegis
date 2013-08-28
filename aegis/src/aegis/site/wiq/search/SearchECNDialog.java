package aegis.site.wiq.search;

import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import aegis.find.FindValuesDialog;
import aegis.origin.Aegis;
import athena.component.CoupledButton;
import athena.origin.AthenaDialog;
import athena.origin.search.AthenaSearchTCTableDialog;
import athena.service.ComponentService;

import com.teamcenter.rac.aif.commands.open.OpenCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;

public class SearchECNDialog extends AthenaSearchTCTableDialog {
	public SearchECNDialog(Aegis aegis) {
		super(aegis);
	}

	protected void deployColumns(JPanel searchPanel) {
		String[] displayColumns = new String[] {"item_id", "object_name", "User.user_id", "creation_date:1", "creation_date:2", "object_desc", "a2DevelopType", "a2Inventory", "a2ChangeReason", "a2PartClassification_", "a2ChangeType", "a2ApplyDate:1", "a2ApplyDate:2", "a2ChangeBefore", "a2ChangeAfter", "a2ApplyWhether", "a2RealApplyDate:1", "a2RealApplyDate:2"};
		ComponentService.deployComponent(hashtable, searchPanel, displayColumns);
	}

	public void addFindButton() {
		searchPanel.add("10.3.left", createFindTableDialog((JTextField)getComponent("a2PartClassification")));
	}

	protected void findTable(CoupledButton button) {
		JTextField textField = button.getTextField();
		if (getComponent("a2PartClassification").equals(textField)) {
			JTextField[] textFields = new JTextField[] {button.getTextField(), (JTextField)getComponent("a2PartClassification_")};
			String[] identifiers = new String[] {"value", "description"};
			AthenaDialog dialog = new FindValuesDialog((Aegis)athena, this, textFields, button, identifiers, "PartClassification");
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
