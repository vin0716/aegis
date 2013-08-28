package aegis.site.isms.part;

import java.awt.Component;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

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
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

public class PartOperation extends AthenaOperation {
	private String[] columns;
	private TCComponentItem item;
	private TCComponentDataset seedPart;
	private Vector<String> vector;
	private boolean clean;

	public PartOperation(Athena athena, TCSession session, Hashtable hashtable, String[] columns, TCComponentDataset seedPart, Vector<String> vector, boolean clean) {
		super(athena);
		this.hashtable = hashtable;
		this.columns = columns;
		this.seedPart = seedPart;
		this.vector = vector;
		this.clean = clean;
	}

	public void execute() {
		try {
			TCComponentItemType itemType = (TCComponentItemType)session.getTypeComponent("Item");
			String item_id = ((JTextField)getComponent("item_id")).getText().trim();
			String object_name = ((JTextField)getComponent("object_name")).getText().trim();
			String object_desc = ((JTextComponent)getComponent("object_desc")).getText().trim();
			item = itemType.create(item_id, "A", "A2Part", object_name, object_desc, null);
			if (clean) {
				setSerial(item.getProperty("item_id"));
			}
			TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
			if (target != null && target instanceof TCComponentFolder) {
				target.add("contents", item);
			} else {
				session.getUser().getHomeFolder().add("contents", item);
			}
			//setProperties(item.getLatestItemRevision());
			if (seedPart == null) {
				DatasetService.createDataset(item.getLatestItemRevision(), "IMAN_specification", vector);
			} else {
				TCComponentDataset dataset = seedPart.saveAs(item_id);
				item.getLatestItemRevision().add("IMAN_specification", dataset);
			}
			isOperationDone = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setSerial(String id) {
		DatabaseService.setSerial("Part", "None", Integer.parseInt(id.substring(4)));
	}
}
