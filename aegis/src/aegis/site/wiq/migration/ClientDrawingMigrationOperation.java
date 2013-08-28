package aegis.site.wiq.migration;

import java.io.File;
import java.util.Hashtable;

import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.service.DatasetService;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;

public class ClientDrawingMigrationOperation extends AthenaOperation {
	private TCSession session;
	private JXTable table;
	private DefaultTableModel model;
	private boolean goOnError;
	private TCComponentItem item;
	private int line;
	private Hashtable<String, TCComponentItem> hashtable;

	public ClientDrawingMigrationOperation(Athena athena, JXTable table, boolean goOnError) {
		super(athena);
		this.session = athena.getSession();
		this.table = table;
		this.model = (DefaultTableModel)table.getModel();
		this.goOnError = goOnError;
		hashtable = new Hashtable();
	}

	public void execute() {
		for (int i = 0; i < model.getRowCount(); i++) {
			if (stop) {
				return;
			}
			line = i;
			try {
				TCComponentItemType itemType = (TCComponentItemType)session.getTypeComponent("Item");
				String item_id = (String)model.getValueAt(i, 0);
				if (item_id.contains(".") && item_id.length() - 1 == item_id.indexOf(".") + 1) {
					item_id = item_id.substring(0, item_id.indexOf("."));
				}
				if (hashtable.containsKey(item_id)) {
					item = hashtable.get(item_id);
					attachTemplate(i);
					model.setValueAt(new String("OK"), line, model.getColumnCount() - 2);
				} else {
					String name = (String)model.getValueAt(i, 1);
					item = itemType.create(item_id, "A", "A2ClientDrawing", name, new String(), null);
					hashtable.put(item_id, item);
					TCComponent target = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
					if (target instanceof TCComponentFolder) {
						target.add("contents", item);
					} else {
						session.getUser().getNewStuffFolder().add("contents", item);
					}
					setProperties(i, item.getLatestItemRevision());
					attachTemplate(i);
					model.setValueAt(new String("OK"), line, model.getColumnCount() - 2);
				}
				table.updateUI();
			} catch (Exception e) {
				e.printStackTrace();
				model.setValueAt(new String("Fail"), line, model.getColumnCount() - 2);
				model.setValueAt(e.getMessage(), line, model.getColumnCount() - 1);
				if (!goOnError) {
					return;
				} else {
					continue;
				}
			}
		}
		isOperationDone = true;
	}

	private void setProperties(int i, TCComponentItemRevision revision) throws Exception {
		TCProperty property = revision.getTCProperty("a2Client");
		property.setStringValue((String)model.getValueAt(i, 3));
		revision.setTCProperty(property);
		property = revision.getTCProperty("object_desc");
		property.setStringValue((String)model.getValueAt(i, 6));
		revision.setTCProperty(property);
		property = revision.getTCProperty("a2ProductCode");
		property.setStringValue((String)model.getValueAt(i, 2));
		revision.setTCProperty(property);
		revision.refresh();
	}

	private void attachTemplate(int i) throws Exception {
		String path = ((String)model.getValueAt(i, 12)).trim();
		String name = ((String)model.getValueAt(i, 7)).trim();
		String originalName = ((String)model.getValueAt(i, 9)).trim();
		if (name.length() > 0 && path.length() > 0 && originalName.length() > 0) {
			File file = new File(path + java.io.File.separator + name);
			File originFile = new File(path + java.io.File.separator + originalName);
			file.renameTo(originFile);
			DatasetService.createDataset(item.getLatestItemRevision(), "IMAN_specification", originFile.getAbsolutePath());
		}
	}
}
