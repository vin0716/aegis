package aegis.site.wiq.ecn;

import java.awt.Component;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.service.ComponentService;
import athena.service.DatabaseService;
import athena.service.DatasetService;
import athena.service.PropertyService;
import athena.service.ResourceService;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

public class ECNOperation extends AthenaOperation {
	private TCSession session;
	private String[] columns;
	private TCComponentItem item;
	private Vector problemVector, impactedVector, fileVector, datasetVector;
	private TCComponent ecr;

	public ECNOperation(Athena athena, TCSession session, Hashtable hashtable, String[] columns, Vector problemVector, Vector impactedVector, Vector fileVector, Vector datasetVector, TCComponentItem ecr) {
		super(athena);
		this.session = session;
		this.hashtable = hashtable;
		this.columns = columns;
		this.problemVector = problemVector;
		this.impactedVector = impactedVector;
		this.fileVector = fileVector;
		this.datasetVector = datasetVector;
		this.ecr = ecr;
	}

	public void execute() {
		try {
			TCComponentItemType itemType = (TCComponentItemType)session.getTypeComponent("Item");
			String object_name = ((JTextField)getComponent("object_name")).getText().trim();
			item = itemType.create(getSerial(), "A", "A2ECN", object_name, new String(), null);
			setSerial(item.getProperty("item_id"));
			TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
			if (target instanceof TCComponentFolder) {
				target.add("contents", item);
			} else {
				session.getUser().getHomeFolder().add("contents", item);
			}
			setProperties();
			if (ecr != null) {
				item.add("EC_reference_item_rel", ecr);
			}
			for (int i = 0; i < problemVector.size(); i++) {
				item.add("EC_problem_item_rel", (TCComponent)problemVector.get(i));
			}
			for (int i = 0; i < impactedVector.size(); i++) {
				item.add("EC_solution_item_rel", (TCComponent)impactedVector.get(i));
			}
			TCComponentDataset[] dataset = DatasetService.createDataset(fileVector);
			if (dataset.length > 0) {
				item.add("IMAN_reference", dataset);
			}
			for (int i = 0; i < datasetVector.size(); i++) {
				item.add("IMAN_reference", (TCComponentDataset)datasetVector.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setProperties() throws Exception {
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
			if (component instanceof JComboBox) {
				String[] information = ResourceService.getStrings("ECNDialog", columns[i]);
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

	private String getSerial() {
		Calendar calendar = Calendar.getInstance();
		String year = String.valueOf(calendar.get(Calendar.YEAR)).substring(2);
		String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
		if (month.length() == 1) {
			month = "0" + month;
		}
		String pattern = year + month;
		String serial = DatabaseService.getSerial("ECN", pattern, 4);
		return "ECN" + pattern + "-" + serial;
	}

	private void setSerial(String id) {
		String serial = id.substring(8);
		DatabaseService.setSerial("ECN", id.substring(3, 7), Integer.parseInt(serial));
	}
}
