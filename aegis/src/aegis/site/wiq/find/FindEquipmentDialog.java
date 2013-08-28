package aegis.site.wiq.find;

import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import aegis.origin.Aegis;
import athena.component.CoupledButton;
import athena.origin.AthenaDialog;
import athena.origin.AthenaTreeNode;
import athena.origin.find.AthenaFindTreeDialog;
import athena.service.ComponentService;

public class FindEquipmentDialog extends AthenaFindTreeDialog {
	public FindEquipmentDialog(Aegis aegis, AthenaDialog dialog, JTextField[] textFields, CoupledButton button, String[] identifiers) {
		super(aegis, dialog, textFields, button, identifiers, "Equipment");
		setModal(true);
	}

	public FindEquipmentDialog(Aegis aegis, JTextField[] textFields, CoupledButton button, String[] identifiers) {
		super(aegis, textFields, button, identifiers, "Equipment");
		setModal(true);
	}

	public void deploy() {
		super.deploy();
		ComponentService.deployComponent(hashtable, searchPanel, new String[] {"value"});
	}

	public void setText() {
		TreePath path = tree.getSelectionPath();
		if (path == null) {
			return;
		}
		AthenaTreeNode node = (AthenaTreeNode)path.getLastPathComponent();
		if (node.isLeaf()) {
			textFields[0].setText(((AthenaTreeNode)node.getParent()).get("value") + "/" + node.get("value"));
		}
		close();
	}
}
