package aegis.site.wiq.clientdrawing;

import java.awt.Component;
import java.util.Hashtable;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.service.PropertyService;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

public class ClientDrawingOperation extends AthenaOperation {
	private String[] columns;
	private TCComponentItem item;

	public ClientDrawingOperation(Athena athena, TCSession session, Hashtable hashtable, String[] columns) {
		super(athena);
		this.hashtable = hashtable;
		this.columns = columns;
	}

	public void execute() {
		try {
			TCComponentItemType itemType = (TCComponentItemType)session.getTypeComponent("Item");
			String item_id = ((JTextComponent)getComponent("item_id")).getText().trim();
			String object_name = ((JTextComponent)getComponent("object_name")).getText().trim();
			String object_desc = ((JTextComponent)getComponent("object_desc")).getText().trim();
			item = itemType.create(item_id, "A", "A2ClientDrawing", object_name, object_desc, null);
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
			PropertyService.setStringToPropertyValue(property, component);
			revision.setTCProperty(property);
		}
		revision.refresh();
	}
}
