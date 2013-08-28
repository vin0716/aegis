package aegis.site.wiq.part;

import javax.swing.JTextField;

import aegis.origin.Aegis;
import aegis.site.wiq.find.FindClientDrawingDialog;
import aegis.site.wiq.find.FindEquipmentDialog;
import athena.component.CoupledButton;
import athena.origin.AthenaDialog;
import athena.origin.AthenaOperation;
import athena.origin.AthenaTCPanel;
import athena.service.ComponentService;
import athena.service.DatabaseService;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.PropertyLayout;

public class PartPanel extends AthenaTCPanel {
	public PartPanel(Aegis aegis, TCComponentItem item) {
		super(aegis, item);
	}

	public PartPanel(Aegis aegis, TCComponentItemRevision revision) {
		super(aegis, revision);
	}

	public void deploy() {
		mainPanel.setLayout(new PropertyLayout());
		ComponentService.deployComponent(hashtable, mainPanel, new String[] {"a2PartType_", "a2PartClassification_", "R.a2Equipment", "R.a2DrawingSize", "R.a2Material", "R.a2StandardProcess", "R.a2ToleranceLimit", "R.a2Client", "R.a2ClientDrawingNumber", "R.object_desc"});
		if (isEditable()) {
			mainPanel.add("3.3.left", createFindTreeDialog((JTextField)getComponent("R.a2Equipment")));
			mainPanel.add("9.3.left", createFindTableDialog((JTextField)getComponent("R.a2ClientDrawingNumber")));
		}
	}

	public void setComponents() {
		JTextField a2PartType = (JTextField)getComponent("R.a2PartType");
		String partType = DatabaseService.getValuesDescription("PartType", a2PartType.getText());
		((JTextField)getComponent("a2PartType_")).setText(partType);
		if (partType.equals("M")) {
			((JTextField)getComponent("R.a2ToleranceLimit")).setEnabled(true);
		}
		JTextField a2PartClassification = (JTextField)getComponent("R.a2PartClassification");
		String value = DatabaseService.getValuesDescription("PartClassification", a2PartClassification.getText());
		((JTextField)getComponent("a2PartClassification_")).setText(value);
	}

	protected void findTable(CoupledButton button) {
		JTextField textField = button.getTextField();
		if (getComponent("R.a2ClientDrawingNumber").equals(textField)) {
			JTextField[] textFields = new JTextField[] {button.getTextField(), (JTextField)getComponent("R.a2Client")};
			String[] identifiers = new String[] {"item_id", "a2Client"};
			AthenaDialog dialog = new FindClientDrawingDialog((Aegis)athena, textFields, button, identifiers);
			athena.launch(dialog);
		}
	}

	protected void findTree(CoupledButton button) {
		JTextField textField = button.getTextField();
		if (getComponent("R.a2Equipment").equals(textField)) {
			AthenaDialog dialog = new FindEquipmentDialog((Aegis)athena, new JTextField[] {button.getTextField()}, button, new String[] {"value"});
			athena.launch(dialog);
		}
	}

	protected AthenaOperation createOperation() {
		return new PartUpdateOperation(athena, revision, hashtable, columns);
	}
}