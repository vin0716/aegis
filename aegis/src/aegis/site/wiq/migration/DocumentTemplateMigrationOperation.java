package aegis.site.wiq.migration;

import java.io.File;

import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.service.DatasetService;

import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class DocumentTemplateMigrationOperation extends AthenaOperation {
	private TCSession session;
	private JXTable table;
	private DefaultTableModel model;
	private boolean goOnError;
	private TCComponentFolder folder;
	private int line;

	public DocumentTemplateMigrationOperation(Athena athena, JXTable table, boolean goOnError) {
		super(athena);
		this.session = session;
		this.table = table;
		this.model = (DefaultTableModel)table.getModel();
		this.goOnError = goOnError;
	}

	public void execute() {
		try {
			if (!session.getUser().getUserId().equals("infodba")) {
				return;
			}
			TCComponentFolderType type = (TCComponentFolderType)session.getTypeComponent("Folder");
			folder = type.create("Document Template", "", "Folder");
			session.getUser().getHomeFolder().add("contents", folder);
		} catch (TCException e) {
			e.printStackTrace();
			return;
		}
		for (int i = 0; i < model.getRowCount(); i++) {
			if (stop) {
				return;
			}
			line = i;
			try {
				attachTemplate(i);
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
			model.setValueAt(new String("OK"), line, model.getColumnCount() - 2);
			table.updateUI();
		}
		isOperationDone = true;
	}

	private void attachTemplate(int i) throws Exception {
		File file = new File((String)model.getValueAt(i, 1));
		TCComponentDataset dataset = DatasetService.createDataset(folder, "contents", file.getAbsolutePath());
		dataset.setProperty("object_desc", "WIQ Document Template");
	}
}
