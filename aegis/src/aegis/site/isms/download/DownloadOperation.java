package aegis.site.isms.download;

import java.io.File;
import java.util.Hashtable;

import javax.swing.table.DefaultTableModel;

import aegis.origin.QueryService;
import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.service.PropertyService;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentFormType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

public class DownloadOperation extends AthenaOperation {
	private TCSession session;
	private Hashtable<TCComponentDataset, TCComponentItem> datasetHashtable;
	private DefaultTableModel model;
	private TCComponentFolder folder;
	private TCComponentItemRevision revision;
	private File dir;

	public DownloadOperation(Athena athena, TCSession session, Hashtable<TCComponentDataset, TCComponentItem> datasetHashtable, DefaultTableModel model, TCComponentFolder folder, TCComponentItemRevision revision, File dir) {
		super(athena);
		this.session = session;
		this.datasetHashtable = datasetHashtable;
		this.model = model;
		this.folder = folder;
		this.revision = revision;
		this.dir = dir;
	}

	public void execute() {
		try {
			if (folder != null) {
				File newDir = new File(dir.getAbsolutePath() + File.separator + folder);
				if (!newDir.exists()) {
					newDir.mkdir();
				}
				download(folder, newDir);
			} else if (revision != null) {
				File newDir = new File(dir.getAbsolutePath());
				if (!newDir.exists()) {
					newDir.mkdir();
				}
				download(revision, newDir);
			}
			isOperationDone = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void download(TCComponentFolder component, File dir) throws Exception {
		AIFComponentContext[] children = component.getChildren();
		for (int i = 0; i < children.length; i++) {
			TCComponent child = (TCComponent)children[i].getComponent();
			if (child instanceof TCComponentFolder) {
				File newFolder = new File(dir.getAbsolutePath() + File.separator + child);
				if (!newFolder.exists()) {
					newFolder.mkdir();
				}
				download((TCComponentFolder)child, newFolder);
			} else if (child instanceof TCComponentItem) {
				TCComponentItemRevision revision = ((TCComponentItem)child).getLatestItemRevision();
				download(revision, dir);
			}
		}
	}

	private void download(TCComponentItemRevision revision, File dir) throws Exception {
		AIFComponentContext[] children = revision.getChildren();
		for (int i = 0; i < children.length; i++) {
			TCComponent child = (TCComponent)children[i].getComponent();
			if (child instanceof TCComponentDataset) {
				TCComponentDataset dataset = (TCComponentDataset)child;
				if (isSelected(dataset)) {
					TCComponentTcFile[] tcFile = dataset.getTcFiles();
					for (int j = 0; j < tcFile.length; j++) {
						tcFile[j].getFile(dir.getAbsolutePath());
					}
					createDownloadHistory(dataset);
				}
			}
		}
	}

	private void createDownloadHistory(TCComponentDataset dataset) throws Exception {
		TCComponentItem item = datasetHashtable.get(dataset);
		TCComponent user = item.getRelatedComponent("owning_user");
		QueryService.isms_newdownloadHistory(dataset.getProperty("object_name"), item.getProperty("item_id"), item.getProperty("object_name"), user.getProperty("user_id"), user.getProperty("user_name"));

	}

	private boolean isSelected(TCComponentDataset dataset) {
		for (int i = 0; i < model.getRowCount(); i++) {
			String puid = (String)model.getValueAt(i, 6);
			if (puid.equals(dataset.getUid())) {
				return ((Boolean)model.getValueAt(i, 0)).booleanValue();
			}
		}
		return false;
	}
}
