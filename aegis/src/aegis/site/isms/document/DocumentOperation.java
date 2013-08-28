package aegis.site.isms.document;

import java.awt.Component;
import java.io.File;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.service.DatabaseService;
import athena.service.DatasetService;
import athena.service.PropertyService;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;

public class DocumentOperation extends AthenaOperation {
	private TCSession session;
	private String[] columns;
	private TCComponentItem item;
	private String tc_uid;
	private Vector<String> vector;

	public DocumentOperation(Athena athena, TCSession session, Hashtable hashtable, String[] columns, String tc_uid, Vector<String> vector) {
		super(athena);
		this.session = session;
		this.hashtable = hashtable;
		this.columns = columns;
		this.tc_uid = tc_uid;
		this.vector = vector;
	}

	public void execute() {
		try {
			TCComponentItemType itemType = (TCComponentItemType)session.getTypeComponent("Item");
			// String item_id = ((JTextField)getComponent("item_id")).getText().trim();
			String object_name = ((JTextField)getComponent("object_name")).getText().trim();
			String object_desc = ((JTextComponent)getComponent("object_desc")).getText().trim();
			item = itemType.create(getSerial(), "A", "A2Document", object_name, object_desc, null);
			setSerial(item.getProperty("item_id"));
			TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
			if (target instanceof TCComponentFolder) {
				target.add("contents", item);
			} else {
				session.getUser().getHomeFolder().add("contents", item);
			}
			setProperties(item.getLatestItemRevision());
			attachTemplate(item.getLatestItemRevision());
			DatasetService.createDataset(item.getLatestItemRevision(), "IMAN_specification", vector);
			isOperationDone = true;
		} catch (Exception e) {
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
			PropertyService.setStringToPropertyValue(property, component);
			revision.setTCProperty(property);
		}
		revision.refresh();
	}

	private void attachTemplate(TCComponentItemRevision revision) throws Exception {
		TCComponentDataset dataset = (TCComponentDataset)session.stringToComponent(tc_uid);
		revision.add("IMAN_specification", dataset.saveAs(dataset.getProperty("object_name")));
	}

	private String getSerial() {
		Calendar calendar = Calendar.getInstance();
		String year = String.valueOf(calendar.get(Calendar.YEAR)).substring(2);
		String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
		if (month.length() == 1) {
			month = "0" + month;
		}
		String pattern = year + month;
		String serial = DatabaseService.getSerial("DOC", pattern, 4);
		return "DOC" + pattern + "-" + serial;
	}

	private void setSerial(String id) {
		DatabaseService.setSerial("Athena", "None", Integer.parseInt(id.substring(id.length() - 5)));
	}
}
