package aegis.site.wiq.find;

import javax.swing.JTextField;

import athena.component.CoupledButton;
import athena.origin.Athena;
import athena.origin.AthenaDialog;
import athena.origin.find.AthenaFindATTableDialog;

public class FindClientDialog extends AthenaFindATTableDialog {
	public FindClientDialog(Athena athena, JTextField[] textFields, CoupledButton button, String[] identifiers) {
		super(athena, textFields, button, identifiers);
	}

	public FindClientDialog(Athena athena, AthenaDialog dialog, JTextField[] textFields, CoupledButton button, String[] identifiers) {
		super(athena, dialog, textFields, button, identifiers);
	}
}
