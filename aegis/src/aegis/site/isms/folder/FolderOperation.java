package aegis.site.isms.folder;

import org.jdesktop.swingx.JXTree;

import athena.origin.Athena;
import athena.origin.AthenaOperation;
import athena.origin.AthenaTreeNode;
import athena.origin.AthenaTreeNodeData;

import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class FolderOperation extends AthenaOperation {
	private TCSession session;
	private JXTree tree;
	private TCComponentFolderType type;

	public FolderOperation(Athena athena, TCSession session, JXTree tree) {
		super(athena);
		this.session = session;
		this.tree = tree;
	}

	public void execute() {
		try {
			type = (TCComponentFolderType)session.getTypeComponent("Folder");
			AthenaTreeNode root = (AthenaTreeNode)tree.getModel().getRoot();
			createFolder((AthenaTreeNode)tree.getSelectionPath().getLastPathComponent(), session.getUser().getHomeFolder());
			isOperationDone = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createFolder(AthenaTreeNode node, TCComponentFolder parent) throws TCException {
		String name = (String)node.get("value");
		TCComponentFolder folder = type.create(name, "", "Folder");
		parent.add("contents", folder);
		for (int i = 0; i < node.getChildCount(); i++) {
			createFolder((AthenaTreeNode)node.getChildAt(i), folder);
		}
	}
}
