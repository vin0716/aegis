package aegis.site.wiq.part;

import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.eclipse.swt.widgets.Shell;
import org.jdesktop.swingx.JXTree;

import aegis.find.FindValuesDialog;
import aegis.origin.Aegis;
import aegis.site.wiq.find.FindClientDrawingDialog;
import aegis.site.wiq.find.FindEquipmentDialog;
import athena.application.hierarchy.HierarchyPanel;
import athena.component.CoupledButton;
import athena.origin.AthenaDialog;
import athena.origin.AthenaOperation;
import athena.origin.AthenaOperationDialog;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.service.ResourceService;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.PropertyLayout;

public class PartDialog extends AthenaOperationDialog {
	private HierarchyPanel hierarchyPanel;
	private JXTree tree;
	private JPanel leftPanel;
	
	public PartDialog(Aegis aegis, Shell shell) {
		super(aegis);
		setModal(true);
	}

	public void createComponents() {
		hierarchyPanel = new HierarchyPanel(athena, "WIQ Part");
		athena.createPanel(hierarchyPanel);
		tree = hierarchyPanel.getTree();
		hierarchyPanel.setBorder(BorderFactory.createTitledBorder(getString("Classification")));
	}

	public void deploy() {
		mainPanel.setLayout(new HorizontalLayout());
		mainPanel.add("left.bind", createLeftPanel());
		mainPanel.add("left.bind", hierarchyPanel);
	}

	private JPanel createLeftPanel() {
		leftPanel = new JPanel(new PropertyLayout());
		deployColumns(leftPanel);
		return leftPanel;
	}

	protected void deployColumns(JPanel mainPanel) {
		String[] displayColumns = new String[] {"item_id", "R.a2PartType", "object_name", "a2PartClassification_", "R.a2Equipment", "R.a2DrawingSize", "R.a2Material", "R.a2StandardProcess", "R.a2ToleranceLimit", "R.a2Client", "R.a2ClientDrawingNumber", "object_desc"};
		ComponentService.deployComponent(hashtable, mainPanel, displayColumns);
	}

	public void addFindButton() {
		leftPanel.add("4.3.left", createFindTableDialog((JTextField)getComponent("R.a2PartClassification")));
		leftPanel.add("5.3.left", createFindTreeDialog((JTextField)getComponent("R.a2Equipment")));
		leftPanel.add("11.3.left", createFindTableDialog((JTextField)getComponent("R.a2ClientDrawingNumber")));
	}

	public void setListener() {
		super.setListener();
		JComboBox a2PartType = (JComboBox)getComponent("R.a2PartType");
		a2PartType.addItemListener(this);
		a2PartType.setActionCommand("R.a2PartType");
	}

	protected AthenaOperation createOperation() {
		return new PartOperation(athena, session, hashtable, columns);
	}

	protected void findTable(CoupledButton button) {
		JTextField textField = button.getTextField();
		if (getComponent("R.a2PartClassification").equals(textField)) {
			JTextField[] textFields = new JTextField[] {button.getTextField(), (JTextField)getComponent("a2PartClassification_")};
			String[] identifiers = new String[] {"value", "description"};
			AthenaDialog dialog = new FindValuesDialog((Aegis)athena, this, textFields, button, identifiers, "PartClassification");
			athena.launch(dialog);
		} else if (getComponent("R.a2ClientDrawingNumber").equals(textField)) {
			JTextField[] textFields = new JTextField[] {button.getTextField(), (JTextField)getComponent("R.a2Client")};
			String[] identifiers = new String[] {"item_id", "a2Client"};
			AthenaDialog dialog = new FindClientDrawingDialog((Aegis)athena, this, textFields, button, identifiers);
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

	protected void comboBoxStateChanged(JComboBox comboBox, String actionCommand) {
		if (actionCommand.equals("R.a2PartType")) {
			JTextField item_id = (JTextField)getComponent("item_id");
			JTextField toleranceLimit = (JTextField)getComponent("R.a2ToleranceLimit");
			if (comboBox.getSelectedItem().equals("")) {
				item_id.setEnabled(false);
				item_id.setText("");
			} else if (comboBox.getSelectedItem().equals("RAW MATERIALS")) {
				item_id.setEnabled(true);
				item_id.setText("");
				toleranceLimit.setEnabled(true);
			} else {
				String a2PartTypeValue = DatabaseService.getValues("PartType", comboBox.getSelectedIndex());
				item_id.setEnabled(false);
				item_id.setText(getSerial(a2PartTypeValue));
				toleranceLimit.setEnabled(false);
			}
		}
	}

	private String getSerial(String string) {
		Calendar calendar = Calendar.getInstance();
		String year = String.valueOf(calendar.get(Calendar.YEAR)).substring(2);
		String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
		if (month.length() == 1) {
			month = "0" + month;
		}
		String pattern = year + month;
		String serial = DatabaseService.getSerial("Part", pattern, 4);
		return ResourceService.Site + pattern + "-" + serial + string;
	}

	protected void documentChanged(Document document, String string) {
		// try {
		// Document a2ClientDocument = ((JTextField)getComponent("R.a2Client")).getDocument();
		// Document a2ClientDrawingNumberDocument = ((JTextField)getComponent("R.a2ClientDrawingNumber")).getDocument();
		// if (document.equals(a2ClientDocument)) {
		// String value = DatabaseService.getValuesDescription(ResourceService.getSite(), "WIQ.Client", string);
		// ((JTextField)getComponent("R.a2Client")).setText(value);
		// } else if (document.equals(a2ClientDrawingNumberDocument)) {
		// TCComponent[] components = session.search("A2Client", new String[] {textService.getTextValue("item_id")}, new String[] {string});
		// if (components.length == 0) {
		// return;
		// }
		// JTextField a2Client = (JTextField)getComponent("R.a2Client");
		// a2Client.setText(components[0].getProperty("a2Client"));
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	protected boolean isUnique() {
		String item_id = ((JTextField)getComponent("item_id")).getText();
		try {
			TCComponent[] components = session.search("Item ID", new String[] {textService.getTextValue("ItemID")}, new String[] {item_id});
			if (components.length > 0) {
				setStatus("Athena", "IDDuplicated");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
