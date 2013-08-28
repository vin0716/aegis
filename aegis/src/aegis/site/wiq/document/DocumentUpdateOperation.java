package aegis.site.wiq.document;

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
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

public class DocumentUpdateOperation extends AthenaOperation {
	private TCComponentItemRevision revision;
	private String[] columns;

	public DocumentUpdateOperation(Athena athena, TCSession session, TCComponentItemRevision revision, Hashtable hashtable, String[] columns) {
		super(athena);
		this.revision = revision;
		this.hashtable = hashtable;
		this.columns = columns;
	}

	public void execute() {
		super.execute();
		try {
			setProperties(revision);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setProperties(TCComponentItemRevision revision) throws TCException {
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
			String[] information = ResourceService.getStrings("DocumentPanel", columns[i]);
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
				revision.setTCProperty(property);
			}
			revision.refresh();
		}
	}
}
