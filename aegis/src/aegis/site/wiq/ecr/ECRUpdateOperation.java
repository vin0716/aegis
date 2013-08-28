package aegis.site.wiq.ecr;

import java.awt.Component;
import java.util.Hashtable;

import javax.swing.JComboBox;

import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.service.PropertyService;
import athena.service.ResourceService;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;

public class ECRUpdateOperation extends AthenaOperation {
	private TCComponentItem item;
	private String[] columns;

	public ECRUpdateOperation(Athena athena, TCComponentItem item, Hashtable hashtable, String[] columns) {
		super(athena);
		this.item = item;
		this.hashtable = hashtable;
		this.columns = columns;
	}

	public void execute() {
		try {
			setProperties(item);
		} catch (Exception e) {
			isOperationDone = false;
			e.printStackTrace();
		}
	}

	private void setProperties(TCComponentItem item) throws TCException {
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
			TCProperty property = item.getTCProperty(column);
			Component component = getComponent(columns[i]);
			String[] information = ResourceService.getStrings("ECRPanel", columns[i]);
			if (!ComponentService.equal(property, component, information)) {
				if (component instanceof JComboBox) {
					if (((JComboBox)component).getSelectedIndex() > 0) {
						String valuesName = ComponentService.getValuesName(information[4]);
						int selectedIndex = ((JComboBox)component).getSelectedIndex();
						PropertyService.setStringToPropertyValue(property, DatabaseService.getValues(valuesName, selectedIndex));
					}
				} else {
					PropertyService.setStringToPropertyValue(property, component);
				}
				item.setTCProperty(property);
			}
			item.refresh();
		}
	}
}
