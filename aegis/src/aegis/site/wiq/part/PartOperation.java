package aegis.site.wiq.part;

import java.awt.Component;
import java.util.Calendar;
import java.util.Hashtable;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.service.PropertyService;
import athena.service.ResourceService;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

public class PartOperation extends AthenaOperation {
	private String[] columns;
	private TCComponentItem item;

	public PartOperation(Athena athena, TCSession session, Hashtable hashtable, String[] columns) {
		super(athena);
		this.hashtable = hashtable;
		this.columns = columns;
	}

	public void execute() {
		try {
			TCComponentItemType itemType = (TCComponentItemType)session.getTypeComponent("Item");
			JComboBox a2PartType = (JComboBox)getComponent("R.a2PartType");
			JTextField item_id = (JTextField)getComponent("item_id");
			String a2PartTypeValue = DatabaseService.getValues("PartType", a2PartType.getSelectedIndex());
			String object_name = ((JTextField)getComponent("object_name")).getText().trim();
			String object_desc = ((JTextComponent)getComponent("object_desc")).getText().trim();
			if (a2PartType.getSelectedItem().equals("RAW MATERIALS")) {
				item = itemType.create(item_id.getText(), "A", "A2Part", object_name, object_desc, null);
			} else {
				item = itemType.create(getSerial(a2PartTypeValue), "A", "A2Part", object_name, object_desc, null);
				setSerial(item.getProperty("item_id"));
			}
			TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
			if (target instanceof TCComponentFolder) {
				target.add("contents", item);
			} else {
				session.getUser().getHomeFolder().add("contents", item);
			}
			setProperties(item.getLatestItemRevision());
		} catch (Exception e) {
			isOperationDone = false;
			e.printStackTrace();
		}
	}

	private void setProperties(TCComponentItemRevision revision) throws Exception {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].endsWith("item_id") || columns[i].endsWith("object_name")) {
				continue;
			}
			String column = columns[i];
			int typeIndex = column.lastIndexOf(".");
			int index = column.lastIndexOf(":");
			if (index > 0) {
				column = column.substring(typeIndex + 1, index);
			} else {
				column = column.substring(typeIndex + 1);
			}
			TCProperty property = revision.getTCProperty(column);
			Component component = getComponent(columns[i]);
			if (component instanceof JComboBox) {
				String[] information = ResourceService.getStrings("PartDialog", columns[i]);
				if (((JComboBox)component).getSelectedIndex() > 0) {
					String valuesName = ComponentService.getValuesName(information[4]);
					int selectedIndex = ((JComboBox)component).getSelectedIndex();
					PropertyService.setStringToPropertyValue(property, DatabaseService.getValues(valuesName, selectedIndex));
				}
			} else {
				PropertyService.setStringToPropertyValue(property, component);
			}
			revision.setTCProperty(property);
		}
		revision.refresh();
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

	private void setSerial(String id) {
		String serial = id.substring(8, 12);
		DatabaseService.setSerial("Part", id.substring(3, 7), Integer.parseInt(serial));
	}
}
