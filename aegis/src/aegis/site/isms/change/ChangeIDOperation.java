package aegis.site.isms.change;

import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.service.ComponentService;
import athena.service.DatabaseService;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCSession;

public class ChangeIDOperation extends AthenaOperation {
	private TCSession session;
	private JXTable table;

	public ChangeIDOperation(Athena athena, TCSession session, JXTable table) {
		super(athena);
		this.session = session;
		this.table = table;
	}

	public void execute() {
		DefaultTableModel model = ((DefaultTableModel)table.getModel());
		for (int i = 0; i < model.getRowCount(); i++) {
			String code = ((String)model.getValueAt(i, ComponentService.getColumnIndex(table, "code")));
			String serial = ((String)model.getValueAt(i, ComponentService.getColumnIndex(table, "serial")));
			String object_name = ((String)model.getValueAt(i, ComponentService.getColumnIndex(table, "object_name")));
			String uid = ((String)model.getValueAt(i, ComponentService.getColumnIndex(table, "uid")));
			try {
				TCComponentItem item = (TCComponentItem)session.stringToComponent(uid);
				item.setProperty("item_id", code + serial);
				item.setProperty("object_name", object_name);
				isOperationDone = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (i == model.getRowCount() - 1) {
				DatabaseService.setSerial("Part", "None", Integer.parseInt(serial));
			}
		}
	}

}
