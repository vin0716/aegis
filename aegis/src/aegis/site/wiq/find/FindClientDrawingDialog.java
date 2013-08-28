package aegis.site.wiq.find;

import javax.swing.JTextField;

import aegis.origin.Aegis;
import athena.component.CoupledButton;
import athena.origin.AthenaDialog;
import athena.origin.find.AthenaFindTCTableDialog;
import athena.service.ComponentService;

public class FindClientDrawingDialog extends AthenaFindTCTableDialog {
	public FindClientDrawingDialog(Aegis aegis, JTextField[] textFields, CoupledButton button, String[] identifiers) {
		super(aegis, textFields, button, identifiers);
		setModal(true);
	}

	public FindClientDrawingDialog(Aegis aegis, AthenaDialog dialog, JTextField[] textFields, CoupledButton button, String[] identifiers) {
		super(aegis, dialog, textFields, button, identifiers);
		setModal(true);
	}
}
