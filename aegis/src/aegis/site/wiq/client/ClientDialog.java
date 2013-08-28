package aegis.site.wiq.client;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.text.JTextComponent;

import aegis.origin.Aegis;
import athena.component.CoupledButton;
import athena.origin.AthenaOperation;
import athena.origin.AthenaOperationDialog;
import athena.utility.ExtensionFileFilter;

public class ClientDialog extends AthenaOperationDialog {
	public ClientDialog(Aegis aegis) {
		super(aegis);
	}

	// protected void deploy() {
	// mainPanel.setLayout(new PropertyLayout());
	// ComponentService.deployComponent(hashtable, mainPanel, new String[] {"code", "name", "staff", "address", "type", "phone", "fax", "description"});
	// }
	protected AthenaOperation createOperation() {
		String code = ((JTextComponent)getComponent("code")).getText();
		String name = ((JTextComponent)getComponent("name")).getText();
		String staff = ((JTextComponent)getComponent("staff")).getText();
		String address = ((JTextComponent)getComponent("address")).getText();
		String type = (String)((JComboBox)getComponent("type")).getSelectedItem();
		String phone = ((JTextComponent)getComponent("phone")).getText();
		String fax = ((JTextComponent)getComponent("fax")).getText();
		String description = ((JTextComponent)getComponent("description")).getText();
		return new ClientOperation(athena, code, name, staff, address, type, phone, fax, description);
	}
}
