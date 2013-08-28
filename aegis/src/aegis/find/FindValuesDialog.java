package aegis.find;

import javax.swing.JTextField;

import aegis.origin.Aegis;
import athena.component.CoupledButton;
import athena.origin.AthenaDialog;
import athena.origin.find.AthenaFindATTableDialog;

public class FindValuesDialog extends AthenaFindATTableDialog {
	private String valuesName;

	public FindValuesDialog(Aegis aegis, JTextField[] textFields, CoupledButton button, String[] identifiers, String valuesName) {
		super(aegis, textFields, button, identifiers);
		this.valuesName = valuesName;
	}

	public FindValuesDialog(Aegis aegis, AthenaDialog dialog, JTextField[] textFields, CoupledButton button, String[] identifiers, String valuesName) {
		super(aegis, dialog, textFields, button, identifiers);
		this.valuesName = valuesName;
	}

	public void postAction() {
		((JTextField)getComponent("name")).setText(valuesName);
	}
}
