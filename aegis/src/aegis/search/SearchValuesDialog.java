package aegis.search;

import javax.swing.JTextField;

import aegis.origin.Aegis;
import athena.origin.AthenaDialog;
import athena.origin.search.AthenaSearchATTableDialog;
import athena.service.ResourceService;

public class SearchValuesDialog extends AthenaSearchATTableDialog {
	public SearchValuesDialog(Aegis aegis) {
		super(aegis);
	}

	public SearchValuesDialog(Aegis aegis, AthenaDialog dialog) {
		super(aegis, dialog);
	}
	
	public void postAction() {
		super.postAction();
		JTextField site = (JTextField)getComponent("site");
		site.setText(ResourceService.Site);
	}

}
